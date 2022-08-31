package clausetree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.trees.Tree;
import utils.JavaASTUtil;
import utils.Utils;

public class ClauseTreeNode {
	
	private String connector;
	private String clause;
	private Tree parseTree;
	private ArrayList<ClauseTreeNode> children;
	
	public ClauseTreeNode(Tree tree, Label label, HashMap<String, String> strCode) {
		this.parseTree = tree;
		String code = strCode.get(label.value());
		if (code != null) {
			code = JavaASTUtil.tokenize(code, Utils.encloseClauseWithParentheses);
			this.clause = code;
			return;
		}
		if (label instanceof CoreLabel) {
			CoreLabel cl = (CoreLabel) label;
			this.clause = cl.get(LemmaAnnotation.class);
			if (this.clause.equals(label.value().toLowerCase()))
				this.clause = null;
		}
		if (this.clause == null)
			this.clause = label.value();
		if (this.clause.endsWith("."))
			this.clause = this.clause.substring(0, this.clause.length()-1);
	}

	public ClauseTreeNode(Tree parseTree, List<Tree> trees, HashMap<String, String> strCode) {
		List<Tree> leaves = new ArrayList<>();
		for (Tree tree : trees)
			leaves.addAll(tree.getLeaves());
		this.parseTree = parseTree;
		for (Tree tree : leaves) {
			Label label = tree.label();
			String code = strCode.get(label.value());
			if (code != null) {
				code = JavaASTUtil.tokenize(code, Utils.encloseClauseWithParentheses);
				if (this.clause == null)
					this.clause = code;
				else
					this.clause += " " + code;
			}
			else if (label instanceof CoreLabel) {
				CoreLabel cl = (CoreLabel) label;
				String lemmaLabel = cl.get(LemmaAnnotation.class);
				if (lemmaLabel.equals(label.value().toLowerCase()))
					lemmaLabel = label.value();
				if (this.clause == null)
					this.clause = lemmaLabel;
				else
					this.clause += " " + lemmaLabel;
			} else {
				if (this.clause == null)
					this.clause = label.value();
				else
					this.clause += " " + label.value();
			}
		}
	}
	
	public ClauseTreeNode(Tree tree, HashMap<String, String> strCode) {
		this(tree, tree.getLeaves(), strCode);
	}

	public ClauseTreeNode(Tree parseTree, ArrayList<ClauseTreeNode> children, String cc) {
		this.parseTree = parseTree;
		if (cc != null) {
			this.connector = cc;
			this.children = new ArrayList<>();
			for (ClauseTreeNode child : children) {
				if (child.connector == null)
					this.children.add(child);
				else {
					if (child.connector.equals(this.connector) || child.connector.equals(","))
						this.children.addAll(child.children);
					else if (this.connector.equals(",")) {
						this.children.addAll(child.children);
						this.connector = child.connector;
					} else
						this.children.add(child);
				}
			}
		} else {
			for (ClauseTreeNode child : children)
				append(child);
			if (this.connector != null) {
				if (this.children.size() > 0) {
					String prefix = "";
					int i = 0;
					while (true) {
						char ch = 0;
						for (ClauseTreeNode child : this.children) {
							if (child.clause == null || i >= child.clause.length()) {
								ch = 0;
								break;
							}
							if (ch == 0)
								ch = child.clause.charAt(i);
							else if (ch != child.clause.charAt(i)) {
								ch = 0;
								break;
							}
						}
						if (ch > 0)
							prefix += ch;
						else
							break;
						i++;
					}
					if (this.connector.equals("or") && prefix.startsWith("one of ")) {
						this.connector = "one of";
						for (ClauseTreeNode child : this.children) {
							child.clause = child.clause.substring("one of ".length());
						}
					}
				}
			}
		}
	}

	public ClauseTreeNode(ClauseTreeNode other) {
		this.clause = other.clause;
		this.connector = other.connector;
		this.children = new ArrayList<>();
		if (other.children != null)
			for (ClauseTreeNode child : other.children)
				this.children.add(new ClauseTreeNode(child));
	}

	public String getConnector() {
		return connector;
	}

	public String getClause() {
		return clause;
	}

	public ArrayList<ClauseTreeNode> getChildren() {
		return children;
	}

