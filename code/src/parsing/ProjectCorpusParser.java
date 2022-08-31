package parsing;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.microsoft.z3.Status;

import clausetree.ClauseTreeNode;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import utils.FileIO;
import utils.JavaASTUtil;
import utils.Utils;

public class ProjectCorpusParser {
	private static final boolean PARSE_INDIVIDUAL_SRC = false, SCAN_FILES_FRIST = false, REMOVE_COMMON_WORDS = true;
	private static final String INHERIT_DOC_TAG = "@inheritDoc";
	@SuppressWarnings("serial")
	private static final HashSet<String> EXCEPTION_TAGS = new HashSet<String>(){{add("@exception"); add("@throw"); add("@throws");}};
	
	private String inPath;
	private PrintStream stLog, stClauseLocations, stClauseSource, stJavadoc; //, stLocations, stSource, stTarget;
	private boolean testing = false, buildClauseTree = false;
	private Set<String> badFiles = new HashSet<>();
	private Map<String, Map<String, String>> methodExceptionDocConditions = new HashMap<>();
	private Map<String, Map<String, String>> methodExceptionCodeConditions = new HashMap<>();
	private Map<String, String> methodLocation = new HashMap<>();
	private Map<String, String> overriddenMethods = new HashMap<>();
	private Map<String, Set<String>> overridingMethods = new HashMap<>();
	private Map<String, SpecMethod> methods = new HashMap<>();
	private Map<String, String> getters = new HashMap<>();
	private Map<String, Expression> constants = new HashMap<>();
	
	public ProjectCorpusParser(String inPath) {
		this.inPath = inPath;
	}
	
	public ProjectCorpusParser(String inPath, boolean testing, boolean buildClauseTree) {
		this(inPath);
		this.testing = testing;
		this.buildClauseTree = buildClauseTree;
	}

