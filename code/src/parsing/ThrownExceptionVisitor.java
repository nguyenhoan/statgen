package parsing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
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
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.WhileStatement;

import com.microsoft.z3.Status;

import utils.JavaASTUtil;
import utils.Utils;

public class ThrownExceptionVisitor extends ASTVisitor {
    public static Set<String> IGNORED_THROWABLES = new HashSet<>();
    private MethodDeclaration method;
    private Map<String, String> overriddenMethods;
    SpecMethod specMethod;
	Map<String, String> thrownExceptions = new HashMap<>();
	Stack<Map<String, List<Expression>>> stkValues = new Stack<>();
	
	static {
	    IGNORED_THROWABLES.add("Error");
        IGNORED_THROWABLES.add("InternalError");
        IGNORED_THROWABLES.add("AssertionError");
        IGNORED_THROWABLES.add("UnsupportedOperationException");
	}
	
	public ThrownExceptionVisitor(final MethodDeclaration method, final SpecMethod specMethod, final Map<String, String> overriddenMethods) {
		this.method = method;
		this.overriddenMethods = overriddenMethods;
		specMethod.calleeArguments = new HashMap<>();
		specMethod.calleeReceivers = new HashMap<>();
        specMethod.calleeConditions = new HashMap<>();
		this.specMethod = specMethod;
		Map<String, List<Expression>> values = new HashMap<>();
		for (Object obj : method.parameters()) {
		    SingleVariableDeclaration svd = (SingleVariableDeclaration) obj;
		    String key = getVariableKey(svd.getName());
		    List<Expression> value = new ArrayList<>();
		    value.add(svd.getName());
		    values.put(key, value);
		}
		stkValues.push(values);
	}
    
    private void getArguments(String[] names, List<?> arguments) {
        for (int i = 0; i < arguments.size(); i++) {
            Object arg = arguments.get(i);
            if (arg instanceof InfixExpression || arg instanceof ConditionalExpression)
                names[i] = "(" + arg.toString() + ")";
            else
                names[i] = arg.toString();
        }
    }

    public void addCallee(ASTNode node, IMethodBinding mb, String receiver, List<?> argumentExpressions) {
        if (method.parameters().isEmpty() && argumentExpressions.isEmpty())
            return;
        if (mb == null || mb.getMethodDeclaration() == null)
            return;
        if (isSelfSetter(node))
            return;
        String packageName = JavaASTUtil.getPackageName(method), calleePackageName = JavaASTUtil.getPackageName(mb);
        if (//!calleePackageName.startsWith("java.lang.") && !calleePackageName.startsWith("java.util.") &&
                (packageName.equals("") || calleePackageName.equals("") 
                || (!packageName.startsWith(calleePackageName) && !calleePackageName.startsWith(packageName))))
            return;
        mb = mb.getMethodDeclaration();
        for (Object obj : argumentExpressions) {
            Expression e = (Expression) obj;
            if (e instanceof SimpleName) {
                IBinding b = ((SimpleName) e).resolveBinding();
                if (b != null && b instanceof IVariableBinding) {
                    IVariableBinding vb = (IVariableBinding) b;
                    if (!vb.isField() && !vb.isParameter() && !vb.isEffectivelyFinal())
                        return;
                }
            }
        }
        if (argumentExpressions.isEmpty() && Modifier.isStatic(mb.getModifiers()))
            return;
        if (argumentExpressions.size() > 0) {
            boolean isConstants = true;
            for (Object obj : argumentExpressions) {
                Expression e = (Expression) obj;
                if (JavaASTUtil.isLiteral(e)) {
                    continue;
                }
                IVariableBinding vb = null;
                if (e instanceof Name) {
                    IBinding b = ((Name) e).resolveBinding();
                    if (b != null && b instanceof IVariableBinding)
                        vb = (IVariableBinding) b;
                } else if (e instanceof FieldAccess) {
                    vb = ((FieldAccess) e).resolveFieldBinding();
                } else if (e instanceof SuperFieldAccess) {
                    vb = ((SuperFieldAccess) e).resolveFieldBinding();
                }
                if (vb != null && !vb.isParameter() && (Modifier.isFinal(vb.getModifiers()) || vb.isEffectivelyFinal()))
                    continue;
                isConstants = false;
                break;
            }
            if (isConstants)
                return;
        }
        ITypeBinding tb = mb.getDeclaringClass();
        if (!mb.isDefaultConstructor() && !mb.isSynthetic() && !mb.isAnnotationMember()
                && tb != null && tb.isFromSource() && !tb.isLocal() && !tb.isAnnotation()
                && (!tb.isEnum() || (!mb.getName().equals("values") && !mb.getName().equals("valueOf")))) {
            String callee = mb.getDeclaringClass().getTypeDeclaration().getQualifiedName() + "." + JavaASTUtil.buildNameWithParameters(mb);
            if (!isRecursive(callee, this.specMethod.id, this.specMethod.calleeArguments.keySet())) {
                List<String[]> sites = this.specMethod.calleeArguments.get(callee);
                List<String> receivers = this.specMethod.calleeReceivers.get(callee);
                List<String> conditions = this.specMethod.calleeConditions.get(callee);
                if (sites == null) {
                    sites = new ArrayList<>();
                    this.specMethod.calleeArguments.put(callee, sites);
                    receivers = new ArrayList<>();
                    this.specMethod.calleeReceivers.put(callee, receivers);
                    conditions = new ArrayList<>();
                    this.specMethod.calleeConditions.put(callee, conditions);
                }
                String[] arguments = new String[argumentExpressions.size()];
                getArguments(arguments, argumentExpressions);
                if (!contains(receivers, sites, receiver, arguments)) {
                    receivers.add(receiver);
                    sites.add(arguments);
                    String guard = getGuard(node);
                    if (guard == null)
                        guard = "true";
                    conditions.add(guard);
                }
            }
        }
    }
	
