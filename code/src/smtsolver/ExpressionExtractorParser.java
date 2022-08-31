package smtsolver;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.internal.core.dom.NaiveASTFlattener;

import utils.FileIO;
import utils.Utils;

import com.microsoft.z3.*;

/*
 * Extract all condition of methods list in location files to call checkSAT API.
 * 
 */
public class ExpressionExtractorParser {
    
	public static final int NULL_TYPE = 100;
	public static final String HEXAREGEX = "(?:0[xX])?[0-9a-fA-F]+";
    private static final HashMap<String, String> setAssignmentOperator = new HashMap<String, String>();
	
    private int numberDefFunction = 0;
	
	static {
	    setAssignmentOperator.put("=", "ASSIGN");
        setAssignmentOperator.put("+=", "PLUS_ASSIGN");
        setAssignmentOperator.put("-=", "MINUS_ASSIGN");
        setAssignmentOperator.put("*=", "TIMES_ASSIGN");
        setAssignmentOperator.put("/=", "DIVIDE_ASSIGN");
        setAssignmentOperator.put("&=", "BIT_OR_ASSIGN");
        setAssignmentOperator.put("^=", "BIT_XOR_ASSIGN");
        setAssignmentOperator.put("%=", "REMAINDER_ASSIGN");
        setAssignmentOperator.put("<<=", "LEFT_SHIFT_ASSIGN");
        setAssignmentOperator.put(">>=", "RIGHT_SHIFT_SIGNED_ASSIGN");
        setAssignmentOperator.put(">>>=", "RIGHT_SHIFT_UNSIGNED_ASSIGN");
	}
	
	public ExpressionExtractorParser() {
		
	}
	
	public boolean checkGetterMethodInvocation(MethodInvocation mi) {
		boolean result = false;
		if (mi.arguments().size() == 0) {
			String strMethodName = mi.getName().toString();
			if (strMethodName.startsWith("get")) {
				result = true;
			}
		}
		return result;
	}

	public String getActionOfGetterMethodInvocation(MethodInvocation mi) {
		String strResult = mi.getName().toString().replaceFirst("get", "");
		String strFirstCharacter = strResult.substring(0, 1);
		strResult = strResult.replaceFirst(strFirstCharacter,
				strFirstCharacter.toLowerCase());
		return strResult;
	}

	public String removeException(String input) {
		String result = "";
		String[] arrInput = input.trim().split("\\s+");
		for (int i = 1; i < arrInput.length; i++) {
			result += arrInput[i] + " ";
		}
		result = result.trim();
		return result;
	}

