package clausetree;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.*;
import edu.stanford.nlp.util.CoreMap;
import parsing.DocumentationParser;
import parsing.NLPSentenceParser;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import clausetree.ClauseTreeNode;

public class TestApache {
    
    @BeforeClass
    public static void oneTimeSetUp() {
    	Utils.encloseClauseWithParentheses = true;
    }
    
	@Test
	public void test1() throws Exception {
		String text = "   if <code>array</code> is <code>null</code>";
		ArrayList<String> paraNames = new ArrayList<>();
		String clause = transform(text, paraNames);
		Assert.assertEquals("( array be null ).", clause);
	}

	private String transform(String text, ArrayList<String> paraNames) {
		HashMap<String, String> codeStr = new HashMap<>(), strCode = new HashMap<>();
		text = DocumentationParser.normalize(text, codeStr, strCode, paraNames);
		StringBuilder sb = new StringBuilder();
		List<CoreMap> sentences = NLPSentenceParser.parse(text);
		for (CoreMap sentence : sentences) {
			Tree tree = sentence.get(TreeAnnotation.class);
			ClauseTreeNode clauseTree = NLPSentenceParser.buildClauseTree(tree, new Stack<>(), new Stack<>(), new HashSet<>(), strCode);
			String flattenedSequence = clauseTree.flatten();
			if (!flattenedSequence.equals("null")) {
				System.out.println(sentence);
				System.out.println(tree);
				NLPSentenceParser.print(tree);
				clauseTree.print();
				System.out.println(flattenedSequence);
				sb.append(flattenedSequence + ". ");
			}
		}
		return sb.toString().trim();
	}
}
