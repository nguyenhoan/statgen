package clausetree;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.*;
import edu.stanford.nlp.util.CoreMap;
import parsing.DocumentationParser;
import parsing.NLPSentenceParser;
import utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import clausetree.ClauseTreeNode;

public class TestBuildClauseTree {
    @Rule
    public TestName name = new TestName();
    
    @BeforeClass
    public static void oneTimeSetUp() {
    	Utils.encloseClauseWithParentheses = true;
    }

    @Test
    public void test1() throws Exception {
    	String text = "limit < 0 , or   count < 1 . ";
		ArrayList<String> paraNames = new ArrayList<>();
		paraNames.add("limit");
		paraNames.add("count");
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( limit be less than 0 ) or ( count be less than 1 ).", clause);
    }

    @Test
    public void test2() throws Exception {
    	String text = "off or len is negative, or len is greater than b.length - off. ";
		System.out.println(text);
    	ArrayList<String> paraNames = new ArrayList<>();
		paraNames.add("off");
		paraNames.add("len");
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( off be negative ) or ( len be negative ) or ( len be greater than b.length - off ).", clause);
    }

    @Test
    public void test3() throws Exception {
    	String text = "code0 is negative,  code1  is negative, or  code1  is greater than  code2. ";
		ArrayList<String> paraNames = new ArrayList<>();
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( code0 be negative ) or ( code1 be negative ) or ( code1 be greater than code2 ).", clause);
    }

    @Test
    public void test4() throws Exception {
    	String text = "code is either 0 or 1. ";
		ArrayList<String> paraNames = new ArrayList<>();
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( code be 0 ) or ( code be 1 ).", clause);
    }

    @Test
    public void test5() throws Exception {
    	String text = "if <code>compressionType</code> is non-<code>null</code> but is not one of the values returned by <code>getCompressionTypes</code>.";
		ArrayList<String> paraNames = new ArrayList<>();
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( compressionType be non-null ) and ( compressionType be not one of the value return by getCompressionTypes ).", clause);
    }

    @Test
    public void test6() throws Exception {
    	String text = "<ul><li>Wraps an <link> IllegalArgumentException</link>  the MBeanInfo passed in parameter is null. <li>Wraps an <link> IllegalStateException</link>  the ModelMBean is currently registered in the MBeanServer.</li> </ul>. ";
		ArrayList<String> paraNames = new ArrayList<>();
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( wrap a IllegalArgumentException the MBeanInfo pass in parameter be null ) or ( wrap a IllegalStateException the ModelMBean be currently register in the MBeanServer ).", clause);
    }

    @Test
    public void test7() throws Exception {
    	String text = "if the JMXConnectorServer is not started (see <link>#isActive()</link>).";
		ArrayList<String> paraNames = new ArrayList<>();
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( the JMXConnectorServer be not start ).", clause);
    }

    @Test
    public void test8() throws Exception {
    	String text = "<code>canWriteEmpty(imageIndex)</code> returns <code>false</code>.";
		ArrayList<String> paraNames = new ArrayList<>();
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( canWriteEmpty ( imageIndex ) return false ).", clause);
    }

    @Test
    public void test9() throws Exception {
    	String text = "if a call to <code>prepareReiplacePixels</code> has been made without a matching call to <code>endReplacePixels</code>.";
		ArrayList<String> paraNames = new ArrayList<>();
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( a call to prepareReiplacePixels have be make without a match call to endReplacePixels ).", clause);
    }

    @Test
    public void test10() throws Exception {
    	String text = "if <code>number</code> is null or not an instance of <code>Number</code>.";
		ArrayList<String> paraNames = new ArrayList<>();
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( number be null ) or ( number be not a instance of Number ).", clause);
    }

    @Test
    public void test11() throws Exception {
    	String text = "if <code>x</code> is not a valid horizontal scrollbar policy, as listed above.";
		ArrayList<String> paraNames = new ArrayList<>();
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( x be not a valid horizontal scrollbar policy ).", clause);
    }