	public String checkConsistencyJavaExpression(String expr1, String expr2) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_EXPRESSION);
		parser.setSource(expr1.toCharArray());

		int typeOfResultCheck = 0;
		String strResult = "Implementation not support";
		int resultConsistency = -1;
		try {
			parser.setKind(ASTParser.K_EXPRESSION);
			parser.setSource(expr1.toCharArray());
			Expression javaExpr1 = (Expression) parser.createAST(null);

			if (javaExpr1 == null) {
				strResult = "Actual cannot parsed";
				return strResult;
			}

			parser.setKind(ASTParser.K_EXPRESSION);
			parser.setSource(expr2.toCharArray());
			Expression javaExpr2 = (Expression) parser.createAST(null);

			if (javaExpr2 == null) {
				strResult = "Translation cannot parsed";
				return strResult;
			}
			try (Context ctx = new Context()) {
    			if (javaExpr1 != null && javaExpr2 != null) {
    				resultConsistency = compareExpression(ctx, 0, javaExpr1,
    						javaExpr2);
    				if (resultConsistency == 1) {
    					strResult = "Consistency";
    				} else if (resultConsistency == 0) {
    					strResult = "Inconsistency";
    				} else if (resultConsistency == -1) {
    					strResult = "Unknown by SAT";
    				} else if (resultConsistency == 2) {
    					strResult = "Actual is not a valid expression";
    				} else if (resultConsistency == 3) {
    					strResult = "Translation is not a valid expression";
    				}
    			}
			} catch (Exception ex) {
	            ex.printStackTrace();
	        }
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return strResult;
	}

	public boolean checkConsistency(String fpLocation, String fpImpl,
			String fpTrans, String fp_outResult) {
		String[] arrLocations = FileIO.readStringFromFile(fpLocation).trim().split("\n");
		String[] arrImpl = FileIO.readStringFromFile(fpImpl).trim().split("\n");
		String[] arrTrans = FileIO.readStringFromFile(fpTrans).trim().split("\n");

		String implExtension = "impl";
		String tranExtension = "tran";
		String strOutputResult = "";

		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setEnvironment(null, new String[] {}, new String[] {}, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(false);

		HashMap<String, Integer> mapMethodsToLine = new HashMap<String, Integer>();
		HashSet<String> setLocations = new HashSet<String>();

		int numConsistency = 0;
		int numInconsistency = 0;
		int numUnknownBySAT = 0;
		int numNotSupportInImpl = 0;
		int numNotSupportInTran = 0;
		HashMap<String, ArrayList<Integer>> mapResults = new HashMap<String, ArrayList<Integer>>();
		mapResults.put("Consistency", new ArrayList<Integer>());
		mapResults.put("Inconsistency", new ArrayList<Integer>());
		mapResults.put("UnknownBySAT", new ArrayList<Integer>());
		mapResults.put("NotParsableInTran", new ArrayList<Integer>());
		mapResults.put("NotSupportInImpl", new ArrayList<Integer>());
		mapResults.put("NotSupportInTran", new ArrayList<Integer>());

		String strConsistent = "Consistent";
		String strInconsistent = "Inconsistent";
		for (int i = 0; i < arrLocations.length; i++) {
			boolean isConsistency = true;
			String item = arrLocations[i].trim();
			String implItem = removeException(arrImpl[i].trim());
			String transItem = removeException(arrTrans[i].trim());
			String fname = String.format("%04d", i + 1);

			Expression expImpl = getExpression(implItem);
			Expression expTran = getExpression(transItem);
			int resultConsistency = 0;
			String strLineResult = strConsistent;
			String strException = "";
			if (expImpl == null) {
				resultConsistency = 2;
			}
			if (expTran == null) {
				isConsistency = false;
				resultConsistency = 4;
				//System.out.println((i + 1) + "\t" + expImpl.toString());
				//System.out.println((i + 1) + "\t" + transItem);
			}
			if (expImpl != null && expTran != null) {
				try (Context ctx = new Context()) {
					resultConsistency = compareExpression(ctx, (i + 1),
							expImpl, expTran);
				} catch (Exception ex) {
					resultConsistency = 2;
					strException = ex.getMessage();
					ex.printStackTrace();
				}

			}

			if (resultConsistency == 1) {
				//System.out.println((i + 1) + "\tConsistency");
				strLineResult = strConsistent;
				mapResults.get("Consistency").add((i + 1));
			} else if (resultConsistency == 0) {
				//System.out.println((i + 1) + "\tInconsistency");
				mapResults.get("Inconsistency").add((i + 1));
			} else if (resultConsistency == -1) {
				//System.out.println((i + 1) + "\tUnknown");
				strLineResult = strInconsistent;
				mapResults.get("UnknownBySAT").add((i + 1));
			} else if (resultConsistency == 2) {
				strLineResult = strConsistent;
				//System.out.println((i + 1) + "\tImpl error: Expression not supported: " + strException);
				mapResults.get("NotSupportInImpl").add((i + 1));
			} else if (resultConsistency == 3) {
				strLineResult = strInconsistent;
				//System.out.println((i + 1) + "\tTranslation error: Expression invallid " + strException);
				mapResults.get("NotSupportInTran").add((i + 1));
			} else if (resultConsistency == 4) {
				strLineResult = strInconsistent;
				//System.out.println((i + 1) + "\tTranslation not parsed: Expression invallid " + strException);
				mapResults.get("NotParsableInTran").add((i + 1));
			}
			strOutputResult += strLineResult + "\n";
		}
		FileIO.writeStringToFile(strOutputResult, fp_outResult);
		//System.out.println("Result in total: " + arrLocations.length);
		String strType = "Consistency";

		//System.out.println(strType + " " + mapResults.get(strType).size() + " : " + mapResults.get(strType).toString());
		strType = "Inconsistency";
		//System.out.printtrType + " " + mapResults.get(strType).size() + " : " + mapResults.get(strType).toString());
		strType = "UnknownBySAT";
		//System.out.println(strType + " " + mapResults.get(strType).size() + " : " + mapResults.get(strType).toString());
		strType = "NotSupportInImpl";
		//System.out.println(strType + " " + mapResults.get(strType).size() + " : " + mapResults.get(strType).toString());
		strType = "NotSupportInTran";
		//System.out.println(strType + " " + mapResults.get(strType).size() + " : " + mapResults.get(strType).toString());
		strType = "NotParsableInTran";
		//System.out.println(strType + " " + mapResults.get(strType).size() + " : " + mapResults.get(strType).toString());
		return true;
	}

	
	public boolean checkConsistencyMethodLevel(String fpSignature,String fpJd, String fpImpl,
			String fpTrans, String fp_outResult,String fp_oracle,String fp_finalZ3) {
		String[] arrSignature = FileIO.readStringFromFile(fpSignature).trim()
				.split("\n");
		String[] arrImpl = FileIO.readStringFromFile(fpImpl).trim().split("\n");
		String[] arrTrans = FileIO.readStringFromFile(fpTrans).trim()
				.split("\n");

		String implExtension = "impl";
		String tranExtension = "tran";
		String strOutputResult = "";

		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM,
				JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setEnvironment(null, new String[] {}, new String[] {}, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(false);

		HashMap<String, Integer> mapMethodsToLine = new HashMap<String, Integer>();
		HashSet<String> setLocations = new HashSet<String>();

		int numConsistency = 0;
		int numInconsistency = 0;
		int numUnknownBySAT = 0;
		int numNotSupportInImpl = 0;
		int numNotSupportInTran = 0;
		HashMap<String, ArrayList<Integer>> mapResults = new HashMap<String, ArrayList<Integer>>();
		mapResults.put("Consistency", new ArrayList<Integer>());
		mapResults.put("Inconsistency", new ArrayList<Integer>());
		mapResults.put("UnknownBySAT", new ArrayList<Integer>());
		mapResults.put("NotParsableInTran", new ArrayList<Integer>());
		mapResults.put("NotSupportInImpl", new ArrayList<Integer>());
		mapResults.put("NotSupportInTran", new ArrayList<Integer>());

		String strConsistent = "Consistent";
		String strInconsistent = "Inconsistent";
		String strUndecidable = "Undecidable";
		for (int i = 0; i < arrSignature.length; i++) {
			boolean isConsistency = true;
			String item = arrSignature[i].trim();
			
			String implItem = removeException(arrImpl[i].trim());
			
			String transItem = removeException(arrTrans[i].trim());
			String fname = String.format("%04d", i + 1);

			if(implItem.isEmpty()){
				implItem="true";
			}
			if(transItem.isEmpty()){
				transItem="true";
			}
			
			Expression expImpl = getExpression(implItem);
			Expression expTran = getExpression(transItem);
			int resultConsistency = 0;
			String strLineResult = strConsistent;
			String strException = "";
			if (expImpl == null) {
				resultConsistency = 2;
			}
			if (expTran == null) {
				isConsistency = false;
				resultConsistency = 4;
				System.out.println((i + 1) + "\t" + expImpl.toString());
				System.out.println((i + 1) + "\t" + transItem);
			}
			if (expImpl != null && expTran != null) {
				try (Context ctx = new Context()) {
					resultConsistency = compareExpression(ctx, (i + 1),
							expImpl, expTran);
				} catch (Exception ex) {
					resultConsistency = 2;
					strException = ex.getMessage();
					ex.printStackTrace();
				}
			}

			if (resultConsistency == 1) {
				System.out.println((i + 1) + "\tConsistency");
				strLineResult = strConsistent;
				mapResults.get("Consistency").add((i + 1));
			} else if (resultConsistency == 0) {
				System.out.println((i + 1) + "\tInconsistency");
				strLineResult = strInconsistent;				
				mapResults.get("Inconsistency").add((i + 1));
			} else if (resultConsistency == -1) {
				System.out.println((i + 1) + "\tUnknown");
				strLineResult = strUndecidable;
				mapResults.get("UnknownBySAT").add((i + 1));
			} else if (resultConsistency == 2) {
				strLineResult = strUndecidable+"\tIMPL_NOT_PARSE";
				System.out.println((i + 1)
						+ "\tImpl error: Expression not supported: "
						+ strException);
				mapResults.get("NotSupportInImpl").add((i + 1));
			} else if (resultConsistency == 3) {
				strLineResult = strUndecidable+"\tTRANS_NOT_PARSE";
				System.out.println((i + 1)
						+ "\tTranslation error: Expression invallid "
						+ strException);
				mapResults.get("NotSupportInTran").add((i + 1));
			} else if (resultConsistency == 4) {
				strLineResult = strUndecidable+"\tTRANS_NOT_PARSE";
				System.out.println((i + 1)
						+ "\tTranslation not parsed: Expression invallid "
						+ strException);
				mapResults.get("NotParsableInTran").add((i + 1));
			}
			strOutputResult +=arrSignature[i].split("\t")[0]+"\t"+ strLineResult + "\n";
		}
		FileIO.writeStringToFile(strOutputResult, fp_outResult);
		System.out.println("Result in total: " + arrSignature.length);
		String strType = "Consistency";

		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		strType = "Inconsistency";
		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		strType = "UnknownBySAT";
		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		strType = "NotSupportInImpl";
		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		strType = "NotSupportInTran";
		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		strType = "NotParsableInTran";
		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		
		//get content from z3
		String[] arrStrZ3OnPair=FileIO.readStringFromFile(fp_outResult).trim().split("\n");
		HashMap<String,HashMap<String,ArrayList<String>>> mapZ3=new HashMap<String,HashMap<String,ArrayList<String>>>();
		for(int i=0;i<arrStrZ3OnPair.length;i++){
			String[] arrItem=arrStrZ3OnPair[i].trim().split("\t");	
			
			String exceptionName=arrImpl[i].trim().split("\\s+")[0];
		//	System.out.println(exceptionName+" exception");
			
			HashMap<String,ArrayList<String>> lstExceptions=mapZ3.get(arrItem[0]);
			if(lstExceptions==null){
				lstExceptions=new HashMap<String,ArrayList<String>>();
				ArrayList<String> lstResult=new ArrayList<String>();
				lstResult.add(arrItem[1]);
				lstExceptions.put(exceptionName,lstResult);
				mapZ3.put(arrItem[0], lstExceptions);
			} else{
				ArrayList<String> lstResult=mapZ3.get(arrItem[0]).get(exceptionName);
				
				if(lstResult==null){
					lstResult=new ArrayList<String>();
					lstResult.add(arrItem[1]);
					mapZ3.get(arrItem[0]).put(exceptionName,lstResult);
				} else{
					lstResult.add(arrItem[1]);
					mapZ3.get(arrItem[0]).put(exceptionName,lstResult);
				}
			}
		}
		
		
		String[] arrOracle=FileIO.readStringFromFile(fp_oracle).trim().split("\n");
		String strFinalZ3="";
		for(int i=1;i<arrOracle.length;i++){
			String[] arrItem=arrOracle[i].trim().split("\t");
			HashMap<String,ArrayList<String>>lstExceptions=mapZ3.get(arrItem[0]);
			String strFinalResultLine="Consistent";
			for(String exceptionName:lstExceptions.keySet()){
				if(lstExceptions.get(exceptionName).size()>1){
					System.out.println(arrItem[0]+"\t"+exceptionName+" has more than 2 exceptions conditions");
				}
				String strR=lstExceptions.get(exceptionName).get(0);
				
				if(strR.equals(strInconsistent)){
					strFinalResultLine=strInconsistent;
					break;
				} else if(strR.equals(strUndecidable)){
					strFinalResultLine=strConsistent;
				//	break;
				}
			}
			strFinalZ3+=strFinalResultLine+"\n";
		}
		//System.out.println(strFinalZ3);
		FileIO.writeStringToFile(strFinalZ3, fp_finalZ3);
		
		return true;
	}
	
	public static int getPatternCount(String item, String regex) {
		int count = 0;
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(item);
		while (matcher.find())
			count++;
		return count;
	}

	private boolean isTranslationAndImplEqualDisjunct(String implItem,
			String transItem) {
		boolean result = true;
		int numImpl = getPatternCount(implItem, " \\|\\| | && ");
		int numTrans = getPatternCount(transItem, " \\|\\| | && ");
		int diff = numImpl - numTrans;
		if (diff != 0) {
			result = false;
		}
		return result;
	}

	public boolean checkConsistencyAssumeCompare(String fpLocation,
			String fpDoc, String fpImpl, String fpTrans, String fp_outResult) {
		String[] arrLocations = FileIO.readStringFromFile(fpLocation).trim().split("\n");
		String[] arrDoc = FileIO.readStringFromFile(fpDoc).trim().split("\n");
		String[] arrImpl = FileIO.readStringFromFile(fpImpl).trim().split("\n");
		String[] arrTrans = FileIO.readStringFromFile(fpTrans).trim().split("\n");

		String implExtension = "impl";
		String tranExtension = "tran";
		String strOutputResult = "";

		@SuppressWarnings("rawtypes")
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setCompilerOptions(options);
		parser.setEnvironment(null, new String[] {}, new String[] {}, true);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(false);

		HashMap<String, Integer> mapMethodsToLine = new HashMap<String, Integer>();
		HashSet<String> setLocations = new HashSet<String>();

		int numConsistency = 0;
		int numInconsistency = 0;
		int numUnknownBySAT = 0;
		int numNotSupportInImpl = 0;
		int numNotSupportInTran = 0;
		HashMap<String, ArrayList<Integer>> mapResults = new HashMap<String, ArrayList<Integer>>();
		mapResults.put("Consistency", new ArrayList<Integer>());
		mapResults.put("Inconsistency", new ArrayList<Integer>());
		mapResults.put("UnknownBySAT", new ArrayList<Integer>());
		mapResults.put("NotParsableInTran", new ArrayList<Integer>());
		mapResults.put("NotSupportInImpl", new ArrayList<Integer>());
		mapResults.put("NotSupportInTran", new ArrayList<Integer>());

		String strConsistent = "Consistent";
		String strInconsistent = "Inconsistent";
		for (int i = 0; i < arrLocations.length; i++) {
			boolean isConsistency = true;
			String item = arrLocations[i].trim();
			String docItem = removeException(arrDoc[i].trim());
			String implItem = removeException(arrImpl[i].trim());
			String transItem = removeException(arrTrans[i].trim());

			int lineNum = i + 1;

			// they are lines that their oracles are not correct in terms that
			// the code-doc are consistency but their oracles are inconsistent
			// if(lineNum==120||lineNum==121||lineNum==122||lineNum==128||lineNum==129||(lineNum>=130&&lineNum<=132)||lineNum==133||lineNum==136||lineNum==201||lineNum==301||lineNum==303||lineNum==159||lineNum==161||lineNum==152||lineNum==153||lineNum==154||lineNum==186||lineNum==187||lineNum==300){
			// transItem=implItem+"&&false";
			// // strOutputResult+="\n";
			// // continue;
			// }
			//
			String fname = String.format("%04d", i + 1);

			Expression expImpl = getExpression(implItem);
			Expression expTran = getExpression(transItem);
			int resultConsistency = 0;
			String strLineResult = strConsistent;
			String strException = "";
			
			if(transItem.contains("CANNOT_PARSE")){
				if(getPatternCount(transItem, "CANNOT_PARSE")>=2) {
					strLineResult = strConsistent;
				}
				else {
					strLineResult=strInconsistent;
				}
				strOutputResult += strLineResult + "\n";
				continue;
			} else if(transItem.contains("trycatch")){
				strLineResult=strInconsistent;
				strOutputResult += strLineResult + "\n";
				continue;
			}
//			if(getPatternCount(transItem, "null")>0&&getPatternCount(transItem, "null")==getPatternCount(implItem, "null")){
//				strLineResult = strConsistent;
//				strOutputResult += strLineResult + "\n";
//				continue;
//			}
			
			if (expImpl == null) {
				resultConsistency = 2;
			}
			if (expTran == null) {
				isConsistency = false;
				resultConsistency = 4;
				System.out.println((i + 1) + "\t" + (expImpl != null));
				System.out.println((i + 1) + "\t" + transItem);
			}
			if (expImpl != null && expTran != null) {
				try (Context ctx = new Context()) {
					resultConsistency = compareExpression(ctx, (i + 1),
							expImpl, expTran);
				} catch (Exception ex) {
					resultConsistency = 2;
					strException = ex.getMessage();
					ex.printStackTrace();
				}
			}

			if (resultConsistency == 1) {
				System.out.println((i + 1) + "\tConsistency");
				strLineResult = strConsistent;
				mapResults.get("Consistency").add((i + 1));
			} else if (resultConsistency == 0) {
				System.out.println((i + 1) + "\tInconsistency");
				strLineResult = strInconsistent;
				mapResults.get("Inconsistency").add((i + 1));
			} else if (resultConsistency == -1) {
				System.out.println((i + 1) + "\tUnknown");
				strLineResult = strConsistent;
				strLineResult = isTranslationAndImplEqualDisjunct(implItem,
						transItem) ? strConsistent : strInconsistent;

				mapResults.get("UnknownBySAT").add((i + 1));
			} else if (resultConsistency == 2) {
				strLineResult = strConsistent;
				strLineResult = strInconsistent;
				strLineResult = isTranslationAndImplEqualDisjunct(implItem,
						transItem) ? strConsistent : strInconsistent;

				System.out.println((i + 1)
						+ "\tImpl error: Expression not supported: "
						+ strException);
				mapResults.get("NotSupportInImpl").add((i + 1));

			} else if (resultConsistency == 3) {
				strLineResult = strConsistent;
				strLineResult = isTranslationAndImplEqualDisjunct(implItem,
						transItem) ? strConsistent : strInconsistent;

				System.out.println((i + 1)
						+ "\tTranslation error: Expression invallid "
						+ strException);
				mapResults.get("NotSupportInTran").add((i + 1));
			} else if (resultConsistency == 4) {
				strLineResult = strConsistent;
				strLineResult = isTranslationAndImplEqualDisjunct(implItem,
						transItem) ? strConsistent : strInconsistent;

				System.out.println((i + 1)
						+ "\tTranslation not parsed: Expression invallid "
						+ strException);
				mapResults.get("NotParsableInTran").add((i + 1));
			}
			strOutputResult += strLineResult + "\n";
		}
		FileIO.writeStringToFile(strOutputResult, fp_outResult);
		System.out.println("Result in total: " + arrLocations.length);
		String strType = "Consistency";

		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		strType = "Inconsistency";
		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		strType = "UnknownBySAT";
		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		strType = "NotSupportInImpl";
		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		strType = "NotSupportInTran";
		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		strType = "NotParsableInTran";
		System.out.println(strType + " " + mapResults.get(strType).size()
				+ " : " + mapResults.get(strType).toString());
		return true;
	}

	public Expr handleOperator(Context ctx, String operator, Expr... listExpr) {
		Expr result = null;
		if (operator.equals("<")) {
			if (listExpr != null && listExpr.length >= 2)
				result = ctx.mkLt((ArithExpr) listExpr[0], (ArithExpr) listExpr[1]);
		} else if (operator.equals(">")) {
			if (listExpr != null && listExpr.length >= 2) {
				// System.out.println(listExpr[0].toString() + "\t" + listExpr[1].toString());
				result = ctx.mkGt((ArithExpr) listExpr[0], (ArithExpr) listExpr[1]);
			}
		} else if (operator.equals("==")) {
			if (listExpr != null && listExpr.length >= 2) {
//				System.out.println(listExpr[0].toString()+"000");
//				System.out.println(listExpr[1].toString()+"111");
				result = ctx.mkEq(listExpr[0], listExpr[1]);
			}
		} else if (operator.equals(">=")) {
			if (listExpr != null && listExpr.length >= 2) {
				result = ctx.mkGe((ArithExpr) listExpr[0], (ArithExpr) listExpr[1]);
				// System.out.println("ok");
			}
		} else if (operator.equals("<=")) {
			if (listExpr != null && listExpr.length >= 2) {
				// System.out.println(listExpr[0].toString() + "\t" + listExpr[1].toString());
				result = ctx.mkLe((ArithExpr) listExpr[0], (ArithExpr) listExpr[1]);
			}
		} else if (operator.equals("!=")) {
			if (listExpr != null && listExpr.length >= 2) {
				result = ctx.mkNot(ctx.mkEq((ArithExpr) listExpr[0], (ArithExpr) listExpr[1]));
				// System.out.println(listExpr[0].toString() + "\t" + listExpr[1].toString());
			}
		} else if (operator.equals("-")) {
			if (listExpr != null && listExpr.length >= 2) {
				result = ctx.mkSub((ArithExpr) listExpr[0], (ArithExpr) listExpr[1]);
			} else if (listExpr != null && listExpr.length == 1) {
				ArithExpr zeroExpr = ctx.mkInt(0);
				result = ctx.mkSub(zeroExpr, (ArithExpr) listExpr[0]);
			}
        } else if (operator.equals("--")) {
            if (listExpr != null) {
                ArithExpr zeroExpr = ctx.mkInt(1);
                result = ctx.mkSub(zeroExpr, (ArithExpr) listExpr[0]);
            }
		} else if (operator.equals("+")) {
			if (listExpr != null && listExpr.length >= 2) {
				result = ctx.mkAdd((ArithExpr) listExpr[0], (ArithExpr) listExpr[1]);
			} else if (listExpr != null && listExpr.length == 1) {
				ArithExpr zeroExpr = ctx.mkInt(0);
				result = ctx.mkAdd(zeroExpr, (ArithExpr) listExpr[0]);
			}
        } else if (operator.equals("++")) {
            if (listExpr != null) {
                ArithExpr zeroExpr = ctx.mkInt(1);
                result = ctx.mkAdd(zeroExpr, (ArithExpr) listExpr[0]);
            }
		} else if (operator.equals("*")) {
			if (listExpr != null && listExpr.length >= 2) {
				result = ctx.mkMul((ArithExpr) listExpr[0], (ArithExpr) listExpr[1]);
			}
		} else if (operator.equals("/")) {
			if (listExpr != null && listExpr.length >= 2) {
				result = ctx.mkDiv((ArithExpr) listExpr[0], (ArithExpr) listExpr[1]);
			}
		} else if (operator.equals("%")) {
			if (listExpr != null && listExpr.length >= 2) {
				result = ctx.mkMod((IntExpr) listExpr[0], (IntExpr) listExpr[1]);
			}
		} else if (operator.equals("||")) {
			if (listExpr != null && listExpr.length >= 2) {
				// System.out.println(operator);
				// System.out.println(listExpr[0].getClass()+"\naaa "+listExpr[1].getClass());
				// System.out.println(listExpr[0].toString()+"\naaaa"+listExpr[1].toString());
				if (!(listExpr[0].getClass().toString()
						.equals("class com.microsoft.z3.BoolExpr") && listExpr[1]
						.getClass().toString()
						.equals("class com.microsoft.z3.BoolExpr"))) {
					throw new UnsupportedOperationException("Invalid conversion to boolean expression!");
				}
				result = ctx.mkOr((BoolExpr) listExpr[0], (BoolExpr) listExpr[1]);
			}
		} else if (operator.equals("&&")) {
			if (listExpr != null && listExpr.length >= 2) {
				if (!(listExpr[0] instanceof com.microsoft.z3.BoolExpr
				        && listExpr[1] instanceof com.microsoft.z3.BoolExpr)) {
					throw new UnsupportedOperationException("Invalid conversion to boolean expression!");
				}
				result = ctx.mkAnd((BoolExpr) listExpr[0], (BoolExpr) listExpr[1]);
			}
		} else if (operator.equals("!")) {
			if (listExpr != null && listExpr.length >= 1) {
				if (!(listExpr[0] instanceof com.microsoft.z3.BoolExpr)) {
					throw new UnsupportedOperationException("Invalid conversion to boolean expression!");
				}
				result = ctx.mkNot((BoolExpr) listExpr[0]);
			}
		} else if (operator.equals("<<")) {
			if (listExpr != null && listExpr.length >= 2) {
				if (!(listExpr[0] instanceof com.microsoft.z3.IntExpr 
				        && listExpr[1] instanceof com.microsoft.z3.IntExpr)) {
					throw new UnsupportedOperationException("Invalid conversion to integer expression!");
				}
				Symbol fname = ctx.mkSymbol("LeftShift_" + numberDefFunction++);
				Sort[] domain = { ctx.getIntSort() };
				FuncDecl fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
				ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
				lstZ3Args.add(listExpr[0]);
				Expr[] fargs = new Expr[lstZ3Args.size()];
				for (int i = 0; i < lstZ3Args.size(); i++) {
					fargs[i] = lstZ3Args.get(i);
				}
				Expr fapp = (IntExpr) ctx.mkApp(fd, fargs);
				result = fapp;
			}
		} else if (operator.equals(">>")) {
			if (listExpr != null && listExpr.length >= 2) {
				if (!(listExpr[0] instanceof com.microsoft.z3.IntExpr 
						&& listExpr[1] instanceof com.microsoft.z3.IntExpr)) {
					throw new UnsupportedOperationException("Invalid conversion to integer expression!");
				}
				Symbol fname = ctx.mkSymbol("RightShift_" + numberDefFunction++);
				Sort[] domain = { ctx.getIntSort() };
				FuncDecl fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
				ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
				lstZ3Args.add(listExpr[0]);
				Expr[] fargs = new Expr[lstZ3Args.size()];
				for (int i = 0; i < lstZ3Args.size(); i++) {
					fargs[i] = lstZ3Args.get(i);
				}
				Expr fapp = (IntExpr) ctx.mkApp(fd, fargs);
				result = fapp;
			}
		} else if (operator.equals(">>>")) {
			if (listExpr != null && listExpr.length >= 2) {
				if (!(listExpr[0] instanceof com.microsoft.z3.IntExpr 
						&& listExpr[1] instanceof com.microsoft.z3.IntExpr)) {
					throw new UnsupportedOperationException("Invalid conversion to integer expression!");
				}
				Symbol fname = ctx.mkSymbol("RightShiftUnsigned_" + numberDefFunction++);
				Sort[] domain = { ctx.getIntSort() };
				FuncDecl fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
				ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
				lstZ3Args.add(listExpr[0]);
				Expr[] fargs = new Expr[lstZ3Args.size()];
				for (int i = 0; i < lstZ3Args.size(); i++) {
					fargs[i] = lstZ3Args.get(i);
				}
				Expr fapp = (IntExpr) ctx.mkApp(fd, fargs);
				result = fapp;
			}
		} else if (operator.equals("~")) {
			if (listExpr != null && listExpr.length >= 1) {
				if (listExpr[0] instanceof  com.microsoft.z3.BoolExpr) {
					Symbol fname = ctx.mkSymbol("ComplementOperator_" + numberDefFunction++);
					Sort[] domain = { ctx.getBoolSort() };
					FuncDecl fd = ctx.mkFuncDecl(fname, domain, ctx.getBoolSort());
					ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
					lstZ3Args.add(listExpr[0]);
					Expr[] fargs = new Expr[lstZ3Args.size()];
					for (int i = 0; i < lstZ3Args.size(); i++) {
						fargs[i] = lstZ3Args.get(i);
					}
					Expr fapp = (BoolExpr) ctx.mkApp(fd, fargs);
					result = fapp;
				} else if (listExpr[0] instanceof com.microsoft.z3.IntExpr) {
					Symbol fname = ctx.mkSymbol("ComplementOperator_" + numberDefFunction++);
					Sort[] domain = { ctx.getIntSort() };
					FuncDecl fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
					ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
					lstZ3Args.add(listExpr[0]);
					Expr[] fargs = new Expr[lstZ3Args.size()];
					for (int i = 0; i < lstZ3Args.size(); i++) {
						fargs[i] = lstZ3Args.get(i);
					}
					Expr fapp = (IntExpr) ctx.mkApp(fd, fargs);
					result = fapp;
				}
			}
		} else if (operator.equals("&")) {
			if (listExpr != null && listExpr.length >= 2) {
				if (listExpr[0] instanceof  com.microsoft.z3.BoolExpr) {
					Symbol fname = ctx.mkSymbol("AndBitOperator_" + numberDefFunction++);
					Sort[] domain = { ctx.getBoolSort(), ctx.getBoolSort() };
					FuncDecl fd = ctx.mkFuncDecl(fname, domain, ctx.getBoolSort());
					ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
					lstZ3Args.add(listExpr[0]);
					lstZ3Args.add(listExpr[1]);
					Expr[] fargs = new Expr[lstZ3Args.size()];
					for (int i = 0; i < lstZ3Args.size(); i++) {
						fargs[i] = lstZ3Args.get(i);
					}
					Expr fapp = (BoolExpr) ctx.mkApp(fd, fargs);
					result = fapp;
				} else if (listExpr[0] instanceof  com.microsoft.z3.IntExpr) {
					Symbol fname = ctx.mkSymbol("AndBitOperator_" + numberDefFunction++);
					Sort[] domain = { ctx.getIntSort(), ctx.getIntSort() };
					// System.out.println(listExpr[0].getClass() + " " + listExpr[1].getClass());
					FuncDecl fd = null;
					fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
					ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
					lstZ3Args.add(listExpr[0]);
					lstZ3Args.add(listExpr[1]);
					Expr[] fargs = new Expr[lstZ3Args.size()];
					for (int i = 0; i < lstZ3Args.size(); i++) {
						fargs[i] = lstZ3Args.get(i);
					}
					Expr fapp = (IntExpr) ctx.mkApp(fd, fargs);
					result = fapp;
				}
			}
		} else if (operator.equals("|")) {
			if (listExpr != null && listExpr.length >= 2) {
				if (!(listExpr[0] instanceof com.microsoft.z3.IntExpr 
						&& listExpr[1] instanceof  com.microsoft.z3.IntExpr)) {
					throw new UnsupportedOperationException("Invalid conversion to integer expression!");
				}
				Symbol fname = ctx.mkSymbol("OrBitOperator_" + numberDefFunction++);
				Sort[] domain = { ctx.getIntSort() };
				FuncDecl fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
				ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
				lstZ3Args.add(listExpr[0]);
				Expr[] fargs = new Expr[lstZ3Args.size()];
				for (int i = 0; i < lstZ3Args.size(); i++) {
					fargs[i] = lstZ3Args.get(i);
				}
				Expr fapp = (IntExpr) ctx.mkApp(fd, fargs);
				result = fapp;
			}
		} else if (operator.equals("^")) {
			if (listExpr != null && listExpr.length >= 2) {
				if (listExpr[0] instanceof com.microsoft.z3.IntExpr 
						&& listExpr[1] instanceof  com.microsoft.z3.IntExpr) {
    				Symbol fname = ctx.mkSymbol("XorBitOperator_" + numberDefFunction++);
    				Sort[] domain = { ctx.getIntSort() };
    				FuncDecl fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
    				ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
    				lstZ3Args.add(listExpr[0]);
    				Expr[] fargs = new Expr[lstZ3Args.size()];
    				for (int i = 0; i < lstZ3Args.size(); i++) {
    					fargs[i] = lstZ3Args.get(i);
    				}
    				Expr fapp = (IntExpr) ctx.mkApp(fd, fargs);
    				result = fapp;
				} else if (listExpr[0] instanceof com.microsoft.z3.BoolExpr 
                        && listExpr[1] instanceof  com.microsoft.z3.BoolExpr) {
                    Expr fapp = ctx.mkXor((BoolExpr) listExpr[0], (BoolExpr) listExpr[1]);
                    result = fapp;
                } else {
                    throw new UnsupportedOperationException("Invalid conversion to XOR expression!");
                }
			}
		} else if (operator.equals("Instanceof")) {
			if (listExpr != null && listExpr.length >= 2) {
				Symbol fname = ctx.mkSymbol("Instanceof_" + numberDefFunction++);
				Sort[] domain = { ctx.getIntSort(), ctx.getIntSort() };
				// System.out.println(listExpr[0].getClass() + " " + listExpr[1].getClass());
				FuncDecl fd = ctx.mkFuncDecl(fname, domain, ctx.getBoolSort());
				ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
				lstZ3Args.add(listExpr[0]);
				lstZ3Args.add(listExpr[1]);
				Expr[] fargs = new Expr[lstZ3Args.size()];
				for (int i = 0; i < lstZ3Args.size(); i++) {
					fargs[i] = lstZ3Args.get(i);
				}
				Expr fapp = (BoolExpr) ctx.mkApp(fd, fargs);
				result = fapp;
			}
		} else if (setAssignmentOperator.containsKey(operator)) {
			if (listExpr != null && listExpr.length >= 2) {
				Symbol fname = ctx.mkSymbol(setAssignmentOperator.get(operator) + "_" + numberDefFunction++);
				Sort[] domain = { ctx.getIntSort(), ctx.getIntSort() };
				// System.out.println(listExpr[0].getClass() + " " + listExpr[1].getClass());
				FuncDecl fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
				ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
				lstZ3Args.add(listExpr[0]);
				lstZ3Args.add(listExpr[1]);
				Expr[] fargs = new Expr[lstZ3Args.size()];
				for (int i = 0; i < lstZ3Args.size(); i++) {
					fargs[i] = lstZ3Args.get(i);
				}
				Expr fapp = (IntExpr) ctx.mkApp(fd, fargs);
				result = fapp;
			}
		} else {
			throw new UnsupportedOperationException("Operation " + operator + " not supported!");
		}

		return result;
	}

	public boolean checkBooleanRequired(String operator, Expression... lstExpr) {
	    if (lstExpr == null)
	        return false;
		if (operator.equals("==") || operator.equals("!=") || operator.equals("^")) {
			if (lstExpr.length == 2) {
	            for (Expression e : lstExpr)
	                if (e.getClass().toString().equals("class org.eclipse.jdt.core.dom.BooleanLiteral")
	                        || e.toString().startsWith("!"))
	                    return true;
			}
		}
		if (operator.equals("&&") 
		        || operator.equals("||")
				|| operator.equals("!")) {
			return true;
		}
		return false;
	}

	public boolean checkRegex(String input, String regex) {
		String line = input;
		String pattern = regex;

		// Create a Pattern object
		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(line);
		if (m.find()) {
		    return true;
		} else {
		    return false;
		}
	}

	public static int hex2decimal(String s) {
		String digits = "0123456789ABCDEF";
		s = s.toUpperCase();
		int val = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			int d = digits.indexOf(c);
			val = 16 * val + d;
		}
		return val;
	}

	public Expr handleExpression(Context ctx, Expression expr, boolean requireBooleanMethod) {
        Expr result = null;
        try {
            if (expr instanceof org.eclipse.jdt.core.dom.InfixExpression) {
                InfixExpression iExpr = (InfixExpression) expr;
                result = handleExpression(ctx, iExpr);
            } else if (expr instanceof InstanceofExpression) {
                InstanceofExpression iExpr = (InstanceofExpression) expr;
                result = handleExpression(ctx, iExpr);
            } else if (expr instanceof org.eclipse.jdt.core.dom.PrefixExpression) {
                PrefixExpression pExpr = (PrefixExpression) expr;
                result = handleExpression(ctx, pExpr);
            } else if (expr instanceof org.eclipse.jdt.core.dom.MethodInvocation) {
                MethodInvocation minvoke = (MethodInvocation) expr;
                result = handleExpression(ctx, minvoke, requireBooleanMethod);
            } else if (expr instanceof org.eclipse.jdt.core.dom.FieldAccess) {
                FieldAccess fieldaccess = (FieldAccess) expr;
                result = handleExpression(ctx, fieldaccess, requireBooleanMethod);
            } else if (expr instanceof org.eclipse.jdt.core.dom.Name) {
                org.eclipse.jdt.core.dom.Name qname = (org.eclipse.jdt.core.dom.Name) expr;
                result = handleExpression(ctx, qname, requireBooleanMethod);
            } else if (expr instanceof org.eclipse.jdt.core.dom.NumberLiteral) {
                result = handleExpression(ctx, (org.eclipse.jdt.core.dom.NumberLiteral) expr);
            } else if (expr instanceof org.eclipse.jdt.core.dom.NullLiteral) {
                result = ctx.mkInt(NULL_TYPE);
            } else if (expr instanceof org.eclipse.jdt.core.dom.ParenthesizedExpression) {
                ParenthesizedExpression paren = (ParenthesizedExpression) expr;
                result = handleExpression(ctx, paren.getExpression(), requireBooleanMethod);
            } else if (expr instanceof org.eclipse.jdt.core.dom.ThisExpression) {
                ThisExpression thex = (ThisExpression) expr;
                result = handleExpression(ctx, thex, requireBooleanMethod);
            } else if (expr instanceof org.eclipse.jdt.core.dom.BooleanLiteral) {
                BooleanLiteral obj = (BooleanLiteral) expr;
                result = ctx.mkBool(obj.booleanValue());
            } else if (expr instanceof org.eclipse.jdt.core.dom.StringLiteral) {
                StringLiteral lit = (StringLiteral) expr;
                result = handleExpression(ctx, lit);
            } else if (expr instanceof org.eclipse.jdt.core.dom.CastExpression) {
                CastExpression castExpr = (CastExpression) expr;
                result = handleExpression(ctx, castExpr);
            } else if (expr instanceof org.eclipse.jdt.core.dom.CharacterLiteral) {
                CharacterLiteral lit = (CharacterLiteral) expr;
                result = handleExpression(ctx, lit);
            } else if (expr instanceof org.eclipse.jdt.core.dom.Assignment) {
                Assignment iExpr = (Assignment) expr;
                result = handleExpression(ctx, iExpr);
            } else if (expr instanceof ConditionalExpression) {
                result = handleExpression(ctx, (ConditionalExpression) expr, requireBooleanMethod);
            } else if (expr instanceof org.eclipse.jdt.core.dom.ArrayCreation) {
                result = handleExpression(ctx, (org.eclipse.jdt.core.dom.ArrayCreation) expr, requireBooleanMethod);
            } else if (expr instanceof org.eclipse.jdt.core.dom.ArrayAccess) {
                result = handleExpression(ctx, (org.eclipse.jdt.core.dom.ArrayAccess) expr, requireBooleanMethod);
            } else if (expr instanceof org.eclipse.jdt.core.dom.ClassInstanceCreation) {
                result = handleExpression(ctx, (org.eclipse.jdt.core.dom.ClassInstanceCreation) expr, requireBooleanMethod);
            } else if (expr instanceof org.eclipse.jdt.core.dom.PostfixExpression) {
                result = handleExpression(ctx, (org.eclipse.jdt.core.dom.PostfixExpression) expr);
            } else if (expr instanceof org.eclipse.jdt.core.dom.TypeLiteral) {
                result = handleExpression(ctx, (org.eclipse.jdt.core.dom.TypeLiteral) expr);
            } else {
//                throw new UnsupportedOperationException("Expression " + expr.getClass().toString() + " not supported !");
                result = handleExpression(ctx, "var___" + Math.abs(expr.toString().hashCode()), requireBooleanMethod);
            }
        } catch (Exception e) {
            result = handleExpression(ctx, "var___" + Math.abs(expr.toString().hashCode()), requireBooleanMethod);
        }
        return result;
	}

    private Expr handleExpression(Context ctx, ArrayCreation expr, boolean requireBooleanMethod) {
        // TODO Auto-generated method stub
        return handleExpression(ctx, "var___" + Math.abs(expr.toString().hashCode()), requireBooleanMethod);
    }

    private Expr handleExpression(Context ctx, ArrayAccess expr, boolean requireBooleanMethod) {
        return handleExpression(ctx, "var___" + Math.abs(expr.toString().hashCode()), requireBooleanMethod);
    }

    private Expr handleExpression(Context ctx, ClassInstanceCreation expr, boolean requireBooleanMethod) {
        return handleExpression(ctx, "var___" + Math.abs(expr.toString().hashCode()), requireBooleanMethod);
    }

    private Expr handleExpression(Context ctx, PostfixExpression pExpr) {
        PostfixExpression.Operator op = pExpr.getOperator();
        Expr operand = handleExpression(ctx, pExpr.getOperand(), false);
        Expr result = handleOperator(ctx, op.toString(), operand);
        return result;
    }

    private Expr handleExpression(Context ctx, TypeLiteral expr) {
        return ctx.mkInt(expr.toString().hashCode());
    }

    public Expr handleExpression(Context ctx, PrefixExpression pExpr) {
        PrefixExpression.Operator op = pExpr.getOperator();
        boolean checkBool = checkBooleanRequired(op.toString());
        Expr operand = handleExpression(ctx, pExpr.getOperand(), checkBool);
        Expr result = handleOperator(ctx, op.toString(), operand);
        return result;
    }

    private Expr handleExpression(Context ctx, CastExpression castExpr) {
        return handleExpression(ctx, castExpr.getExpression(), false);
    }

    private Expr handleExpression(Context ctx, ConditionalExpression expr, final boolean requireBooleanMethod) {
        if (Utils.satcheck(expr.getExpression()) == Status.UNSATISFIABLE)
            return handleExpression(ctx, expr.getElseExpression(), requireBooleanMethod);
        if (Utils.satcheck("!(" + expr.getExpression().toString() + ")") == Status.UNSATISFIABLE)
            return handleExpression(ctx, expr.getThenExpression(), requireBooleanMethod);
        return ctx.mkInt(expr.toString().hashCode());
    }

    private Expr handleExpression(Context ctx, InfixExpression iExpr) {
        Expression leftExpr = iExpr.getLeftOperand();
        Expression rightExpr = iExpr.getRightOperand();
        InfixExpression.Operator op = iExpr.getOperator();

        // handle expression
        //System.out.println(iExpr.toString());
        boolean checkBool = checkBooleanRequired(op.toString(), leftExpr, rightExpr);
        Expr leftZ3Expr = handleExpression(ctx, leftExpr, checkBool);
        Expr rightZ3Expr = handleExpression(ctx, rightExpr, checkBool);
        // handle operator
        Expr tempRes = handleOperator(ctx, op.toString(), leftZ3Expr, rightZ3Expr);
        if (iExpr.hasExtendedOperands()) {
            for (int i = 0; i < iExpr.extendedOperands().size(); i++) {
                Expression item = (Expression) iExpr.extendedOperands().get(i);
                Expr rightZ3Expr2 = handleExpression(ctx, item, checkBool);
                tempRes = handleOperator(ctx, op.toString(), tempRes, rightZ3Expr2);
            }
        }
        return tempRes;
    }

	private Expr handleExpression(Context ctx, InstanceofExpression iExpr) {
        Expression leftExpr = iExpr.getLeftOperand();
        Type rightType = iExpr.getRightOperand();

        // handle expression
        boolean checkBool = false;
        Expr leftZ3Expr = handleExpression(ctx, leftExpr, checkBool);

        // handle operator
        // create symbol of right type
        Symbol fname = ctx.mkSymbol(rightType.toString());
        Sort[] domain = {};
        FuncDecl fd = null;// ctx.mkFuncDecl(fname, domain,
        fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
        Expr[] fargs = {};
        Expr fapp = null;
        fapp = (IntExpr) ctx.mkApp(fd, fargs);
        return handleOperator(ctx, "Instanceof", leftZ3Expr, fapp);
    }

    private Expr handleExpression(Context ctx, MethodInvocation minvoke, boolean requireBooleanMethod) {
        return handleExpression(ctx, "var___" + Math.abs(minvoke.toString().hashCode()), requireBooleanMethod);
        
//        NaiveASTFlattener v = new NaiveASTFlattener() {
//            @Override
//            public boolean visit(FieldAccess node) {
//                if (!(node.getExpression() instanceof ThisExpression)) {
//                    node.getExpression().accept(this);
//                    this.buffer.append(".");//$NON-NLS-1$
//                }
//                node.getName().accept(this);
//                return false;
//            }
//            
//            @Override
//            public boolean visit(MethodInvocation node) {
//                if (node.getExpression() != null && !(node.getExpression() instanceof ThisExpression)) {
//                    node.getExpression().accept(this);
//                    this.buffer.append(".");//$NON-NLS-1$
//                }
//                node.getName().accept(this);
//                this.buffer.append("(");//$NON-NLS-1$
//                for (Iterator it = node.arguments().iterator(); it.hasNext(); ) {
//                    Expression e = (Expression) it.next();
//                    e.accept(this);
//                    if (it.hasNext()) {
//                        this.buffer.append(",");//$NON-NLS-1$
//                    }
//                }
//                this.buffer.append(")");//$NON-NLS-1$
//                return false;
//            }
//        };
//        minvoke.accept(v);
//        return handleExpression(ctx, "var___" + Math.abs(v.getResult().hashCode()), requireBooleanMethod);
        
//        String methodName = minvoke.getName().toString();
//        String className = minvoke.getExpression() != null ? minvoke.getExpression().toString() : null;
//        Expr result = null;
//        
//        boolean isGetterMethod = checkGetterMethodInvocation(minvoke);
//        if (requireBooleanMethod) {
//            List<ASTNode> listArg = (List<ASTNode>) minvoke.arguments();
//            Symbol fname = ctx.mkSymbol(isGetterMethod ? getActionOfGetterMethodInvocation(minvoke) : methodName);
//            FuncDecl fd = null;
//
//            Sort[] domain = null;
//            int firstNumber = -1;
//            ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
//            if (minvoke.getExpression() == null) {
//                domain = new Sort[listArg.size()];
//            } else {
//                domain = new Sort[listArg.size() + 1];
//                firstNumber++;
//                domain[firstNumber] = ctx.getIntSort();
//                lstZ3Args.add(ctx.mkIntConst(ctx.mkSymbol(className)));
//            }
//
//            for (ASTNode itemArg : listArg) {
//                firstNumber++;
//                domain[firstNumber] = ctx.getIntSort();
//            }
//            fd = ctx.mkFuncDecl(fname, domain, ctx.getBoolSort());
//
//            for (ASTNode itemArg : listArg) {
//                Expr itemZ3 = handleExpression(ctx, (Expression) itemArg, false);
//                lstZ3Args.add(itemZ3);
//            }
//            Expr[] fargs = new Expr[lstZ3Args.size()];
//            for (int i = 0; i < lstZ3Args.size(); i++) {
//                fargs[i] = lstZ3Args.get(i);
//            }
//            Expr fapp = null; // {
//                                // ctx.mkIntConst(ctx.mkSymbol(className))
//                                // };
//            fapp = (BoolExpr) ctx.mkApp(fd, fargs);
//            result = fapp;
//        } else {
//            List<ASTNode> listArg = (List<ASTNode>) minvoke.arguments();
//            Symbol fname = ctx.mkSymbol(isGetterMethod ? getActionOfGetterMethodInvocation(minvoke) : methodName);
//            FuncDecl fd = null;
//            Sort[] domain = null;
//            int firstNumber = -1;
//            ArrayList<Expr> lstZ3Args = new ArrayList<Expr>();
//            if (minvoke.getExpression() == null) {
//                domain = new Sort[listArg.size()];
//            } else {
//                domain = new Sort[listArg.size() + 1];
//                firstNumber++;
//                domain[firstNumber] = ctx.getIntSort();
//                lstZ3Args.add(ctx.mkIntConst(ctx.mkSymbol(className)));
//            }
//            for (ASTNode itemArg : listArg) {
//                firstNumber++;
//                domain[firstNumber] = ctx.getIntSort();
//            }
//            fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
//            for (ASTNode itemArg : listArg) {
//                Expr itemZ3 = handleExpression(ctx,
//                        (Expression) itemArg, false);
//                lstZ3Args.add(itemZ3);
//            }
//            Expr[] fargs = new Expr[lstZ3Args.size()];
//            for (int i = 0; i < lstZ3Args.size(); i++) {
//                fargs[i] = lstZ3Args.get(i);
//            }
//            // { ctx.mkIntConst(ctx.mkSymbol(className)) };
//            Expr fapp = (IntExpr) ctx.mkApp(fd, fargs);
//
//            result = fapp;
//        }
//        return result;
    }

    private Expr handleExpression(Context ctx, FieldAccess fieldaccess, boolean requireBooleanMethod) {
        String name = fieldaccess.toString();
//        if (fieldaccess.getExpression() instanceof ThisExpression)
//            name = fieldaccess.getName().getIdentifier();
        Symbol fname = ctx.mkSymbol(name);
        Sort[] domain = {};
        FuncDecl fd = null;
        if (requireBooleanMethod) {
            fd = ctx.mkFuncDecl(fname, domain, ctx.getBoolSort());
        } else {
            fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
        }
        Expr[] fargs = {};
        Expr fapp = null;

        if (requireBooleanMethod) {
            fapp = (BoolExpr) ctx.mkApp(fd, fargs);
        } else {
            fapp = (IntExpr) ctx.mkApp(fd, fargs);
        }
        return fapp;
    }

    private Expr handleExpression(Context ctx, org.eclipse.jdt.core.dom.Name name, boolean requireBooleanMethod) {
        Symbol fname = ctx.mkSymbol(name.toString());
        Sort[] domain = {};
        FuncDecl fd = null;
        if (requireBooleanMethod) {
            fd = ctx.mkFuncDecl(fname, domain, ctx.getBoolSort());
        } else {
            fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
        }
        Expr[] fargs = {};
        Expr fapp = null;

        if (requireBooleanMethod) {
            fapp = (BoolExpr) ctx.mkApp(fd, fargs);
        } else {
            fapp = (IntExpr) ctx.mkApp(fd, fargs);
        }
        return fapp;
    }

    private Expr handleExpression(Context ctx, String name, boolean requireBooleanMethod) {
        Symbol fname = ctx.mkSymbol(name);
        Sort[] domain = {};
        FuncDecl fd = null;
        if (requireBooleanMethod) {
            fd = ctx.mkFuncDecl(fname, domain, ctx.getBoolSort());
        } else {
            fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
        }
        Expr[] fargs = {};
        Expr fapp = null;

        if (requireBooleanMethod) {
            fapp = (BoolExpr) ctx.mkApp(fd, fargs);
        } else {
            fapp = (IntExpr) ctx.mkApp(fd, fargs);
        }
        return fapp;
    }

    private Expr handleExpression(Context ctx, NumberLiteral lit) {
        // System.out.println("aaa "+expr1.toString());
        String strNumber = lit.getToken();
        if (strNumber.endsWith("L")) {
            strNumber = strNumber.replaceAll("L", "");
        }
        String strNumberToTadd = strNumber;
        boolean isDecima = false;
        try {
            strNumberToTadd = Double.parseDouble(strNumberToTadd) + "";
            isDecima = true;
        } catch (Exception ex) {}
        
        if (!isDecima) {
            if (checkRegex(strNumber, HEXAREGEX)) {
                // System.out.println("hexa");
                strNumberToTadd = hex2decimal(strNumber) + "";
            }
        }
        return ctx.mkReal(strNumberToTadd);
    }

    private Expr handleExpression(Context ctx, ThisExpression thex, boolean requireBooleanMethod) {
        Symbol fname = ctx.mkSymbol(thex.toString());
        Sort[] domain = {};
        FuncDecl fd = null;// ctx.mkFuncDecl(fname, domain,
                            // ctx.getIntSort());
        if (requireBooleanMethod) {
            fd = ctx.mkFuncDecl(fname, domain, ctx.getBoolSort());
        } else {
            fd = ctx.mkFuncDecl(fname, domain, ctx.getIntSort());
        }
        Expr[] fargs = {};
        Expr fapp = null;

        if (requireBooleanMethod) {
            fapp = (BoolExpr) ctx.mkApp(fd, fargs);
        } else {
            fapp = (IntExpr) ctx.mkApp(fd, fargs);
        }
        return fapp;
    }

    private Expr handleExpression(Context ctx, StringLiteral lit) {
        return ctx.mkInt(lit.getLiteralValue().hashCode());
    }

    private Expr handleExpression(Context ctx, CharacterLiteral lit) {
        return ctx.mkInt(lit.charValue());
    }

    private Expr handleExpression(Context ctx, Assignment iExpr) {
	    Expression leftExpr = iExpr.getLeftHandSide();
        Expression rightExpr = iExpr.getRightHandSide();
        Assignment.Operator op = iExpr.getOperator();

        // handle expression
        boolean checkBool = checkBooleanRequired(op.toString(), leftExpr,
                rightExpr);
        Expr leftZ3Expr = handleExpression(ctx, leftExpr, checkBool);
        Expr rightZ3Expr = handleExpression(ctx, rightExpr, checkBool);
        // handle operator
        //System.out.println("checkbool is "+checkBool);
        Expr tempRes = handleOperator(ctx, op.toString(), leftZ3Expr, rightZ3Expr);
        return tempRes;
    }

    public int compareExpression(Context ctx, int index, Expression expr1, Expression expr2) {
		int result = 0;
//		System.out.println(index + "\t" + expr1.toString() + "\n" + index
//				+ "\t" + expr1.getClass() + "\n" + index + "\t"
//				+ expr2.toString() + "\n" + index + "\t" + expr2.getClass());
		BoolExpr z3Expr1 = (BoolExpr) handleExpression(ctx, expr1, true);
		BoolExpr z3Expr2 = null;
		try {
			z3Expr2 = (BoolExpr) handleExpression(ctx, expr2, true);
		} catch (Exception ex) {
			ex.printStackTrace();
			return 3;
		}

		// System.out.println(z3Expr1.toString()+"\n"+z3Expr2.toString());
		BoolExpr z3Result = createSATForCheckFormula(ctx, z3Expr1, z3Expr2);
		// check SAT
		Solver s = ctx.mkSolver();
		s.add(z3Result);
		Status q = s.check();
		switch (q) {
		case UNKNOWN:
			// System.out.println("Unknown because: " +
			// s.getReasonUnknown());
			result = -1;
			break;
		case SATISFIABLE:
			// System.out.println("SAT");
			result = 0;
			break;
		case UNSATISFIABLE:
			result = 1;
			// System.out.println("UNSAT");
		}
		// throw new Exception("This ");

		return result;
	}

	public static BoolExpr createSATForCheckFormula(Context ctx, BoolExpr a,
			BoolExpr b) {
		BoolExpr m1 = ctx.mkAnd(a, ctx.mkNot(b));
		BoolExpr m2 = ctx.mkAnd(b, ctx.mkNot(a));
		// System.out.println(m1.toString());
		// System.out.println(m2.toString());
		BoolExpr result = ctx.mkOr(m1, m2);
		// System.out.println(result.toString());
		return result;
	}

	public Expression getExpression(String strCondition) {
		Expression result = null;
		try {
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setKind(ASTParser.K_EXPRESSION);
			// System.out.println(strCondition+" aaa");

			parser.setSource(strCondition.toCharArray());
			parser.setBindingsRecovery(false);
			parser.setStatementsRecovery(false);

			result = (Expression) parser.createAST(null);
			// result = JavaASTUtil.parseExpression(strCondition);
			return result;
		} catch (Throwable t) {
			// t.printStackTrace();

		}
		return result;
	}

	public static void main(String[] args) {
		String fop_location = "ICSE2018Data\\jdk-consistency\\";
		String fop_project = "JDK_coreSource\\";
		ExpressionExtractorParser eep = new ExpressionExtractorParser();
		eep.checkConsistency(fop_location + "test.locations.txt", fop_location
				+ "test.impl", fop_location + "test.tune.baseline.trans",
				fop_location + "zeResult.txt");
		// fop_outputSource);

	}

	private void getSourcePaths(File file, HashSet<String> paths,
			HashSet<String> exts, boolean recursive) {
		if (file.isDirectory()) {
			if (paths.isEmpty() || recursive)
				for (File sub : file.listFiles())
					getSourcePaths(sub, paths, exts, recursive);
		} else if (exts.contains(getExtension(file.getName())))
			paths.add(file.getAbsolutePath());
	}

	private Object getExtension(String name) {
		int index = name.lastIndexOf('.');
		if (index < 0)
			index = 0;
		return name.substring(index);
	}
	
}
