package eval;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import utils.FileIO;
import utils.JavaASTUtil;

public class OptimizeDocClauseBeforeTranslation {

	
	public String step1RemoveWhenWords(String input){
		String strResult=input;
		strResult=input.replaceAll(" When "," ");		
		return strResult;
	}
	
	public String step2TokenizeLengthFunction(String input){
		String strResult=input;
		strResult=input.replaceAll("\\.length\\(\\)"," . length \\( \\) ");
		strResult=strResult.replaceAll("length\\(\\)"," length \\( \\)");
		return strResult;
	}
	
	public String step3HandleBeNotNull(String input){
		String strResult=input;
		strResult=input.replaceAll("be not null","not equal null");
		return strResult;
	}
	public String step4BeInRange(String input){
		String strResult=input;
		if(input.contains("be in the range")&&input.split("be in the range").length==2){
			String strValue=input.split("be in the range")[1];
			strResult= input.split("be in the range")[0]+" greater than or equal to "+strValue.split("-")[0];
			strResult+=" and "+input.split("be in the range")[0]+" less than or equal to "+strValue.split("-")[1];
		} else if(input.contains("be not in the range")&&input.split("be not in the range").length==2){
			String strValue=input.split("be not in the range")[1];
			strResult= input.split("be not in the range")[0]+" less than "+strValue.split("-")[0];
			strResult+=" or "+input.split("be not in the range")[0]+" greater than  "+strValue.split("-")[1];
		}
		return strResult;
	}
	
	
	public String step3NullParameterTemplate(String input,String fpMethodSignature){
		String strResult=input;
		if(input.equals("null parameter")){
			ArrayList<String> listParameters=new ArrayList<String>();
			
		}
		
//		strResult=input.replaceAll(".length()"," . length ( ) ");
//		strResult=strResult.replaceAll("length()"," length ( ) ");
		return strResult;
	}
	
	public String step4GreaterLessThanEqual(String input){
		String strResult=input;		
		strResult=input.replaceAll(" be greater than or equal "," greater than or equal ");
		strResult=strResult.replaceAll(" be less than or equal "," less than or equal ");
		return strResult;
	}
	
	
	
	

	

	public ArrayList<String> getAllParameterAbleToAccessByMethod(String fpMethodSignature,String fopRootDir){
		ArrayList<String> lstResults=new ArrayList<String>();
		try{
			String strContent=FileIO.readStringFromFile(fopRootDir+fpMethodSignature);
			Map options = JavaCore.getOptions();
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
			ASTParser parser = ASTParser.newParser(AST.JLS8);
	    	parser.setCompilerOptions(options);
			parser.setSource(strContent.toCharArray());
			ASTNode ast = parser.createAST(null);
	    	CompilationUnit cu =  (CompilationUnit) ast;
	    	OptDocVisitor odv=new OptDocVisitor();
	    	cu.accept(odv);
	    	
	    	
		} catch(Exception ex){
			
		}
		
		//cu.accept(visitor);
		return lstResults;
	}
	
	private class OptDocVisitor extends ASTVisitor{
		public OptDocVisitor() {
			// TODO Auto-generated constructor stub
			//System.out.println("hello world");
		}
		
		@Override
		public boolean visit(TypeDeclaration node) {
			//super(node);
			return true;
		}
		
		@Override
		public boolean visit(MethodDeclaration node) {
			//super(node);
			System.out.println(node.toString());
			return true;
		}
		
		private String getParameters(MethodDeclaration method) {
			StringBuilder sb = new StringBuilder();
			sb.append("(");
			for (int i = 0; i < method.parameters().size(); i++) {
				SingleVariableDeclaration d = (SingleVariableDeclaration) (method.parameters().get(i));
				String type = JavaASTUtil.getSimpleType(d.getType());
				sb.append("\t" + type);
			}
			sb.append("\t)");
			return sb.toString();
		}
		
		public String getMethodSignature(MethodDeclaration node){
			String strMethodName=node.getName().toString();
			
			return strMethodName;
		}
		
	}
	
	public void normalizeDocConditions(String fp_input,String fp_output){
		String[] arrJdConditions=FileIO.readStringFromFile(fp_input).trim().split("\n");
		String strTotal="";
		for(int i=0;i<arrJdConditions.length;i++){
			String strResult=arrJdConditions[i];
			//strResult=step1RemoveWhenWords(strResult);
			//strResult=step2TokenizeLengthFunction(strResult);			
			strResult=step3HandleBeNotNull(strResult);
		//	strResult=step4BeInRange(strResult);
			strResult=step4GreaterLessThanEqual(strResult);
			strTotal+=strResult+"\n";
		}
		FileIO.writeStringToFile(strTotal, fp_output);
		
	}
	
	
	
	
	public static void main(String[] args) {
		OptimizeDocClauseBeforeTranslation odcbt=new OptimizeDocClauseBeforeTranslation();
//		odcbt.getAllParameterAbleToAccessByMethod("\\java\\security\\spec\\RSAMultiPrimePrivateCrtKeySpec.java", fop);
		String fop="Replication-package\\Replication-package\\SpecTransOutput\\jdk_all_5\\out\\translate_2\\";
		odcbt.normalizeDocConditions(fop+"\\clauses.txt", fop+"normalize\\test.jd");
	}

}
