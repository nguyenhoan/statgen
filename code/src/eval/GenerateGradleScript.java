package eval;

import java.io.File;

import utils.FileIO;

public class GenerateGradleScript {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String fop="C:\\githubWellMaintainedProject\\";
		String fopOut="C:\\Fall2017Data\\";
		File dirProjects=new File(fop);
		File[] arrProjects=dirProjects.listFiles();
		String strScript="";
		int index=0;
		for(int i=0;i<arrProjects.length;i++){
			File[] arrItems=arrProjects[i].listFiles();
			boolean isGradle=false;
			strScript=arrProjects[i].getName()+"\t"+arrProjects[i].getAbsolutePath()+"\\\n";
			FileIO.writeStringToFile(strScript, fopOut+"github\\listGithub_"+i+".txt");
//			for(int j=0;j<arrItems.length;j++){
//				if(arrItems[j].isFile()&&arrItems[j].getName().startsWith("gradle")){
//					isGradle=true;
//					break;
//				}
//			}
//			if(isGradle){
//				index++;
//				strScript+="cd "+arrProjects[i].getAbsolutePath()+"\n";
//				strScript+="gradlew.bat\n";
//			}
			
			
		}
		strScript+="pause\n";
		
		
	}

}
