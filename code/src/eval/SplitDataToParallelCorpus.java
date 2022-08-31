package eval;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import utils.FileIO;

public class SplitDataToParallelCorpus {

	public static String fop_ALLDATA = "C:\\ICSE2018_ex2\\";
	public static String FOPJDKSOURCE = "D:\\data\\jdk8\\";
	public static HashSet<String> setJavaKeywords;

	public static boolean checkContainPair(String strSource, String strTarget) {
		boolean result = true;
		String[] arrSource = strSource.trim().split("\\s+");
		String[] arrTarget = strTarget.trim().split("\\s+");

		if (arrSource.length == 2 && arrSource[1].equals("null")
				|| arrSource.length == 1) {
			result = false;
		}

		if (arrTarget.length == 2 && arrTarget[1].equals("null")
				|| arrTarget.length == 1) {
			result = false;
		}

		return result;

	}

	public static String getSignatureFromLocation(String input) {
		String[] arrContent = input.trim().split("\\s+");
		String strResult = "";
		for (int i = 0; i < arrContent.length - 2; i++) {
			strResult += arrContent[i] + "\t";
		}
		return strResult.trim();
	}

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

	private static ArrayList<String> getTagCodeValues(final String str) {
		final ArrayList<String> tagValues = new ArrayList<String>();
		final Matcher matcher = Pattern.compile("<code>(.+?)</code>").matcher(
				str);
		while (matcher.find()) {
			String item = matcher.group(1).trim();
			String[] arrItem = item.trim().split("\\s+");
			if (arrItem.length == 1
					&& !arrItem[0].isEmpty()
					&& (arrItem[0].charAt(0) + "").toLowerCase().equals(
							(arrItem[0].charAt(0) + "")) && !item.contains(".")
					&& !item.contains("(") && item.matches("[a-zA-Z]+")
					&& !setJavaKeywords.contains(item)) {
				tagValues.add(matcher.group(1));
			}
		}
		return tagValues;
	}

	public static String getVariableInJavadoc(ArrayList<String> lst) {
		String strResult = "";
		for (int i = 0; i < lst.size(); i++) {
			strResult += lst.get(i) + " ";
		}
		return strResult.trim();
	}

	public static String getStringOfSet(HashSet<String> set) {
		String strResult = "";
		Iterator<String> iter = set.iterator();
		while (iter.hasNext()) {
			strResult += iter.next() + "\n";
		}
		return strResult;
	}