	private boolean isSelfSetter(ASTNode node) {
	    if (node instanceof MethodInvocation) {
	        MethodInvocation mi = (MethodInvocation) node;
	        String name = mi.getName().getIdentifier();
	        if (mi.arguments().size() == 1 && name.length() > 3 && name.startsWith("set")) {
	            String field = name.substring(3).toLowerCase();
	            Object arg = mi.arguments().get(0);
	            if (!(arg instanceof SimpleName) && arg.toString().toLowerCase().endsWith(field))
	                return true;
	        }
	    }
        return false;
    }

    private String getGuard(ASTNode node) {
        ASTNode p = node.getParent();
        if (p == null || p == method)
            return null;
        if (p instanceof Block) {
            List<?> statements = ((Block) p).statements();
            int index = statements.indexOf(node) - 1;
            while (index >= 0) {
                Object statement = statements.get(index);
                if (statement instanceof IfStatement && isTransferControl((IfStatement) statement)) {
                    String condition = JavaASTUtil.negate(((IfStatement) (statements.get(index))).getExpression()).toString();
                    return condition;
                }
                index--;
            }
            return getGuard(p);
        } else if (p instanceof IfStatement) {
            String condition = null;
            if (JavaASTUtil.isInTrueBranch(node, (IfStatement) p))
                condition = JavaASTUtil.normalize(((IfStatement) p).getExpression()).toString();
            else
                condition = JavaASTUtil.negate(((IfStatement) p).getExpression()).toString();
            return condition;
        } else
            return getGuard(p);
    }

    private boolean contains(List<String> receivers, List<String[]> sites, String receiver, String[] arguments) {
	    for (int i = 0; i < sites.size(); i++) {
	        String[] site = sites.get(i);
	        if ((receiver == null && receivers.get(i) == null || receiver != null && receiver.equals(receivers.get(i))) && Arrays.equals(site, arguments))
	            return true;
	    }
        return false;
    }

    @Override
	public void endVisit(ClassInstanceCreation node) {
        IMethodBinding mb = node.resolveConstructorBinding();
        substitute((List<ASTNode>) (node.arguments()));
        addCallee(node, mb, node.getExpression() == null ? null : node.getExpression().toString(), node.arguments());
	}

    @Override
	public void endVisit(ConstructorInvocation node) {
        IMethodBinding mb = node.resolveConstructorBinding();
        substitute((List<ASTNode>) (node.arguments()));
        addCallee(node, mb, null, node.arguments());
	}
	
	@Override
	public void endVisit(SuperConstructorInvocation node) {
        IMethodBinding mb = node.resolveConstructorBinding();
        substitute((List<ASTNode>) (node.arguments()));
        addCallee(node, mb, null, node.arguments());
	}
	
