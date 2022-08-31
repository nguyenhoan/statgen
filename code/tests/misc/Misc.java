package misc;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jdt.core.dom.ASTNode;
import parsing.DocumentationParser;
import utils.JavaASTUtil;

public class Misc {

	public static void main(String[] args) {
		ASTNode e = JavaASTUtil.parseExpression("! isValidCodePoint ( ( codePoint && && a ) ");
		System.out.println(e);
		
		String text = "a b  c \t \n d";
		String[] parts = text.split("[\\s]+");
		System.out.println(Arrays.asList(parts));
		
		ArrayList<String> paraNames = new ArrayList<>();
		paraNames.add("n");
		System.out.println(DocumentationParser.isParameter("\"n\"", paraNames ));
	}

}
