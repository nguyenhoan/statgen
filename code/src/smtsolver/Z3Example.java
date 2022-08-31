package smtsolver;


import smtsolver.JavaExample.TestFailedException;

import com.microsoft.z3.ArithExpr;
import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Params;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

public class Z3Example {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		checkIndexLessThanZero();
	}

	/*
	 * index < 0 class org.eclipse.jdt.core.dom.InfixExpression
	 */
	public static void createIndexLessThanZero() {
		Context ctx = new Context();
		/* do something with the context */
		BoolExpr x1 = ctx.mkLt(ctx.mkIntConst(ctx.mkSymbol("index")),
				ctx.mkInt(1));
		// BoolExpr x2 =
		// ctx.mkGt(ctx.mkIntConst(ctx.mkSymbol("index")),ctx.mkInt(1));

		/* be kind to dispose manually and not wait for the GC. */
		ctx.close();
	}

	public static void checkIndexLessThanZero() {
		Context ctx = new Context();
		BoolExpr x1 = ctx.mkLt(ctx.mkIntConst(ctx.mkSymbol("index")),
				ctx.mkInt(1));
		BoolExpr x2 = ctx.mkLt(ctx.mkIntConst(ctx.mkSymbol("index")),
				ctx.mkInt(1));
		BoolExpr result = createSATForCheckFormula(ctx, x1, x2);
		// check SAT
		Solver s = ctx.mkSolver();
		s.add(result);
		Status q = s.check();
		switch (q) {
		case UNKNOWN:
			System.out.println("Unknown because: " + s.getReasonUnknown());
			break;
		case SATISFIABLE:
			System.out.println("SAT");
			break;
		case UNSATISFIABLE:
			System.out.println("UNSAT");
			//throw new Exception("This ");
		}
		/* be kind to dispose manually and not wait for the GC. */
		ctx.close();
	}

	public static BoolExpr createSATForCheckFormula(Context ctx, BoolExpr a,
			BoolExpr b) {
		BoolExpr m1 = ctx.mkAnd(a, ctx.mkNot(b));
		BoolExpr m2 = ctx.mkAnd(b, ctx.mkNot(a));
		BoolExpr result = ctx.mkOr(m1, m2);
		return result;
	}
}