	public static void getTrainTestFromSourceTargetLocation() {
		String fop_in = fop_ALLDATA + "\\jdk_all_7\\";
		String fop_out = fop_ALLDATA + "\\jdk_all_7\\";
		String fp_Map = fop_ALLDATA + "\\jdk_all_7\\map.csv";
		String fp_mapOldNewTrain = fop_ALLDATA
				+ "\\jdk_all_7\\map.old.locations.txt";
		String[] arrMap = FileIO.readStringFromFile(fp_Map).trim().split("\n");

		String strLineOldNewMap = "", strTrainDoc = "", strTrainImpl = "", strTestDoc = "", strTestImpl = "", strTestLocation = "", strTrainLocation = "", strTestLineInOldCorpus = "";
		String strMapLocation = "";

		String[] arrLocations = FileIO.readStringFromFile(
				fop_in + "locations.txt").split("\n");
		String[] arrSources = FileIO.readStringFromFile(fop_in + "source.txt")
				.split("\n");
		String[] arrTargets = FileIO.readStringFromFile(fop_in + "target.txt")
				.split("\n");
		String[] arrMapOldNewTrain = FileIO
				.readStringFromFile(fp_mapOldNewTrain).trim().split("\n");

		String[] arrClauseLocations = FileIO.readStringFromFile(
				fop_in + "clause-locations.txt").split("\n");
		String[] arrClauseSources = FileIO.readStringFromFile(
				fop_in + "clause-source.txt").split("\n");
		String[] arrJavadocs = FileIO
				.readStringFromFile(fop_in + "javadoc.txt").split("\n");

		HashMap<String, String> mapJavaDocSource = new HashMap<String, String>();
		HashMap<String, String> mapJavaDocLine = new HashMap<String, String>();
		HashMap<String, String> mapVariablesToLine = new HashMap<String, String>();
		HashSet<String> setVariableInCorpus = new HashSet<String>();
		HashSet<String> setExceptionInCorpus = new HashSet<String>();

		setJavaKeywords = new HashSet<String>();
		setJavaKeywords.add("abstract");
		setJavaKeywords.add("continue");
		setJavaKeywords.add("for");
		setJavaKeywords.add("new");
		setJavaKeywords.add("switch");
		setJavaKeywords.add("assert");
		setJavaKeywords.add("default");
		setJavaKeywords.add("goto");
		setJavaKeywords.add("package");
		setJavaKeywords.add("synchronized");
		setJavaKeywords.add("boolean");
		setJavaKeywords.add("do");
		setJavaKeywords.add("if");
		setJavaKeywords.add("private");
		setJavaKeywords.add("this");
		setJavaKeywords.add("break");
		setJavaKeywords.add("double");
		setJavaKeywords.add("implements");
		setJavaKeywords.add("protected");
		setJavaKeywords.add("throw");
		setJavaKeywords.add("byte");
		setJavaKeywords.add("else");
		setJavaKeywords.add("import");
		setJavaKeywords.add("public");
		setJavaKeywords.add("throws");
		setJavaKeywords.add("case");
		setJavaKeywords.add("enum");
		setJavaKeywords.add("instanceof");
		setJavaKeywords.add("return");
		setJavaKeywords.add("transient");
		setJavaKeywords.add("catch");
		setJavaKeywords.add("extends");
		setJavaKeywords.add("int");
		setJavaKeywords.add("short");
		setJavaKeywords.add("try");
		setJavaKeywords.add("char");
		setJavaKeywords.add("final");
		setJavaKeywords.add("interface");
		setJavaKeywords.add("static");
		setJavaKeywords.add("void");
		setJavaKeywords.add("class");
		setJavaKeywords.add("finally");
		setJavaKeywords.add("long");
		setJavaKeywords.add("strictfp");
		setJavaKeywords.add("volatile");
		setJavaKeywords.add("const");
		setJavaKeywords.add("float");
		setJavaKeywords.add("native");
		setJavaKeywords.add("super");
		setJavaKeywords.add("while");

		for (int i = 0; i < arrClauseLocations.length; i++) {
			String[] arrItemCL = arrClauseLocations[i].trim().split("\\s+");
			String strLocations = arrItemCL[0].replace(FOPJDKSOURCE, "\\")
					+ "\t";
			for (int j = 1; j < arrItemCL.length - 1; j++) {
				strLocations += arrItemCL[j] + "\t";
			}
			strLocations += arrClauseSources[i].split("\\s+")[0];
			mapJavaDocSource.put(strLocations, arrJavadocs[i]);
			ArrayList<String> listVar = getTagCodeValues(arrJavadocs[i]);
			for (int j = 0; j < listVar.size(); j++) {
				setVariableInCorpus.add(listVar.get(j));
			}
			mapVariablesToLine.put(strLocations, getVariableInJavadoc(listVar));

		}
		System.out.println(mapJavaDocSource.size() + " size javadoc");

		HashMap<String, String> mapTrainLoc = new HashMap<String, String>();
		for (int i = 0; i < arrMapOldNewTrain.length; i++) {
			String[] arrItems = arrMapOldNewTrain[i].trim().split("\\s+");
			String strItemContent = "";
			for (int j = 0; j < arrItems.length - 1; j++) {
				strItemContent += arrItems[j] + "\t";
			}
			mapTrainLoc.put(strItemContent.trim(),
					arrItems[arrItems.length - 1]);
		}

		HashMap<Integer, ArrayList<Integer>> mapCheck = new HashMap<Integer, ArrayList<Integer>>();

		for (int i = 1; i < arrMap.length; i++) {
			String[] arrItem = arrMap[i].split(",");
			ArrayList<Integer> listCheck;
			if (mapCheck.containsKey(new Integer(Integer.parseInt(arrItem[0])))) {
				listCheck = mapCheck.get(Integer.parseInt(arrItem[0]));
			} else {
				listCheck = new ArrayList<Integer>();
			}
			listCheck.add(Integer.parseInt(arrItem[1]));
			mapCheck.put(Integer.parseInt(arrItem[0]), listCheck);
		}
		int line = 0;
		int indexTest = 0;

		String strTrainJavaDocSource = "", strTrainVar = "";
		String strTestJavaDocSource = "", strTestVar = "";
		for (int i = 0; i < arrLocations.length; i++) {
			boolean isPair = checkContainPair(arrSources[i], arrTargets[i]);
			if (isPair) {
				line++;

				String strItemContent = "";
				arrLocations[i] = arrLocations[i].replace("D:\\data\\jdk8\\",
						"\\");
				String[] arrItems = arrLocations[i].trim().split("\\s+");
				for (int j = 0; j < arrItems.length - 2; j++) {
					strItemContent += arrItems[j] + "\t";
				}
				strItemContent += arrSources[i].split("\\s+")[0];
				ArrayList<Integer> listPriorLine = null;
				String strTrainLine = mapTrainLoc.get(strItemContent);
				if (strTrainLine != null) {
					listPriorLine = mapCheck.get(new Integer(strTrainLine));
				}
				if (listPriorLine != null) {

					strMapLocation += getSignatureFromLocation(arrLocations[i])
							+ "\t" + arrSources[i].split("\\s+")[0] + "\t"
							+ (i + 1) + "\n";
					// strLineOldNewMap += line + "\t" + (i + 1) + "\t";
					for (int j = 0; j < listPriorLine.size(); j++) {
						indexTest++;
						strTestDoc += arrSources[i].trim() + "\n";
						strTestImpl += arrTargets[i].trim() + "\n";
						strTestLocation += arrLocations[i].trim() + "\n";
						strLineOldNewMap += indexTest + "\t" + line + "\t"
								+ listPriorLine.get(j) + "\n";
						strTestJavaDocSource += mapJavaDocSource
								.get(getSignatureFromLocation(arrLocations[i])
										.replace(FOPJDKSOURCE, "\\")
										+ "\t"
										+ arrSources[i].split("\\s+")[0])
								+ "\n";
						strTestVar += mapVariablesToLine
								.get(getSignatureFromLocation(arrLocations[i])
										.replace(FOPJDKSOURCE, "\\")
										+ "\t"
										+ arrSources[i].split("\\s+")[0])
								+ "\n";
					}
					// strLineOldNewMap += "\n";
				}
				setExceptionInCorpus.add(arrSources[i].split("\\s+")[0]);
				strTrainLocation += arrLocations[i] + "\n";
				strTrainDoc += arrSources[i] + "\n";
				strTrainImpl += arrTargets[i] + "\n";
				strTrainVar += mapVariablesToLine.get(getSignatureFromLocation(
						arrLocations[i]).replace(FOPJDKSOURCE, "\\")
						+ "\t" + arrSources[i].split("\\s+")[0])
						+ "\n";
				strTrainJavaDocSource += mapJavaDocSource
						.get(getSignatureFromLocation(arrLocations[i]).replace(
								FOPJDKSOURCE, "\\")
								+ "\t" + arrSources[i].split("\\s+")[0])
						+ "\n";
			}
		}

		// FileIO.writeStringToFile(strMapLocation
		// ,fop_out+"map.old.locations.txt");
		FileIO.writeStringToFile(strTrainVar, fop_out + "train.variables.txt");
		FileIO.writeStringToFile(strTrainJavaDocSource, fop_out
				+ "train.javadoc.txt");
		FileIO.writeStringToFile(strTrainLocation, fop_out
				+ "train.locations.txt");
		FileIO.writeStringToFile(strTrainDoc, fop_out + "train.jd");
		FileIO.writeStringToFile(strTrainImpl, fop_out + "train.impl");
		FileIO.writeStringToFile(strTrainLocation, fop_out
				+ "tune.locations.txt");
		FileIO.writeStringToFile(strTrainDoc, fop_out + "tune.jd");
		FileIO.writeStringToFile(strTrainImpl, fop_out + "tune.impl");
		FileIO.writeStringToFile(strLineOldNewMap, fop_out + "test.mapped.txt");
		FileIO.writeStringToFile(strTestDoc, fop_out + "test.jd");
		FileIO.writeStringToFile(strTestImpl, fop_out + "test.impl");
		FileIO.writeStringToFile(strTestVar, fop_out + "test.variables.txt");
		FileIO.writeStringToFile(strTestLocation, fop_out
				+ "test.locations.txt");
		FileIO.writeStringToFile(strTestJavaDocSource, fop_out
				+ "test.javadoc.txt");
		FileIO.writeStringToFile(getStringOfSet(setVariableInCorpus), fop_out
				+ "setVariables.txt");
		FileIO.writeStringToFile(getStringOfSet(setExceptionInCorpus), fop_out
				+ "setExceptions.txt");
		FileIO.writeStringToFile(strTrainDoc, fop_out + "train.jd");
	}

