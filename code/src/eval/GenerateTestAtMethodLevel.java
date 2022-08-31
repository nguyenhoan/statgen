package eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import utils.FileIO;

public class GenerateTestAtMethodLevel {

	public static String getSignatureRemovePathFromLocation(String input) {
		String[] arrContent = input.trim().split("\\s+");
		String strResult = "";
		String strAppendChar = ".";
		for (int i = 1; i < arrContent.length - 2; i++) {
			if (arrContent[i + 1].equals("(")) {
				strAppendChar = ",";
				strResult += arrContent[i];
			} else if (arrContent[i + 1].equals(")")) {
				strAppendChar = "";
				strResult += arrContent[i];
			} else if (arrContent[i].equals(")")) {
				strResult += arrContent[i];
			} else if (arrContent[i].equals("(")) {
				strResult += arrContent[i];
			} else {
				strResult += arrContent[i] + strAppendChar;
			}
		}
		return strResult.trim();
	}

	public static void main(String[] args) {
		String fop = "C:\\Fall2017Data\\";

		String[] arrMethodOracles = FileIO
				.readStringFromFile(fop + "oracle_methodLevel_3cols.txt")
				.trim().split("\n");
		String[] arrMethodFull = FileIO
				.readStringFromFile(fop + "oracle_full.txt").trim().split("\n");
		HashMap<String, String> mapMethodOracles = new HashMap<String, String>();
		HashMap<String, HashSet<String>> mapMethodFull = new HashMap<String, HashSet<String>>();

		for (int i = 1; i < arrMethodOracles.length; i++) {
			String[] arrItem = arrMethodOracles[i].split("\t");
			mapMethodOracles.put(arrItem[0], arrItem[1]);
		}

		for (int i = 1; i < arrMethodFull.length; i++) {
			String[] arrItem = arrMethodFull[i].split("\t");
			String exceptionName = arrItem[6].split("]")[0].replace("[", "");
			HashSet<String> setItemException = mapMethodFull
					.get(arrMethodFull[0]);
			if (setItemException == null) {
				setItemException = new HashSet<String>();
				setItemException.add(exceptionName);
				mapMethodFull.put(arrItem[0], setItemException);
			} else {
				setItemException.add(exceptionName);
			}
		}

		String[] arrTrainLoc = FileIO
				.readStringFromFile(fop + "train.locations.txt").trim()
				.split("\n");
		String[] arrTrainJd = FileIO.readStringFromFile(fop + "train.jd")
				.trim().split("\n");
		String[] arrTrainImpl = FileIO.readStringFromFile(fop + "train.impl")
				.trim().split("\n");

		HashMap<String, ArrayList<Integer>> mapTrain = new HashMap<String, ArrayList<Integer>>();
		for (int i = 0; i < arrTrainLoc.length; i++) {
			String itemSignature = getSignatureRemovePathFromLocation(arrTrainLoc[i]);
			
			ArrayList<Integer> lst = mapTrain.get(itemSignature);
			if (lst == null) {
				lst = new ArrayList<Integer>();
				lst.add(i);
				mapTrain.put(itemSignature, lst);
			} else {
				lst.add(i);
			}
		}

		String strTestLoc = "", strTestJd = "", strTestImpl = "";
		for (int i = 1; i < arrMethodOracles.length; i++) {
			String[] arrItem = arrMethodOracles[i].split("\t");
			HashSet<String> setItemException = mapMethodFull.get(arrItem[0]);
			ArrayList<Integer> lstLines = mapTrain.get(arrItem[0]);

			if (lstLines != null) {
				for (int j = 0; j < lstLines.size(); j++) {
					int line = lstLines.get(j);
					String strExceptionLine = arrTrainJd[line].split("\\s+")[0];
					//if (setItemException.contains(strExceptionLine)) {
						strTestLoc+=arrItem[0]+"\t"+strExceptionLine+"\n";
						strTestJd+=arrTrainJd[line]+"\n";
						strTestImpl+=arrTrainImpl[line]+"\n";
					//}
				}
			} else {
				for(String itemException:setItemException){
					strTestLoc+=arrItem[0]+"\t"+itemException+"\n";
					strTestJd+=itemException+"\n";
					strTestImpl+=itemException+" false\n";
				}
			}
		}
		
		FileIO.writeStringToFile(strTestLoc, fop+"test.signatures.txt");
		FileIO.writeStringToFile(strTestJd, fop+"test.jd");
		FileIO.writeStringToFile(strTestImpl, fop+"test.impl");

	}

}
