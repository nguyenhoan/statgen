package parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Stack;

import clausetree.ClauseTreeNode;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import utils.JavaASTUtil;

public class NLPSentenceParser {
	// build pipeline
	private static Properties props = new Properties();
	private static StanfordCoreNLP pipeline = null;
	
	static {
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
		pipeline = new StanfordCoreNLP(props);
	}
	
	public static List<CoreMap> parse(String text) {
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		return annotation.get(SentencesAnnotation.class);
	}

	public static void print(Tree tree) {
		print(tree, 0);
	}

	/**
	 * Only prints the local tree structure, does not recurse
	 */
	public static void print(Tree tree, int indent) {
		if (!tree.isLeaf())
			printIndent(indent);
		if (tree.isPreTerminal())
			System.out.print(tree.label() + " ");
		else
			System.out.println(tree.label().value() + " ");
		for (Tree kid : tree.children()) {
			print(kid, indent+1);
		}
	}

	private static void printIndent(int indent) {
		for (int i = 0; i < indent; i++)
			System.out.print("\t");
	}
	
	public static ClauseTreeNode buildClauseTree(Tree tree, Stack<Tree> trees, Stack<Integer> indexes, HashSet<Tree> processedTrees, HashMap<String, String> strCode) {
		if (tree.isPreTerminal())
			return new ClauseTreeNode(tree, tree.firstChild().label(), strCode);
		else if (isComparator(tree))
			return new ClauseTreeNode(tree, strCode);
		else {
			ClauseTreeNode ct = null;
			if (tree.label().value().equals("ROOT"))
				ct = buildSimpleClauseTree(tree, strCode);
			if (ct == null) {
				ct = buildComplexClauseTree(tree, trees, indexes, processedTrees, strCode);
			}
			return ct;
		}
	}

	private static ClauseTreeNode buildSimpleClauseTree(Tree tree, HashMap<String, String> strCode) {
		List<Tree> leaves = tree.getLeaves();
		ArrayList<String> labels = new ArrayList<>();
		ArrayList<Integer> separators = new ArrayList<>();
		String cc = null;
		for (int i = 0; i < leaves.size(); i++) {
			Tree leaf = leaves.get(i);
			String label = leaf.label().value();
			labels.add(label);
			if (label.equals(",") || label.equals(";"))
				separators.add(i);
			else if (label.equals("and") || label.equals("or") || label.equals("for") || label.equals("but") || label.equals("yet") || label.equals("so") || label.equals("nor")) {
				separators.add(i);
				if (label.equals("for") || label.equals("but") || label.equals("yet") || label.equals("so"))
					label = "and";
				else if (label.equals("nor"))
					label = "or";
				if (cc == null)
					cc = label;
				else if (!cc.equals(label))
					return null;
			}
		}
		if (labels.get(labels.size() - 1).equals(".") || labels.get(labels.size() - 1).equals("!"))
			separators.add(labels.size() - 1);
		else
			separators.add(labels.size());
		int start = 0;
		ArrayList<ClauseTreeNode> children = new ArrayList<>();
		for (int i = 0; i < separators.size(); i++) {
			int end = separators.get(i);
			if (end > start) {
				StringBuilder sb = new StringBuilder();
				for (int j = start; j < end; j++)
					sb.append(labels.get(j) + " ");
				String clause = sb.toString();
				List<CoreMap> sentences = NLPSentenceParser.parse(clause);
				if (sentences.size() != 1)
					return null;
				CoreMap sentence = sentences.get(0);
				Tree ctree = sentence.get(TreeAnnotation.class);
				if (!isClause(ctree.firstChild()))
					return null;
				children.add(new ClauseTreeNode(null, leaves.subList(start, end), strCode));
			}
			start = end + 1;
		}
		if (children.size() == 1)
			return children.get(0);
		return new ClauseTreeNode(tree, children, cc);
	}