	@Override
	public void endVisit(MethodInvocation node) {
	    IMethodBinding mb = node.resolveMethodBinding();
	    if (mb != null && Modifier.isStatic(mb.getModifiers())) {
    	    ITypeBinding tb = mb.getDeclaringClass();
    	    if (tb != null) {
        	    if (tb.getTypeDeclaration() != null)
        	        tb = tb.getTypeDeclaration();
        	    node.setExpression(JavaASTUtil.createName(node.getAST(), tb.getQualifiedName()));
    	    } else
    	        node.setExpression(node.getAST().newSimpleName("Static"));
	    }
	    substitute((List<ASTNode>) (node.arguments()));
        addCallee(node, mb, node.getExpression() == null || node.getExpression() instanceof ThisExpression ? null : node.getExpression().toString(), node.arguments());
	}
    
    @Override
    public void endVisit(SuperMethodInvocation node) {
        IMethodBinding mb = node.resolveMethodBinding();
        substitute((List<ASTNode>) (node.arguments()));
        addCallee(node, mb, null, node.arguments());
    }

    private boolean isRecursive(String callee, String caller) {
	    if (callee.equals(caller))
	        return true;
//	    caller = this.overriddenMethods.get(caller);
//	    if (caller != null)
//	        return isRecursive(callee, caller);
        return false;
    }

    private boolean isRecursive(String callee, String caller, Set<String> callees) {
        if (isRecursive(callee, caller))
            return true;
        for (String other : callees)
            if (isRecursive(callee, other))
                return true;
        return false;
    }

    @Override
	public boolean visit(AnonymousClassDeclaration node) {
	    return false;
	}
    
    @Override
    public boolean visit(FieldAccess node) {
        IVariableBinding vb = node.resolveFieldBinding();
        if (vb != null) {
            String key = vb.getKey();
            if (key != null) {
                Map<String, List<Expression>> values = stkValues.peek();
                List<Expression> value = values.get(key);
                if (value != null && value.size() == 1) {
                    Expression val = value.get(0);
                    if (val instanceof InfixExpression || val instanceof ConditionalExpression) {
                        ParenthesizedExpression newNode = node.getAST().newParenthesizedExpression();
                        newNode.setExpression((Expression) ASTNode.copySubtree(newNode.getAST(), val));
                        JavaASTUtil.replace(node, newNode);
                    } else {
                        JavaASTUtil.replace(node, ASTNode.copySubtree(node.getAST(), val));
                    }
                    return false;
                }
            }
        }
        node.getExpression().accept(this);
        return false;
    }
    
//    @Override
//    public boolean visit(ThisExpression node) {
//        ITypeBinding tb = node.resolveTypeBinding();
//        if (tb != null) {
//            tb = tb.getTypeDeclaration();
//            String qn = JavaASTUtil.getQualifiedName(tb);
//            if (!qn.isEmpty()) {
//                Name name = JavaASTUtil.createName(node.getAST(), qn);
//                JavaASTUtil.replace(node, name);
//            }
//        }
//        return false;
//    }
    
    @Override
    public boolean visit(SuperFieldAccess node) {
        IVariableBinding vb = node.resolveFieldBinding();
        if (vb != null) {
            String key = vb.getKey();
            if (key != null) {
                Map<String, List<Expression>> values = stkValues.peek();
                List<Expression> value = values.get(key);
                if (value != null && value.size() == 1) {
                    Expression val = value.get(0);
                    if (val instanceof InfixExpression || val instanceof ConditionalExpression) {
                        ParenthesizedExpression newNode = node.getAST().newParenthesizedExpression();
                        newNode.setExpression((Expression) ASTNode.copySubtree(newNode.getAST(), val));
                        JavaASTUtil.replace(node, newNode);
                    } else {
                        JavaASTUtil.replace(node, ASTNode.copySubtree(node.getAST(), val));
                    }
                    return false;
                }
            }
        }
        String qn = JavaASTUtil.getQualifiedName(vb.getDeclaringClass().getTypeDeclaration());
        if (!qn.isEmpty()) {
            Name name = JavaASTUtil.createName(node.getAST(), qn + "." + node.getName().getIdentifier());
            JavaASTUtil.replace(node, name);
        }
        return false;
    }
    
    @Override
    public boolean visit(QualifiedName node) {
        node.getQualifier().accept(this);
        return false;
    }
    
    @Override
    public boolean visit(Assignment node) {
        node.getRightHandSide().accept(this);
        return false;
    }
    
    @Override
    public boolean visit(VariableDeclarationFragment node) {
        if (node.getInitializer() != null)
            node.getInitializer().accept(this);
        return false;
    }
    