	public void generateParallelCorpus(String outPath, boolean recursive) {
		ArrayList<String> rootPaths = getRootPaths();
		
		new File(outPath).mkdirs();
		try {
			stClauseLocations = new PrintStream(new FileOutputStream(outPath + "/clause-locations.txt"));
			stClauseSource = new PrintStream(new FileOutputStream(outPath + "/clause-source.txt"));
			stJavadoc = new PrintStream(new FileOutputStream(outPath + "/javadoc.txt"));
//			stLocations = new PrintStream(new FileOutputStream(outPath + "/locations.txt"));
//			stSource = new PrintStream(new FileOutputStream(outPath + "/source.txt"));
//			stTarget = new PrintStream(new FileOutputStream(outPath + "/target.txt"));
			stLog = new PrintStream(new FileOutputStream(outPath + "/log.txt"));
		} catch (FileNotFoundException e) {
			if (testing)
				System.err.println(e.getMessage());
			return;
		}
		if (testing)
		    System.out.println("Start parsing");
		for (String rootPath : rootPaths) {
			String[] sourcePaths = getSourcePaths(rootPath, new String[]{".java"}, recursive);
			
			@SuppressWarnings("rawtypes")
			Map options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setCompilerOptions(options);
			parser.setEnvironment(null, new String[]{}, new String[]{}, true);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(false);
			
			final HashMap<String, CompilationUnit> cus = new HashMap<>();
			final FileASTRequestor r = new FileASTRequestor() {
				@Override
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					if (ast.getPackage() == null)
						return;
					cus.put(sourceFilePath, ast);
				}
			};
			try {
				parser.createASTs(sourcePaths, null, new String[0], r, null);
			} catch (Throwable t) {
				t.printStackTrace(stLog);
				if (testing) {
					System.err.println(t.getMessage());
					t.printStackTrace();
					System.exit(-1);
				}
			}
			buildGetters(cus);
			List<String> list = new ArrayList<>(cus.keySet());
			Collections.sort(list);
			for (int j = 0; j < list.size(); j++) {
			    String sourceFilePath = list.get(j);
				CompilationUnit ast = cus.get(sourceFilePath);
//				if (testing)
//					System.out.println(sourceFilePath);
				stLog.println(sourceFilePath);
				for (int i = 0; i < ast.types().size(); i++) {
					if (ast.types().get(i) instanceof AbstractTypeDeclaration) {
					    AbstractTypeDeclaration td = (AbstractTypeDeclaration) ast.types().get(i);
						generateSequence(td, sourceFilePath, ast.getPackage().getName().getFullyQualifiedName(), "");
					}
				}
			}
		}
        if (testing)
            System.out.println("Done parsing");
        System.out.println("Memory (MB): " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
		inlineInheritedElements();
		ArrayList<String> list = new ArrayList<>(methodExceptionCodeConditions.keySet());
		Collections.sort(list);
		StringBuilder sbSource = new StringBuilder(), sbTarget = new StringBuilder(), sbLocations = new StringBuilder();
		for (String method : list) {
			Map<String, String> exceptionDocConditions = methodExceptionDocConditions.get(method), exceptionCodeConditions = methodExceptionCodeConditions.get(method);
			for (String exception : exceptionCodeConditions.keySet()) {
				String codeCondition = exceptionCodeConditions.get(exception);
				if (!codeCondition.isEmpty()) {
				    Expression exp = JavaASTUtil.parseExpression(codeCondition);
				    if (exp != null) {
    				    exp.accept(new ASTVisitor() {
    				        @Override
    				        public boolean visit(QualifiedName node) {
    				            String fqn = node.getFullyQualifiedName();
    				            Expression constant = constants.get(fqn);
    				            if (constant != null) {
//    				                ASTNode c = null;
//    				                if (constant instanceof Number) {
//    				                    c = node.getAST().newNumberLiteral(constant.toString());
//    				                } else if (constant instanceof Boolean) {
//    				                    c = node.getAST().newBooleanLiteral((boolean) constant);
//                                    } else if (constant instanceof Character) {
//                                        c = node.getAST().newCharacterLiteral();
//                                        ((CharacterLiteral) c).setCharValue((char) constant);
//                                    } else {
//                                        c = node.getAST().newStringLiteral();
//                                        ((StringLiteral) c).setLiteralValue(constant.toString());
//                                    }
                                    JavaASTUtil.replace(node, ASTNode.copySubtree(node.getAST(), constant));
                                    return false;
    				            }
    				            String method = getters.get(fqn);
    				            if (method != null) {
    				                MethodInvocation call = node.getAST().newMethodInvocation();
    				                call.setName(node.getAST().newSimpleName(method));
    				                JavaASTUtil.replace(node, call);
    				                return false;
    				            }
    				            return false;
    				        }
    				        
    				        @Override
    				        public boolean visit(FieldAccess node) {
    				            if (node.getExpression() instanceof Name) {
    	                            String method = getters.get(node.toString());
    	                            if (method != null) {
    	                                MethodInvocation call = node.getAST().newMethodInvocation();
    	                                call.setName(node.getAST().newSimpleName(method));
    	                                JavaASTUtil.replace(node, call);
    	                            }
    				            }
    				            return false;
    				        }
                        });
    				    codeCondition = JavaASTUtil.optimize(exp.toString());
    				    if (codeCondition.equals("false"))
    				        continue;
    				    exp = JavaASTUtil.parseExpression(codeCondition);
    				    if (exp != null) {
                            ExpressionStatement s = (ExpressionStatement) exp.getParent();
                            codeCondition = JavaASTUtil.tokenize(s.getExpression(), Utils.encloseClauseWithParentheses);
                            if (!(s.getExpression() instanceof ParenthesizedExpression) && !JavaASTUtil.isLogicalInfix(s.getExpression()))
//                            if (!codeCondition.startsWith("(") || !codeCondition.endsWith(")"))
                                codeCondition = "( " + codeCondition + " )";
    				    }
        				sbTarget.append(exception + " " + codeCondition + "\n");
                        sbSource.append(exception + " " + exceptionDocConditions.get(exception) + "\n");
        				sbLocations.append(methodLocation.get(method) + "\n");
				    } else {
				        System.err.println();
				    }
				}
			}
		}
		FileIO.writeStringToFile(sbSource.toString(), outPath + "/doc.txt");
		FileIO.writeStringToFile(sbTarget.toString(), outPath + "/code.txt");
		FileIO.writeStringToFile(sbLocations.toString(), outPath + "/locations.txt");
		stLog.println(new ArrayList<String>(DocumentationParser.seenTags));
	}
	
	private void buildGetters(HashMap<String, CompilationUnit> cus) {
		for (String sourceFilePath : cus.keySet()) {
			CompilationUnit ast = cus.get(sourceFilePath);
			for (int i = 0; i < ast.types().size(); i++) {
				if (ast.types().get(i) instanceof AbstractTypeDeclaration) {
				    AbstractTypeDeclaration td = (AbstractTypeDeclaration) ast.types().get(i);
					buildGetters(td);
				}
			}
		}
	}