	private static boolean isClause(Tree tree) {
		if (JavaASTUtil.isInfixExpression(tree))
			return true;
		if (!tree.label().value().equals("S"))
			return false;
		if (tree.children().length < 2)
			return false;
		boolean hasSubject = false;
		for (Tree child : tree.children()) {
			if (child.isLeaf())
				continue;
			String label = child.label().value();
			if (isSubject(label)) {
				hasSubject = true;
			} else if (isVerb(label)) {
				if (!hasSubject)
					return false;
				return true;
			}
		}
		return false;
	}

	private static boolean isVerb(String label) {
		return label.equals("VP");
	}

	private static boolean isSubject(String label) {
		return label.equals("NP");
	}

	public static ClauseTreeNode buildComplexClauseTree(Tree tree, Stack<Tree> trees, Stack<Integer> indexes, HashSet<Tree> processedTrees, HashMap<String, String> strCode) {
		ArrayList<ClauseTreeNode> clauseChildren = new ArrayList<>();
		String cc = null;
		int start = 0;
		Tree[] parseChildren = tree.children();
		int numOfParseChildren = parseChildren.length;
		for (int i = 0; i < numOfParseChildren; i++) {
			Tree child = parseChildren[i];
			int end = -1;
			String label = getLabel(child);
			if (processedTrees.remove(child)) {
			} else if (label.equals("either") || label.equals("both") || label.equals("neither")) {
			} else if (isCC(child)) {
				cc = label;
				if (cc.equals("for") || cc.equals("but") || cc.equals("yet") || cc.equals("so"))
					cc = "and";
				else if (cc.equals("nor"))
					cc = "or";
				end = clauseChildren.size();
			} else if (label.equals(",") || label.equals(";")) {
				if (cc == null)
					cc = ",";
				end = clauseChildren.size();
			} else if (label.equals("greater than") || label.equals("less than")) {
				List<Tree> subTrees = new ArrayList<>();
				subTrees.add(child);
				Tree next = parseChildren[++i];
				subTrees.add(next);
				if (getLabel(next).equals("or")) {
					i++;
					subTrees.add(parseChildren[i]);
				}
				ClauseTreeNode c = new ClauseTreeNode(null, subTrees, strCode);
				clauseChildren.add(c);
			} else if (i == numOfParseChildren-1 && (label.equals(".") || label.equals("!"))) {
				
			} else if (label.equals("if")){
				
			} else if (label.equals("-LRB-")) {
				int j = i + 1;
				while (/*j < numOfParseChildren && */!parseChildren[j].firstChild().value().equals("-RRB-"))
					j++;
				i = j;
			} else if (child.isPreTerminal()) {
				trees.push(tree);
				indexes.push(i);
				ClauseTreeNode c = buildClauseTree(child, trees, indexes, processedTrees, strCode);
				trees.pop();
				indexes.pop();
				Tree next = null;
				if (i < numOfParseChildren - 1)
					next = parseChildren[i+1];
				else {
					for (int j = trees.size() - 1; j >= 0; j--) {
						Tree t = trees.get(j);
						int index = indexes.get(j);
						if (index < t.numChildren() - 1) {
							next = t.getChild(index + 1);
							break;
						}
					}
				}
				if (next != null && next.getLeaves().get(0).value().equals("than")) {
					c.append(buildClauseTree(next, new Stack<>(), new Stack<>(), new HashSet<>(), strCode));
					clauseChildren.add(c);
					processedTrees.add(next);
				} else {
					clauseChildren.add(c);
				}
			} else if (!isExamplePhrase(child)) {
				if (child.value().equals("S") && child.getLeaves().size() < tree.getLeaves().size() - 2) {
					StringBuilder clause = new StringBuilder(500);
					for (Tree leaf : child.getLeaves())
						clause.append(leaf.value() + " ");
					clause.append(".");
					List<CoreMap> sentences = NLPSentenceParser.parse(clause.toString());
					Tree ctree = sentences.get(0).get(TreeAnnotation.class);
					ClauseTreeNode c = buildClauseTree(ctree, new Stack<>(), new Stack<>(), new HashSet<>(), strCode);
					if (c.getConnector() != null)
						end = clauseChildren.size();
					clauseChildren.add(c);
				} else {
					trees.push(tree);
					indexes.push(i);
					ClauseTreeNode c = buildClauseTree(child, trees, indexes, processedTrees, strCode);
					trees.pop();
					indexes.pop();
					if (c.getConnector() != null)
						end = clauseChildren.size();
					clauseChildren.add(c);
				}
			}
			if (i+1 < numOfParseChildren) {
				if (isExamplePhrase(parseChildren[i+1]))
					i++;
				else if (parseChildren[i+1].isPreTerminal()) {
					label = parseChildren[i+1].firstChild().label().value();
					if (label.equals(",") || label.equals(";"))
						if (i+2 < numOfParseChildren && isExamplePhrase(parseChildren[i+2]))
							i += 2;
				}
			}
			if (i == numOfParseChildren-1 && start > 0 && start < clauseChildren.size() - 1) {
				if ("not".equals(clauseChildren.get(start).getClause())) {
					end = clauseChildren.size(); 
				} else {
					ArrayList<ClauseTreeNode> subChildren = new ArrayList<>();
					for (int j = 0; j <= start; j++)
						subChildren.add(clauseChildren.remove(0));
					ClauseTreeNode node = new ClauseTreeNode(null, subChildren, cc);
					clauseChildren.add(0, node);
					return new ClauseTreeNode(null, clauseChildren, null);
				}
			}
			if (end > start) {
//					ClauseTreeNode startNode = children.get(start);
				if (end - start > 1/* && startNode.getClause() != null && startNode.getClause().equals("not")*/) {
					ArrayList<ClauseTreeNode> subChildren = new ArrayList<>();
					for (int j = start; j < end; j++)
						subChildren.add(clauseChildren.remove(start));
					ClauseTreeNode node = new ClauseTreeNode(null, subChildren, null);
					clauseChildren.add(start, node);
				}
				start = clauseChildren.size();
			}
		}
		return new ClauseTreeNode(tree, clauseChildren, cc);
	}
	