    @Override
    public boolean visit(SimpleName node) {
        IBinding b = node.resolveBinding();
        if (b != null) {
            if (b instanceof IVariableBinding) {
                IVariableBinding vb = (IVariableBinding) b;
                String key = getVariableKey(node);
                if (key != null) {
                    Map<String, List<Expression>> values = stkValues.peek();
                    List<Expression> value = values.get(key);
                    if (value != null && value.size() == 1) {
                        ASTNode p = node.getParent();
                        Expression val = value.get(0);
                        if (p instanceof QualifiedName) {
                            if (node == ((QualifiedName) p).getQualifier()) {
                                if (val instanceof Name) {
                                    JavaASTUtil.replace(node, ASTNode.copySubtree(node.getAST(), val));
                                    return false;
                                }
                            } else {
                                if (val instanceof SimpleName) {
                                    JavaASTUtil.replace(node, ASTNode.copySubtree(node.getAST(), val));
                                    return false;
                                }
                            }
                        } else {
                            if (val instanceof InfixExpression || val instanceof ConditionalExpression) {
                                ParenthesizedExpression newNode = node.getAST().newParenthesizedExpression();
                                newNode.setExpression((Expression) ASTNode.copySubtree(newNode.getAST(), val));
                                JavaASTUtil.replace(node, newNode);
                            } else {
                                JavaASTUtil.replace(node, ASTNode.copySubtree(node.getAST(), val));
                            }
                            return false;
                        }
                    }
                }
                if (vb.isField()) {
                    ITypeBinding tb = vb.getDeclaringClass().getTypeDeclaration();
                    String qn = JavaASTUtil.getQualifiedName(tb);
                    if (!qn.isEmpty()) {
                        Name name = JavaASTUtil.createName(node.getAST(), qn + "." + node.getIdentifier());
                        JavaASTUtil.replace(node, name);
                        return false;
                    }
                }
            } else if (b instanceof ITypeBinding) {
                ITypeBinding tb = (ITypeBinding) b;
                tb = tb.getTypeDeclaration();
                String qn = JavaASTUtil.getQualifiedName(tb);
                if (!qn.isEmpty()) {
                    Name name = JavaASTUtil.createName(node.getAST(), qn);
                    JavaASTUtil.replace(node, name);
                }
            }
        }
        return false;
    }
    
    @Override
    public boolean visit(ArrayInitializer node) {
        if (!(node.getParent() instanceof ArrayCreation)) {
            SimpleName name = node.getAST().newSimpleName("_array_" + node.expressions().size() + "_" + Math.abs(node.toString().hashCode()));
            JavaASTUtil.replace(node, name);
            return false;
        }
        return true;
    }
    
    @Override
    public boolean visit(ConditionalExpression node) {
        SimpleName name = node.getAST().newSimpleName("_conditional_expression_" + Math.abs(node.toString().hashCode()));
        JavaASTUtil.replace(node, name);
        return false;
    }
    
    @Override
    public void endVisit(Assignment node) {
        String key = getVariableKey(node.getLeftHandSide());
        if (key != null) {
            Map<String, List<Expression>> values = stkValues.peek();
            List<Expression> value = new ArrayList<>();
            if (!JavaASTUtil.isLiteral(node))
                value.add(node.getRightHandSide());
            else
                value.add(node.getLeftHandSide());
            values.put(key, value);
        }
        JavaASTUtil.replace(node, ASTNode.copySubtree(node.getAST(), node.getRightHandSide()));
    }

    @Override
    public void endVisit(VariableDeclarationFragment node) {
        String key = getVariableKey(node.getName());
        Map<String, List<Expression>> values = stkValues.peek();
        List<Expression> value = new ArrayList<>();
        if (node.getInitializer() != null && !node.getInitializer().getClass().getSimpleName().endsWith("Literal"))
            value.add(node.getInitializer());
        else
            value.add(node.getName());
        values.put(key, value);
    }
    
    private String getVariableKey(Expression e) {
        if (e instanceof SimpleName)
            return getVariableKey((SimpleName) e);
        if (e instanceof FieldAccess)
            return getVariableKey((FieldAccess) e);
        if (e instanceof SuperFieldAccess)
            return getVariableKey((SuperFieldAccess) e);
        return null;
    }
    
    private String getVariableKey(FieldAccess name) {
        IVariableBinding vb = name.resolveFieldBinding();
        if (vb != null)
            return vb.getKey();
        return null;
        
    }
    
    private String getVariableKey(SuperFieldAccess name) {
        IVariableBinding vb = name.resolveFieldBinding();
        if (vb != null)
            return vb.getKey();
        return null;
        
    }
    
