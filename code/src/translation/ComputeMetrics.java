package translation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;

import utils.FileIO;

public class ComputeMetrics {
	private static final int N = 4;

	private static final int SAMPLE_SIZE = 100;
	
	private static File dir = new File("result");
	private static HashMap<String, ArrayList<Integer>> typeTests = new HashMap<>(), complexityTests = new HashMap<>();
	
	public static void main(String[] args) {
		read(dir.getAbsolutePath() + "/test.type.csv");
		computeMetrics("code2spec", dir.getAbsolutePath() + "/code2Text.trans", dir.getAbsolutePath() + "/test.jd");
		computeMetrics("spec2code", dir.getAbsolutePath() + "/text2Code.trans", dir.getAbsolutePath() + "/test.impl");
	}
	
	private static void read(String path) {
		String content = FileIO.readStringFromFile(path);
		Scanner sc = new Scanner(content);
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			String line = sc.nextLine();
			String[] parts = line.split(",");
			int l = parts.length;
			String complex = parts[0];
			if (complex.length() > 1 || complex.compareTo("1") > 0)
				complex = "1";
			for (int j = 0; j < l/3; j++) {
				int k = 1 + j * 3;
				String type = parts[k];
				if (type.equals("para"))
					type += "-" + parts[k+1];
				update(typeTests, i, type);
				update(complexityTests, i, complex);
			}
		}
		sc.close();
	}

	private static void update(HashMap<String, ArrayList<Integer>> typeTests, int i, String type) {
		ArrayList<Integer> tests = typeTests.get(type);
		if (tests == null) {
			tests = new ArrayList<>();
			typeTests.put(type, tests);
		}
		tests.add(i);
	}

	private static double[][] computeMetrics(String name, String translationFile, String referenceFile) {
		double[][] scores = new double[SAMPLE_SIZE + 1][4 + N];
		String transContent = FileIO.readStringFromFile(translationFile);
		String refContent = FileIO.readStringFromFile(referenceFile);
		Scanner tsc = new Scanner(transContent), rsc = new Scanner(refContent);
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			String transSentence = tsc.nextLine(), refSentence = rsc.nextLine();
			String[] transSequence = transSentence.split("\\s+");
			String[] refSequence = refSentence.split("\\s+");
			int lcs = lcs(transSequence, refSequence);
			int tl = transSequence.length, rl = refSequence.length;
			double p = lcs * 1.0 / tl, r = lcs * 1.0 / rl;
			double[] blues = computeBlueScores(transSequence, refSequence, N);
			scores[i][0] = transSequence.length;
			scores[i][1] = refSequence.length;
			scores[i][2] = p;
			scores[i][3] = r;
			for (int n = 1; n <= N; n++) {
				double score = 0;
				for (int k = 0; k < n; k++)
					score += Math.log(blues[k]);
				score /= n;
				score = Math.exp(score);
				if (tl <= rl)
					score = score * Math.exp(1 - rl * 1.0 / tl);
				scores[i][4 + n-1] = score;
			}
			for (int j = 0; j < 4 + N; j++)
				scores[SAMPLE_SIZE][j] += scores[i][j];
		}
		tsc.close();
		rsc.close();
		for (int j = 0; j < 4 + N; j++)
			scores[SAMPLE_SIZE][j] = scores[SAMPLE_SIZE][j] / 100;
		StringBuilder sbTable = new StringBuilder(), sbTable1 = new StringBuilder(), sbColumn1 = new StringBuilder(), sbColumn2 = new StringBuilder();
		sbTable.append("Translation,Reference,Precision,Recall");
		for (int i = 1; i <= N; i++)
			sbTable.append(",BLUE " + i + "-gram");
		sbTable.append("\n");
		for (int i = 0; i < SAMPLE_SIZE + 1; i++) {
			sbTable.append(scores[i][0] + "," + scores[i][1] + "," + scores[i][2] + "," + scores[i][3]);
			for (int j = 0; j < N; j++)
				sbTable.append("," + scores[i][4 + j]);
			sbTable.append("\n");
		}
		print(sbColumn1, scores, "Translation", 0);
		print(sbColumn1, scores, "Reference", 1);
		print(sbColumn2, scores, "Precision", 2);
		print(sbColumn2, scores, "Recall", 3);
		for (int i = 1; i <= N; i++)
			print(sbColumn2, scores, "BLUE-" + i, 3 + i);
		int maxn = 0;
		for (String key : typeTests.keySet()) {
			int size = typeTests.get(key).size();
			if (size > maxn)
				maxn = size;
		}
		/*for (int i = 0; i <= maxn; i++)
			//sbTable1.append(",Precision,Recall,BLEU");
			sbTable1.append(",BLEU");
		sbTable1.append("\n");*/
		String[] types = new String[]{"global", "receiver", "para-type", "para-value", "para-method"}, complexity = new String[]{"0", "1"};
		for (String type : types) {
			sbTable1.append(type);
			ArrayList<Integer> tests = typeTests.get(type);
			double avg = 0;
			for (int i : tests) {
				sbTable1.append("," + /*scores[i][2] + "," + scores[i][3] + "," + */scores[i][N+3]);
				avg += scores[i][N+3];
			}
			for (int i = 0; i < maxn - tests.size(); i++)
				sbTable1.append(",");
			sbTable1.append("," + avg / tests.size());
			sbTable1.append("\n");
		}
		FileIO.writeStringToFile(sbTable1.toString(), dir.getAbsolutePath() + "/" + name + "-metrics-accuracy-type.csv");
		maxn = 0;
		for (String key : complexityTests.keySet()) {
			int size = complexityTests.get(key).size();
			if (size > maxn)
				maxn = size;
		}
		sbTable1 = new StringBuilder();
		/*for (int i = 0; i <= maxn; i++)
			//sbTable1.append(",Precision,Recall,BLEU");
			sbTable1.append(",BLEU");
		sbTable1.append("\n");*/
		for (String complex : complexity) {
			sbTable1.append(complex);
			ArrayList<Integer> tests = complexityTests.get(complex);
			double avg = 0;
			for (int i : complexityTests.get(complex)) {
				sbTable1.append("," + /*scores[i][2] + "," + scores[i][3] + "," + */scores[i][N+3]);
				avg += scores[i][N+3];
			}
			for (int i = 0; i < maxn - tests.size(); i++)
				sbTable1.append(",");
			sbTable1.append("," + avg / tests.size());
			sbTable1.append("\n");
		}
		FileIO.writeStringToFile(sbTable1.toString(), dir.getAbsolutePath() + "/" + name + "-metrics-accuracy-complex.csv");
		FileIO.writeStringToFile(sbTable.toString(), dir.getAbsolutePath() + "/" + name + "-metrics-table.csv");
		FileIO.writeStringToFile(sbColumn1.toString(), dir.getAbsolutePath() + "/" + name + "-metrics-length-column.csv");
		FileIO.writeStringToFile(sbColumn2.toString(), dir.getAbsolutePath() + "/" + name + "-metrics-accuracy-column.csv");
		StringBuilder sb = new StringBuilder();
		for (String complex : complexity)
			sb.append(",," + complex + ",");
		sb.append("\n");
		for (int i = 0; i < complexity.length; i++)
			sb.append(",P,R,B");
		sb.append("\n");
		for (String type : types) {
			sb.append(type);
			for (String complex : complexity) {
				HashSet<Integer> tests = new HashSet<>(complexityTests.get(complex));
				tests.retainAll(typeTests.get(type));
				if (tests.isEmpty())
					sb.append(",,n/a,");
				else {
					double[] avg = {0, 0, 0};
					for (int t : tests) {
						avg[0] += scores[t][2];
						avg[1] += scores[t][3];
						avg[2] += scores[t][N+3];
					}
					for (int i = 0; i < avg.length; i++)
						avg[i] = avg[i] * 100 / tests.size();
					sb.append("," + print(avg[0]) + "," + print(avg[1]) + "," + print(avg[2]));
				}
			}
			sb.append("\n");
		}
		FileIO.writeStringToFile(sb.toString(), dir.getAbsolutePath() + "/" + name + "-metrics-accuracy-table.csv");
		
		return scores;
	}

	private static String print(double d) {
		if (Double.isNaN(d))
			return "NaN";
		return (int) d + "%";
	}

	private static void print(StringBuilder sbColumn, double[][] scores, String name, int column) {
		for (int i = 0; i < SAMPLE_SIZE; i++) {
			sbColumn.append(name + "," + scores[i][column] + "\n");
		}
	}

	private static double[] computeBlueScores(String[] transSequence, String[] refSequence, int n) {
		double[] scores = new double[n];
		for (int i = 1; i <= n; i++) {
			HashMap<String, Integer> transBag = buildBagOfGrams(transSequence, i);
			HashMap<String, Integer> refBag = buildBagOfGrams(refSequence, i);
			int tc = cardinality(transBag), inter = intersection(transBag, refBag);
			scores[i-1] = inter * 1.0 / tc;
		}
		return scores;
	}

	private static int cardinality(HashMap<String, Integer> bag) {
		int c = 0;
		for (int v : bag.values())
			c += v;
		return c;
	}
	
	private static int intersection(HashMap<String, Integer> bag1, HashMap<String, Integer> bag2) {
		int c = 0;
		for (String key : bag1.keySet()) {
			if (bag2.containsKey(key)) {
				c += Math.min(bag1.get(key), bag2.get(key));
			}
		}
		return c;
	}
	
	private static HashMap<String, Integer> buildBagOfGrams(String[] sequence, int n) {
		HashMap<String, Integer> bag = new HashMap<>();
		for (int i = 0; i < sequence.length - (n-1); i++) {
			String gram = buildGram(sequence, i, n);
			add(bag, gram);
		}
		return bag;
	}

	private static void add(HashMap<String, Integer> bag, String gram) {
		int c = 1;
		if (bag.containsKey(gram))
			c += bag.get(gram);
		bag.put(gram, c);
	}

	private static String buildGram(String[] sequence, int i, int n) {
		StringBuilder sb = new StringBuilder();
		for (int j = i; j < i + n - 1; j++)
			sb.append(sequence[j] + " ");
		return sb.toString();
	}

	private static int lcs(String[] sequence1, String[] sequence2) {
		int lenM = sequence1.length, lenN = sequence2.length;
		int[][] d = new int[2][lenN + 1];
		char[][] p = new char[lenM + 1][lenN + 1];
		for (int j = 0; j <= lenN; j++)
			d[1][j] = 0;
		for (int i = lenM-1; i >= 0; i--) {
			for (int j = 0; j <= lenN; j++)
				d[0][j] = d[1][j];
			for (int j = lenN-1; j >= 0; j--) {
				if (sequence1[i].equals(sequence2[j])) {
					d[1][j] = d[0][j + 1] + 1;
					p[i][j] = 'D';
				} else if (d[0][j] >= d[1][j + 1]) {
					d[1][j] = d[0][j];
					p[i][j] = 'U';
				} else {
					d[1][j] = d[1][j + 1];
					p[i][j] = 'R';
				}
			}
		}
		int i = 0, j = 0, len = 0;
		while (i < lenM && j < lenN) {
			if (p[i][j] == 'D') {
				i++;
				j++;
				len++;
			} else if (p[i][j] == 'U')
				i++;
			else
				j++;
		}
		return len;
	}

}