	private void buildGetters(AbstractTypeDeclaration td) {
	    for (int i = 0; i < td.bodyDeclarations().size(); i++) {
	        BodyDeclaration bd = (BodyDeclaration) td.bodyDeclarations().get(i);
	        if (bd instanceof AbstractTypeDeclaration) {
	            buildGetters((AbstractTypeDeclaration) bd);
	        } else if (bd instanceof MethodDeclaration) {
	            MethodDeclaration method = (MethodDeclaration) bd;
	            if (method.parameters().isEmpty() && method.getBody() != null) {
	                Block body = method.getBody();
	                if (body.statements().size() == 1 && body.statements().get(0) instanceof ReturnStatement) {
	                    ReturnStatement s  = (ReturnStatement) body.statements().get(0);
	                    if (s.getExpression() != null) {
	                        String fqn = JavaASTUtil.getFQN(s.getExpression());
	                        if (fqn != null)
	                            getters.put(fqn, method.getName().getIdentifier());
	                    }
	                }
	            }
	        } else if (bd instanceof FieldDeclaration) {
	            FieldDeclaration fd = (FieldDeclaration) bd;
	            if (Modifier.isFinal(fd.getModifiers())) {
	                for (int j = 0; j < fd.fragments().size(); j++) {
	                    VariableDeclarationFragment f = (VariableDeclarationFragment) fd.fragments().get(j);
	                    if (f.getInitializer() != null) {
    	                    String fqn = JavaASTUtil.getFQN(f.getName());
    	                    if (fqn != null) {
    	                        Expression constant = f.getInitializer();
    	                        if (!(constant instanceof MethodInvocation))
    	                            constants.put(fqn, constant);
    	                    }
	                    }
	                }
	            }
	        }
	    }
	}

