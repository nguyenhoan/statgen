package parsetree;

import java.util.List;
import java.util.Scanner;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import parsing.NLPSentenceParser;

public class TestParseTree {

	public static void main(String[] args) {
		String text = "an I/O error, such as A, occurs.";
		List<CoreMap> sentences = NLPSentenceParser.parse(text);
		for (CoreMap sentence : sentences) {
			Tree tree = sentence.get(TreeAnnotation.class);
			System.out.println(sentence);
			NLPSentenceParser.print(tree);
		}
		Scanner scan=new Scanner(System.in);
		while (true) {
			text = scan.nextLine();
			sentences = NLPSentenceParser.parse(text);
			for (CoreMap sentence : sentences) {
				System.out.println(sentence);
				Tree tree = sentence.get(TreeAnnotation.class);
				NLPSentenceParser.print(tree);
			}
			if (text.isEmpty())
				break;
		}
		scan.close();
	}

}
