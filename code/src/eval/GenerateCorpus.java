package eval;

import parsing.ParallelCorpusParser;
import utils.FileIO;

public class GenerateCorpus {

	public static void getParallelCorpus() {
		String fop = "C:\\Fall2017Data\\github\\";
		String strResult="";
		ParallelCorpusParser pcp = null;
		
		for(int i=325;i<515;i++){
			String strLog=i+"\tOK";
			try{
				System.out.println("Before "+strLog);
				pcp = new ParallelCorpusParser(fop + "listGithub_"+i+".txt");
				pcp.generateParallelCorpus(fop, true, false, true);
				System.out.println(strLog);
			}catch(Exception ex){
				strLog=i+"\t"+ex.getMessage();
				System.out.println(i+"\t"+ex.getMessage());
			}
			System.gc();
			pcp=null;
			strResult+=strLog+"\n";
		}

		FileIO.writeStringToFile(strResult, fop+"log.txt");
		
	}

	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	//	 getParallelCorpus();
		Object a=null;
		if(a instanceof GenerateCorpus){
			System.out.println("ahc");
		} 
	}

}