	private void inlineInheritedElements() {
        if (testing)
            System.out.println("Start inlining docs");
		inlineInheritedDocs();
        if (testing)
            System.out.println("Done inlining docs");
        System.out.println("Memory (MB): " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
        if (testing)
            System.out.println("Start inlining code");
		inlineCallees();
        if (testing)
            System.out.println("Done inlining code");
        System.out.println("Memory (MB): " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024);
	}

	private void inlineCallees() {
		Set<String> workingMethods = new HashSet<>(methodExceptionCodeConditions.keySet());
		int workingSize = workingMethods.size();
		while (!workingMethods.isEmpty()) {
    		for (String method : new HashSet<String>(workingMethods))
    		    inlineCallees(method, workingMethods);
    		if (workingMethods.size() == workingSize)
    		    break;
    		workingSize = workingMethods.size();
		}
	}

	private void inlineCallees(String method, Set<String> workingMethods) {
        SpecMethod specMethod = methods.get(method);
        if (specMethod.calleeArguments == null) {
            Set<String> inter = new HashSet<>(overridingMethods.get(method));
            inter.retainAll(workingMethods);
            if (!inter.isEmpty())
                return;
            Map<String, String> exceptionConditions = methodExceptionCodeConditions.get(method);
            if (exceptionConditions == null)
                exceptionConditions = new HashMap<>();
            for (String e : exceptionConditions.keySet()) {
                String condition = exceptionConditions.get(e);
                condition = JavaASTUtil.stripOuterParenthesis(condition);
                exceptionConditions.put(e, condition);
            }
            int length = 0;
            for (String overridingMethod : overridingMethods.get(method)) {
                Map<String, String> overridingExceptionConditions = methodExceptionCodeConditions.get(overridingMethod);
                int l = conditionLength(overridingExceptionConditions);
                if ((overridingExceptionConditions.size() > exceptionConditions.size())
                        || (overridingExceptionConditions.size() == exceptionConditions.size() && l > length)) {
                    exceptionConditions = new HashMap<>(overridingExceptionConditions);
                    length = l;
                }
            }
            optimize(exceptionConditions);
            methodExceptionCodeConditions.put(method, exceptionConditions);
            workingMethods.remove(method);
        } else {
            Set<String> inter = new HashSet<>(specMethod.calleeArguments.keySet());
            inter.retainAll(workingMethods);
            if (!inter.isEmpty())
                return;
            Map<String, String> exceptionConditions = methodExceptionCodeConditions.get(method);
            if (exceptionConditions == null)
                exceptionConditions = new HashMap<>();
            for (String e : exceptionConditions.keySet()) {
                String condition = exceptionConditions.get(e);
                condition = JavaASTUtil.stripOuterParenthesis(condition);
                exceptionConditions.put(e, condition);
            }
            for (String callee : specMethod.calleeArguments.keySet()) {
                List<String[]> sites = specMethod.calleeArguments.get(callee);
                List<String> receivers = specMethod.calleeReceivers.get(callee);
                List<String> conditions = specMethod.calleeConditions.get(callee);
                for (int i = 0; i < sites.size(); i++ ) {
                    String[] arguments = sites.get(i);
                    Map<String, String> substitutedCalleeExceptionConditions = substitute(callee, conditions.get(i), receivers.get(i), arguments);
                    Utils.addConditions(exceptionConditions, substitutedCalleeExceptionConditions);
                }
            }
            optimize(exceptionConditions);
            methodExceptionCodeConditions.put(method, exceptionConditions);
            workingMethods.remove(method);
        }
    }

    private void optimize(Map<String, String> exceptionConditions) {
        for (String e : exceptionConditions.keySet()) {
            String codeCondition = exceptionConditions.get(e);
            if (!codeCondition.isEmpty()) {
                codeCondition = JavaASTUtil.optimize(codeCondition);
                exceptionConditions.put(e, codeCondition);
            }
        }
    }

    private Map<String, String> substitute(final String callee, final String guard, final String receiver, final String[] arguments) {
        Map<String, String> calleeExceptionConditions = methodExceptionCodeConditions.get(callee);
        Map<String, String> substitutedCalleeExceptionConditions = new HashMap<>();
        SpecMethod calleeSpecMethod = methods.get(callee);
        String[] parameters = calleeSpecMethod.parameterNames;
        if (arguments.length != parameters.length) {
            substitutedCalleeExceptionConditions.putAll(calleeExceptionConditions);
            return substitutedCalleeExceptionConditions;
        }
        Map<String, String> parametersMap = new HashMap<>();
        for (int i = 0; i < parameters.length; i++) {
            parametersMap.put(parameters[i], arguments[i]);
        }
        for (String e : calleeExceptionConditions.keySet()) {
            String condition = calleeExceptionConditions.get(e);
            if (condition.equals("true"))
                continue;
            String subCondition = substitute(condition, receiver, parametersMap);
            Status status = Status.UNKNOWN;
            if (receiver == null)
                status = Utils.satcheck("&&", guard, "this != null", subCondition);
            else
                status = Utils.satcheck("&&", guard, "this != null", receiver + " != null", subCondition);
            if (status != Status.UNSATISFIABLE) {
                subCondition = JavaASTUtil.removeNonPredicateMethodCalls(subCondition);
                if (!subCondition.equals("false"))
                    substitutedCalleeExceptionConditions.put(e, subCondition);
            }
        }
        return substitutedCalleeExceptionConditions;
    }

    private String substitute(final String condition, final String receiver, final Map<String, String> parametersMap) {
        Expression expr = JavaASTUtil.parseExpression(condition);
        if (expr == null)
            return condition;
        
        Expression receiverExpression = (receiver == null) ? null : JavaASTUtil.parseExpression(receiver);
        
        ExpressionStatement ast = (ExpressionStatement) expr.getParent();
        
        ast.accept(new ASTVisitor() {
            
            @Override
            public boolean visit(FieldAccess node) {
                return false;
            }
            
            @Override
            public boolean visit(SuperFieldAccess node) {
                return false;
            }
            
            @Override
            public boolean visit(QualifiedName node) {
                node.getQualifier().accept(this);
                return false;
            }
            
            @Override
            public boolean visit(MethodInvocation node) {
                if (node.getExpression() != null && !(node.getExpression() instanceof ThisExpression))
                    node.getExpression().accept(this);
                else if (receiverExpression != null)
                    node.setExpression((Expression) ASTNode.copySubtree(expr.getAST(), receiverExpression));
                for (int i = 0; i < node.arguments().size(); i++)
                    ((ASTNode) (node.arguments().get(i))).accept(this);
                return false;
            }
            
            @Override
            public void endVisit(MethodInvocation node) {
                if (node.getName().getIdentifier().equals("isNaN") 
                        && node.arguments().size() == 1 
                        && node.arguments().get(0) instanceof NumberLiteral) {
                    JavaASTUtil.replace(node, node.getAST().newBooleanLiteral(false));
                }
            }
            
            @Override
            public boolean visit(SuperMethodInvocation node) {
                for (int i = 0; i < node.arguments().size(); i++)
                    ((ASTNode) (node.arguments().get(i))).accept(this);
                return false;
            }
            
            @Override
            public void endVisit(SuperMethodInvocation node) {
                if (receiverExpression != null) {
                    MethodInvocation call = node.getAST().newMethodInvocation();
                    call.setExpression((Expression) ASTNode.copySubtree(expr.getAST(), receiverExpression));
                    call.setName(node.getAST().newSimpleName(node.getName().getIdentifier()));
                    call.arguments().addAll(ASTNode.copySubtrees(node.getAST(), node.arguments()));
                    JavaASTUtil.replace(node, call);
                }
            }
            
            @Override
            public boolean visit(ThisExpression node) {
                if (receiverExpression != null)
                    JavaASTUtil.replace(node, ASTNode.copySubtree(expr.getAST(), receiverExpression));
                return false;
            }
            
            @Override
            public boolean visit(SimpleName node) {
                String name = node.getIdentifier();
                String arg = parametersMap.get(name);
                if (arg != null) {
                    ASTNode newNode = JavaASTUtil.parseExpression(arg);
                    if (newNode == null)
                        return false; // FIXME
                    ASTNode p = node.getParent();
                    if (p instanceof QualifiedName) {
                        if (node == ((QualifiedName) p).getQualifier()) {
                            if (newNode instanceof Name) {
                                newNode = ASTNode.copySubtree(node.getAST(), newNode);
                                JavaASTUtil.replace(node, newNode);
                            }
                        } else {
                            if (newNode instanceof SimpleName) {
                                newNode = ASTNode.copySubtree(node.getAST(), newNode);
                                JavaASTUtil.replace(node, newNode);
                            }
                        }
                    } else {
                        newNode = ASTNode.copySubtree(node.getAST(), newNode);
                        JavaASTUtil.replace(node, newNode);
                    }
                }
                return false;
            }
            
            @Override
            public void endVisit(QualifiedName node) {
                if (node.getParent() instanceof QualifiedName)
                    return;
                String fqn = node.getFullyQualifiedName();
                Expression constant = constants.get(fqn);
                if (constant != null) {
//                    ASTNode c = null;
//                    if (constant instanceof Number) {
//                        c = node.getAST().newNumberLiteral(constant.toString());
//                    } else if (constant instanceof Boolean) {
//                        c = node.getAST().newBooleanLiteral((boolean) constant);
//                    } else if (constant instanceof Character) {
//                        c = node.getAST().newCharacterLiteral();
//                        ((CharacterLiteral) c).setCharValue((char) constant);
//                    } else {
//                        c = node.getAST().newStringLiteral();
//                        ((StringLiteral) c).setLiteralValue(constant.toString());
//                    }
                    JavaASTUtil.replace(node, ASTNode.copySubtree(node.getAST(), constant));
                    return;
                }
                String method = getters.get(fqn);
                if (method != null) {
                    MethodInvocation call = node.getAST().newMethodInvocation();
                    call.setName(node.getAST().newSimpleName(method));
                    if (receiverExpression != null)
                        call.setExpression((Expression) ASTNode.copySubtree(expr.getAST(), receiverExpression));
                    JavaASTUtil.replace(node, call);
                    return;
                }
                return;
            }
            
//            @Override
//            public void endVisit(InfixExpression node) {
//                if (JavaASTUtil.isBool(node.getOperator())) {
//                    Status status = Utils.satcheck(node);
//                    if (status == Status.UNSATISFIABLE) {
//                        JavaASTUtil.replace(node, node.getAST().newBooleanLiteral(false));
//                    } else {
//                        status = Utils.satcheck("!(" + node.toString() + ")");
//                        if (status == Status.UNSATISFIABLE) {
//                            JavaASTUtil.replace(node, node.getAST().newBooleanLiteral(true));
//                        }
//                    }
//                }
//            }
            
        });
        return ast.getExpression().toString();
    }

    private int conditionLength(Map<String, String> exceptionConditions) {
        int len = 0;
        for (String condition : exceptionConditions.values())
            len += condition.trim().length();
        return len;
    }

    private void inlineInheritedDocs() {
		HashSet<String> doneMethods = new HashSet<>();
		for (String method : new HashSet<String>(methodExceptionDocConditions.keySet())) {
			inlineInheritedDocs(method, doneMethods);
		}
	}

	private void inlineInheritedDocs(String method, HashSet<String> doneMethods) {
		if (doneMethods.contains(method))
			return;
		Map<String, String> exceptionDocConditions = methodExceptionDocConditions.get(method);
		if (exceptionDocConditions != null) {
    		for (String exception : exceptionDocConditions.keySet()) {
    			String condition = exceptionDocConditions.get(exception);
    			String[] conditions = condition.split(" or ");
    			String inheritDoc = null;
    			StringBuilder sb = new StringBuilder();
    			for (int i = 0; i < conditions.length; i++) {
    				String c = conditions[i];
    				c = c.trim();
    				if (c.equals(INHERIT_DOC_TAG)) {
    					if (inheritDoc == null) {
    						String overriddenMethod = overriddenMethods.get(method);
    						inlineInheritedDocs(overriddenMethod, doneMethods);
    						Map<String, String> m = methodExceptionDocConditions.get(overriddenMethod);
    						if (m != null)
    							inheritDoc = m.get(exception);
    						if (inheritDoc == null) {
    							inheritDoc = "";
    							c = "";
    						} else
    							c = inheritDoc;
    					} else
    						c = "";
    				}
    				if (!c.isEmpty()) {
    					if (i > 0)
    						sb.append(" or ");
    					sb.append(c);
    				}
    			}
    			exceptionDocConditions.put(exception, sb.toString());
    		}
		}
		doneMethods.add(method);
	}

	private int generateSequence(AbstractTypeDeclaration td, String path, String packageName, String outer) {
		String[] source = new String[1];
		int numOfSequences = 0;
		String name = outer.isEmpty() ? td.getName().getIdentifier() : outer + "." + td.getName().getIdentifier();
		for (int bdi = 0; bdi < td.bodyDeclarations().size(); bdi++) {
		    BodyDeclaration bd = (BodyDeclaration) td.bodyDeclarations().get(bdi);
		    if (!(bd instanceof MethodDeclaration))
		        continue;
		    MethodDeclaration method = (MethodDeclaration) bd;
			String methodShortName = JavaASTUtil.buildNameWithParameters(method);
			String methodName = packageName + "." + name + "." + methodShortName;
            stLog.println(path + "\t" + name + "\t" + methodShortName);
            
			ITypeBinding tb = td.resolveBinding();
			if (tb != null)
				buildMethodHierarchy(methodName, methodShortName, tb, method);
			
			Block body = method.getBody();
//			if (body == null)
//				continue;
//			if (body.statements().isEmpty())
//				continue;
			SpecMethod specMethod = new SpecMethod(methodName, method);
			ThrownExceptionVisitor tev = new ThrownExceptionVisitor(method, specMethod, overriddenMethods);
			if (body != null)
				body.accept(tev);
		    methods.put(methodName, specMethod);
//			if (tev.thrownExceptions.isEmpty())
//				continue;
			final HashMap<String, String> thrownExceptions = new HashMap<>(tev.thrownExceptions);
			Javadoc doc = method.getJavadoc();
//			if (doc == null)
//				continue;
			ArrayList<String> paraNames = new ArrayList<>();
			for (int i = 0; i < method.parameters().size(); i++) {
				SingleVariableDeclaration svd = (SingleVariableDeclaration) method.parameters().get(i);
				paraNames.add(svd.getName().getIdentifier());
			}
			HashMap<String, String> docExceptionCondition = new HashMap<>();
			for (int i = 0; doc != null && i < doc.tags().size(); i++) {
				TagElement tagElement = (TagElement) doc.tags().get(i);
				if (EXCEPTION_TAGS.contains(tagElement.getTagName())) {
					if (!tagElement.fragments().isEmpty() && tagElement.fragments().get(0) instanceof Name) {
						Name exName = (Name) tagElement.fragments().get(0);
						if (exName instanceof QualifiedName)
							exName = ((QualifiedName) exName).getName();
						String exceptionName = ((SimpleName) exName).getIdentifier();
						if (exceptionName.endsWith("Exception")) {
							boolean isInheritDoc = false;
							final StringBuilder sb = new StringBuilder();
							for (int j = 1; j < tagElement.fragments().size(); j++) {
								ASTNode fragment = (ASTNode) tagElement.fragments().get(j);
								if (fragment instanceof TagElement) {
									TagElement tag = (TagElement) fragment;
									if (tag.getTagName() != null && tag.getTagName().equals(INHERIT_DOC_TAG)) {
										isInheritDoc = true;
										break;
									}
								}
								ASTNode pre = (ASTNode) tagElement.fragments().get(j-1);
								if (pre.getStartPosition() + pre.getLength() < fragment.getStartPosition())
									sb.append(" ");
								if (fragment instanceof TagElement && ExceptionDocVisitor.missingFragments((TagElement) fragment, path, source))
									j = ExceptionDocVisitor.handleEmptyTagElement((TagElement) fragment, j, tagElement, path, source, sb);
								else {
									ExceptionDocVisitor edv = new ExceptionDocVisitor(path, source, sb, getters);
									fragment.accept(edv);
								}
							}
							if (isInheritDoc) {
								String condition = docExceptionCondition.get(exceptionName);
								if (condition == null)
									condition = INHERIT_DOC_TAG;
								else 
									condition += " or " + INHERIT_DOC_TAG;
								docExceptionCondition.put(exceptionName, condition);
							} else {
//								System.out.println(sb.toString());
								if (buildClauseTree) {
									HashMap<String, String> codeStr = new HashMap<>(), strCode = new HashMap<>();
									String docSequence = DocumentationParser.normalize(sb.toString(), codeStr, strCode, paraNames);
									List<CoreMap> sentences = NLPSentenceParser.parse(docSequence);
									for (CoreMap sentence : sentences) {
										numOfSequences++;
//										System.out.println(sentence);
										Tree tree = sentence.get(TreeAnnotation.class);
//										System.out.println(tree);
//										NLPSentenceParser.print(tree);
										ClauseTreeNode clauseTree = NLPSentenceParser.buildClauseTree(tree, new Stack<>(), new Stack<>(), new HashSet<>(), strCode);
//										clauseTree.print();
										String flattenedSequence = clauseTree.flatten();
										if (!flattenedSequence.equals("null")) {
											if (REMOVE_COMMON_WORDS) {
//												flattenedSequence = flattenedSequence.replace(" be ", " ");
												flattenedSequence = flattenedSequence.replace("the ", "");
												flattenedSequence = flattenedSequence.replace(" specify ", " ");
											}
											String condition = docExceptionCondition.get(exceptionName);
											if (condition == null)
												condition = flattenedSequence;
											else 
												condition += " or " + flattenedSequence;
											docExceptionCondition.put(exceptionName, condition);
											stClauseLocations.print(path + "\t" + packageName + "\t" + name + "\t" + methodShortName + "\t" + sentences.size() + "\n");
											stClauseSource.print(exceptionName + " " + flattenedSequence + "\n");
											stJavadoc.print(sb.toString() + "\n");
										}
									}
								} else {
									String flattenedSequence = sb.toString();
									if (!flattenedSequence.isEmpty()) {
										String condition = docExceptionCondition.get(exceptionName);
										if (condition == null)
											condition = flattenedSequence;
										else 
											condition += " or " + flattenedSequence;
										docExceptionCondition.put(exceptionName, condition);
										stClauseLocations.print(path + "\t" + packageName + "\t" + name + "\t" + methodShortName + "\t" + 1 + "\n");
										stClauseSource.print(exceptionName + " " + flattenedSequence + "\n");
										stJavadoc.print(sb.toString() + "\n");
									}
								}
							}
						}
					}
				}
			}
			/*if (!docExceptionCondition.isEmpty())*/ {
				methodExceptionCodeConditions.put(methodName, thrownExceptions);
				methodExceptionDocConditions.put(methodName, docExceptionCondition);
				methodLocation.put(methodName, path + "\t" + packageName + "\t" + name + "\t" + methodShortName + "\t" + docExceptionCondition.size() + "\t" + thrownExceptions.size());
			}
		}
		for (int bdi = 0; bdi < td.bodyDeclarations().size(); bdi++) {
            BodyDeclaration bd = (BodyDeclaration) td.bodyDeclarations().get(bdi);
            if (!(bd instanceof AbstractTypeDeclaration))
                continue;
            AbstractTypeDeclaration inner = (AbstractTypeDeclaration) bd;
			numOfSequences += generateSequence(inner, path, packageName, name);
		}
		return numOfSequences;
	}

	private void buildMethodHierarchy(String methodName, String methodShortName, ITypeBinding tb, MethodDeclaration method) {
		if (tb.getSuperclass() != null) {
			ITypeBinding stb = tb.getSuperclass().getTypeDeclaration();
			for (IMethodBinding mb : stb.getDeclaredMethods()) {
				if (method.resolveBinding() != null && method.resolveBinding().overrides(mb)) {
					String name = mb.getDeclaringClass().getQualifiedName() + "." + methodShortName;
					overriddenMethods.put(methodName, name);
					Set<String> ms = overridingMethods.get(name);
					if (ms == null) {
					    ms = new HashSet<>();
					    overridingMethods.put(name, ms);
					}
					ms.add(methodName);
					return;
				}
			}
			buildMethodHierarchy(methodName, methodShortName, stb, method);
			if (this.overriddenMethods.containsKey(methodName))
				return;
		}
		for (ITypeBinding itb : tb.getInterfaces()) {
			for (IMethodBinding mb : itb.getTypeDeclaration().getDeclaredMethods()) {
				if (method.resolveBinding() != null && method.resolveBinding().overrides(mb)) {
					String name = mb.getDeclaringClass().getQualifiedName() + "." + methodShortName;
					overriddenMethods.put(methodName, name);
                    Set<String> ms = overridingMethods.get(name);
                    if (ms == null) {
                        ms = new HashSet<>();
                        overridingMethods.put(name, ms);
                    }
                    ms.add(methodName);
					return;
				}
			}
			buildMethodHierarchy(methodName, methodShortName, itb.getTypeDeclaration(), method);
			if (this.overriddenMethods.containsKey(methodName))
				return;
		}
	}

	private String[] getSourcePaths(String path, String[] extensions, boolean recursive) {
		HashSet<String> exts = new HashSet<>();
		for (String e : extensions)
			exts.add(e);
		HashSet<String> paths = new HashSet<>();
		getSourcePaths(new File(path), paths, exts, recursive);
		paths.removeAll(badFiles);
		return (String[]) paths.toArray(new String[0]);
	}

	private void getSourcePaths(File file, HashSet<String> paths, HashSet<String> exts, boolean recursive) {
		if (file.isDirectory()) {
			if (paths.isEmpty() || recursive)
				for (File sub : file.listFiles())
					getSourcePaths(sub, paths, exts, recursive);
		} else if (exts.contains(getExtension(file.getName())))
			paths.add(file.getAbsolutePath());
	}

	private Object getExtension(String name) {
		int index = name.lastIndexOf('.');
		if (index < 0)
			index = 0;
		return name.substring(index);
	}

	private ArrayList<String> getRootPaths() {
		ArrayList<String> rootPaths = new ArrayList<>();
		if (PARSE_INDIVIDUAL_SRC)
			getRootPaths(new File(inPath), rootPaths);
		else {
			if (SCAN_FILES_FRIST)
				getRootPaths(new File(inPath), rootPaths);
			rootPaths = new ArrayList<>();
			rootPaths.add(inPath);
		}
		return rootPaths;
	}

	private void getRootPaths(File file, ArrayList<String> rootPaths) {
		if (file.isDirectory()) {
//			System.out.println(rootPaths);
			for (File sub : file.listFiles())
				getRootPaths(sub, rootPaths);
		} else if (file.getName().endsWith(".java")) {
			@SuppressWarnings("rawtypes")
			Map options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setCompilerOptions(options);
			parser.setSource(FileIO.readStringFromFile(file.getAbsolutePath()).toCharArray());
			try {
				CompilationUnit ast = (CompilationUnit) parser.createAST(null);
				if (ast.getPackage() != null && !ast.types().isEmpty() && ast.types().get(0) instanceof TypeDeclaration) {
					String name = ast.getPackage().getName().getFullyQualifiedName();
					name = name.replace('.', '\\');
					String p = file.getParentFile().getAbsolutePath();
					if (p.endsWith(name))
						add(p.substring(0, p.length() - name.length() - 1), rootPaths);
				} /*else 
					badFiles.add(file.getAbsolutePath());*/
			} catch (Throwable t) {
				badFiles.add(file.getAbsolutePath());
			}
		}
	}

	private void add(String path, ArrayList<String> rootPaths) {
		int index = Collections.binarySearch(rootPaths, path);
		if (index < 0) {
			index = - index - 1;
			int i = rootPaths.size() - 1;
			while (i > index) {
				if (rootPaths.get(i).startsWith(path))
					rootPaths.remove(i);
				i--;
			}
			i = index - 1;
			while (i >= 0) {
				if (path.startsWith(rootPaths.get(i)))
					return;
				i--;
			}
			rootPaths.add(index, path);
		}
	}

}