	public static HashSet<String> getSetOfJavaKeyword() {
		setJavaKeywords = new HashSet<String>();
		setJavaKeywords.add("abstract");
		setJavaKeywords.add("continue");
		setJavaKeywords.add("for");
		setJavaKeywords.add("new");
		setJavaKeywords.add("switch");
		setJavaKeywords.add("assert");
		setJavaKeywords.add("default");
		setJavaKeywords.add("goto");
		setJavaKeywords.add("package");
		setJavaKeywords.add("synchronized");
		setJavaKeywords.add("boolean");
		setJavaKeywords.add("do");
		setJavaKeywords.add("if");
		setJavaKeywords.add("private");
		setJavaKeywords.add("this");
		setJavaKeywords.add("break");
		setJavaKeywords.add("double");
		setJavaKeywords.add("implements");
		setJavaKeywords.add("protected");
		setJavaKeywords.add("throw");
		setJavaKeywords.add("byte");
		setJavaKeywords.add("else");
		setJavaKeywords.add("import");
		setJavaKeywords.add("public");
		setJavaKeywords.add("throws");
		setJavaKeywords.add("case");
		setJavaKeywords.add("enum");
		setJavaKeywords.add("instanceof");
		setJavaKeywords.add("return");
		setJavaKeywords.add("transient");
		setJavaKeywords.add("catch");
		setJavaKeywords.add("extends");
		setJavaKeywords.add("int");
		setJavaKeywords.add("short");
		setJavaKeywords.add("try");
		setJavaKeywords.add("char");
		setJavaKeywords.add("final");
		setJavaKeywords.add("interface");
		setJavaKeywords.add("static");
		setJavaKeywords.add("void");
		setJavaKeywords.add("class");
		setJavaKeywords.add("finally");
		setJavaKeywords.add("long");
		setJavaKeywords.add("strictfp");
		setJavaKeywords.add("volatile");
		setJavaKeywords.add("const");
		setJavaKeywords.add("float");
		setJavaKeywords.add("native");
		setJavaKeywords.add("super");
		setJavaKeywords.add("while");
		return setJavaKeywords;
	}

