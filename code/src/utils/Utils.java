package utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import smtsolver.ExpressionExtractorParser;

public class Utils {
	public static boolean encloseClauseWithParentheses = true;
	public static String EXEC_DOT = "D:/Program Files (x86)/Graphviz2.36/bin/dot.exe"; // Windows
	private static Set<String> NON_NEGATIVE_VALUE_NAMES = new HashSet<>();
	
	static {
	    NON_NEGATIVE_VALUE_NAMES.add("size");
        NON_NEGATIVE_VALUE_NAMES.add("length");
        NON_NEGATIVE_VALUE_NAMES.add("width");
        NON_NEGATIVE_VALUE_NAMES.add("height");
        NON_NEGATIVE_VALUE_NAMES.add("depth");
	}
	
	public static void toGraphics(String file, String type) {
		Runtime rt = Runtime.getRuntime();

		String[] args = { EXEC_DOT, "-T" + type, file + ".dot", "-o",
				file + "." + type };
		try {
			Process p = rt.exec(args);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

    public static void addConditions(Map<String, String> exceptionConditions, Map<String, String> substitutedCalleeExceptionConditions) {
        for (String e : substitutedCalleeExceptionConditions.keySet()) {
            String condition = exceptionConditions.get(e);
            if (condition == null)
                condition = substitutedCalleeExceptionConditions.get(e);
            else if (!condition.equals("true")) {
                String subCondition = substitutedCalleeExceptionConditions.get(e);
                if (subCondition.equals("true"))
                    condition = subCondition;
                else
                    condition += " || " + subCondition;
            }
            exceptionConditions.put(e, condition);
        }
    }
    
    public static Status satcheck(String op, String... conditions) {
        StringBuilder sb = new StringBuilder();
        if (conditions.length > 0) {
            sb.append("(" + conditions[0] + ")");
            for (int i = 1; i < conditions.length; i++)
                sb.append(op + "(" + conditions[i] + ")");
        }
        return satcheck(sb.toString());
    }

    public static Status satcheck(String s) {
        Expression expr = JavaASTUtil.parseExpression(s);
        if (expr != null)
            return satcheck(expr);
        return Status.UNKNOWN;
    }

    private static boolean hasTooManyArithmeticOperators(Expression e) {
        int[] count = new int[]{0};
        e.accept(new ASTVisitor() {
            @Override
            public boolean visit(InfixExpression node) {
                if (node.getOperator() == InfixExpression.Operator.DIVIDE
                        || node.getOperator() == InfixExpression.Operator.TIMES)
                    count[0]++;
                return super.visit(node);
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
            public boolean visit(ClassInstanceCreation node) {
                return false;
            }
            
            @Override
            public boolean visit(MethodInvocation node) {
                return false;
            }
        });
        return count[0] > 10;
    }

    private static Set<String> getInvariants(Expression e) {
        final Set<String> invariants = new HashSet<String>();
        e.accept(new ASTVisitor() {
            @Override
            public boolean visit(ArrayCreation node) {
                invariants.add(node.toString() + " != null");
                return super.visit(node);
            }
            
            @Override
            public boolean visit(ClassInstanceCreation node) {
                invariants.add(node.toString() + " != null");
                return super.visit(node);
            }
            
            @Override
            public boolean visit(FieldAccess node) {
                invariants.add(node.getExpression().toString() + " != null");
                String name = node.getName().getIdentifier().toLowerCase();
                if (NON_NEGATIVE_VALUE_NAMES.contains(name))
                    invariants.add(node.toString() + " >= 0");
                if (node.getName().getIdentifier().equals(node.getName().getIdentifier().toUpperCase()))
                    invariants.add(node.toString() + " != null");
                return super.visit(node);
            }
            
            @Override
            public boolean visit(MethodInvocation node) {
                if (node.getExpression() != null)
                    invariants.add(node.getExpression().toString() + " != null");
                String name = node.getName().getIdentifier().toLowerCase();
                if (NON_NEGATIVE_VALUE_NAMES.contains(name))
                    invariants.add(node.toString() + " >= 0");
                return super.visit(node);
            }
            
            @Override
            public boolean visit(QualifiedName node) {
                invariants.add(node.getQualifier().toString() + " != null");
                String name = node.getName().getIdentifier().toLowerCase();
                if (NON_NEGATIVE_VALUE_NAMES.contains(name))
                    invariants.add(node.toString() + " >= 0");
                if (node.getName().getIdentifier().equals(node.getName().getIdentifier().toUpperCase()))
                    invariants.add(node.toString() + " != null");
                return false;
            }
            
            @Override
            public boolean visit(SimpleName node) {
                String name = node.getIdentifier().toLowerCase();
                if (NON_NEGATIVE_VALUE_NAMES.contains(name))
                    invariants.add(node.toString() + " >= 0");
//                if (node.getIdentifier().equals(node.getIdentifier().toUpperCase()))
//                    invariants.add(node.toString() + " != null");
                return false;
            }
            
            @Override
            public boolean visit(SuperFieldAccess node) {
                String name = node.getName().getIdentifier().toLowerCase();
                if (NON_NEGATIVE_VALUE_NAMES.contains(name))
                    invariants.add(node.toString() + " >= 0");
                if (node.getName().getIdentifier().equals(node.getName().getIdentifier().toUpperCase()))
                    invariants.add(node.toString() + " != null");
                return super.visit(node);
            }
            
            @Override
            public boolean visit(SuperMethodInvocation node) {
                String name = node.getName().getIdentifier().toLowerCase();
                if (NON_NEGATIVE_VALUE_NAMES.contains(name))
                    invariants.add(node.toString() + " >= 0");
                return super.visit(node);
            }
        });
        return invariants;
    }

    public static Status satcheck(Expression expr) {
        if (hasTooManyArithmeticOperators(expr))
            return Status.UNKNOWN;
        Set<String> invariants = getInvariants(expr);
        if (!invariants.isEmpty()) {
            String s = "(" + expr.toString() + ")";
            for (String i : invariants)
                s += " && (" + i + ")";
            expr = JavaASTUtil.parseExpression(s);
        }
        Status status = Status.UNKNOWN;
        ExpressionExtractorParser eep = new ExpressionExtractorParser();
        try (Context ctx = new Context()){
            Expr z3Expr = eep.handleExpression(ctx, expr, true);
            if (z3Expr instanceof BoolExpr) {
                Solver solver = ctx.mkSolver();
                BoolExpr z3BoolExpr = (BoolExpr) z3Expr;
                solver.add(z3BoolExpr);
                status = solver.check();
            }
        } catch (Exception e) { System.err.println(e.getMessage());}
        return status;
    }
    
    public static Object evaluate(String expression) {
        try {
            org.apache.commons.jexl2.JexlEngine jexl = new org.apache.commons.jexl2.JexlEngine();
            jexl.setDebug(false);
            jexl.setSilent(false);
            jexl.setLenient(false);
            // Create an expression
            org.apache.commons.jexl2.Expression jexlExp = jexl.createExpression(expression, null);
            // Now evaluate the expression, getting the result
            Object result = jexlExp.evaluate(null);
            return result;
        } catch (Throwable t) {}
        return null;
    }
    
}
