package smtsolver;

import java.nio.channels.AcceptPendingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

import utils.JavaASTUtil;

public class ExpressionExtractorVisitor extends ASTVisitor {

	private String path;
	private String[] source;
	MethodDeclaration method;
	ArrayList<Object> exprMethod;
	HashMap<String, String> thrownExceptions = new HashMap<>();
	HashMap<String, String> setTypeExpr;
	boolean isVisitThrow = false;
	private String condition;
	private String fullCondition;
	private Expression currentExpr;

	// public ExpressionExtractorVisitor(MethodDeclaration method,
	// HashMap<String, String> setTypeExpr) {
	// this.method = method;
	// this.setTypeExpr = setTypeExpr;
	// exprMethod = new ArrayList<Object>();
	// condition="";
	// }

	public ExpressionExtractorVisitor(MethodDeclaration method,
			HashMap<String, String> setTypeExpr) {
		this.method = method;
		this.setTypeExpr = setTypeExpr;
		exprMethod = new ArrayList<Object>();
		condition = "";
		currentExpr = null;
	}

	public Expression getCurrentExpr() {
		return currentExpr;
	}

	public void setCurrentExpr(Expression currentExpr) {
		this.currentExpr = currentExpr;
	}

	public String getFullCondition() {
		return fullCondition;
	}

	public void setFullCondition(String fullCondition) {
		this.fullCondition = fullCondition;
	}

	public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public ArrayList<Object> getExprMethod() {
		return exprMethod;
	}

	public void setExprMethod(ArrayList<Object> exprMethod) {
		this.exprMethod = exprMethod;
	}

	public boolean visit(Expression node) {
		System.out.println(node + " aaaa");
		return true;
	}

	public boolean visit(Annotation node) {
		return false;
	}

	@Override
	public boolean visit(ArrayAccess node) {
		return false;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		return false;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		return false;
	}

	@Override
	public boolean visit(Assignment node) {
		System.out.println("this is assignment");
		return false;
	}

	@Override
	public boolean visit(BooleanLiteral node) {

		return false;
	}

	@Override
	public boolean visit(CastExpression node) {
		return false;
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		return false;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		return false;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		return false;
	}

	@Override
	public boolean visit(CreationReference node) {
		return false;
	}

	@Override
	public boolean visit(ExpressionMethodReference node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}

