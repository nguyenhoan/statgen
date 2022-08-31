package eval;

import java.util.ArrayList;
import java.util.HashSet;

import utils.FileIO;

public class OptimizeImplClauseAfterTranslation {

	//this class will optimize the content of conditions.txt by:
	//- tokenize method invocation in the text
	//- remove similar words like When.
	
	public static String correctTranslatedResultByRule(String input){
		String strResult=input.trim();
		if(strResult.endsWith("<")){
			strResult=strResult.substring(0, strResult.length()-1);
		}
		return strResult;
	}
	
	public static String normalizeOpenCloseBracket(String input){
		String strResult=input;
		//traverse position in open or close bracket, remove the one that cannot be a valid bracket
		
		//normalize open bracket.
		boolean needCheckOpen=true;
		String strCheck=input;
		int numOpenBracket=0,numCloseBracket=0;
		for(int i=0;i<strCheck.length();i++){
			if((strCheck.charAt(i)+"").equals("(")){
				numOpenBracket++;					
			} else if((strCheck.charAt(i)+"").equals(")")){
				numCloseBracket++;
			}
		}
		if(numOpenBracket>numCloseBracket){
			//find mismatch open bracket
			try{
				ArrayList<Integer> setErrorOpenBracket=new ArrayList<Integer>();
				for(int i=0;i<strCheck.length();i++){
					if((strCheck.charAt(i)+"").equals("(")){
						int checkOpen=1;
						for(int j=i+1;j<strCheck.length();j++){
							if((strCheck.charAt(j)+"").equals("(")){
								checkOpen++;
							} else if((strCheck.charAt(j)+"").equals(")")){
								checkOpen--;
							}
							if(checkOpen==0){
								break;
							}
						}
						if(checkOpen>0){
							setErrorOpenBracket.add(i);
						}
					}
				}
				int indexRemove=0;
				for(Integer pos:setErrorOpenBracket){
					strCheck=strCheck.substring(0, pos-indexRemove) + strCheck.substring(pos-indexRemove+1);
					indexRemove++;
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
			
			
		} else if(numOpenBracket<numCloseBracket){
			//find mismatch open bracket
			try{
				ArrayList<Integer> setErrorCloseBracket=new ArrayList<Integer>();
				for(int i=strCheck.length()-1;i>=0;i--){
					if((strCheck.charAt(i)+"").equals(")")){
						int checkOpen=1;
						int lastIndex=strCheck.length();
						for(int j=i-1;j>=0;j--){
							if((strCheck.charAt(j)+"").equals("(")){
								checkOpen--;
							} else if((strCheck.charAt(j)+"").equals(")")){
								checkOpen++;
							}
							if(checkOpen==0){
								break;
							}
						}
						if(checkOpen>0){
							setErrorCloseBracket.add(i);
						}
					}
				}
				
				int indexRemove=0;
				for(Integer pos:setErrorCloseBracket){
					strCheck=strCheck.substring(0, pos-indexRemove) + strCheck.substring(pos-indexRemove+1);
					
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
		}
		return strCheck;
	}
	
	public void normalizeImplConditions(String fp_input,String fp_output){
		String[] arrImplConditions=FileIO.readStringFromFile(fp_input).trim().split("\n");
		String strTotal="";
		for(int i=0;i<arrImplConditions.length;i++){
			String strResult=arrImplConditions[i];
			strResult=correctTranslatedResultByRule(strResult);
			strResult=normalizeOpenCloseBracket(strResult);
//			strResult=step3HandleBeNotNull(strResult);
//			strResult=step4BeInRange(strResult);
//			strResult=step4GreaterLessThanEqual(strResult);
			strTotal+=strResult+"\n";
		}
		FileIO.writeStringToFile(strTotal, fp_output);
		
	}
	
	public static void main(String[] args) {
		OptimizeImplClauseAfterTranslation oicbt=new OptimizeImplClauseAfterTranslation();
		String fop="Replication-package\\Replication-package\\SpecTransOutput\\jdk_all_5\\out\\translate_2\\";
		oicbt.normalizeImplConditions(fop+"\\test.tune.baseline.trans", fop+"conditions.txt");

	}

}
