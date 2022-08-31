package translation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.Expression;

import utils.FileIO;
import utils.JavaASTUtil;

public class Translator {
    public static final String DUMMY_VARIABLE = "___dummy___";
	public static final HashSet<String> separators = new HashSet<>();

	static {
		separators.add("and");
		separators.add("or");
		separators.add("&&");
		separators.add("||");
	}

	public static void splitClauses(String input, String intermediate) {
		String content = FileIO.readStringFromFile(input);
		StringBuilder clauses = new StringBuilder(), connectors = new StringBuilder();
		int count = 0;
		Scanner sc = new Scanner(content);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			String[] tokens = split(line, separators);
			for (String token : tokens) {
				if (separators.contains(token)) {
					connectors.append(token + " ");
				} else {
					clauses.append(token + "\n");
					count++;
					connectors.append(count + " ");
				}
			}
			connectors.append("\n");
		}
		sc.close();
		FileIO.writeStringToFile(clauses.toString(), intermediate + "/clauses.txt");
		FileIO.writeStringToFile(connectors.toString(), intermediate + "/connectors.txt");
	}
	
	public static void construct(String intermediate, String output) {
        File fClauses = new File(intermediate + "/clauses.txt");
        while (true) {
            File fConditions = new File(intermediate + "/conditions.txt");
            if (fConditions.exists() && fConditions.lastModified() > fClauses.lastModified())
                break;
            try {
                System.out.println("Waiting for translating clauses.txt to conditions.txt ...");
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        
		HashMap<String, String> conditions = new HashMap<>();
		String content = FileIO.readStringFromFile(intermediate + "/conditions.txt");
		Scanner sc = new Scanner(content);
		int i = 0;
		while (sc.hasNextLine()) {
			i++;
			conditions.put(i + "", sc.nextLine().trim());
		}
		sc.close();

		StringBuilder sb = new StringBuilder();
		content = FileIO.readStringFromFile(intermediate + "/connectors.txt");
		sc = new Scanner(content);
		while (sc.hasNextLine()) {
		    StringBuilder subsb = new StringBuilder();
			String line = sc.nextLine();
			String[] tokens = line.split(" ");
			String condition = conditions.get(tokens[0].trim());
			condition = condition.replace("( == ", "( " + DUMMY_VARIABLE + " == ");
            subsb.append(condition);
			for (int j = 1; j < tokens.length; j += 2) {
			    condition = conditions.get(tokens[j+1].trim());
			    String token = tokens[j].trim();
                if (token.equals("or"))
					subsb.append(" ||");
				else if (token.equals("and"))
					subsb.append(" &&");
				else if (token.equals("||"))
					subsb.append(" or");
				else if (token.equals("&&"))
					subsb.append(" and");
				else
					subsb.append(" " + token);
                condition = condition.replace("( == ", "( " + DUMMY_VARIABLE + " == ");
				subsb.append(" " + condition);
			}
			line = subsb.toString();
			int index = line.indexOf(" ");
			String exception = line.substring(0, index);
			line = line.substring(index + 1);
			
			LinkedList<String> parts = new LinkedList<>();
			Pattern p = Pattern.compile("\\|\\||&&");
			Matcher m = p.matcher(line);
			int start = 0;
	        while (m.find()) {
	            String clause = line.substring(start, m.start());
	            Expression exp = JavaASTUtil.parseExpression(clause);
	            if (exp != null) {
	                parts.offer(clause);
	                parts.offer(line.substring(m.start(), m.end()));
	            }
	            start = m.end();
	        }
            String clause = line.substring(start);
            Expression exp = JavaASTUtil.parseExpression(clause);
            if (exp != null)
                parts.offer(clause);
            else if (!parts.isEmpty())
                parts.removeLast();
            
            sb.append(exception + " ");
            for (String part : parts)
                sb.append(part);
			sb.append("\n");
		}
		sc.close();
		FileIO.writeStringToFile(sb.toString(), output);
	}

	public static void translate(String input, String inter, String output) {
		splitClauses(input, inter);
		construct(inter, output);
	}

	private static String[] split(String line, HashSet<String> separators) {
		ArrayList<String> tokens = new ArrayList<>();
		line = line.replace(" than or ", " than_or ");
		String[] parts = line.split(" ");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < parts.length; i++) {
			String token = parts[i].trim();
			if (separators.contains(token)) {
				tokens.add(sb.toString().trim());
				sb = new StringBuilder();
				tokens.add(token);
			} else {
				sb.append(token.replace("than_or", "than or") + " ");
			}
		}
		tokens.add(sb.toString().trim());
		return tokens.toArray(new String[]{});
	}

	public static void main(String[] args) {
		translate("T:/spectrans/specs.txt", "T:/spectrans", "T:/spectrans/code.txt");
	}
}
