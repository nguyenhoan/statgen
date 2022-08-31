package eval;

import translation.Translator;

public class TranslationEvaluation {

	public static void main(String[] args) {
		String fop = "translate\\";
		//translated file
		Translator.translate(fop+"origin.jd", fop, fop+"trans.impl");
		
	}

}