		return false;
	}

	@Override
	public boolean visit(FieldAccess node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		// System.out.println(node.toString());
		return false;
	}

	@Override
	public boolean visit(InfixExpression node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}

		return false;
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}

		return false;
	}

	@Override
	public boolean visit(LambdaExpression node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		// System.out.println(node.toString() + " hello");
		if (!node.toString().startsWith("Call.templateFunction")) {
			return false;
		}
		if (node.arguments() != null && node.arguments().size() > 0) {
			currentExpr = (Expression) node.arguments().get(0);
			// System.out.println(currentExpr.getClass().toString());

		}

		return false;
	}

	public boolean visit(MethodReference node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	public boolean visit(Name node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(NullLiteral node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(NumberLiteral node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(PostfixExpression node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(PrefixExpression node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(StringLiteral node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(SuperMethodReference node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(ThisExpression node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(TypeMethodReference node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		if (isVisitThrow) {
			setTypeExpr.put(node.getClass().getSimpleName(), method.toString());
		}
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		// super(node);
		return true;
	}

	// public ThrownExceptionVisitor(MethodDeclaration method) {
	// this.method = method;
	// }

	// @Override
	// public boolean visit(ThrowStatement node) {
	//
	// if (node.getExpression() instanceof ClassInstanceCreation) {
	// condition = null;
	// ASTNode p = node.getParent();
	// if (p instanceof IfStatement) {
	// condition = ((IfStatement) p).getExpression().toString();
	// isVisitThrow = true;
	// (((IfStatement) p).getExpression()).accept(this);
	// isVisitThrow = false;
	// exprMethod.add(((IfStatement) p).getExpression());
	// } else if (p instanceof Block) {
	// if (p.getParent() == method
	// && ((Block) p).statements().size() == 1) {
	// condition = "true";
	// exprMethod.add("true");
	// }
	//
	// if (p.getParent() instanceof CatchClause) {
	// return false;
	// }
	//
	// if (p.getParent() instanceof IfStatement) {
	// condition = ((IfStatement) p.getParent()).getExpression()
	// .toString();
	// exprMethod.add(((IfStatement) p.getParent())
	// .getExpression());
	// isVisitThrow = true;
	// (((IfStatement) p.getParent()).getExpression())
	// .accept(this);
	// isVisitThrow = false;
	// } else {
	// List<?> l = ((Block) p).statements();
	// int index = l.indexOf(node) - 1;
	// while (index >= 0) {
	// if (l.get(index) instanceof IfStatement
	// && isTransferControl((IfStatement) l.get(index))) {
	// if (condition == null) {
	// condition = "";
	// } else {
	// exprMethod.add("&&");
	// condition += " && ";
	// exprMethod.add("!");
	// exprMethod.add("(");
	// exprMethod.add(((IfStatement) (l.get(index)))
	// .getExpression());
	// exprMethod.add(")");
	// isVisitThrow = true;
	// (((IfStatement) (l.get(index))).getExpression())
	// .accept(this);
	// isVisitThrow = false;
	// condition += "!("
	// + ((IfStatement) (l.get(index)))
	// .getExpression().toString()
	// + ")";
	// }
	// }
	// index--;
	// }
	// }
	// } else if (p instanceof SwitchStatement) {
	// SwitchStatement ss = (SwitchStatement) p;
	// List<?> l = ss.statements();
	// int index = l.indexOf(node) - 1;
	// while (index > 0) {
	// if (l.get(index) instanceof SwitchCase) {
	// SwitchCase sc = (SwitchCase) l.get(index);
	// if (sc.isDefault()) {
	// for (int i = 0; i < ss.statements().size(); i++) {
	// if (ss.statements().get(i) instanceof SwitchCase) {
	// sc = (SwitchCase) ss.statements().get(i);
	// if (sc.isDefault())
	// break;
	// if (condition == null) {
	// condition = ss.getExpression()
	// .toString()
	// + " != "
	// + sc.getExpression().toString();
	// exprMethod.add(ss.getExpression());
	// exprMethod.add("!=");
	// exprMethod.add(sc.getExpression());
	// isVisitThrow = true;
	// ss.getExpression().accept(this);
	// sc.getExpression().accept(this);
	// isVisitThrow = false;
	// }
	//
	// else {
	// condition += " && "
	// + ss.getExpression().toString()
	// + " != "
	// + sc.getExpression().toString();
	// exprMethod.add("&&");
	// exprMethod.add(ss.getExpression());
	// exprMethod.add("!=");
	// exprMethod.add(sc.getExpression());
	// isVisitThrow = true;
	// ss.getExpression().accept(this);
	// sc.getExpression().accept(this);
	// isVisitThrow = false;
	// }
	// }
	// }
	// } else {
	// condition = ss.getExpression().toString() + " == "
	// + sc.getExpression().toString();
	// exprMethod.add(ss.getExpression());
	// exprMethod.add("==");
	// exprMethod.add(sc.getExpression());
	// isVisitThrow = true;
	// ss.getExpression().accept(this);
	// sc.getExpression().accept(this);
	// isVisitThrow = false;
	// }
	// break;
	// }
	// index--;
	// }
	// }
	// String type = JavaASTUtil
	// .getSimpleType(((ClassInstanceCreation) node
	// .getExpression()).getType());
	// if (condition == null)
	// condition = "true";
	// //condition = JavaASTUtil.tokenize(condition);
	// fullCondition = thrownExceptions.get(type);
	// if (fullCondition == null)
	// fullCondition = condition;
	// else
	// fullCondition += " || " + condition;
	// // thrownExceptions.put(type, fullCondition);
	// }
	// isVisitThrow = false;
	// return false;
	// }

	private boolean isTransferControl(IfStatement is) {
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
		if (s instanceof ExpressionStatement) {
			ExpressionStatement es = (ExpressionStatement) s;
			if (es.getExpression() instanceof MethodInvocation) {
				MethodInvocation m = (MethodInvocation) es.getExpression();
				if (m.getName().getIdentifier().equals("exit")
						&& m.arguments().size() == 1
						&& m.getExpression() != null
						&& m.getExpression().toString().equals("System"))
					return true;
			}
		}
		return false;
	}

	// visit only the expression inside if condition
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