    @Test
    public void test12() throws Exception {
    	String text = "if <code>  limit < 0</code> , or <code>  count < 1</code> .";
		ArrayList<String> paraNames = new ArrayList<>();
		paraNames.add("limit");
		paraNames.add("count");
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( limit be less than 0 ) or ( count be less than 1 ).", clause);
    }

    @Ignore
    @Test
    public void test13() throws Exception {
    	String text = "  if a database access error occurs, this method is called on a closed <code>Statement</code> or the driver does not support batch statements. Throws <link> BatchUpdateException</link> (a subclass of <code>SQLException</code>) if one of the commands sent to the database fails to execute properly or attempts to return a result set.";
		ArrayList<String> paraNames = new ArrayList<>();
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("(  ).", clause);
    }
    
    @Test
    public void test14() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "(a is 1) and b.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("b.", clause);
    }
    
    @Test
    public void test15() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "(a is 1) b.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("b.", clause);
    }
    
    @Test
    public void test16() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "(a < 1) and (b > 0).";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( a < 1 ) and ( b > 0 ).", clause);
    }
    
    @Test
    public void test17() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "if this method is called on a closed <code>Statement</code> <p>.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( this method be call on a closed Statement ).", clause);
    }
    
    @Test
    public void test18() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "<code>src[i]-offset</code> is either less than zero or greater than or equal to the length of the lookup table for any band.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( src[i] - offset be less than zero ) or ( src[i] - offset be greater than or equal to the length of the lookup table for any band ).", clause);
    }
    
    @Test
    public void test19() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = " if <code>mc.precision</code> <literal>></literal> 0 and the result requires a precision of more than <code>mc.precision</code> digits.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( mc . precision be greater than 0 ) and ( the result require a precision of more than mc . precision digit ).", clause);
    }
    
    @Test
    public void test20() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "if the system property <b>java.rmi.server.codebase</b> contains an invalid URL.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( the system property java.rmi.server.codebase contain a invalid URL ).", clause);
    }
    
    @Test
    public void test21() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "always thrown.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("always.", clause);
    }
    
    @Test
    public void test22() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = " if any of the following conditions is met: <ul> <li> the given <code>loader</code> is <code>null</code> ;</li> <li> <code>intf</code> is null;</li> <li> any of the given proxy interfaces is non-public.</li> </ul>.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( any of the following condition be meet : the give loader be null intf be null any of the give proxy interface be non-public ).", clause);
    }
    
    @Test
    public void test23() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = " if a matching method is not found or if the name is \"&lt;init&gt;\" or \"&lt;clinit&gt;\".";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( a matching method be not find ) or ( the name be <init> ) or ( the name be <clinit> ).", clause);
    }
    
    @Test
    public void test24() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "if a matching method is not found or if <code>method(\"para\")</code> or \"&lt;clinit&gt;\" is invoked.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( a matching method be not find ) or ( method ( \"para\" ) be invoke ) or ( <clinit> be invoke ).", clause);
    }

    @Test
    public void test25() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "if <code>start</code> is negative, greater than <code>length()</code>, or greater than <code>end</code>.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( start be negative ) or ( start be greater than length() ) or ( start be greater than end ).", clause);
    }
    @Test
    public void test26() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "index is greater than or equals to length()!.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( index be greater than or equal to length() ).", clause);
    }
    
    @Test
    public void test27() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "NOT_SUPPORTED_ERR: Raised if this document does not support the \"XML\" feature.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( NOT_SUPPORTED_ERR : raise this document do not support the XML feature ).", clause);
    }

    @Test
    public void test28() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "if stackDepth &ge; 0 but stackFrame is null, or stackDepth < 0 but stackFrame is not null.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( stackDepth be greater than or equal to 0 ) and ( stackFrame be null ) or ( stackDepth be less than 0 ) and ( stackFrame be not null ).", clause);
    }

    @Test
    public void test29() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "if <code>dstOffset</code> is negative or greater than <code>this.length()</code>, or <code>start</code> or <code>end</code> are negative, or <code>start</code> is greater than <code>end</code> or <code>end</code> is greater than <code>s.length()</code>.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( dstOffset be negative ) or ( dstOffset be greater than length() ) or ( start be negative ) or ( end be negative ) or ( start be greater than end ) or ( end be greater than s . length() ).", clause);
    }
    
    @Test
    public void test30() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "if the index is out of range (<code>index < 0 || index >= size()</code>).";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("( the index be out of range ( index < 0 ) || ( index >= size() ) ).", clause);
    }

    @Ignore
    @Test
    public void test31() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = "any of parameter a, b or c is null.";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("(  ).", clause);
    }

    @Test
    public void test32() throws Exception {
        ArrayList<String> paraNames = new ArrayList<>();
        String text = "<code>uri</code> is <code>null</code> or <code>uri.length() == 0</code>.";
        String clause = transform(text, paraNames);
        Assert.assertEquals("( uri be null ) or ( uri . length() == 0 ).", clause);
    }
    
    @Ignore
    @Test
    public void test() throws Exception {
		ArrayList<String> paraNames = new ArrayList<>();
    	String text = ".";
    	String clause = transform(text, paraNames);
    	Assert.assertEquals("(  ).", clause);
    }

	public static void main(String[] args) throws IOException {
//		String text = "<code> <code>index</code> < 0 </code>. "
//				+ "'n' is negative. "
//				+ "if <code>number</code> is null or not an instance of <code>Number</code>.";
		String text = "if <code>x</code> is not a valid horizontal scrollbar policy, as listed above. ";
		HashMap<String, String> codeStr = new HashMap<>(), strCode = new HashMap<>();
		ArrayList<String> paraNames = new ArrayList<>();
		paraNames.add("limit");
		paraNames.add("count");
		paraNames.add("off");
		paraNames.add("len");
		paraNames.add("n");
		text = DocumentationParser.normalize(text, codeStr, strCode, paraNames );
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
			}
		}
		Scanner scan=new Scanner(System.in);
		while (true) {
			text = scan.nextLine();
			sentences = NLPSentenceParser.parse(text);
			for (CoreMap sentence : sentences) {
				System.out.println(sentence);
				Tree tree = sentence.get(TreeAnnotation.class);
				System.out.println(tree);
				NLPSentenceParser.print(tree);
				ClauseTreeNode clauseTree = NLPSentenceParser.buildClauseTree(tree, new Stack<>(), new Stack<>(), new HashSet<>(), strCode);
				clauseTree.print();
				String flattenedSequence = clauseTree.flatten();
				System.out.println(flattenedSequence);
			}
			if (text.isEmpty())
				break;
		}
		scan.close();
	}

	private String transform(String text, ArrayList<String> paraNames) {
		HashMap<String, String> codeStr = new HashMap<>(), strCode = new HashMap<>();
		text = DocumentationParser.normalize(text, codeStr, strCode, paraNames );
		System.out.println(text);
		StringBuilder sb = new StringBuilder();
		List<CoreMap> sentences = NLPSentenceParser.parse(text);
		for (CoreMap sentence : sentences) {
			Tree tree = sentence.get(TreeAnnotation.class);
			System.out.println(sentence);
			System.out.println(tree);
			NLPSentenceParser.print(tree);
			ClauseTreeNode clauseTree = NLPSentenceParser.buildClauseTree(tree, new Stack<>(), new Stack<>(), new HashSet<>(), strCode);
			String flattenedSequence = clauseTree.flatten();
			if (!flattenedSequence.equals("null")) {
				clauseTree.print();
				System.out.println(flattenedSequence);
				sb.append(flattenedSequence + ". ");
			}
		}
		return sb.toString().trim();
	}

}