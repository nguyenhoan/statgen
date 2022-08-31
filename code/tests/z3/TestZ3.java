package z3;

import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
//
import smtsolver.ExpressionExtractorParser;

public class TestZ3 {

	public static void main(String[] args) {
		
	}
	
	@Ignore
	@Test
	public void test1() throws Exception {
		//
		String exprImpl = "index <= 0";
		String exprTran = "index > 0";
		String javaLibPath = System.getProperty("java.library.path");
		javaLibPath+=":/opt/atlassian/pipelines/agent/build/";
		System.out.println("java.lib.path: "+javaLibPath);
		String dir=System.getProperty("user.dir");
        System.out.println("current dir = " + dir);
		String strPath="";				
		try{
			System.setProperty("LD_LIBRARY_PATH",javaLibPath);
			System.setProperty("java.library.path",javaLibPath);
			File fLib=new File("/opt/atlassian/pipelines/agent/build/");
			File[] arrLib=fLib.listFiles();
//			System.load(fLib.getAbsolutePath()+"/"+"libgomp.so.1");
//			System.load(fLib.getAbsolutePath()+"/"+"libz3.so");
//			System.load(fLib.getAbsolutePath()+"/"+"libz3java.so");
//			for(int i=0;i<arrLib.length;i++){
//				if(arrLib[i].getName().endsWith(".so")||arrLib[i].getName().endsWith(".so.1")){
//					System.load(arrLib[i].getAbsolutePath());
//				}
//				strPath+=(i+1)+"\t"+arrLib[i].getAbsolutePath()+"\n";
//			}
			System.out.println(strPath);
			
			System.out.println("New java.lib.path: "+javaLibPath);
		} catch(Exception ex){
			ex.printStackTrace();
		}
		
		ExpressionExtractorParser eep=new ExpressionExtractorParser();
		
		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
		Assert.assertEquals("Inconsistency",
				strResult);
	}
//	
//	@Test
//	public void test2() throws Exception {
//		String exprImpl = "index > 0";
//		String exprTran = "index > 0";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test3() throws Exception {
//		String exprImpl = "index > 0 && compure(object)";
//		String exprTran = "index > 0 && compure(object2)";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Inconsistency",
//				strResult);
//	}
//	
//	@Test
//	public void test4() throws Exception {
//		String exprImpl = "index > 0 && compure(object)";
//		String exprTran = "index > 0 && compure(object)";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test5() throws Exception {
//		String exprImpl = "index >= 0";
//		String exprTran = "index == 0 || index > 0";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test6() throws Exception {
//		String exprImpl = "(component < 0) || (component > numComponents - 1)";
//		String exprTran = "(component < 0) || (component > this.getNumComponents() - 1)";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}

//	@Test
//	public void test7() throws Exception {
//		String exprImpl = "( offset < 0 )  ||  ( offset >  length (  )  )";
//		String exprTran = "( offset < 0 )  ||  ( offset >  length (  )  )";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test8() throws Exception {
//		String exprImpl = "( index < 0 )  ||  ( index >  length (  )  ) || ( offset < 0 )  ||  ( len < 0 )  ||  ( offset > str . length - len )";
//		String exprTran = "( index < 0 )  ||  ( index >  length (  )  ) || ( offset < 0 )  ||  ( len < 0 )  ||  ( offset > str . length - len )";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test9() throws Exception {
//		String exprImpl = "!  ( bound > 0.0 )";
//		String exprTran = "!  ( bound > 0.0 )";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test10() throws Exception {
//		String exprImpl = "!  name . equals ( \"control\" ) || actions != null &&  actions . length (  )  > 0";
//		String exprTran = "!  name . equals ( \"control\" ) || actions != null &&  actions . length (  )  > 0";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test11() throws Exception {
//		String exprImpl = "name . length (  )  > 0xFFFF";
//		String exprTran = "name . length (  )  > 0xFFFF";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test12() throws Exception {
//		String exprImpl = "this . comment . length > 0xffff";
//		String exprTran = "this . comment . length > 0xffff";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test13() throws Exception {
//		String exprImpl = "fields == null || name == null ||  name . equals ( \"\" ) || map . containsKey ( name )";
//		String exprTran = "fields == null || name == null ||  name . equals ( \"\" ) || map . containsKey ( name )";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test14() throws Exception {
//		String exprImpl = "fields == null || name == null ||  name . equals ( \"\" ) || map . containsKey ( name )";
//		String exprTran = "fields == null || name == null ||  name . equals ( \"abc\" ) || map . containsKey ( name )";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Inconsistency",
//				strResult);
//	}
//	
//	@Test
//	public void test15() throws Exception {
//		String exprImpl = "name == null ||   name . trim (  )  . equals ( \"\" ) || description == null ||   description . trim (  )  . equals ( \"\" )";
//		String exprTran = "name == null ||   name . trim (  )  . equals ( \"\" ) || description == null ||   description . trim (  )  . equals ( \"\" )";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test16() throws Exception {
//		String exprImpl = "name == null ||   name . trim (  )  . equals ( \"\" ) || description == null ||   description . trim (  )  . equals ( \"\" ) || returnOpenType == null || impact != ACTION && impact != ACTION_INFO && impact != INFO && impact != UNKNOWN";
//		String exprTran = "name == null ||   name . trim (  )  . equals ( \"\" ) || description == null ||   description . trim (  )  . equals ( \"\" ) || returnOpenType == null || impact != ACTION && impact != ACTION_INFO && impact != INFO && impact != UNKNOWN";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test17() throws Exception {
//		String exprImpl = "!  host . endsWith ( \"]\" ) || !  isNumericIPv6Address ( host ) || host . startsWith ( \"[\" )";
//		String exprTran = "!  host . endsWith ( \"]\" ) || !  isNumericIPv6Address ( host ) || host . startsWith ( \"[\" )";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
//	
//	@Test
//	public void test18() throws Exception {
//		String exprImpl = "offset < 0 || offset >  str . length (  ) || pos < 1 || pos >  this . length (  ) || (long) ( length )  > origLen || ( length + offset )  >  str . length (  )";
//		String exprTran = "offset < 0 || offset >  str . length (  ) || pos < 1 || pos >  this . length (  ) || (long) ( length )  > origLen || ( length + offset )  >  str . length (  )";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
	
//	@Test
//	public void test19() throws Exception {
//		System.out.println((int) 0.0+"");
//		String exprImpl = "!  ( bound > 0.0 )";
//		String exprTran = "bound <= 0";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}
	
//	@Test
//	public void test20() throws Exception {
//		System.out.println((int) 0.0+"");
//		String exprImpl = "( start < 0 )  ||  ( start > end )  ||  ( end >  s . length (  )  )";
//		String exprTran = "start < 0 || start > end || end > s . length ( ) ";
//		ExpressionExtractorParser eep=new ExpressionExtractorParser();
////		a.b(((start < 0) || (start > end) || (end > s.length())));
//		
//		String strResult=eep.checkConsistencyJavaExpression(exprImpl, exprTran);
//		Assert.assertEquals("Consistency",
//				strResult);
//	}

}
