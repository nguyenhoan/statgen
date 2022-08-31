package eval;

import java.util.HashSet;

import utils.FileIO;

public class RemoveIncorrectPhrase {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop="dataStatType\\tbl_cases\\268\\in\\";
		String fOut="dataStatType\\tbl_cases\\268\\out\\";
		String fop3="dataStatType\\tbl_cases\\268\\";
		String fopInput1=fop;
		String fnInput1="lo-hier.msd2-bidirectional-fe";
		//String fopInput2=fOut";
		String fnInput2="phrase-table";
		String fopOutput=fOut;
		
		handleParameterNameAndExceptionPhrase(fopInput1,fop3+"setVariables.txt",fop3+"setExceptions.txt", fnInput1, fopOutput);
		handleParameterNameAndExceptionPhrase(fopInput1,fop3+"setVariables.txt",fop3+"setExceptions.txt", fnInput2, fopOutput);
		
	}
	
	public static void handleBePhrase(String fopInput,String fnInput,String fopOutput){
		String[] arrInput1=FileIO.readStringFromFile(fopInput+fnInput).trim().split("\n");
		String strContent="";
		for(int i=0;i<arrInput1.length;i++){
			String[] arrItem1=arrInput1[i].split("\\|\\|\\|");
			if(arrItem1.length>=2){
				String[] arrSmallItem1=arrItem1[0].trim().split("\\s+");
				String[] arrSmallItem2=arrItem1[1].trim().split("\\s+");
				boolean isValidPhrase=true;
				if(arrSmallItem1[0].equals("be")&&arrSmallItem1.length==1){
					if(!(arrSmallItem2[0].endsWith("Exception")&&arrSmallItem2.length==1)){
						isValidPhrase=false;
					}
				}
				if(isValidPhrase){
					strContent+=arrInput1[i]+"\n";
				}
			}
			//System.out.println(arrInput1[i]);
		}
		FileIO.writeStringToFile(strContent,fopOutput+fnInput);
	}
	
	public static void handleExceptionPhrase(String fopInput,String fnInput,String fopOutput){
		String[] arrInput1=FileIO.readStringFromFile(fopInput+fnInput).trim().split("\n");
		String strContent="";
		for(int i=0;i<arrInput1.length;i++){
			String[] arrItem1=arrInput1[i].split("\\|\\|\\|");
			if(arrItem1.length>=2){
				System.out.println(arrItem1[0]+"\t"+i);
				
				String[] arrSmallItem1=arrItem1[0].trim().split("\\s+");
				String[] arrSmallItem2=arrItem1[1].trim().split("\\s+");
				boolean isValidPhrase=true;
				if(arrSmallItem1[0].endsWith("Exception")&&arrSmallItem1.length==1){
					if(!(arrSmallItem2[0].endsWith("Exception")&&arrSmallItem2.length==1)){
						isValidPhrase=false;
					}
				}
				if(isValidPhrase){
					strContent+=arrInput1[i]+"\n";
				}
			}
			//System.out.println(arrInput1[i]);
		}
		FileIO.writeStringToFile(strContent,fopOutput+fnInput);
	}
	

	public static void handleOneVarName(String fopInput,String fnVar,String fpSetException,String fnInput,String fopOutput){
		String[] arrInput1=FileIO.readStringFromFile(fopInput+fnInput).trim().split("\n");
		String strContent="";
		String[] arrExceptions=FileIO.readStringFromFile(fpSetException).trim().split("\n");
		HashSet<String> setVarEx=new HashSet<String>();
//		for(int i=0;i<arrExceptions.length;i++){
//			setVarEx.add(arrExceptions[i]);
//		}
		
		//for(int i=0;i<arrVariables.length;i++){
		setVarEx.add(fnVar);
		//}
		for(int i=0;i<arrInput1.length;i++){
			String[] arrItem1=arrInput1[i].split("\\|\\|\\|");
			if(i%1000==0){
				System.out.println(i+"\t"+i);
				
			}
			
			if(arrItem1.length>=2){
			//	System.out.println(arrItem1[0]+"\t"+i);
				
				//check validation of these phase
				boolean isValidPhrase=true;
				if(setVarEx.contains(arrItem1[0].trim())){
					//System.out.println(arrItem1[0].trim());
//					String[] arrSmallItem1=arrItem1[0].trim().split("\\s+");
//					String[] arrSmallItem2=arrItem1[1].trim().split("\\s+");
					
					if(!arrItem1[0].equals(arrItem1[1])){
						isValidPhrase=false;
					}
					
				}
				if(isValidPhrase){
					strContent+=arrInput1[i]+"\n";
				}
				
			}
			//System.out.println(arrInput1[i]);
		}
		FileIO.writeStringToFile(strContent,fopOutput+fnInput);
	}
	
	public static void handleParameterNameAndExceptionPhrase(String fopInput,String fpSetVar,String fpSetException,String fnInput,String fopOutput){
		String[] arrInput1=FileIO.readStringFromFile(fopInput+fnInput).trim().split("\n");
		String strContent="";
		String[] arrExceptions=FileIO.readStringFromFile(fpSetException).trim().split("\n");
		String[] arrVariables=FileIO.readStringFromFile(fpSetVar).trim().split("\n");
		HashSet<String> setVarEx=new HashSet<String>();
		for(int i=0;i<arrExceptions.length;i++){
			setVarEx.add(arrExceptions[i]);
		}
		
		for(int i=0;i<arrVariables.length;i++){
			setVarEx.add(arrVariables[i].trim());
			System.out.println(arrVariables[i].trim()+"aaa");
		}
		for(int i=0;i<arrInput1.length;i++){
			String[] arrItem1=arrInput1[i].split("\\|\\|\\|");
			if(i%1000==0){
				System.out.println(i+"\t"+i);
				
			}
			
			if(arrItem1.length>=2){
			//	System.out.println(arrItem1[0]+"\t"+i);
				
				//check validation of these phase
				boolean isValidPhrase=true;
				if(setVarEx.contains(arrItem1[0].trim())){
					//System.out.println(arrItem1[0].trim());
//					String[] arrSmallItem1=arrItem1[0].trim().split("\\s+");
//					String[] arrSmallItem2=arrItem1[1].trim().split("\\s+");
					
					if(!arrItem1[0].equals(arrItem1[1])){
						isValidPhrase=false;
						System.out.println(arrInput1[i]);
					}
					
				}
				if(isValidPhrase){
					strContent+=arrInput1[i]+"\n";
				}
				
			}
			//System.out.println(arrInput1[i]);
		}
		FileIO.writeStringToFile(strContent,fopOutput+fnInput);
	}

}
