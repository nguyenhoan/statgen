package eval;

import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.FileIO;

public class EvaluatePrecisionRecallCompareConsistency {

	public static int getPatternCount(String item, String regex) {
		int count = 0;
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(item);
		while (matcher.find())
			count++;
		return count;
	}

		public static void originalEvaluation() {
		String fopData = "jdk";
		String fpOurResult = fopData + "\\z3.txt";
		String fpLineMap = fopData + "\\test.mapped.txt";
		String fpOracle = "Replication-package\\Replication-package\\ex2.csv";
		String fpResultCompare = fopData + "\\pairToPairZ3.txt";
		String fpSubsetExcel = fopData + "\\subsetLine.csv";
		String fpLineOmitDueToOracle = fopData + "\\lineFilterErrorOracle.txt";

		String strResult = "";

		String[] arrLineMap = FileIO.readStringFromFile(fpLineMap).trim()
				.split("\n");
		String[] arrLineOmit = FileIO.readStringFromFile(fpLineOmitDueToOracle)
				.trim().split("\n");
		HashSet<Integer> setLineOmit = new HashSet<Integer>();
		for (int i = 0; i < arrLineOmit.length; i++) {
			setLineOmit
					.add(new Integer(Integer.parseInt(arrLineOmit[i].trim())));
		}
		String[] arrOurResult = FileIO.readStringFromFile(fpOurResult).trim()
				.split("\n");
		String[] arrOracles = FileIO.readStringFromFile(fpOracle).trim()
				.split("\n");
		int numTP = 0, numFP = 0, numTN = 0, numFN = 0;
		int oldTP = 0, oldFP = 0, oldTN = 0, oldFN = 0;
		String strInconsistent = "Inconsistent";
		String strConsistent = "Consistent";
		String strExcelOutData = "";
		for (int i = 0; i < arrOurResult.length; i++) {
			
			String[] arrMapItem = arrLineMap[i].trim().split("\\s+");
			int lineOrc = Integer.parseInt(arrMapItem[2]);
			String[] orcItems = arrOracles[lineOrc - 1].trim().split(",");
			strExcelOutData += lineOrc + "," + arrOurResult[i] + ","
					+ arrOracles[lineOrc - 1] + "\n";
			strResult += arrOurResult[i] + "\t" + orcItems[7] + "\t"
					+ orcItems[8] + "\t" + lineOrc + "\t" + (i + 1) + "\n";
			System.out.println(arrOurResult[i] + "\t" + orcItems[7] + "\t"
					+ orcItems[8] + "\t" + lineOrc);

			int lineNum = i + 1;


			if (setLineOmit.contains(new Integer(lineNum))) {
				continue;
			}
			

			if (arrOurResult[i].trim().equals(strInconsistent)
					&& orcItems[8].trim().equals(strInconsistent)) {
				numTP++;
			} else if (arrOurResult[i].trim().equals(strInconsistent)
					&& orcItems[8].trim().equals(strConsistent)) {
				numFP++;
			} else if (arrOurResult[i].trim().equals(strConsistent)
					&& orcItems[8].trim().equals(strInconsistent)) {
				numFN++;
			} else if (arrOurResult[i].trim().equals(strConsistent)
					&& orcItems[8].trim().equals(strConsistent)) {
				numTN++;
			}

			if (orcItems[7].trim().equals(strInconsistent)
					&& orcItems[8].trim().equals(strInconsistent)) {
				oldTP++;
			} else if (orcItems[7].trim().equals(strInconsistent)
					&& orcItems[8].trim().equals(strConsistent)) {
				oldFP++;
			} else if (orcItems[7].trim().equals(strConsistent)
					&& orcItems[8].trim().equals(strInconsistent)) {
				oldFN++;
			} else if (orcItems[7].trim().equals(strConsistent)
					&& orcItems[8].trim().equals(strConsistent)) {
				oldTN++;
			}
		}
		System.out.println("Test on " + arrOurResult.length + " result");
		System.out.println("Their precision: " + oldTP + "/" + (oldTP + oldFP)
				+ " is " + (oldTP * 1.0 / (oldTP + oldFP)));
		System.out.println("Their recall: " + oldTP + "/" + (oldTP + oldFN)
				+ " is " + (oldTP * 1.0 / (oldTP + oldFN)));
		System.out.println("Our precision: " + numTP + "/" + (numTP + numFP)
				+ " is " + (numTP * 1.0 / (numTP + numFP)));
		System.out.println("Our recall: " + numTP + "/" + (numTP + numFN)
				+ " is " + (numTP * 1.0 / (numTP + numFN)));
		FileIO.writeStringToFile(strResult, fpResultCompare);
		FileIO.writeStringToFile(strExcelOutData, fpSubsetExcel);

	}

	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		originalEvaluation();

	}

}
