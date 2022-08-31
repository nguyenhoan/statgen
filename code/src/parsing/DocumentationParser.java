package parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;

import utils.JavaASTUtil;

public class DocumentationParser {
	private static final HashSet<String> CODE_TAGS = new HashSet<>(), LIST_TAGS = new HashSet<>(), IGNORE_TAGS = new HashSet<>(), CONTENT_TAG = new HashSet<>();
	public static HashSet<String> seenTags = new HashSet<>();
	
	static {
		CONTENT_TAG.add("body");
		CONTENT_TAG.add("pre");
		CONTENT_TAG.add("p");
		CONTENT_TAG.add("li");
		CONTENT_TAG.add("i");
		CONTENT_TAG.add("em");
		CONTENT_TAG.add("b");
		CONTENT_TAG.add("literal");
		CONTENT_TAG.add("sup");
		CONTENT_TAG.add("sub");
		
		CODE_TAGS.add("code");
		CODE_TAGS.add("link");
		CODE_TAGS.add("tt");
		CODE_TAGS.add("quote");
		CODE_TAGS.add("linkplain");
		CODE_TAGS.add("ocde");
		CODE_TAGS.add("value");
		
		IGNORE_TAGS.add("a");
		IGNORE_TAGS.add("br");
		
		LIST_TAGS.add("ul");
		LIST_TAGS.add("ol");
	}
	
	public static String normalize(String doc, HashMap<String, String> codeStr, HashMap<String, String> strCode, ArrayList<String> paraNames) {
		doc = doc.replace("<link>", "<code>");
		doc = doc.replace("</link>", "</code>");
		doc = convertQuotes(doc);
		StringBuilder sb = new StringBuilder();
		String[] tokens = doc.split("[\\s]+");
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i].toLowerCase();
			if (token.equals("<code>")) {
				if (i+1 < tokens.length && !tokens[i+1].toLowerCase().startsWith(token))
					sb.append(" " + tokens[i]);
			} else if (token.equals("</code>")) {
				if (i-1 >= 0 && !tokens[i-1].toLowerCase().endsWith(token))
					sb.append(" " + tokens[i]);
			} else
				sb.append(" " + tokens[i]);
		}
		doc = sb.toString();
		
		doc = doc.replace(" &lt; ", " is less than ");
		doc = doc.replace(" &gt; ", " is greater than ");
		doc = doc.replace(" &lt;= ", " is less than or equal to ");
		doc = doc.replace(" &gt;= ", " is greater than or equal to ");
		doc = doc.replace(" &le; ", " is less than or equal to ");
		doc = doc.replace(" &ge; ", " is greater than or equal to ");
		
		Document html = Jsoup.parse(doc);
		Element body = html.getElementsByTag("body").first();
		sb = new StringBuilder();
		normalize(body, sb, codeStr, strCode, paraNames);
		doc = sb.toString();
		sb = new StringBuilder();
		tokens = doc.split("[\\s]+");
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i].toLowerCase();
			if (token.equals("throws") || token.equals("throw") || token.equals("thrown") || token.equals("thrown.")) {
				if (i + 1 < tokens.length) {
					String next = tokens[i+1];
					if (isException(next))
						i++;
				}
			} else
				sb.append(" " + tokens[i]);
		}
		doc = sb.toString();
		doc = removeIf(doc);
