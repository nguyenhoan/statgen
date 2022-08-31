package eval;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import smtsolver.ExpressionExtractorParser;
import utils.FileIO;

public class HandleErrorSyntaxInImplementation {

	// Step 1: Find all substring.
	// Step 2: Try if the string can be parsed by a Java Expression or not.
	// Step 3: get the longest substring that is a valid Java Expression/

	public ArrayList<String> getSubList(ArrayList<String> prefix,
			int startIndex, int endIndex) {
		ArrayList<String> lstResult = new ArrayList<String>();
		for (int i = startIndex; i < endIndex; i++) {
			lstResult.add(prefix.get(i));
		}
		return lstResult;
	}

	public void combinationsListWords(ArrayList<String> suffix,
			ArrayList<String> prefix, ArrayList<String> out) {
		if (prefix.size() < 0)
			return;
		String strResult = "";
		for (int i = 0; i < suffix.size(); i++) {
			strResult += suffix.get(i) + " ";
		}
		strResult = strResult.trim();
		if (!strResult.isEmpty()) {
			out.add(strResult);
		}

		// System.out.println(suffix);
		ArrayList<String> lstSuffix = new ArrayList<String>();
		for (int i = 0; i < prefix.size(); i++) {
			lstSuffix = new ArrayList<String>();
			for (int j = 0; j < suffix.size(); j++) {
				lstSuffix.add(suffix.get(j));
			}
			lstSuffix.add(prefix.get(i));
			combinationsListWords(lstSuffix,
					getSubList(prefix, i + 1, prefix.size()), out);
		}
	}

	public void combinations(String suffix, String prefix) {
		if (prefix.length() < 0)
			return;
		System.out.println(suffix);
		for (int i = 0; i < prefix.length(); i++)
			combinations(suffix + prefix.charAt(i),
					prefix.substring(i + 1, prefix.length()));
	}

	public ArrayList<String> getArrayListOfString(String input) {
		String[] arr = input.trim().split("\\s+");
		ArrayList<String> lstResult = new ArrayList<String>();
		for (int i = 0; i < arr.length; i++) {
			if (!arr[i].trim().isEmpty()) {
				lstResult.add(arr[i]);
			}

		}
		return lstResult;
	}

	public void testGetCombineOfWord() {
		ArrayList<String> lstResult = new ArrayList<String>();
		combinationsListWords(getArrayListOfString(""),
				getArrayListOfString("1 2 3 4 5"), lstResult);
		System.out.println(lstResult);
	}

	public String removeException(String input) {
		String result = input;
		String[] arrInput = input.trim().split("\\s+");
		if (arrInput[0].endsWith("Exception")) {
			result="";
			for (int i = 1; i < arrInput.length; i++) {
				result += arrInput[i] + " ";
			}
			result = result.trim();
		}
		;

		return result;
	}

	public String[] handleErrorExpression(String strTranslatedExpr,
			boolean testing, ExpressionExtractorParser eep) {
		ArrayList<String> lstContents = new ArrayList<String>();
		HashMap<String, String> mapResults = new HashMap<String, String>();
		String[] arrResult = new String[2];

		String strInput = removeException(strTranslatedExpr);
		String strResult = "";
		int maxNumToken = 0;
		arrResult[0] = strInput;
		boolean isFirstExpr = false;
		if (strTranslatedExpr.split("\\s+")[0].endsWith("Exception")) {
			isFirstExpr = true;
		}
		if (getArrayListOfString(strInput).size() > 15) {
			arrResult[0] = strTranslatedExpr.split("\\s+")[0] + " "
					+ arrResult[0];
			arrResult[1] = maxNumToken + "";
			return arrResult;
		}
		lstContents.clear();
		if(testing){
			System.out.println("input "+strInput);
		}
		combinationsListWords(getArrayListOfString(""),
				getArrayListOfString(strInput), lstContents);

		

		for (int i = 0; i < lstContents.size(); i++) {
			// try to convert to java expression
			if(testing){
				System.out.println((i+1)+"\t"+lstContents.get(i));
			}
			boolean isJavaExpr = false;
			boolean isZ3Expr = false;
			Expression result = null;
			try (Context ctx = new Context()) {
				@SuppressWarnings("rawtypes")
				ASTParser parser = ASTParser.newParser(AST.JLS8);
				parser.setKind(ASTParser.K_EXPRESSION);
				parser.setSource(lstContents.get(i).toCharArray());
				result = (Expression) parser.createAST(null);
				if (testing) {
					System.out.println(lstContents.get(i));
				}
				isJavaExpr = true;
				BoolExpr z3Expr1 = (BoolExpr) eep.handleExpression(ctx, result, true);
				isZ3Expr = true;
			} catch (Exception ex) {
				// ex.printStackTrace();
			}

			int currentLength = lstContents.get(i).split("\\s+").length;
			if (isJavaExpr && isZ3Expr) {

				if (testing) {
					System.out.println(lstContents.get(i) + " " + isJavaExpr
							+ " " + currentLength);
				}
				if (currentLength >= maxNumToken) {
					arrResult[0] = lstContents.get(i);
					arrResult[1] = maxNumToken + "";
					maxNumToken = currentLength;
				}
			}

		}
		if (isFirstExpr) {
			//System.out.println(arrResult[0]+" aaa");
			arrResult[0] = strTranslatedExpr.split("\\s+")[0] + " "
					+ arrResult[0];
			arrResult[1] = maxNumToken + "";
		}

		return arrResult;
	}

	public void normalizeExpressionInTranslation(String fpIn, String fpOut,
			boolean testing, ExpressionExtractorParser eep) {
		String[] arrConditions = FileIO.readStringFromFile(fpIn).trim()
				.split("\n");
		String strContent = "";
		for (int i = 0; i < arrConditions.length; i++) {
			if (testing) {
				if (i + 1 != 1) {
					continue;
				}
			}
			String strResult = handleErrorExpression(arrConditions[i], testing,
					eep)[0];
			System.out.println((i + 1) + "\t" + strResult);
			strContent += strResult + "\n";
		}
		FileIO.writeStringToFile(strContent, fpOut);

	}

	public static void main(String[] args) {
		String fpIn = "Replication-package\\Replication-package\\SpecTransOutput\\jdk_all\\translate_4\\conditions.txt";
		String fpOut = "Replication-package\\Replication-package\\SpecTransOutput\\jdk_all\\translate_4\\normalize\\conditions.txt";
		ExpressionExtractorParser eep = new ExpressionExtractorParser();
		HandleErrorSyntaxInImplementation hesii = new HandleErrorSyntaxInImplementation();
		hesii.normalizeExpressionInTranslation(fpIn, fpOut, false, eep);
	}

}