	public static void getTrainTestFromSourceTargetLocationMultiProject() {
		String fop_in = "C:\\Fall2017Data\\github\\";
		String fop_out = "C:\\Fall2017Data\\outGithub\\";

		setJavaKeywords = getSetOfJavaKeyword();
		File foIn = new File(fop_in);
		File[] arrFolderProject = foIn.listFiles();

		FileIO.writeStringToFile("", fop_out + "train.variables.txt");
		FileIO.writeStringToFile("", fop_out + "train.javadoc.txt");
		FileIO.writeStringToFile("", fop_out + "train.locations.txt");
		FileIO.writeStringToFile("", fop_out + "train.jd");
		FileIO.writeStringToFile("", fop_out + "train.impl");
		FileIO.writeStringToFile("", fop_out + "tune.locations.txt");
		FileIO.writeStringToFile("", fop_out + "tune.jd");
		FileIO.writeStringToFile("", fop_out + "tune.impl");

		for (int j = 0; j < arrFolderProject.length; j++) {
			if (!arrFolderProject[j].isDirectory()) {
				continue;
			}
			try {
				String strTrainDoc = "", strTrainImpl = "", strTestDoc = "", strTestImpl = "", strTestLocation = "", strTrainLocation = "", strTestLineInOldCorpus = "";

				String[] arrLocations = FileIO.readStringFromFile(
						fop_in + arrFolderProject[j].getName() + "\\"
								+ "locations.txt").split("\n");
				String[] arrSources = FileIO.readStringFromFile(
						fop_in + arrFolderProject[j].getName() + "\\"
								+ "source.txt").split("\n");
				String[] arrTargets = FileIO.readStringFromFile(
						fop_in + arrFolderProject[j].getName() + "\\"
								+ "target.txt").split("\n");

				String[] arrClauseLocations = FileIO.readStringFromFile(
						fop_in + arrFolderProject[j].getName() + "\\"
								+ "clause-locations.txt").split("\n");
				String[] arrClauseSources = FileIO.readStringFromFile(
						fop_in + arrFolderProject[j].getName() + "\\"
								+ "clause-source.txt").split("\n");
				String[] arrJavadocs = FileIO.readStringFromFile(
						fop_in + arrFolderProject[j].getName() + "\\"
								+ "javadoc.txt").split("\n");

				HashMap<String, String> mapJavaDocSource = new HashMap<String, String>();
				HashMap<String, String> mapVariablesToLine = new HashMap<String, String>();
				HashSet<String> setVariableInCorpus = new HashSet<String>();
				HashSet<String> setExceptionInCorpus = new HashSet<String>();

				for (int i = 0; i < arrClauseLocations.length; i++) {
					String[] arrItemCL = arrClauseLocations[i].trim().split(
							"\\s+");
					String strLocations = arrItemCL[0].replace(FOPJDKSOURCE,
							"\\") + "\t";
					for (int k = 1; k < arrItemCL.length - 1; k++) {
						strLocations += arrItemCL[k] + "\t";
					}
					strLocations += arrClauseSources[i].split("\\s+")[0];
					mapJavaDocSource.put(strLocations, arrJavadocs[i]);
					ArrayList<String> listVar = getTagCodeValues(arrJavadocs[i]);
					for (int k = 0; k < listVar.size(); k++) {
						setVariableInCorpus.add(listVar.get(k));
					}
					mapVariablesToLine.put(strLocations,
							getVariableInJavadoc(listVar));

				}
				System.out.println(mapJavaDocSource.size() + " size javadoc");

				int line = 0;
				int indexTest = 0;

				String strTrainJavaDocSource = "", strTrainVar = "";
				String strTestJavaDocSource = "", strTestVar = "";
				for (int i = 0; i < arrLocations.length; i++) {
					boolean isPair = checkContainPair(arrSources[i],
							arrTargets[i]);
					if (isPair) {
						line++;

						String strItemContent = "";
						arrLocations[i] = arrLocations[i].replace(
								"C:\\githubWellMaintainedProject\\", "\\");
						String[] arrItems = arrLocations[i].trim()
								.split("\\s+");
						for (int k = 0; k < arrItems.length - 2; k++) {
							strItemContent += arrItems[k] + "\t";
						}
						strItemContent += arrSources[i].split("\\s+")[0];

						setExceptionInCorpus
								.add(arrSources[i].split("\\s+")[0]);
						strTrainLocation += arrLocations[i] + "\n";
						strTrainDoc += arrSources[i] + "\n";
						strTrainImpl += arrTargets[i] + "\n";
						strTrainVar += mapVariablesToLine
								.get(getSignatureFromLocation(arrLocations[i])
										.replace(FOPJDKSOURCE, "\\")
										+ "\t"
										+ arrSources[i].split("\\s+")[0])
								+ "\n";
						strTrainJavaDocSource += mapJavaDocSource
								.get(getSignatureFromLocation(arrLocations[i])
										.replace(FOPJDKSOURCE, "\\")
										+ "\t"
										+ arrSources[i].split("\\s+")[0])
								+ "\n";
					}
				}

				// FileIO.writeStringToFile(strMapLocation
				// ,fop_out+"map.old.locations.txt");
				FileIO.appendStringToFile(strTrainVar, fop_out
						+ "train.variables.txt");
				FileIO.appendStringToFile(strTrainJavaDocSource, fop_out
						+ "train.javadoc.txt");
				FileIO.appendStringToFile(strTrainLocation, fop_out
						+ "train.locations.txt");
				FileIO.appendStringToFile(strTrainDoc, fop_out + "train.jd");
				FileIO.appendStringToFile(strTrainImpl, fop_out + "train.impl");
				FileIO.appendStringToFile(strTrainLocation, fop_out
						+ "tune.locations.txt");
				FileIO.appendStringToFile(strTrainDoc, fop_out + "tune.jd");
				FileIO.appendStringToFile(strTrainImpl, fop_out + "tune.impl");

			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

	}

	public static void splitTrainTest(String fopJDK) {
		String fop_library = fopJDK;

		String fn_location = "locations.txt";
		String fn_source = "source.txt";
		String fn_target = "target.txt";

		String[] arrLocations = FileIO
				.readStringFromFile(fop_library + fn_location).trim()
				.split("\n");
		String[] arrSources = FileIO
				.readStringFromFile(fop_library + fn_source).trim().split("\n");
		String[] arrTargets = FileIO
				.readStringFromFile(fop_library + fn_target).trim().split("\n");
		HashSet<String> setMethods = new HashSet<String>();
		HashSet<String> setNotExistThrows = new HashSet<String>();

		String fopCorpus = fop_library + "corpusMissingJd\\";
		String fopHaveThrow = fop_library + "haveThrowInImplInJd\\";
		

		File dirCorpus = new File(fopCorpus);
		File dirHaveThrow = new File(fopHaveThrow);
		if (!dirCorpus.isDirectory()) {
			dirCorpus.mkdir();
		}

		if (!dirHaveThrow.isDirectory()) {
			dirHaveThrow.mkdir();
		}

		StringBuilder sbCorpusLocation, sbCorpusSource, sbCorpusTarget;
		StringBuilder sbHaveThrowLocation, sbHaveThrowSource, sbHaveThrowTarget;
		sbCorpusLocation = new StringBuilder();
		sbCorpusSource = new StringBuilder();
		sbCorpusTarget = new StringBuilder();
		sbHaveThrowLocation = new StringBuilder();
		sbHaveThrowSource = new StringBuilder();
		sbHaveThrowTarget = new StringBuilder();
		for (int i = 0; i < arrTargets.length; i++) {
			System.out.println(i);
			String itemSourceStr = arrSources[i].trim();
			String itemTargetStr = arrTargets[i].trim();
			String[] arrWordSources = itemSourceStr.split("\\s+");
			//String[] arrWordTargets = itemTargetStr.split("\\s+");

			if ((arrWordSources.length == 1 || (arrWordSources.length == 2 && arrWordSources[1]
					.equals("null")))) {
				setNotExistThrows.add(arrLocations[i].trim());
				sbCorpusLocation.append(arrLocations[i].trim() + "\n");
				sbCorpusSource.append(itemSourceStr + "\n");
				sbCorpusTarget.append(itemTargetStr + "\n");
				// System.out.println(itemTargetStr + "\t" + itemSourceStr);
			} else {
				sbHaveThrowLocation.append(arrLocations[i].trim() + "\n");
				sbHaveThrowSource.append(itemSourceStr + "\n");
				sbHaveThrowTarget.append(itemTargetStr + "\n");
				// System.out.println("have parallel "+itemTargetStr + "\t"
				// + itemSourceStr);

				setMethods.add(arrLocations[i].trim());

				// countMethodNotHaveJdSpecs++;
			}
		}

		FileIO.writeStringToFile(sbCorpusLocation.toString(), fopCorpus
				+ fn_location);
		FileIO.writeStringToFile(sbCorpusSource.toString(), fopCorpus
				+ fn_source);
		FileIO.writeStringToFile(sbCorpusTarget.toString(), fopCorpus
				+ fn_target);
		FileIO.writeStringToFile(sbHaveThrowLocation.toString(), fopHaveThrow
				+ fn_location);
		FileIO.writeStringToFile(sbHaveThrowSource.toString(), fopHaveThrow
				+ fn_source);
		FileIO.writeStringToFile(sbHaveThrowTarget.toString(), fopHaveThrow
				+ fn_target);
		
		FileIO.writeStringToFile(sbHaveThrowSource.toString(), fopHaveThrow
				+ "train.jd");
		FileIO.writeStringToFile(sbHaveThrowTarget.toString(), fopHaveThrow
				+ "train.impl");
				
		FileIO.writeStringToFile(sbHaveThrowSource.toString(), fopHaveThrow
				+ "tune.jd");
		FileIO.writeStringToFile(sbHaveThrowTarget.toString(), fopHaveThrow
				+ "tune.impl");
		String fopRefs=fopHaveThrow+"\\refs\\";
		String fopTestRefs=fopHaveThrow+"\\refs\\test\\";
		String fopTuneRefs=fopHaveThrow+"\\refs\\tune\\";
		File dirRefs = new File(fopRefs);
		File dirTuneRefs = new File(fopTuneRefs);
		File dirTestRefs = new File(fopTestRefs);
		if(!dirRefs.isDirectory()){
			dirRefs.mkdir();			
		}
		if(!dirTuneRefs.isDirectory()){
			dirTuneRefs.mkdir();			
		}
		if(!dirTestRefs.isDirectory()){
			dirTestRefs.mkdir();			
		}
		FileIO.writeStringToFile(sbHaveThrowTarget.toString(), dirTuneRefs.getAbsolutePath()+"\\ref0");
		FileIO.writeStringToFile("", dirTestRefs.getAbsolutePath()+"\\ref0");
		
		
		
		System.out.println(setNotExistThrows.size() + "\t" + setMethods.size());
	}

	// select 50% of each libraries and test
	public static void createRandomTest() {
		String fop = "C:\\ICSE2018_ex2\\jdk_all_9\\";
		String fopRandom = fop + "randomTest\\";

		File fileRandom = new File(fopRandom);
		if (!fileRandom.isDirectory()) {
			fileRandom.mkdir();
		}

		HashMap<String, ArrayList<Integer>> setLibrariesChoose = new HashMap<String, ArrayList<Integer>>();
		setLibrariesChoose.put("\\javax\\xml\\", new ArrayList<Integer>());
		setLibrariesChoose.put("\\javax\\management\\",
				new ArrayList<Integer>());
		setLibrariesChoose.put("\\java\\util\\", new ArrayList<Integer>());
		setLibrariesChoose.put("\\java\\security\\", new ArrayList<Integer>());
		setLibrariesChoose.put("\\javax\\sql\\", new ArrayList<Integer>());
		setLibrariesChoose.put("\\java\\lang\\", new ArrayList<Integer>());

		String[] arrTrainLoc = FileIO
				.readStringFromFile(fop + "train.locations.txt").trim()
				.split("\n");
		String[] arrTrainJd = FileIO.readStringFromFile(fop + "train.jd")
				.trim().split("\n");
		String[] arrTrainImpl = FileIO.readStringFromFile(fop + "train.impl")
				.trim().split("\n");

		String[] arrTestLoc = FileIO
				.readStringFromFile(fop + "test.locations.txt").trim()
				.split("\n");
		String[] arrTestJd = FileIO.readStringFromFile(fop + "test.jd").trim()
				.split("\n");
		String[] arrTestImpl = FileIO.readStringFromFile(fop + "test.impl")
				.trim().split("\n");

		HashSet<String> setOldTestLoc = new HashSet<String>();

		for (int i = 0; i < arrTestJd.length; i++) {
			String exceptionName = arrTestJd[i].trim().split("\\s+")[0];
			String strSignatureException = getSignatureFromLocation(arrTestLoc[i])
					+ "\t" + exceptionName;
			setOldTestLoc.add(strSignatureException);

		}

		for (int i = 0; i < arrTrainJd.length; i++) {
			String exceptionName = arrTrainJd[i].trim().split("\\s+")[0];
			String strSignatureException = getSignatureFromLocation(arrTrainLoc[i]);
			if (!setOldTestLoc.contains(strSignatureException)) {
				for (String strLibPrefix : setLibrariesChoose.keySet()) {
					if (strSignatureException.startsWith(strLibPrefix)) {
						System.out.println(strSignatureException + "\t"
								+ strLibPrefix);
						setLibrariesChoose.get(strLibPrefix)
								.add(new Integer(i));
					}
				}
			}
		}

		String strNewTestLoc = "", strNewTestJd = "", strNewTestImpl = "", strNewTestLine = "", strNewTestSignature = "";

		for (String strLibPrefix : setLibrariesChoose.keySet()) {
			ArrayList<Integer> lstPairs = setLibrariesChoose.get(strLibPrefix);
			int numSpecsRequired = lstPairs.size() / 2;
			HashSet<Integer> setLineSelectPerLib = new HashSet<Integer>();
			while (setLineSelectPerLib.size() < numSpecsRequired) {
				int randomLine = ThreadLocalRandom.current().nextInt(0,
						lstPairs.size());
				setLineSelectPerLib.add(new Integer(randomLine));
			}
			String strLineTestSe = "";
			for (Integer item : setLineSelectPerLib) {
				int lineInTrain = lstPairs.get(item.intValue());
				strLineTestSe += lineInTrain + " ";
				strNewTestLoc += arrTrainLoc[lineInTrain] + "\n";
				strNewTestJd += arrTrainJd[lineInTrain] + "\n";
				strNewTestImpl += arrTrainImpl[lineInTrain] + "\n";
				strNewTestLine += lineInTrain + "\n";
				strNewTestSignature += getSignatureRemovePathFromLocation(arrTrainLoc[lineInTrain])
						+ "\t"
						+ arrTrainJd[lineInTrain].split("\\s+")[0]
						+ "\n";
			}

			System.out.println(strLibPrefix + " in new test: " + strLineTestSe);
			System.out.println(strLibPrefix + " in train: "
					+ lstPairs.toString());
			System.out.println("select " + numSpecsRequired + "/"
					+ lstPairs.size() + " for lib" + strLibPrefix);

			// for(int i=0;i<lstPairs.size();i++){
			// }
		}
		FileIO.writeStringToFile(strNewTestLoc, fopRandom
				+ "test.locations.txt");
		FileIO.writeStringToFile(strNewTestJd, fopRandom + "test.jd");
		FileIO.writeStringToFile(strNewTestImpl, fopRandom + "test.impl");
		FileIO.writeStringToFile(strNewTestLine, fopRandom
				+ "test.lineInTrain.txt");
		FileIO.writeStringToFile(strNewTestSignature, fopRandom
				+ "test.signature.txt");

	}

	public static void removeExceptionNameInTestDocAndImpl() {
		String fop = "C:\\ICSE2018_ex2\\jdk_all_9\\randomTest_50Percent\\";
		String fnTestJd = "test.jd";
		String fnTestImpl = "test.impl";
		String[] arrTestJd = FileIO.readStringFromFile(fop + fnTestJd).trim()
				.split("\n");
		String strNewTestJd = "";
		for (int i = 0; i < arrTestJd.length; i++) {
			String[] arrItem = arrTestJd[i].trim().split("\\s+");
			String item = "";
			for (int j = 1; j < arrItem.length; j++) {
				item += arrItem[j] + " ";
			}
			strNewTestJd += item.trim() + "\n";
		}
		FileIO.writeStringToFile(strNewTestJd, fop + "test.onlyText.jd");

		String[] arrTestImpl = FileIO.readStringFromFile(fop + fnTestImpl)
				.trim().split("\n");
		String strNewTestImpl = "";
		for (int i = 0; i < arrTestImpl.length; i++) {
			String[] arrItem = arrTestImpl[i].trim().split("\\s+");
			String item = "";
			for (int j = 1; j < arrItem.length; j++) {
				item += arrItem[j] + " ";
			}
			strNewTestImpl += item.trim() + "\n";
		}
		FileIO.writeStringToFile(strNewTestImpl, fop + "test.onlyText.impl");

	}

	public static void main(String[] args) {
		String fopJDK = "InlineProcedureCorpus\\";
		splitTrainTest(fopJDK);
		// getTrainTestFromSourceTargetLocation();
		// createRandomTest();
		// removeExceptionNameInTestDocAndImpl();
		// getTrainTestFromSourceTargetLocationMultiProject();
	}
}