	public void append(ClauseTreeNode next) {
		if (this.parseTree != null && next.parseTree != null && this.parseTree.label().value().equals("JJ") && next.parseTree.getLeaves().get(0).label().value().equals("than"))
			return;
		if (this.connector == null) {
			if (this.clause == null) {
				this.connector = next.connector;
				this.clause = next.clause;
				this.children = next.children;
			} else {
				if (next.connector == null)
					this.clause = append(this.clause, next.clause);
				else {
					if ((this.clause.endsWith(" not") || this.clause.contains(" not ")) && next.connector.equals("one of"))
						next.connector = "and";
					next.appendBefore(this);
					this.connector = next.connector;
					this.clause = null;
					this.children = new ArrayList<>(next.children);
				}
			}
		} else {
			if (next.connector == null) {
				if (this.connector.equals("and") && next.clause != null && next.clause.equals("together")) {
					this.clause = flatten() + " " + next.clause;
					this.connector = null;
				}
				else
					appendAfter(next);
			} else {
				if (connector.equals(next.connector)) {
					ArrayList<ClauseTreeNode> extChildren = new ArrayList<>();
					for (ClauseTreeNode nextChild : next.children) {
						for (ClauseTreeNode child : children) {
							ClauseTreeNode extChild = new ClauseTreeNode(child);
							extChild.append(new ClauseTreeNode(nextChild));
							extChildren.add(extChild);
						}
					}
					this.children = extChildren;
				} else {
					ArrayList<ClauseTreeNode> extChildren = new ArrayList<>();
					for (ClauseTreeNode nextChild : next.children) {
						ClauseTreeNode ext = new ClauseTreeNode(this);
						ext.append(nextChild);
						extChildren.add(ext);
					}
					this.children = extChildren;
					connector = next.connector;
				}
			}
		}
	}

	private String append(String s, String next) {
		if (s.endsWith(" " + next))
			return this.clause;
		if (next.startsWith(s + " "))
			return next;
		return s + " " + next;
	}

	private void appendAfter(ClauseTreeNode next) {
		if (this.parseTree != null && next.parseTree != null && this.parseTree.label().value().equals("JJ") && next.parseTree.getLeaves().get(0).label().value().equals("than"))
			return;
		if (this.connector == null)
			this.clause = append(this.clause, next.clause);
		else
			for (ClauseTreeNode child : children)
				child.appendAfter(next);
	}

	private void appendBefore(ClauseTreeNode pre) {
		String clause = pre.clause;
		if (this.connector == null)
			this.clause = append(clause, this.clause);
		else
			for (ClauseTreeNode child : children)
				child.appendBefore(pre);
	}

	public String flatten() {
		StringBuilder sb = new StringBuilder();
		flatten(sb);
		return sb.toString();
	}

	private void flatten(StringBuilder sb) {
		if (this.connector == null) {
			if (this.clause != null && this.clause.contains(" ")) {
				String clause = this.clause.trim();
				if (Utils.encloseClauseWithParentheses && !clause.startsWith("("))
					clause = "( " + clause + " )";
				sb.append(clause);
			} else
				sb.append(clause);
		}
		else {
			if (this.connector == ",")
				this.connector = "or";
			if (this.children.isEmpty()) {
				// FIXME
			} else {
				this.children.get(0).flatten(sb);
				if (this.children.size() > 1) {
					for (int i = 1; i < this.children.size(); i++) {
						sb.append(" " + this.connector + " ");
						this.children.get(i).flatten(sb);
					}
				}
			}
		}
	}
	
	public ArrayList<ClauseTreeNode> getLeaves() {
		ArrayList<ClauseTreeNode> leaves = new ArrayList<>();
		if (this.clause != null)
			leaves.add(this);
		else {
			for (ClauseTreeNode child : this.children)
				leaves.addAll(child.getLeaves());
		}
		return leaves;
	}

	public void print() {
		System.out.println(this);;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		toString(sb, 0);
		return sb.toString();
	}

	private void toString(StringBuilder sb, int indent) {
		for (int i = 0; i < indent; i++)
			sb.append("\t");
		if (this.connector == null)
			sb.append(this.clause + "\n");
		else {
			sb.append(this.connector + "\n");
			for (ClauseTreeNode child : this.children)
				child.toString(sb, indent + 1);
		}
	}
}
