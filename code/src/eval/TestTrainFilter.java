package eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import utils.FileIO;

public class TestTrainFilter {

	public static String getAPISignature(String inputLocation) {
		String arrItem[] = inputLocation.trim().split("\\s+");
		String strResult = "";
		strResult += arrItem[1] + "." + arrItem[2] + "." + arrItem[3]
				+ arrItem[4];
		for (int i = 5; i < arrItem.length - 3; i++) {
			strResult += arrItem[i];
			if (i != arrItem.length - 4) {
				strResult += ",";
			}
		}
		strResult+=")";
		return strResult;
	}
	
	public void compareSignature(){
		String fop_location = "Replication-package\\Replication-package\\SpecTransOutput\\jdk_core\\";
		String[] arrLocations = FileIO
				.readStringFromFile(fop_location + "locations.txt").trim()
				.split("\n");
		HashMap<String,String> mapLocations=new HashMap<String,String>();
		for(int i=0;i<arrLocations.length;i++){
			String apiSignature=getAPISignature(arrLocations[i]);
			mapLocations.put(apiSignature,i+1+"");
		}
		ArrayList<String[]> listPriorWorkExceptions=getPriorWorkExperimentSignature();
		int count=0;
		for(int i=0;i<listPriorWorkExceptions.size();i++){
			String[] arrItem=listPriorWorkExceptions.get(i);
			if(mapLocations.containsKey(arrItem[0])){
				System.out.println(mapLocations.get(arrItem[0])+"\t"+arrItem[2]+"\t"+arrItem[0]+"\t"+arrItem[1]);
				count++;
			};
		}
		System.out.println(count);
	}
	
	public ArrayList<String[]> getPriorWorkExperimentSignature(){
		ArrayList<String[]> listSigs=new ArrayList<String[]>();
		String fop_csvData="C:\\Users\\pdhung\\Desktop\\hungData\\research\\ImportantProjects\\SpecMiningProject\\SummerWork2017\\ICSE2018Data\\icse-2017-priorWorks\\Replication-package\\Replication-package\\";
		String[] arrLines=FileIO.readStringFromFile(fop_csvData+"ex2.csv").trim().split("\n");
		for(int i=0;i<arrLines.length;i++){
			String[] arrContentPerLine=arrLines[i].split(",");
			String apiName=arrContentPerLine[1].replace("AAAABBBBAAAA", ",").replaceFirst(".java.", ".");
			String calleeName=arrContentPerLine[0].replace("AAAABBBBAAAA", ",");
			String exceptionName=arrContentPerLine[6].replace("AAAABBBBAAAA", ",").split("\\]\\[\\[")[0].replace("[", "");
			String[] output=new String[3];
			if(apiName.equals(calleeName)){
				output[0]=apiName;
				output[1]=exceptionName;
				output[2]=(i+1)+"";
				listSigs.add(output);
			}
			
		}
		System.out.println(listSigs.size());
		return listSigs;
	}

	public static void main(String[] args) {
		TestTrainFilter ttf=new TestTrainFilter();
		ttf.compareSignature();
	//	ttf.getPriorWorkExperimentSignature();

	}

}
