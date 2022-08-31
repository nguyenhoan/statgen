package eval;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;

import utils.FileIO;
import utils.JavaASTUtil;

public class CheckConsistency {

    public static void main(String[] args) {
        String path = "T:/spectrans/jdk8-java-javax";
        
        Set<String> methods = new HashSet<>(), inconsistentMethods = new HashSet<>();
        
        String content = FileIO.readStringFromFile(path + "/null.loc");
        Scanner sc = new Scanner(content);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String sig = getMethodSignature(line);
            methods.add(sig);
            inconsistentMethods.add(sig);
        }
        sc.close();
        
        Set<String> oracleMethods = new HashSet<>(), oracleInconsistentMethods = new HashSet<>();
        readOracle(path + "/oracle_recheck_methods.xlsx", oracleMethods, oracleInconsistentMethods);

        List<String> trans = read(path + "/test.trans"), codes = read(path + "/test.code"), locs = read(path + "/test.loc");
        for (int i = 0; i < locs.size(); i++) {
            String sig = getMethodSignature(locs.get(i));
            methods.add(sig);
            String tran = trans.get(i), code = codes.get(i);
            tran = tran.substring(tran.indexOf(' ')).trim();
            code = code.substring(code.indexOf(' ')).trim();
            if (!tran.equals(code)) {
                if (!isConsistent(tran, code)) {
                    if (!inconsistentMethods.contains(sig) && oracleInconsistentMethods.contains(sig))
                        System.out.println();
                    inconsistentMethods.add(sig);
                }
            }
        }
        
        Set<String> tmp = new HashSet<>(inconsistentMethods);
        tmp.retainAll(oracleInconsistentMethods);
        int tp = tmp.size();
        tmp = new HashSet<>(inconsistentMethods);
        tmp.retainAll(oracleMethods);
        int fp = tmp.size() - tp;
        int precision = tp * 100 / tmp.size();
        tmp.removeAll(oracleInconsistentMethods);
        print(tmp);
        int fn = oracleInconsistentMethods.size() - tp;
        tmp = new HashSet<>(oracleMethods);
        tmp.removeAll(oracleInconsistentMethods);
        tmp.removeAll(inconsistentMethods);
        int tn = tmp.size();
        
        System.out.println("TP\tTN\tFP\tFN");
        System.out.println(tp + "\t" + tn + "\t" + fp + "\t" + fn);
        System.out.println(tp*100.0/oracleMethods.size() + "%\t" + tn*100.0/oracleMethods.size() + "%\t" + fp*100.0/oracleMethods.size() + "%\t" + fn*100.0/oracleMethods.size() + "%");
        System.out.println("Precision: " + precision + "%");
        System.out.println("Recall: " + (tp * 100 / oracleInconsistentMethods.size()) + "%");
        System.out.println();
    }

    private static boolean isConsistent(String tran, String code) {
        List<Expression> tranOrOperands = new ArrayList<>();
        JavaASTUtil.getOrOperands(tran, tranOrOperands);
        Map<String, String> groupedTransOperands = groupOperands(tranOrOperands);
        if (groupedTransOperands.isEmpty())
            return true;
        Expression eCode = JavaASTUtil.parseExpression(code);
        if (eCode == null)
            return true;
        List<Expression> codeOrOperands = new ArrayList<>();
        JavaASTUtil.getOrOperands(eCode, codeOrOperands);
        Map<String, String> groupedCodeOperands = groupOperands(codeOrOperands);
        for (String names : groupedCodeOperands.keySet()) {
            String tranOperand = groupedTransOperands.get(names);
            if (tranOperand == null)
                continue;
            String codeOperand = groupedCodeOperands.get(names);
            if (!JavaASTUtil.equivalent(codeOperand, tranOperand))
                return false;
        }
        /*Expression eTrans = JavaASTUtil.parseExpression(tran);
        if (eTrans != null && eCode != null)
            return JavaASTUtil.equivalent(eTrans, eCode);*/
        return true;
    }

    private static Map<String, String> groupOperands(List<Expression> operands) {
        Map<String, String> map = new HashMap<String, String>();
        for (Expression e : operands) {
            final Set<String> names = new HashSet<>();
            e.accept(new ASTVisitor() {
                @Override
                public boolean visit(QualifiedName node) {
                    names.add(node.getFullyQualifiedName());
                    return super.visit(node);
                }
                
                @Override
                public boolean visit(SimpleName node) {
                    names.add(node.getIdentifier());
                    return super.visit(node);
                }
            });
            if (!names.isEmpty()) {
                List<String> list = new ArrayList<>(names);
                Collections.sort(list);
                StringBuilder sb = new StringBuilder();
                sb.append(list.get(0));
                for (int i = 1; i < list.size(); i++)
                    sb.append(" " + list.get(i));
                String key = sb.toString();
                String gOperand = map.get(key);
                if (gOperand == null) {
                    gOperand = e.toString();
                } else
                    gOperand += " || " + e.toString();
                map.put(key, gOperand);
            }
        }
        return map;
    }

    private static void print(Set<String> set) {
        for (String item : set) {
            System.out.println(item);
        }
    }

    private static void readOracle(String path, Set<String> oracleMethods, Set<String> oracleInconsistentMethods) {
        XSSFWorkbook book = getExcelWorkBook(path);
        XSSFSheet sheet = book.getSheetAt(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            XSSFRow row = sheet.getRow(i);
            if (row == null) continue;
            String sig = row.getCell(0).getStringCellValue();
            String result = row.getCell(1).getStringCellValue().toLowerCase();
            if (result.equals("inconsistent")) {
                oracleInconsistentMethods.add(sig);
                oracleMethods.add(sig);
            } else if (result.equals("consistent")) {
                oracleMethods.add(sig);
            }
        }
    }

    private static XSSFWorkbook getExcelWorkBook(String fileName) {
        XSSFWorkbook book = null;
        try {
            book = new XSSFWorkbook(new FileInputStream(fileName));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return book;
    }

    private static List<String> read(String path) {
        List<String> lines = new ArrayList<String>();
        String content = FileIO.readStringFromFile(path);
        Scanner sc = new Scanner(content);
        while (sc.hasNextLine()) {
            lines.add(sc.nextLine());
        }
        sc.close();
        return lines;
    }

    private static String getMethodSignature(String line) {
        String[] parts = line.split("\t");
        String sig = parts[1] + "." + parts[2] + "." + parts[3] + "(";
        int i = 5;
        while (!parts[i].equals(")")) {
            sig += (i == 5 ? "" : ",") + parts[i];
            i++;
        }
        sig += ")";
        return sig;
    }

}
