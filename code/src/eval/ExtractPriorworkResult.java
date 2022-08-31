package eval;

import java.util.ArrayList;
import java.util.HashMap;

import utils.FileIO;

public class ExtractPriorworkResult {

	public static String fopExp2="priorWorks\\";
	
	
	public static void checkOracle(String[] args) {
		String strExcel2=FileIO.readStringFromFile(fopExp2+"Experiment_II_Data_check_csv_2.csv");
		String[] arrLines=strExcel2.trim().split("\n");
		// TODO Auto-generated method stub
		HashMap<String,ArrayList<Integer>> mapLines=new HashMap<String,ArrayList<Integer>>();
		for(int i=0;i<arrLines.length;i++){
			String[] arrItem=arrLines[i].trim().split(",");
			ArrayList<Integer> lst=mapLines.get(arrItem[0]);
			if(lst==null){
				lst=new ArrayList<Integer>();
				lst.add(i);
				mapLines.put(arrItem[0], lst);
			} else{
				lst.add(i);
				//mapLines.put(arrItem[0], lst);
			}
		}
		
		String strFinalResult="";
		for(int i=1;i<arrLines.length;i++){
			String[] arrItem=arrLines[i].trim().split(",");
			if(i==51||i==12) continue;
			if(mapLines.containsKey(arrItem[0])){
				ArrayList<Integer> lst=mapLines.get(arrItem[0]);
				int currentLine=i;
				for(int j=0;j<lst.size();j++){
					currentLine=lst.get(j);
					String[] arrI2=arrLines[currentLine].trim().split(",");
					if(arrI2[8].equals("Consistent")){
						break;
					}
				}
				strFinalResult+=arrLines[currentLine]+"\n";
				i+=lst.size()-1;
			}			
		}
		FileIO.writeStringToFile(strFinalResult, fopExp2+"oracle_result.csv");
		

	}
	
	public static void extractConsByOldWays(){
		String strExcel2=FileIO.readStringFromFile(fopExp2+"Experiment_II_Data_check_csv_2.csv");
		String[] arrLines=strExcel2.trim().split("\n");
		// TODO Auto-generated method stub
		HashMap<String,ArrayList<Integer>> mapLines=new HashMap<String,ArrayList<Integer>>();
		for(int i=0;i<arrLines.length;i++){
			String[] arrItem=arrLines[i].trim().split(",");
			ArrayList<Integer> lst=mapLines.get(arrItem[0]);
			if(lst==null){
				lst=new ArrayList<Integer>();
				lst.add(i);
				mapLines.put(arrItem[0], lst);
			} else{
				lst.add(i);
				//mapLines.put(arrItem[0], lst);
			}
		}
		
		String strFinalResult="";
		for(int i=1;i<arrLines.length;i++){
			String[] arrItem=arrLines[i].trim().split(",");
			if(mapLines.containsKey(arrItem[0])){
				ArrayList<Integer> lst=mapLines.get(arrItem[0]);
				boolean isIncons=false;
				int currentLine=i;
				for(int j=0;j<lst.size();j++){
					currentLine=lst.get(j);
					String[] arrI2=arrLines[currentLine].trim().split(",");
					//prior work oracle
					if(arrI2[7].equals("Consistent")){
						//isIncons=true;						
						break;
					}
				}
				strFinalResult+=arrLines[currentLine]+"\n";
				i+=lst.size()-1;
			}			
		}
		FileIO.writeStringToFile(strFinalResult, fopExp2+"zhou_result.csv");
	}
	
	public static void main(String[] args) {
		checkOracle(args);
		

	}
	
	

}
