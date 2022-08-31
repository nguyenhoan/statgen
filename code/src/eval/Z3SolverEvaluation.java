package eval;

import smtsolver.ExpressionExtractorParser;

public class Z3SolverEvaluation {

	public static void main(String[] args) {
		String fop = "data\\";
		ExpressionExtractorParser eep=new ExpressionExtractorParser();
		eep.checkConsistencyMethodLevel(fop+"test.signatures.txt", fop+"test.jd", fop+"test.impl", fop+"trans.impl", fop+"z3.txt", fop+"oracle_methodLevel_3cols.txt",fop+"final_z3.txt");
	}
	

}
