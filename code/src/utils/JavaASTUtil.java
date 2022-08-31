package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.Assignment.Operator;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;

import com.microsoft.z3.Status;

import edu.stanford.nlp.trees.Tree;

public class JavaASTUtil {
	public static final HashMap<String, String> infixExpressionLables = new HashMap<>(), assignmentLabels = new HashMap<>();
	protected static final String PROPERTY_CONDITIONAL_OPERATOR = "CC";

	static {
		// Arithmetic Operators
		infixExpressionLables.put(InfixExpression.Operator.DIVIDE.toString(), "<a>");
		infixExpressionLables.put(InfixExpression.Operator.MINUS.toString(), "<a>");
		infixExpressionLables.put(InfixExpression.Operator.PLUS.toString(), "<a>");
		infixExpressionLables.put(InfixExpression.Operator.REMAINDER.toString(), "<a>");
		infixExpressionLables.put(InfixExpression.Operator.TIMES.toString(), "<a>");
		// Equality and Relational Operators
		infixExpressionLables.put(InfixExpression.Operator.EQUALS.toString(), "<r>");
		infixExpressionLables.put(InfixExpression.Operator.GREATER.toString(), "<r>");
		infixExpressionLables.put(InfixExpression.Operator.GREATER_EQUALS.toString(), "<r>");
		infixExpressionLables.put(InfixExpression.Operator.LESS.toString(), "<r>");
		infixExpressionLables.put(InfixExpression.Operator.LESS_EQUALS.toString(), "<r>");
		infixExpressionLables.put(InfixExpression.Operator.NOT_EQUALS.toString(), "<r>");
		// Conditional Operators
		infixExpressionLables.put(InfixExpression.Operator.CONDITIONAL_AND.toString(), "<c>");
		infixExpressionLables.put(InfixExpression.Operator.CONDITIONAL_OR.toString(), "<c>");
		// Bitwise and Bit Shift Operators
		infixExpressionLables.put(InfixExpression.Operator.AND.toString(), "<b>");
		infixExpressionLables.put(InfixExpression.Operator.OR.toString(), "<b>");
		infixExpressionLables.put(InfixExpression.Operator.XOR.toString(), "<b>");
		infixExpressionLables.put(InfixExpression.Operator.LEFT_SHIFT.toString(), "<b>");
		infixExpressionLables.put(InfixExpression.Operator.RIGHT_SHIFT_SIGNED.toString(), "<b>");
		infixExpressionLables.put(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED.toString(), "<b>");

		assignmentLabels.put(Assignment.Operator.ASSIGN.toString(), "=");
		// Arithmetic Operators
		assignmentLabels.put(Assignment.Operator.DIVIDE_ASSIGN.toString(), "<a>");
		assignmentLabels.put(Assignment.Operator.MINUS_ASSIGN.toString(), "<a>");
		assignmentLabels.put(Assignment.Operator.PLUS_ASSIGN.toString(), "<a>");
		assignmentLabels.put(Assignment.Operator.REMAINDER_ASSIGN.toString(), "<a>");
		assignmentLabels.put(Assignment.Operator.TIMES_ASSIGN.toString(), "<a>");
		// Bitwise and Bit Shift Operators
		assignmentLabels.put(Assignment.Operator.BIT_AND_ASSIGN.toString(), "<b>");
		assignmentLabels.put(Assignment.Operator.BIT_OR_ASSIGN.toString(), "<b>");
		assignmentLabels.put(Assignment.Operator.BIT_XOR_ASSIGN.toString(), "<b>");
		assignmentLabels.put(Assignment.Operator.LEFT_SHIFT_ASSIGN.toString(), "<b>");
		assignmentLabels.put(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN.toString(), "<b>");
		assignmentLabels.put(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN.toString(), "<b>");
	}