//		doc = normalizeIf(doc);
		
		doc = trim(doc);
		if (!doc.endsWith("."))
			doc = doc + ".";
		tokens = doc.split("[\\s]+");
		sb = new StringBuilder();
		for (int i = 0; i < tokens.length; i++) {
			String token = tokens[i];
			int j = token.length()-1;
			while (j >= 0) {
				String ch = token.substring(j, j+1);
				if (!Pattern.matches("[\\.,;:\\!]", ch))
					break;
				j--;
			}
			String suffix = token.substring(j+1);
			token = token.substring(0, j+1);
			String para = isParameter(token, paraNames);
			if (para != null)
				token = update(codeStr, strCode, para);
			else if (JavaASTUtil.isMethodInvocation(token))
				token = update(codeStr, strCode, token);
			if (!token.isEmpty())
				sb.append(" " + token);
			if (!suffix.isEmpty())
				sb.append(" " + suffix);
		}
		
		doc = removeParenthesis(sb.toString(), codeStr, strCode);

		doc = doc.replace(" < ", " is less than ");
		doc = doc.replace(" > ", " is greater than ");
		doc = doc.replace(" <= ", " is less than or equal to ");
		doc = doc.replace(" >= ", " is greater than or equal to ");
		doc = doc.replace(" but ", " and ");
		doc = doc.replace(" nor ", " or ");
		
		return doc;
	}

	private static String convertQuotes(String doc) {
		StringBuilder sb = new StringBuilder();
		int i = 0, start = -1;
		while (i < doc.length()) {
			char ch = doc.charAt(i);
			if (ch == '\"') {
				if (start == -1)
					start = i;
				else {
					sb.append("<quote>");
					sb.append(doc.substring(start + 1, i));
					sb.append("</quote>");
					start = -1;
				}
			} else {
				if (start == -1) {
					sb.append(ch);
					if (ch == '\\' && i+1 < doc.length())
						sb.append(doc.charAt(i+1));
				}
				if (ch == '\\')
					i++;
			}
			i++;
		}
		if (start > -1)
			sb.append(doc.substring(start));
		return sb.toString();
	}

	public static String trim(String doc) {
		doc = doc.trim();
		int i = 0;
		while (i < doc.length()) {
			char ch = doc.charAt(i);
			if (Character.isWhitespace(ch) || ch == '-')
				i++;
			else
				break;
		}
		doc = doc.substring(i);
		return doc;
	}

	private static void normalize(Node node, StringBuilder sb, HashMap<String, String> codeStr, HashMap<String, String> strCode, ArrayList<String> paraNames) {
		if (node instanceof TextNode) {
			TextNode text = (TextNode) node;
			sb.append(text.text());
		} else if (node instanceof Element) {
			Element e = (Element) node;
			String tag = e.tagName();
			if (node.childNodeSize() == 1 && node.childNode(0) instanceof Element) {
				Element child = (Element) node.childNode(0);
				if (child.tagName().equals(tag)) {
					normalize(child, sb, codeStr, strCode, paraNames);
					return;
				}
			}
			if (CONTENT_TAG.contains(tag)) {
				boolean endsWithNon = false;
				for (int i = 0; i < node.childNodeSize(); i++) {
					Node child = node.childNodes().get(i);
					String s = "";
					if (endsWithNon) {
						StringBuilder csb = new StringBuilder();
						escape(child, csb);
						s = csb.toString();
					} else {
						StringBuilder csb = new StringBuilder();
						normalize(child, csb, codeStr, strCode, paraNames);
						s = csb.toString();
					}
					sb.append(s);
					endsWithNon = s.endsWith("non-");
				}
			} else if (CODE_TAGS.contains(tag)) {
				StringBuilder csb = new StringBuilder();
				escape(node, csb);
				String code = csb.toString();
				if (isLiteral(code))
					sb.append(code);
				else if (isException(code))
					sb.append(code);
				else if (JavaASTUtil.isComparison(code))
                    sb.append(code);
				else {
					String str = update(codeStr, strCode, code);
					if (!code.isEmpty() && Character.isWhitespace(code.charAt(0)))
						sb.append(" ");
					sb.append(str);
					if (!code.isEmpty() && Character.isWhitespace(code.charAt(code.length()-1)))
						sb.append(" ");
				}
			} else if (IGNORE_TAGS.contains(tag)) {
				return;
			} else if (LIST_TAGS.contains(tag)) {
				for (int i = 0; i < node.childNodeSize(); i++) {
					Node child = node.childNodes().get(i);
					if (child instanceof TextNode) {
						if (((TextNode) child).text().trim().isEmpty())
							continue;
					} else if (child instanceof Element) {
						if (!((Element) child).tagName().equals("li"))
							continue;
					} else
						continue;
					StringBuilder csb = new StringBuilder();
					normalize(child, csb, codeStr, strCode, paraNames);
					String s = csb.toString();
					if (i < node.childNodeSize() - 1) {
						s = s.trim();
						if (s.endsWith(".") || s.endsWith(";"))
							s = s.substring(0, s.length() - 1);
						if (!s.endsWith(","))
							s += " , ";
					}
					sb.append(s);
				}
			} else {
//				System.err.println("Unknown tag!!!");
//				System.exit(-1); // TODO
				// treat other tag the same as content tags
				boolean endsWithNon = false;
				for (int i = 0; i < node.childNodeSize(); i++) {
					Node child = node.childNodes().get(i);
					String s = "";
					if (endsWithNon) {
						StringBuilder csb = new StringBuilder();
						escape(child, csb);
						s = csb.toString();
					} else {
						StringBuilder csb = new StringBuilder();
						normalize(child, csb, codeStr, strCode, paraNames);
						s = csb.toString();
					}
					sb.append(s);
					endsWithNon = s.endsWith("non-");
				}
			}
		}
	}

	private static void escape(Node node, StringBuilder sb) {
		if (node instanceof TextNode)
			sb.append(((TextNode) node).text());
		else if (node instanceof Element) {
			String tag = ((Element) node).tagName();
			if (sb.length() == 0 || CONTENT_TAG.contains(tag)) {
				for (Node child : node.childNodes())
					escape(child, sb);
			} else if (CODE_TAGS.contains(tag)) {
				if (tag.equals("quote")) {
					sb.append("\"");
					for (Node child : node.childNodes())
						escape(child, sb);
					sb.append("\"");
				} else {
					System.err.println("Code tag in escaping!!!");
					System.exit(-1);
				}
			} else if (LIST_TAGS.contains(tag)) {
				System.err.println("List tag in escaping!!!");
				System.exit(-1);
			} else if (!IGNORE_TAGS.contains(tag)) {
				System.err.println("Unknown tag in escaping!!!");
				System.exit(-1);
			}
		}
	}

	private static boolean isException(String token) {
		return token.endsWith("Exception");
	}

	@SuppressWarnings("unused")
	private static String normalizeIf(String str) {
		String doc = str;
		doc = doc.replace("if and only if ", "if ");
		doc = doc.replace("If and only if ", "if ");
		doc = doc.replace("If and only If ", "if ");
		return doc;
	}

	private static String removeParenthesis(String doc, HashMap<String, String> codeStr, HashMap<String, String> strCode) {
		StringBuilder sb = new StringBuilder();
		Stack<Integer> stk = new Stack<>();
		for (int i = 0; i < doc.length(); i++) {
			char ch = doc.charAt(i);
			switch (ch) {
			case '(':
				stk.push(i);
				break;
			case ')':
				if (stk.isEmpty()) {
					sb.append(ch);
					break;
				}
				int start = stk.pop();
				if (stk.isEmpty()) {
					String sub = doc.substring(start+1, i).trim();
					if (strCode.containsKey(sub)) {
						String code = update(codeStr, strCode, "( " + strCode.get(sub) + " )");
						sb.append(code);
					} else if (JavaASTUtil.isExpression(sub)) {
						String code = update(codeStr, strCode, "( " + sub + " )");
						sb.append(code);
					}
				}
				break;
			default:
				if (stk.isEmpty())
					sb.append(ch);
			}
		}
		return sb.toString();
	}

	private static String removeIf(String str) {
		String doc = str;
		doc = doc.replace("if and only if ", " ");
		doc = doc.replace("If and only if ", " ");
		doc = doc.replace("if ", " ");
		doc = doc.replace("If ", " ");
		doc = doc.replace("IF ", " ");
		doc = doc.replace("if<", "<");
		doc = doc.replace("If<", "<");
		doc = doc.replace("IF<", "<");
		doc = doc.replace("When ", " ");
		doc = doc.replace("when ", " ");
		return doc;
	}

	private static boolean isLiteral(String code) {
		if (code.equals("true") || code.equals("false") || code.equals("null"))
			return true;
		try {
			Double.parseDouble(code);
			return true;
		} catch (NumberFormatException e) {}
		return false;
	}

	public static String isParameter(String token, ArrayList<String> paraNames) {
		int index = paraNames.indexOf(token);
		if (index > -1)
			return token;
		if (token.length() < 3)
			return null;
		token = strip(token);
		if (token == null)
			return null;
		index = paraNames.indexOf(token);
		if (index > -1)
			return token;
		return null;
	}

	private static String strip(String token) {
		char ch = token.charAt(0);
		if (!Character.isJavaIdentifierStart(ch) && Character.isJavaIdentifierStart(token.charAt(1)) && token.charAt(token.length()-1) == ch) {
			return token.substring(1, token.length()-1);
		}
		return null;
	}

	private static String update(HashMap<String, String> codeStr, HashMap<String, String> strCode, String code) {
		code = code.trim();
		String str = codeStr.get(code);
		if (str != null)
			return str;
		str = "CODE" + codeStr.size();
		codeStr.put(code, str);
		strCode.put(str, code);
		return str;
	}
}
