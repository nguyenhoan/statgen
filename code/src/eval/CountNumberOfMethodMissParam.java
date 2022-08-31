package eval;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import utils.FileIO;
import utils.JavaASTUtil;

public class CountNumberOfMethodMissParam  extends ASTVisitor {

	private String path;
	private String[] source;
	private StringBuilder doc = new StringBuilder();
	private String packageName;
	private String className;
	private String methodSig;
	private String currentCheckMethod;
	private ASTParser parser ;
	private boolean isContainThrowExTag=false;
	
	
	
	

	public CountNumberOfMethodMissParam(String path, String[] source, StringBuilder sb) {
		this.path = path;
		this.source = source;
		this.doc = sb;
		
	}
	
	public boolean isContainThrowExTag() {
		return isContainThrowExTag;
	}

	public void setContainThrowExTag(boolean isContainThrowExTag) {
		this.isContainThrowExTag = isContainThrowExTag;
	}
	
	public void constructParserForFilePath(String javaClass,String methodSig){
		//String[] sourcePaths = getSourcePaths(rootPath, new String[]{".java"}, recursive);
		this.methodSig=methodSig;
		String[] sourcePaths = {javaClass};
		Map options = JavaCore.getOptions();
		String strContentSource=FileIO.readStringFromFile(javaClass);
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setEnvironment(null, new String[]{}, new String[]{}, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(false);
		
		parser.setSource(strContentSource.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		
	
		
		try {
			final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
			cu.accept(this);
			parser.createASTs(sourcePaths, null, new String[0], null, null);
		} catch (Throwable t) {
//			t.printStackTrace(stLog);
//			if (testing) {
//				System.err.println(t.getMessage());
//				t.printStackTrace();
//				System.exit(-1);
//			}
		}
	}
	
	
	@Override
	public boolean visit(CompilationUnit node) {
		packageName=node.getPackage().getName().toString();
		//System.out.println(packageName+"  ssss");
	   // System.out.println("Compilation unit: " + node.getPackage().getName());
	    //System.out.println("Imports: " + node.imports());
	    return true;
	}
	
	@Override
	public boolean visit(TypeDeclaration node) {
		className=node.getName().toString(); 	
		//System.out.println(className+"  ssss "+node.getMethods().length);
		//node.getMethods()
		for(MethodDeclaration mi:node.getMethods()){
			visit(mi);
		}
		return false;
	}
	
	@Override
	public boolean visit(Javadoc node) {
		
		if(currentCheckMethod.equals(methodSig)){
			List<TagElement> lstTags= node.tags();
		//	System.out.println("Inside javadoc "+lstTags.size());
			for(TagElement nodeTag:lstTags){
				String tagStr = nodeTag.getTagName();
				if (tagStr != null) {
					//System.out.println(tagStr+" content");
					if(tagStr.equals("@throws")||tagStr.equals("@exception")){
						isContainThrowExTag=true;
					}
				} 
			}
		}
		
		return false;
	}
	
	
	
	@Override
	public boolean visit(TagElement node) {
		String tag = node.getTagName();
		if (tag != null) {
		//	System.out.println(tag+" content");
			if(tag.equals("throw")||tag.equals("exception")){
				isContainThrowExTag=true;
			}
		} 
		return false;
	}
	
	
	
	@Override
	public boolean visit(MethodDeclaration node) {
		 
		MethodDeclaration method=node;
	//	System.out.println("Visit aaa");
		String methodShortName = method.getName().getIdentifier() +getSigParameters(method);
		String methodName = packageName + "." + className + "." + methodShortName;
		currentCheckMethod=methodName;
		
		if(methodSig.equals(methodName)){
			isContainThrowExTag=false;
			
		//	System.out.println("Visit "+methodName);
			visit(node.getJavadoc());
		}
	//	
		return false;
	}
	
	
	
	private String getSigParameters(MethodDeclaration method) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < method.parameters().size(); i++) {
			SingleVariableDeclaration d = (SingleVariableDeclaration) (method.parameters().get(i));
			String type = JavaASTUtil.getSimpleType(d.getType());
			sb.append( type);
			if(i<method.parameters().size()-1){
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}
	
	public static String getPackageInfo(String input){
		String[] arrOutput=input.trim().split("\\.");
		String strOutput="";
		for(int i=0;i<arrOutput.length;i++){
			if(!Character.isUpperCase(arrOutput[i].charAt(0))){
				strOutput+=arrOutput[i]+".";
			} else{
				strOutput+=arrOutput[i]+"AAAjava";
				break;
			}			
		}
		return strOutput;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String jdkLocation="C:\\jdk_all_2\\";
		String fop="new_oracle\\";
		String fpOracle=fop+"oracle_v1.csv";
		String fpOnlyParamCheckResult="paramResult.txt";
		String strFix="AABBAA";
		String strContent=FileIO.readStringFromFile(fpOracle);
		String[] arrContent=strContent.split("\n");
		
		
		StringBuilder sb=new StringBuilder();
		CountNumberOfMethodMissParam cmp=new CountNumberOfMethodMissParam(jdkLocation, null, sb);
		int countTrue=0;
		String strContainResult="";
		for(int i=0;i<arrContent.length;i++){
//			if(i!=1){
//				continue;
//			}
			String arrItem[]=arrContent[i].split("\t");
			String strMethodSig=arrItem[0].trim().replaceAll(strFix, ",").trim();
			String className=getPackageInfo(strMethodSig).replace(".", File.separator).replace("AAA", ".");
			String fpClass=jdkLocation+className;
		//	System.out.println(fpClass);
			cmp.constructParserForFilePath(fpClass,strMethodSig);
			boolean isContainOnlyParam=cmp.isContainThrowExTag();
			strContainResult+=isContainOnlyParam+"\n";
			if(isContainOnlyParam){
				countTrue++;
				System.out.println((i+1)+"\t"+strMethodSig+"\t"+cmp.isContainThrowExTag());
			}
			//System.out.println(i+"\t"+strMethodSig+"\t"+cmp.isContainThrowExTag());
			
		}
		FileIO.writeStringToFile(strContainResult, fpOnlyParamCheckResult);
	}

}