	private static String getLabel(Tree tree) {
		StringBuilder sb = new StringBuilder();
		for (Tree child : tree.getLeaves())
			sb.append(child.value() + " ");
		return sb.toString().trim();
	}

	@SuppressWarnings("unused")
	private static boolean isComplementaryPhrase(Tree tree) {
		return isPrepositionalPhrase(tree) || isExamplePhrase(tree);
	}
	
	private static boolean isPrepositionalPhrase(Tree tree) {
		return tree.label().value().equals("PP");
	}
	
	private static boolean isExamplePhrase(Tree tree) {
		if (tree.getLeaves().get(0).label().value().equals("e.g."))
			return true;
		if (tree.value().equals("SBAR") && tree.firstChild().firstChild().label().value().equals("as"))
			return true;
		if (tree.numChildren() >= 2 && tree.firstChild().firstChild().value().equals("-LRB-") && tree.lastChild().firstChild().value().equals("-RRB-"))
			return true;
		if (!tree.label().value().equals("PP"))
			return false;
		if (tree.numChildren() >= 2 && tree.firstChild().firstChild().label().value().equals("such") && tree.getChild(1).firstChild().label().value().equals("as"))
			return true;
		return false;
	}

	private static boolean isComparator(Tree tree) {
		if (tree.children().length == 3) {
			if (tree.children()[1].firstChild().label().value().equals("or")) {
				List<Tree> leaves = tree.children()[0].getLeaves();
				Tree leaf = leaves.get(leaves.size() - 1);
				if (leaf.label().value().equals("than"))
					return true;
			}
		}
		return false;
	}

	private static boolean isCC(Tree tree) {
		List<Tree> leaves = tree. getLeaves();
		if (leaves.size() != 1)
			return false;
		return leaves.get(0).ancestor(1, tree).value().equals("CC");
	}

}
