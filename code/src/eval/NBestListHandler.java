package eval;

import java.io.File;
import java.util.Scanner;

import org.eclipse.jdt.core.dom.Expression;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;

import smtsolver.ExpressionExtractorParser;
import utils.FileIO;

public class NBestListHandler {

	public String fop="randomTest_50Percent\\clauses\\";
	private void updateByNBest(String fnFileTrans,String fnInput,String fnOutput,String fnLine,boolean isCreateNewDir){
		String fopNewDir=fop+"splitNBest\\";
		File fNewDir=new File(fopNewDir);
		ExpressionExtractorParser eep=new ExpressionExtractorParser();
		
		if(isCreateNewDir){
			if(!fNewDir.isDirectory()){
				fNewDir.mkdir();
			}
			String[] arrNewDir=FileIO.readStringFromFile(fop+fnFileTrans).trim().split("\n");
			int line=0;
			for(int i=0;i<arrNewDir.length;i++){
				
				String strResult="";
				String fnLineName=line+".txt";
				//System.out.println(arrNewDir[i].trim().startsWith(line+"")+" "+line);
				while(i<arrNewDir.length&&arrNewDir[i].trim().startsWith(""+line+"")){
					strResult+=arrNewDir[i].trim()+"\n";
					
					System.out.println(i);
					i++;
				}
				System.out.println("out");
				line++;
				FileIO.writeStringToFile(strResult,fopNewDir+fnLineName);
				//System.out.println(i+"\t"+arrNewDir[i]);
			}
		}
		
		String[] arrTest= FileIO.readStringFromFile(fop+fnInput).trim().split("\n");
		String strResult="";
		String strLine="";
		System.out.println(arrTest.length);
		Context ctx=new Context();
		for(int i=0;i<arrTest.length;i++){
			String[] arrCandidate=FileIO.readStringFromFile(fopNewDir+i+".txt").split("\n");
			boolean isParsable=false;
			int line=0;
			Expression expJava=null;
			Expr expZ3=null;
			String exceptionName=arrTest[i].trim().replace(eep.removeException(arrTest[i].trim()),"").trim();
			System.out.println(fopNewDir+i+".txt"+"\nCandidate: "+arrCandidate.length);
			if(arrCandidate.length>=1&&!arrCandidate[0].trim().isEmpty()){
				while(line<arrCandidate.length&&!isParsable){
					
					String item=arrCandidate[line].trim().split("\\|\\|\\|")[1].trim();
					
					String candidateItem=arrTest[i].trim().split("\\s+")[0].endsWith("Exception")?eep.removeException(item):item;
					//System.out.println(candidateItem+"\t"+line);
					expJava=eep.getExpression(candidateItem);
//					if(i+1==582){
//						System.out.println(candidateItem);
//						Scanner sc=new Scanner(System.in);
//						sc.nextLine();
//					}
//					System.out.println(expJava.toString());
					if(expJava!=null){
						isParsable=true;
						break;
//						try{
//							expZ3=eep.handleExpression(ctx, expJava, true);
//							if(expZ3!=null){
//								isParsable=true;
//								break;
//							}
//						}catch(Exception ex){
//							ex.printStackTrace();
//						}
						
						
					}
					line++;
				}
			}
			
			if(isParsable){
				strResult+=(arrTest[i].trim().split("\\s+")[0].endsWith("Exception")? arrTest[i].trim().split("\\s+")[0]+" "+expJava.toString():(expJava.toString()))+"\n";
				strLine+=(line+1)+"\n";
			} else{
				strResult+="CANNOT_PARSE"+"\n";
				strLine+="CANNOT_PARSE"+"\n";
			}
			
			System.out.println(i+" "+isParsable);
		}
		ctx.close();
		
		FileIO.writeStringToFile(strResult,fop+fnOutput);
		FileIO.writeStringToFile(strLine,fop+fnLine);
		
		
	}
	public static void main(String[] args) {
		NBestListHandler nblh=new NBestListHandler();
		nblh.updateByNBest("test.tune.baseline.100best", "test.tune.baseline.trans", "test.tune.baseline_normalize.txt","test.tune.baseline_line.txt", false);
	}

}