    private String getVariableKey(SimpleName name) {
        IBinding b = name.resolveBinding();
        if (b != null && b instanceof IVariableBinding) {
            IVariableBinding vb = (IVariableBinding) b;
//            if (!vb.isField() && !vb.isParameter())
                return vb.getKey();
        } 
        return null;
        
    }
    
    @Override
    public boolean visit(IfStatement node) {
        node.getExpression().accept(this);
        substitute(node.getExpression());
        Map<String, List<Expression>> values = stkValues.peek(), trueValues = new HashMap<>(), falseValues = new HashMap<>();
        for (String key : values.keySet()) {
            List<Expression> value = values.get(key);
            trueValues.put(key, new ArrayList<>(value));
            falseValues.put(key, new ArrayList<>(value));
        }
        stkValues.push(trueValues);
        node.getThenStatement().accept(this);
        stkValues.pop();
        if (node.getElseStatement() != null) {
            stkValues.push(falseValues);
            node.getElseStatement().accept(this);
            stkValues.pop();
        }
        for (String key : values.keySet()) {
            List<Expression> value = new ArrayList<>();
            value.addAll(trueValues.get(key));
            value.addAll(falseValues.get(key));
        }
        return false;
    }
    
    @Override
    public boolean visit(SwitchStatement node) {
        node.getExpression().accept(this);
        substitute(node.getExpression());
        for (int i = 0; i < node.statements().size(); i++)
            ((Statement) node.statements().get(i)).accept(this);
        return false;
    }
    
    private void substitute(List<ASTNode> list) {
        for (ASTNode node : list)
            substitute(node);
    }