	@SuppressWarnings("rawtypes")
	public static ASTNode parseSource(String source, String path, String name, String[] classpaths) {
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		String srcDir = getSrcDir(source, path, name);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setEnvironment(
				classpaths == null ? new String[]{} : classpaths, 
						new String[]{srcDir}, 
						new String[]{"UTF-8"}, 
						true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setSource(source.toCharArray());
		parser.setUnitName(name);
		ASTNode ast = parser.createAST(null);
		return ast;
	}

	@SuppressWarnings("rawtypes")
	public static ASTNode parseSource(String source, int kind) {
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setSource(source.toCharArray());
		parser.setKind(kind);
		ASTNode ast = parser.createAST(null);
		return ast;
	}

    public static Expression parseExpression(String source) {
        ASTNode ast = parseSource(source, ASTParser.K_EXPRESSION);
        if (isExpression(ast, source))
            return (Expression) ast;
        return null;
    }
    
	@SuppressWarnings("rawtypes")
	private static String getSrcDir(String source, String path, String name) {
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setSource(source.toCharArray());
		ASTNode ast = parser.createAST(null);
		CompilationUnit cu =  (CompilationUnit) ast;
		String srcDir = path;
		if (cu.getPackage() != null) {
			String p = cu.getPackage().getName().getFullyQualifiedName();
			int end = path.length() - p.length() - 1 - name.length();
			if (end > 0)
				srcDir = path.substring(0, end);
		} else {
			int end = path.length() - name.length();
			if (end > 0)
				srcDir = path.substring(0, end);
		}
		return srcDir;
	}

	public static String getSource(ASTNode node) {
		NaiveASTFlattener flatterner = new NaiveASTFlattener();
		node.accept(flatterner);
		return flatterner.getResult();
	}

	public static boolean isLiteral(int astNodeType) {
		return ASTNode.nodeClassForType(astNodeType).getSimpleName().endsWith("Literal");
	}

    protected static boolean areLiterals(List<?> list) {
        for (Object obj : list)
            if (!(obj instanceof ASTNode) || !isLiteral((ASTNode) obj))
                return false;
        return true;
    }

	public static boolean isLiteral(ASTNode node) {
		int type = node.getNodeType();
		if (type == ASTNode.BOOLEAN_LITERAL || 
				type == ASTNode.CHARACTER_LITERAL || 
				type == ASTNode.NULL_LITERAL || 
				type == ASTNode.NUMBER_LITERAL || 
				type == ASTNode.STRING_LITERAL)
			return true;
		if (node instanceof QualifiedName) {
		    String name = FileIO.getSimpleClassName(((Name) node).getFullyQualifiedName());
		    return name.equals(name.toUpperCase());
		}
		if (type == ASTNode.PREFIX_EXPRESSION) {
			PrefixExpression pe = (PrefixExpression) node;
			return isLiteral(pe.getOperand());
		}
		if (type == ASTNode.POSTFIX_EXPRESSION) {
			PostfixExpression pe = (PostfixExpression) node;
			return isLiteral(pe.getOperand());
		}
		if (type == ASTNode.PARENTHESIZED_EXPRESSION) {
			ParenthesizedExpression pe = (ParenthesizedExpression) node;
			return isLiteral(pe.getExpression());
		}

		return false;
	}

	public static boolean isPublic(MethodDeclaration declaration) {
		for (int i = 0; i < declaration.modifiers().size(); i++) {
			Modifier m = (Modifier) declaration.modifiers().get(i);
			if (m.isPublic())
				return true;
		}
		return false;
	}
    
	public static String getParameters(MethodDeclaration method) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < method.parameters().size(); i++) {
            SingleVariableDeclaration d = (SingleVariableDeclaration) (method.parameters().get(i));
            String type = getSimpleType(d.getType());
            for (int j = 0; j < d.getExtraDimensions(); j++)
                type += "[]";
            if (d.isVarargs())
                type += "[]";
            sb.append("\t" + type);
        }
        sb.append("\t)");
        return sb.toString();
    }

	public static String buildNameWithParameters(MethodDeclaration method) {
		return method.getName().getIdentifier() + "\t" + getParameters(method);
	}

	public static String buildNameWithParameters(IMethodBinding mb) {
        return mb.getName() + "\t" + getParameters(mb);
	}

	private static String getParameters(IMethodBinding mb) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < mb.getParameterTypes().length; i++) {
            ITypeBinding tb = mb.getParameterTypes()[i].getTypeDeclaration();
            String type = "";
            if (tb.isArray()) {
                type = tb.getElementType().getTypeDeclaration().getName();
                for (int j = 0; j < tb.getDimensions(); j++)
                    type += "[]";
            } else
                type = tb.getName();
            sb.append("\t" + type);
        }
        sb.append("\t)");
        return sb.toString();
    }

    public static String getSimpleType(VariableDeclarationFragment f) {
		String dimensions = "";
		for (int i = 0; i < f.getExtraDimensions(); i++)
			dimensions += "[]";
		ASTNode p = f.getParent();
		if (p instanceof FieldDeclaration)
			return getSimpleType(((FieldDeclaration) p).getType()) + dimensions;
		if (p instanceof VariableDeclarationStatement)
			return getSimpleType(((VariableDeclarationStatement) p).getType()) + dimensions;
		if (p instanceof VariableDeclarationExpression)
			return getSimpleType(((VariableDeclarationExpression) p).getType()) + dimensions;
		throw new UnsupportedOperationException("Get type of a declaration!!!");
	}

	public static String getSimpleType(Type type) {
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			String pt = getSimpleType(t.getElementType());
			for (int i = 0; i < t.getDimensions(); i++)
				pt += "[]";
			return pt;
			//return type.toString();
		} else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getSimpleType(t.getType());
		} else if (type.isPrimitiveType()) {
			String pt = type.toString();
			/*if (pt.equals("byte") || pt.equals("short") || pt.equals("int") || pt.equals("long") 
					|| pt.equals("float") || pt.equals("double"))
				return "number";*/
			return pt;
		} else if (type.isQualifiedType()) {
			QualifiedType t = (QualifiedType) type;
			return t.getName().getIdentifier();
		} else if (type.isSimpleType()) {
			String pt = type.toString();
			pt = FileIO.getSimpleClassName(pt);
			/*if (pt.equals("Byte") || pt.equals("Short") || pt.equals("Integer") || pt.equals("Long") 
					|| pt.equals("Float") || pt.equals("Double"))
				return "number";*/
			return pt;
		} else if (type.isUnionType()) {
			UnionType ut = (UnionType) type;
			String s = getSimpleType((Type) ut.types().get(0));
			for (int i = 1; i < ut.types().size(); i++)
				s += "|" + getSimpleType((Type) ut.types().get(i));
			return s;
		} else if (type.isWildcardType()) {
			//WildcardType t = (WildcardType) type;
			System.err.println("ERROR: Declare a variable with wildcard type!!!");
			System.exit(0);
		}
		System.err.println("ERROR: Declare a variable with unknown type!!!");
		System.exit(0);
		return null;
	}

	public static String getSimpleType(Type type, HashSet<String> typeParameters) {
		if (type.isArrayType()) {
			ArrayType t = (ArrayType) type;
			String pt = getSimpleType(t.getElementType(), typeParameters);
			for (int i = 0; i < t.getDimensions(); i++)
				pt += "[]";
			return pt;
			//return type.toString();
		} else if (type.isParameterizedType()) {
			ParameterizedType t = (ParameterizedType) type;
			return getSimpleType(t.getType(), typeParameters);
		} else if (type.isPrimitiveType()) {
			return type.toString();
		} else if (type.isQualifiedType()) {
			QualifiedType t = (QualifiedType) type;
			return t.getName().getIdentifier();
		} else if (type.isSimpleType()) {
			if (typeParameters.contains(type.toString()))
				return "Object";
			return type.toString();
		} else if (type.isUnionType()) {
			UnionType ut = (UnionType) type;
			String s = getSimpleType((Type) ut.types().get(0), typeParameters);
			for (int i = 1; i < ut.types().size(); i++)
				s += "|" + getSimpleType((Type) ut.types().get(i), typeParameters);
			return s;
		} else if (type.isWildcardType()) {
			//WildcardType t = (WildcardType) type;
			System.err.println("ERROR: Declare a variable with wildcard type!!!");
			System.exit(0);
		}
		System.err.println("ERROR: Declare a variable with unknown type!!!");
		System.exit(0);
		return null;
	}

	public static String getSimpleName(Name name) {
		if (name.isSimpleName())
			return name.toString();
		QualifiedName qn = (QualifiedName) name;
		return qn.getName().getIdentifier();
	}

	public static String getInfixOperator(Operator operator) {
		if (operator == Operator.ASSIGN)
			return null;
		String op = operator.toString();
		return op.substring(0, op.length() - 1);
	}

	public static TypeDeclaration getType(TypeDeclaration td, String name) {
		for (TypeDeclaration inner : td.getTypes())
			if (inner.getName().getIdentifier().equals(name))
				return inner;
		return null;
	}

	public static boolean isDeprecated(MethodDeclaration method) {
		Javadoc doc = method.getJavadoc();
		if (doc != null) {
			for (int i = 0; i < doc.tags().size(); i++) {
				TagElement tag = (TagElement) doc.tags().get(i);
				if (tag.getTagName() != null && tag.getTagName().toLowerCase().equals("@deprecated"))
					return true;
			}
		}
		return false;
	}

	public static int countLeaves(ASTNode node) {
		class LeaveCountASTVisitor extends ASTVisitor {
			private Stack<Integer> numOfChildren = new Stack<Integer>();
			private int numOfLeaves = 0;

			public LeaveCountASTVisitor() {
				numOfChildren.push(0);
			}

			@Override
			public void preVisit(ASTNode node) {
				int n = numOfChildren.pop();
				numOfChildren.push(n + 1);
				numOfChildren.push(0);
			}

			@Override
			public void postVisit(ASTNode node) {
				int n = numOfChildren.pop();
				if (n == 0)
					numOfLeaves++;
			}
		};
		LeaveCountASTVisitor v = new LeaveCountASTVisitor();
		node.accept(v);
		return v.numOfLeaves;
	}

	public static ArrayList<String> tokenizeNames(ASTNode node) {
		return new ASTVisitor() {
			private ArrayList<String> names = new ArrayList<>();

			@Override
			public boolean visit(org.eclipse.jdt.core.dom.SimpleName node) {
				names.add(node.getIdentifier());
				return false;
			};
		}.names;
	}

	public static String buildLabel(InfixExpression.Operator operator) {
		return infixExpressionLables.get(operator.toString());
	}

	public static String getAssignOperator(Operator operator) {
		return assignmentLabels.get(operator.toString());
	}

	public static boolean isInfixExpression(Tree tree) {
		StringBuilder sb = new StringBuilder();
		for (Tree l : tree.getLeaves())
			sb.append(l.value() + " ");
		String source = sb.toString();
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(source.toCharArray());
		ASTNode ast = parser.createAST(null);
		return (ast != null && ((ast.getFlags() & ASTNode.RECOVERED) == 0) && ((ast.getFlags() & ASTNode.MALFORMED) == 0) 
				&& ast.getNodeType() == ASTNode.INFIX_EXPRESSION 
				&& ast.toString().length() >= source.length() - tree.getLeaves().size());
	}

	public static boolean isMethodInvocation(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(source.toCharArray());
		ASTNode ast = parser.createAST(null);
		if (!(ast instanceof MethodInvocation))
			return false;
		return isExpression(ast, source);
	}

	public static boolean isExpression(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(source.toCharArray());
		ASTNode ast = parser.createAST(null);
		return isExpression(ast, source);
	}

	private static boolean isExpression(ASTNode ast, String source) {
		if (ast == null || !(ast instanceof Expression) || ast.toString().length() < source.length() / 2)
			return false;
		ErrorCheckVisitor v = new ErrorCheckVisitor();
		ast.accept(v);
		return !v.hasError;
	}
	
	private static class ErrorCheckVisitor extends ASTVisitor {
		public boolean hasError = false;

		@Override
		public boolean preVisit2(ASTNode node) {
			if ((node.getFlags() & ASTNode.MALFORMED) != 0 || (node.getFlags() & ASTNode.RECOVERED) != 0)
				hasError = true;
			return !hasError;
		}
	}

	public static String tokenize(String condition, final boolean encloseClauseWithParentheses) {
		ASTNode ast = JavaASTUtil.parseSource(condition, ASTParser.K_EXPRESSION);
		if (isExpression(ast, condition))
			return tokenize(ast, encloseClauseWithParentheses);
		return condition;
	}

	public static String tokenize(final ASTNode ast, final boolean encloseClauseWithParentheses) {
		NaiveASTFlattener visitor = new NaiveASTFlattener() {
			
			@Override
			public void preVisit(ASTNode node) {
				if (!encloseClauseWithParentheses)
					return;
				ASTNode p = getParent(node);
				if (node instanceof InfixExpression) {
					InfixExpression.Operator op = ((InfixExpression) node).getOperator();
					if ((p == null || !(p instanceof MethodInvocation))
							&& (op == InfixExpression.Operator.CONDITIONAL_AND || op == InfixExpression.Operator.CONDITIONAL_OR))
						node.setProperty(PROPERTY_CONDITIONAL_OPERATOR, true);
				} else if (node instanceof PrefixExpression) {
				    PrefixExpression.Operator op = ((PrefixExpression) node).getOperator();
				    if ((p == null || !(p instanceof MethodInvocation))
				            && (op == PrefixExpression.Operator.NOT))
				        node.setProperty(PROPERTY_CONDITIONAL_OPERATOR, true);
				}
				if (!(node instanceof ParenthesizedExpression) && !(node instanceof Name) 
						&& node.getProperty(PROPERTY_CONDITIONAL_OPERATOR) == null 
						&& p != null && p.getProperty(PROPERTY_CONDITIONAL_OPERATOR) != null)
					this.buffer.append(" ( ");
			}

			@Override
			public void postVisit(ASTNode node) {
				if (!encloseClauseWithParentheses)
					return;
				ASTNode p = getParent(node);
				if (!(node instanceof ParenthesizedExpression) && !(node instanceof Name) 
						&& node.getProperty(PROPERTY_CONDITIONAL_OPERATOR) == null 
						&& p != null && p.getProperty(PROPERTY_CONDITIONAL_OPERATOR) != null)
					this.buffer.append(" ) ");
			}
			
			private ASTNode getParent(ASTNode node) {
				if (node == ast)
					return null;
				ASTNode p = node.getParent();
				if (p != null && p instanceof ParenthesizedExpression)
					return getParent(p);
				return p;
			}
			
			@Override
			public boolean visit(Assignment node) {
				node.getRightHandSide().accept(this);
				return false;
			}

			@Override
			public boolean visit(ClassInstanceCreation node) {
				this.buffer.append(" new ");//$NON-NLS-1$
				this.buffer.append(JavaASTUtil.getSimpleType(node.getType()));
				this.buffer.append(" ( ");//$NON-NLS-1$
				for (Iterator<?> it = node.arguments().iterator(); it.hasNext(); ) {
					Expression e = (Expression) it.next();
					e.accept(this);
					if (it.hasNext()) {
						this.buffer.append(" , ");//$NON-NLS-1$
					}
				}
				this.buffer.append(" ) ");//$NON-NLS-1$
				return false;
			}
			
			@Override
			public boolean visit(CastExpression node) {
				this.buffer.append(" ( ");//$NON-NLS-1$
				node.getType().accept(this);
				this.buffer.append(" ) ");//$NON-NLS-1$
				node.getExpression().accept(this);
				return false;
			}

			@Override
			public boolean visit(FieldAccess node) {
				Expression e = node.getExpression();
				if (!(e instanceof ThisExpression)) {
					node.getExpression().accept(this);
					this.buffer.append(" . ");//$NON-NLS-1$
				}
				node.getName().accept(this);
				return false;
			}
			
			@Override
			public boolean visit(SuperFieldAccess node) {
				node.getName().accept(this);
				return false;
			}

			@Override
			public boolean visit(MethodInvocation node) {
				this.buffer.append(" ");
				if (node.getExpression() != null && !(node.getExpression() instanceof ThisExpression)) {
					node.getExpression().accept(this);
					this.buffer.append(" . ");//$NON-NLS-1$
				}
				this.buffer.append(node.getName().getIdentifier());
				if (node.arguments().isEmpty())
					this.buffer.append("() ");
				else {
					this.buffer.append(" ( ");//$NON-NLS-1$
					for (Iterator<?> it = node.arguments().iterator(); it.hasNext(); ) {
						Expression e = (Expression) it.next();
						e.accept(this);
						if (it.hasNext()) {
							this.buffer.append(" , ");//$NON-NLS-1$
						}
					}
					this.buffer.append(" ) ");//$NON-NLS-1$
				}
				return false;
			}
			
			@Override
			public boolean visit(SuperMethodInvocation node) {
				this.buffer.append(node.getName().getIdentifier());
				if (node.arguments().isEmpty())
					this.buffer.append("() ");
				else {
					this.buffer.append(" ( ");//$NON-NLS-1$
					for (Iterator<?> it = node.arguments().iterator(); it.hasNext(); ) {
						Expression e = (Expression) it.next();
						e.accept(this);
						if (it.hasNext()) {
							this.buffer.append(" , ");//$NON-NLS-1$
						}
					}
					this.buffer.append(" ) ");//$NON-NLS-1$
				}
				return false;
			}
			
			@Override
			public boolean visit(ParenthesizedExpression node) {
			    boolean needParenthesis = false;
			    Expression e = node.getExpression();
			    ASTNode p = node.getParent();
			    if (e instanceof InfixExpression && p instanceof InfixExpression 
			            && ((InfixExpression) e).getOperator() == InfixExpression.Operator.DIVIDE
			            && ((InfixExpression) p).getOperator() == InfixExpression.Operator.TIMES)
			        needParenthesis = true;
				if (needParenthesis)
				    this.buffer.append(" ( ");
				node.getExpression().accept(this);
				if (needParenthesis)
                    this.buffer.append(" ) ");
				return false;
			}

			@Override
			public boolean visit(PostfixExpression node) {
				node.getOperand().accept(this);
				this.buffer.append(" ");
				this.buffer.append(node.getOperator().toString());
				return false;
			}

			@Override
			public boolean visit(PrefixExpression node) {
				this.buffer.append(node.getOperator().toString());
				this.buffer.append(" ");
				node.getOperand().accept(this);
				return false;
			}
			
			@Override
			public boolean visit(InfixExpression node) {
			    InfixExpression.Operator op = node.getOperator();
			    if (isComparation(op)) {
			        Expression[] operands = null;
			        if (node.getLeftOperand() instanceof NumberLiteral && node.getLeftOperand().toString().equals("0")) {
			            operands = getMinusOperands(node.getRightOperand());
	                    if (operands != null) {
	                        JavaASTUtil.replace(node.getLeftOperand(), ASTNode.copySubtree(node.getAST(), operands[1]));
                            JavaASTUtil.replace(node.getRightOperand(), ASTNode.copySubtree(node.getAST(), operands[0]));
	                    }
			        } else if (node.getRightOperand() instanceof NumberLiteral && node.getRightOperand().toString().equals("0")) {
                        operands = getMinusOperands(node.getLeftOperand());
                        if (operands != null) {
                            JavaASTUtil.replace(node.getLeftOperand(), ASTNode.copySubtree(node.getAST(), operands[0]));
                            JavaASTUtil.replace(node.getRightOperand(), ASTNode.copySubtree(node.getAST(), operands[1]));
                        }
                    }
			    }
                return super.visit(node);
			}
			
			private Expression[] getMinusOperands(Expression expr) {
			    if (expr instanceof ParenthesizedExpression)
			        return getMinusOperands(((ParenthesizedExpression) expr).getExpression());
			    if (expr instanceof InfixExpression) {
			        InfixExpression ie = (InfixExpression) expr;
			        if (ie.getOperator() == InfixExpression.Operator.MINUS)
			            return new Expression[]{ie.getLeftOperand(), ie.getRightOperand()};
			    }
                return null;
            }

            @Override
			public boolean visit(QualifiedName node) {
                List<String> list = new ArrayList<>();
                Name temp = node;
                while (true) {
                    if (temp instanceof SimpleName) {
                        list.add(0, temp.getFullyQualifiedName());
                        break;
                    }
                    QualifiedName qn = (QualifiedName) temp;
                    String name = qn.getName().getIdentifier();
                    list.add(0, name);
                    if (Character.isUpperCase(name.charAt(0)))
                        break;
                    temp = qn.getQualifier();
                }
                this.buffer.append(list.get(0));
                for (int i = 1; i < list.size(); i++)
                    this.buffer.append(" . " + list.get(i));
				return false;
			}
			
			@Override
			public boolean visit(SimpleName node) {
				return super.visit(node);
			}
		};
		ast.accept(visitor);
		String source = visitor.getResult().trim();
		String[] tokens = source.split("[\\s]+");
		StringBuilder sb = new StringBuilder();
		sb.append(tokens[0]);
		for (int i = 1; i < tokens.length; i++)
			sb.append(" " + tokens[i]);
		return sb.toString();
	}

	public static boolean isComparation(InfixExpression.Operator op) {
        return op == InfixExpression.Operator.LESS
                || op == InfixExpression.Operator.LESS_EQUALS
                || op == InfixExpression.Operator.GREATER
                || op == InfixExpression.Operator.GREATER_EQUALS
                || op == InfixExpression.Operator.EQUALS
                || op == InfixExpression.Operator.NOT_EQUALS;
    }
	
	public static String getFQN(ASTNode node) {
	    if (node instanceof Name)
	        return getFQN((Name) node);
        if (node instanceof FieldAccess)
            return getFQN((FieldAccess) node);
        if (node instanceof SuperFieldAccess)
            return getFQN((SuperFieldAccess) node);
	    return null;
	}
    
    public static String getFQN(Name node) {
        if (node.isQualifiedName()) {
            QualifiedName qn = (QualifiedName) node;
            String qual = getFQN(qn.getQualifier());
            if (qual != null)
                return qual + "." + qn.getName().getIdentifier();
        } else {
            IBinding b = node.resolveBinding();
            if (b != null && b instanceof IVariableBinding) {
                IVariableBinding vb = (IVariableBinding) b;
                if (vb.isField()) {
                    ITypeBinding tb = vb.getDeclaringClass();
                    if (tb != null) {
                        tb = tb.getTypeDeclaration();
                        String qn = getQualifiedName(tb);
                        if (!qn.isEmpty())
                            return qn + "." + node.toString();
                    }
                }
            }
        }
        return null;
    }
    
    public static String getFQN(FieldAccess node) {
        if (node.getExpression() instanceof ThisExpression) {
            IVariableBinding vb = node.resolveFieldBinding();
            if (vb != null && vb.getDeclaringClass() != null) {
                ITypeBinding tb = vb.getDeclaringClass().getTypeDeclaration();
                String qn = getQualifiedName(tb);
                if (!qn.isEmpty())
                    return qn + "." + node.getName().getIdentifier();
            }
        }
        return null;
    }
    
    public static String getFQN(SuperFieldAccess node) {
        IVariableBinding vb = node.resolveFieldBinding();
        if (vb != null && vb.getDeclaringClass() != null) {
            ITypeBinding tb = vb.getDeclaringClass().getTypeDeclaration();
            String qn = getQualifiedName(tb);
            if (!qn.isEmpty())
                return qn + "." + node.getName().getIdentifier();
        }
        return null;
    }
    
    public static String getQualifiedName(ITypeBinding tb) {
        String qn = tb.getQualifiedName();
        if (qn.isEmpty())
            qn = tb.getName();
        return qn;
    }
    
	public static Expression negate(Expression ex) {
		Expression e = (Expression) ASTNode.copySubtree(ex.getAST(), ex);
		if (e instanceof InfixExpression) {
			InfixExpression ie = (InfixExpression) e;
			InfixExpression.Operator op = ie.getOperator();
			if (op == InfixExpression.Operator.EQUALS)
				op = InfixExpression.Operator.NOT_EQUALS;
			else if (op == InfixExpression.Operator.NOT_EQUALS)
				op = InfixExpression.Operator.EQUALS;
			else if (op == InfixExpression.Operator.GREATER)
				op = InfixExpression.Operator.LESS_EQUALS;
			else if (op == InfixExpression.Operator.GREATER_EQUALS)
				op = InfixExpression.Operator.LESS;
			else if (op == InfixExpression.Operator.LESS)
				op = InfixExpression.Operator.GREATER_EQUALS;
			else if (op == InfixExpression.Operator.LESS_EQUALS)
				op = InfixExpression.Operator.GREATER;
			else if (op == InfixExpression.Operator.CONDITIONAL_AND) {
				op = InfixExpression.Operator.CONDITIONAL_OR;
				List<?> list = ie.structuralPropertiesForType();
				negate(list, ie);
			} else if (op == InfixExpression.Operator.CONDITIONAL_OR) {
				op = InfixExpression.Operator.CONDITIONAL_AND;
				List<?> list = ie.structuralPropertiesForType();
				negate(list, ie);
			} else {
				PrefixExpression pe = e.getAST().newPrefixExpression();
                pe.setOperator(PrefixExpression.Operator.NOT);
				ParenthesizedExpression paren = e.getAST().newParenthesizedExpression();
				paren.setExpression(e);
				pe.setOperand(paren);
				return pe;
			}
			ie.setOperator(op);
			return e;
		}
		if (e instanceof PrefixExpression)
			return (Expression) ASTNode.copySubtree(e.getAST(), ((PrefixExpression) e).getOperand());
		PrefixExpression pe = e.getAST().newPrefixExpression();
		pe.setOperator(PrefixExpression.Operator.NOT);
		if (e instanceof MethodInvocation)
			pe.setOperand(e);
		else if (e instanceof ParenthesizedExpression)
			return negate(((ParenthesizedExpression) e).getExpression());
		else {
			ParenthesizedExpression paren = e.getAST().newParenthesizedExpression();
			paren.setExpression(e);
			pe.setOperand(paren);
		}
		return pe;
	}

	private static void negate(List<?> list, ASTNode node) {
		for (int i = 0; i < list.size(); i++) {
	        StructuralPropertyDescriptor curr = (StructuralPropertyDescriptor) list.get(i);
	        Object child = node.getStructuralProperty(curr);
	        if (child instanceof Expression)
	        	node.setStructuralProperty(curr, negate((Expression) child));
	        else if (child instanceof List) {
	        	List<Object> sub = (List<Object>) child;
	        	for (int j = 0; j < sub.size(); j++) {
	        		Object obj = sub.get(j);
	        		if (obj instanceof Expression)
	        			sub.set(j, (Object) negate((Expression) obj));
	        	}
//	        	node.setStructuralProperty(curr, sub);
	        }
	    }
	}

	public static Expression normalize(Expression e) {
		if (e instanceof PrefixExpression) {
			PrefixExpression pe = (PrefixExpression) e;
			if (pe.getOperator() == PrefixExpression.Operator.NOT)
				return negate(pe.getOperand());
		}
		return e;
	}

    public static boolean isInTrueBranch(ASTNode node, IfStatement p) {
        ASTNode temp = node;
        while (temp.getParent() != p)
            temp = temp.getParent();
        return temp == p.getThenStatement();
    }

    public static void replace(ASTNode oldNode, ASTNode newNode) {
        ASTNode parent = oldNode.getParent();
        StructuralPropertyDescriptor location = oldNode.getLocationInParent();
        if (location.isChildProperty()) {
            parent.setStructuralProperty(location, newNode);
        }
        if (location.isChildListProperty()) {
            List<ASTNode> list = ((List<ASTNode>) parent.getStructuralProperty(location));
            int index = list.indexOf(oldNode);
            list.set(index, newNode);
        }
    }

    public static String stripOuterParenthesis(String condition) {
        Expression e = parseExpression(condition);
        if (e == null)
            return condition;
        while (e instanceof ParenthesizedExpression)
            e = ((ParenthesizedExpression) e).getExpression();
        return e.toString();
    }

    public static String optimize(String condition) {
        // DEBUG
        System.out.println("Optimizing " + condition);
        condition = removeDuplicate(condition);
        System.out.println("Optimized " + condition);
        return condition;
    }

    private static String removeDuplicate(String condition) {
        Expression e = parseExpression(condition);
        if (e == null)
            return condition;
        List<Expression> operands = new ArrayList<>();
        getOrOperands(e, operands);
        for (int i = 0; i < operands.size(); i++) {
            operands.set(i, simplify(operands.get(i)));
            if (operands.get(i).toString().equals("true"))
                return "true";
        }
        
        // remove duplicate clauses
        int i = operands.size() - 1;
        while (i >= 0) {
            Expression operand = operands.get(i);
            if (operand.toString().equals("unresolvable")) {
                operands.remove(i);
            } else if (Utils.satcheck(operand) == Status.UNSATISFIABLE) {
                operands.remove(i);
            } else {
                for (int j = 0; j < i; j++) {
                    if (imply(operand, operands.get(j))) {
                        operands.remove(i);
                        break;
                    }
                    if (imply(operands.get(j), operand)) {
                        operands.remove(j);
                        j--;
                        i--;
                    }
                }
            }
            i--;
        }
        
        // TODO remove duplicate operands
        
        if (operands.isEmpty())
            return "false";
        condition = operands.get(0).toString();
        for (int j = 1; j < operands.size(); j++)
            condition += " || " + operands.get(j).toString();
        return condition;
    }
    
    private static Expression simplify(Expression e) {
        Expression[] result = new Expression[1];
        result[0] = e;
        e.accept(new ASTVisitor() {
            
            @Override
            public boolean preVisit2(ASTNode node) {
                if (node instanceof ArrayInitializer)
                    return super.preVisit2(node); // JExl does not handle array initializer well
                Object value = Utils.evaluate(node.toString());
                Expression newNode = null;
                if (value != null) {
                    if (value instanceof Number)
                        newNode = node.getAST().newNumberLiteral(value.toString());
                    else if (value instanceof String) {
                        newNode = node.getAST().newStringLiteral();
                        ((StringLiteral) newNode).setLiteralValue(value.toString());
                    } else if (value instanceof Boolean)
                        newNode = node.getAST().newBooleanLiteral(((Boolean) value).booleanValue());
                    else if (value instanceof Character) {
                        newNode = node.getAST().newCharacterLiteral();
                        ((CharacterLiteral) newNode).setCharValue(((Character) value).charValue());
                    }
                }
                if (newNode != null) {
                    replace(node, newNode);
                    if (node == e)
                        result[0] = newNode;
                    return false;
                }
                return super.preVisit2(node);
            }
            
            @Override
            public void endVisit(InfixExpression node) {
                InfixExpression.Operator op = node.getOperator();
                if (op == InfixExpression.Operator.DIVIDE) {
                    Expression lhs = node.getLeftOperand();
                    List<Expression> operands = new ArrayList<>();
                    getMultiplicationOperands(lhs, operands);
                    String rhs = node.getRightOperand().toString();
                    for (int i = 0; i < operands.size(); i++) {
                        if (operands.get(i).toString().equals(rhs)) {
                            operands.remove(i);
                            Expression newNode = null;
                            if (operands.size() == 0)
                                newNode = node.getAST().newNumberLiteral("1");
                            else if (operands.size() == 1) {
                                newNode = (Expression) ASTNode.copySubtree(node.getAST(), operands.get(0));
                            } else {
                                newNode = node.getAST().newInfixExpression();
                                ((InfixExpression) newNode).setOperator(InfixExpression.Operator.TIMES);
                                ((InfixExpression) newNode).setLeftOperand((Expression) ASTNode.copySubtree(newNode.getAST(), operands.get(0)));
                                ((InfixExpression) newNode).setRightOperand((Expression) ASTNode.copySubtree(newNode.getAST(), operands.get(1)));
                                if (operands.size() > 2) {
                                    for (int j = 2; j < operands.size(); j++) {
                                        ((InfixExpression) newNode).extendedOperands().add((Expression) ASTNode.copySubtree(newNode.getAST(), operands.get(j)));
                                    }
                                }
                            }
                            replace(node, newNode);
                            if (node == e)
                                result[0] = newNode;
                            break;
                        }
                    }
                }
            }
            
            @Override
            public void endVisit(MethodInvocation node) {
                if (node.getName().getIdentifier().equals("isNaN") 
                        && node.arguments().size() == 1 
                        && node.arguments().get(0) instanceof NumberLiteral) {
                    BooleanLiteral newNode = node.getAST().newBooleanLiteral(false);
                    replace(node, newNode);
                    if (node == e)
                        result[0] = newNode;
                }
            }
        });
        return result[0];
    }

    protected static void getMultiplicationOperands(Expression e, List<Expression> operands) {
        if (e instanceof ParenthesizedExpression)
            getMultiplicationOperands(((ParenthesizedExpression) e).getExpression(), operands);
        else if (e instanceof InfixExpression) {
            InfixExpression ie = (InfixExpression) e;
            if (ie.getOperator() == InfixExpression.Operator.TIMES) {
                getMultiplicationOperands(ie.getLeftOperand(), operands);
                getMultiplicationOperands(ie.getRightOperand(), operands);
                if (ie.hasExtendedOperands()) {
                    for (int i = 0; i < ie.extendedOperands().size(); i++) {
                        Expression operand = (Expression) ie.extendedOperands().get(i);
                        getMultiplicationOperands(operand, operands);
                    }
                }
            } else
                operands.add(e);
        } else
            operands.add(e);
    }

    public static boolean equivalent(String e1, String e2) {
        Expression e3 = parseExpression(e1), e4 = parseExpression(e2);
        return e3 != null && e4 != null && equivalent(e3, e4); 
    }

    public static boolean equivalent(Expression e1, Expression e2) {
        if (Utils.satcheck("(" + e1.toString() + ")" + " && !(" + e2.toString() + ")") == Status.UNSATISFIABLE
                && Utils.satcheck("!(" + e1.toString() + ") && " + "(" + e2.toString() + ")") == Status.UNSATISFIABLE)
            return true;
        return false;
    }

    public static boolean imply(Expression e1, Expression e2) {
        if (Utils.satcheck("(" + e1.toString() + ")" + " && !(" + e2.toString() + ")") == Status.UNSATISFIABLE)
            return true;
        return false;
    }

    public static void getOrOperands(Expression e, List<Expression> operands) {
        if (e instanceof ParenthesizedExpression)
            getOrOperands(((ParenthesizedExpression) e).getExpression(), operands);
        else if (e instanceof InfixExpression) {
            InfixExpression ie = (InfixExpression) e;
            if (ie.getOperator() == InfixExpression.Operator.CONDITIONAL_OR) {
                getOrOperands(ie.getLeftOperand(), operands);
                getOrOperands(ie.getRightOperand(), operands);
                if (ie.hasExtendedOperands()) {
                    for (int i = 0; i < ie.extendedOperands().size(); i++) {
                        Expression operand = (Expression) ie.extendedOperands().get(i);
                        getOrOperands(operand, operands);
                    }
                }
            } else if (!contains(operands, e))
                operands.add(e);
        } else if (!contains(operands, e))
            operands.add(e);
    }

    public static void getOrOperands(String source, List<Expression> operands) {
        String[] parts = source.split(" \\|\\| ");
        for (String part : parts) {
            Expression e = parseExpression(part);
            if (e != null)
                operands.add(e);
        }
    }

    private static boolean contains(List<Expression> operands, Expression e) {
        String s = e.toString();
        for (Expression operand : operands)
            if (operand.toString().equals(s))
                return true;
        return false;
    }

    public static boolean isBool(InfixExpression.Operator op) {
        return op == InfixExpression.Operator.CONDITIONAL_AND 
                || op == InfixExpression.Operator.CONDITIONAL_OR
                || op == InfixExpression.Operator.EQUALS
                || op == InfixExpression.Operator.GREATER
                || op == InfixExpression.Operator.GREATER_EQUALS
                || op == InfixExpression.Operator.LESS
                || op == InfixExpression.Operator.LESS_EQUALS
                || op == InfixExpression.Operator.NOT_EQUALS
                || op == InfixExpression.Operator.XOR;
    }

    public static Name createName(AST ast, String sname) {
        String[] parts = sname.split("\\.");
        LinkedList<String> names = new LinkedList<>();
        for (String part : parts)
            names.add(part);
        return createName(ast, names);
    }

    private static Name createName(AST ast, LinkedList<String> names) {
        SimpleName name = ast.newSimpleName(names.removeLast());
        if (names.isEmpty())
            return name;
        return ast.newQualifiedName(createName(ast, names), name);
    }

    public static String removeNonPredicateMethodCalls(String condition) {
        Expression e = parseExpression(condition);
        if (e == null)
            return "false";
        List<Expression> operands = new ArrayList<>();
        getOrOperands(e, operands);
        int i = 0;
        while (i < operands.size()) {
            Expression operand = operands.get(i);
            if (hasNonPredicateMethodCall(operand))
                operands.remove(i);
            else
                i++;
        }
        
        if (operands.isEmpty())
            return "false";
        condition = operands.get(0).toString();
        for (int j = 1; j < operands.size(); j++)
            condition += " || " + operands.get(j).toString();
        return condition;
    }

    private static boolean hasNonPredicateMethodCall(Expression ex) {
        final int maxDepth = 1, maxArguments = 1;
        boolean[] result = new boolean[]{false};
        ex.accept(new ASTVisitor() {
            private int depth = 0;
            
            @Override
            public boolean preVisit2(ASTNode node) {
                if (result[0])
                    return false;
                return super.preVisit2(node);
            }
            
            @Override
            public boolean visit(ClassInstanceCreation node) {
                if (node.arguments().size() > maxArguments) {
                    result[0] = true;
                    return false;
                }
                depth++;
                if (depth > maxDepth) {
                    result[0] = true;
                    return false;
                }
                if (node.arguments().size() > 0 && JavaASTUtil.areLiterals(node.arguments())) {
                    result[0] = true;
                    return false;
                }
                return true;
            }
            
            @Override
            public void endVisit(ClassInstanceCreation node) {
                depth--;
            }
            
            @Override
            public boolean visit(ConstructorInvocation node) {
                if (node.arguments().size() > maxArguments) {
                    result[0] = true;
                    return false;
                }
                depth++;
                if (depth > maxDepth) {
                    result[0] = true;
                    return false;
                }
                if (node.arguments().size() > 0 && JavaASTUtil.areLiterals(node.arguments())) {
                    result[0] = true;
                    return false;
                }
                return true;
            }
            
            @Override
            public void endVisit(ConstructorInvocation node) {
                depth--;
            }
            
            @Override
            public boolean visit(MethodInvocation node) {
                if (node.arguments().size() > maxArguments) {
                    result[0] = true;
                    return false;
                }
                depth++;
                if (depth > maxDepth) {
                    result[0] = true;
                    return false;
                }
                if (node.arguments().size() > 0 && JavaASTUtil.areLiterals(node.arguments())) {
                    result[0] = true;
                    return false;
                }
                return true;
            }
            
            @Override
            public void endVisit(MethodInvocation node) {
                depth--;
            }
            
            @Override
            public boolean visit(SuperConstructorInvocation node) {
                if (node.arguments().size() > maxArguments) {
                    result[0] = true;
                    return false;
                }
                depth++;
                if (depth > maxDepth) {
                    result[0] = true;
                    return false;
                }
                if (node.arguments().size() > 0 && JavaASTUtil.areLiterals(node.arguments())) {
                    result[0] = true;
                    return false;
                }
                return true;
            }
            
            @Override
            public void endVisit(SuperConstructorInvocation node) {
                depth--;
            }
            
            @Override
            public boolean visit(SuperMethodInvocation node) {
                if (node.arguments().size() > maxArguments) {
                    result[0] = true;
                    return false;
                }
                depth++;
                if (depth > maxDepth) {
                    result[0] = true;
                    return false;
                }
                if (node.arguments().size() > 0 && JavaASTUtil.areLiterals(node.arguments())) {
                    result[0] = true;
                    return false;
                }
                return true;
            }
            
            @Override
            public void endVisit(SuperMethodInvocation node) {
                depth--;
            }
        });
        return result[0];
    }

    public static boolean isComparison(String source) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_EXPRESSION);
        parser.setSource(source.toCharArray());
        ASTNode ast = parser.createAST(null);
        if (!isExpression(ast, source) || !(ast instanceof InfixExpression))
            return false;
        InfixExpression.Operator op = ((InfixExpression) ast).getOperator();
        return (op == InfixExpression.Operator.EQUALS
                || op == InfixExpression.Operator.GREATER 
                || op == InfixExpression.Operator.GREATER_EQUALS
                || op == InfixExpression.Operator.LESS
                || op == InfixExpression.Operator.LESS_EQUALS
                || op == InfixExpression.Operator.NOT_EQUALS);
    }

    public static boolean isLogicalInfix(Expression e) {
        if (e instanceof InfixExpression) {
            InfixExpression.Operator op = ((InfixExpression) e).getOperator();
            return op == InfixExpression.Operator.CONDITIONAL_AND || op == InfixExpression.Operator.CONDITIONAL_OR;
        }
        return false;
    }

    public static String getPackageName(MethodDeclaration method) {
        CompilationUnit cu = (CompilationUnit) method.getRoot();
        if (cu.getPackage() != null) {
            return cu.getPackage().getName().getFullyQualifiedName();
        }
        return "";
    }

    public static String getPackageName(IMethodBinding mb) {
        ITypeBinding tb = mb.getDeclaringClass();
        if (tb != null) {
            IPackageBinding pb = tb.getPackage();
            if (pb != null) {
                return pb.getName();
            }
        }
        return "";
    }

    public static String[] getSourcePaths(String path, String[] extensions, boolean recursive) {
        HashSet<String> exts = new HashSet<>();
        for (String e : extensions)
            exts.add(e);
        HashSet<String> paths = new HashSet<>();
        getSourcePaths(new File(path), paths, exts, recursive);
        return (String[]) paths.toArray(new String[0]);
    }

    private static void getSourcePaths(File file, HashSet<String> paths, HashSet<String> exts, boolean recursive) {
        if (file.isDirectory()) {
            if (paths.isEmpty() || recursive)
                for (File sub : file.listFiles())
                    getSourcePaths(sub, paths, exts, recursive);
        } else if (exts.contains(getExtension(file.getName())))
            paths.add(file.getAbsolutePath());
    }

    private static Object getExtension(String name) {
        int index = name.lastIndexOf('.');
        if (index < 0)
            index = 0;
        return name.substring(index);
    }
}
