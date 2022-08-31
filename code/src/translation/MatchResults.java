package translation;

import java.io.File;
import java.util.Arrays;
import java.util.Scanner;

import utils.FileIO;

public class MatchResults {

	public static void main(String[] args) {
		File dir = new File("result");
		String gen = FileIO.readStringFromFile(dir.getAbsolutePath() + "/text2Code.trans"), ref = FileIO.readStringFromFile(dir.getAbsolutePath() + "/test.impl");
		String[] genLines = read(gen), refLines = read(ref);
		int count = 0;
		for (int i = 0; i < genLines.length; i++)
			if (match(genLines[i], refLines[i])) {
				System.out.println("same");
				count++;
			}
			else
				System.out.println();
		System.out.println(count);
	}

	private static boolean match(String s1, String s2) {
		String[] seq1 = s1.split("\\s+"), seq2 = s2.split("\\s+");
		return Arrays.equals(seq1, seq2);
	}

	@SuppressWarnings("unused")
	private static int next(String[] seq, int i) {
		while (i < seq.length && seq[i].trim().isEmpty())
			i++;
		return i;
	}

	private static String[] read(String content) {
		String[] lines = new String[100];
		Scanner sc = new Scanner(content);
		for (int i = 0; i < lines.length; i++)
			lines[i] = sc.nextLine();
		sc.close();
		return lines;
	}

}