	private void substitute(ASTNode node) {
        node.accept(new ASTVisitor() {
            @Override
            public boolean visit(AnonymousClassDeclaration node) {
                return false;
            }
            
            @Override
            public boolean visit(LambdaExpression node) {
                return false;
            }
            
            @Override
            public boolean visit(TypeDeclarationStatement node) {
                return false;
            }
            
            @Override
            public boolean visit(SimpleName name) {
                String key = getVariableKey(name);
                if (key != null) {
                    Map<String, List<Expression>> values = stkValues.peek();
                    List<Expression> value = values.get(key);
                    if (value != null && value.size() == 1) {
                        ASTNode p = name.getParent();
                        Expression val = value.get(0);
                        if (p instanceof QualifiedName) {
                            if (node == ((QualifiedName) p).getQualifier()) {
                                if (val instanceof Name)
                                    JavaASTUtil.replace(name, ASTNode.copySubtree(name.getAST(), val));
                            } else {
                                if (val instanceof SimpleName)
                                    JavaASTUtil.replace(name, ASTNode.copySubtree(name.getAST(), val));
                            }
                        } else {
                            if (val instanceof InfixExpression || val instanceof ConditionalExpression) {
                                ParenthesizedExpression newNode = name.getAST().newParenthesizedExpression();
                                newNode.setExpression((Expression) ASTNode.copySubtree(newNode.getAST(), val));
                                JavaASTUtil.replace(name, newNode);
                            } else {
                                JavaASTUtil.replace(name, ASTNode.copySubtree(name.getAST(), val));
                            }
                        }
                    }
                }
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
        });
    }
	
	@Override
	public boolean visit(TypeDeclarationStatement node) {
	    return false;
	}
	
	@Override
	public boolean visit(DoStatement node) {
	    return false;
	}
	
	@Override
	public boolean visit(EnhancedForStatement node) {
        return false;
	}
	
	@Override
	public boolean visit(ForStatement node) {
        return false;
	}
	
	@Override
	public boolean visit(WhileStatement node) {
        return false;
	}
	
	@Override
	public boolean visit(TryStatement node) {
	    for (Object obj : node.catchClauses())
	        ((ASTNode) obj).accept(this);
	    if (node.getFinally() != null)
	        node.getFinally().accept(this);
	    return false;
	}
	
    @Override
	public boolean visit(ThrowStatement node) {
//        String type = JavaASTUtil.getSimpleType(((ClassInstanceCreation) node.getExpression()).getType());
        String type = null;
        ITypeBinding tb = node.getExpression().resolveTypeBinding();
        if (tb != null) {
            if (tb.getTypeDeclaration() != null)
                tb = tb.getTypeDeclaration();
            type = tb.getName();
        }
//        if (node.getExpression() instanceof ClassInstanceCreation) {
        if (type != null) {
            String condition = null;
			ASTNode p = getParent(node);
			if (p == method)
				condition = "true";
			else if (p instanceof CatchClause) {
				if (!type.equals("Error"))
				    condition = "trycatch";
			} else if (p instanceof IfStatement) {
			    if (JavaASTUtil.isInTrueBranch(node, (IfStatement) p))
			        condition = JavaASTUtil.normalize(((IfStatement) p).getExpression()).toString();
                else
                    condition = JavaASTUtil.negate(((IfStatement) p).getExpression()).toString();
			} else if (p instanceof Block) {
				if (p.getParent() instanceof CatchClause) {
				    if (!type.equals("Error"))
	                    condition = "trycatch";
				} else if (p.getParent() instanceof IfStatement) {
					IfStatement pp = (IfStatement) p.getParent();
					if (p == pp.getThenStatement())
						condition = JavaASTUtil.normalize(pp.getExpression()).toString();
					else
						condition = JavaASTUtil.negate(pp.getExpression()).toString();
				} else {
					List<?> l = ((Block) p).statements();
					int index = l.indexOf(node) - 1;
					while (index >= 0) {
						if (l.get(index) instanceof IfStatement && isTransferControl((IfStatement) l.get(index))) {
							if (condition == null)
								condition = "";
							else
								condition += " && ";
							condition += "(" + JavaASTUtil.negate(((IfStatement) (l.get(index))).getExpression()).toString() + ")";
						}
						index--;
					}
				}
			} else if (p instanceof SwitchStatement) {
				SwitchStatement ss = (SwitchStatement) p;
				List<?> l = ss.statements();
				int index = l.size() - 1;
				while (true) {
					ASTNode s = (ASTNode) l.get(index);
					if (s.getStartPosition() <= node.getStartPosition() && s.getStartPosition() + s.getLength() >= node.getStartPosition() + node.getLength())
						break;
					index--;
				}
				index--;
				while (index >= 0) {
					if (l.get(index) instanceof SwitchCase) {
						SwitchCase sc = (SwitchCase) l.get(index);
						if (sc.isDefault()) {
							for (int i = 0; i < ss.statements().size(); i++) {
								if (ss.statements().get(i) instanceof SwitchCase) {
									sc = (SwitchCase) ss.statements().get(i);
									if (sc.isDefault())
										break;
									if (condition == null)
										condition = "(" + ss.getExpression().toString() + ") != " + sc.getExpression().toString();
									else
										condition += " && (" + ss.getExpression().toString() + ") != " + sc.getExpression().toString();
								}
							}
						} else {
							condition = "(" + ss.getExpression().toString() + ") == " + sc.getExpression().toString();
						}
						break;
					}
					index--;
				}
			}
			if (condition == null)
				condition = "unresolvable";
			else {
			    String guard = getGuard(p);
                if (guard == null)
                    guard = "true";
			    if (Utils.satcheck("&&", guard, condition) == Status.UNSATISFIABLE)
			        condition = "false";
			}
			String fullCondition = thrownExceptions.get(type);
			if (fullCondition == null)
				fullCondition = "(" + condition + ")";
			else {
				fullCondition = fullCondition + " || (" + condition + ")";
			}
			thrownExceptions.put(type, fullCondition);
		}
		return false;
	}

	private ASTNode getParent(ASTNode node) {
		ASTNode p = node.getParent();
		if (p instanceof Block) {
			if (((Block) p).statements().size() == 1)
				return getParent(p);
		}
		return p;
	}

	private boolean isTransferControl(IfStatement is) {
		if (is.getElseStatement() != null)
			return false;
		Statement s = null;
		if (is.getThenStatement() instanceof Block) {
			List<?> l = ((Block) is.getThenStatement()).statements();
			if (l.size() > 0)
				s = (Statement) l.get(l.size() - 1);
		} else
			s = is.getThenStatement();
		return isTransferStatement(s);
	}

	private boolean isTransferStatement(Statement s) {
		if (s == null)
			return false;
		if (s instanceof BreakStatement)
			return true;
		if (s instanceof ContinueStatement)
			return true;
		if (s instanceof ReturnStatement)
			return true;
        if (s instanceof ThrowStatement)
            return true;
		if (s instanceof ExpressionStatement) {
			ExpressionStatement es = (ExpressionStatement) s;
			if (es.getExpression() instanceof MethodInvocation) {
				MethodInvocation m = (MethodInvocation) es.getExpression();
				if (m.getName().getIdentifier().equals("exit") && m.arguments().size() == 1 && m.getExpression() != null && m.getExpression().toString().equals("System"))
					return true;
			}
		}
		return false;
	}
}
