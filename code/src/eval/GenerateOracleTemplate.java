package eval;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import utils.FileIO;

public class GenerateOracleTemplate {

    public static void main(String[] args) {
        Map<String, Set<String>> packageAPIs = new HashMap<>();
        read("data/ExperimentI_Data.xlsx", packageAPIs);
        read("data/ExperimentII_Data.xlsx", packageAPIs);
        
        String path = "T:/spectrans/jdk8-java-javax";
        
        List<String> ps = new ArrayList<>(packageAPIs.keySet());
        Collections.sort(ps);
        XSSFWorkbook workbook = new XSSFWorkbook();
        for (String p : ps) {
            XSSFSheet sheet = workbook.createSheet(p);
            List<String> apis = new ArrayList<>(packageAPIs.get(p));
            Collections.sort(apis);
            int rowNum = 0;
            Row row = sheet.createRow(rowNum++);
            Cell cell = row.createCell(0);
            cell.setCellValue("API");
            cell = row.createCell(1);
            cell.setCellValue("Consistent?");
            cell = row.createCell(2);
            cell.setCellValue("Missing exception?");
            cell = row.createCell(3);
            cell.setCellValue("Mismatched exception?");
            cell = row.createCell(4);
            cell.setCellValue("Mismatched condition?");
            for (String api : apis) {
                row = sheet.createRow(rowNum++);
                cell = row.createCell(0);
                cell.setCellValue(api);
            }
        }

        try {
            FileOutputStream outputStream = new FileOutputStream(path + "/doc-code.xlsx");
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        count(path + "/doc-code.xlsx");
    }

    private static void count(String path) {
        int count = 0;
        XSSFWorkbook book = getExcelWorkBook(path);
        System.out.println("Sheets: " + book.getNumberOfSheets());
        for (int si = 0; si < book.getNumberOfSheets(); si++) {
            XSSFSheet sheet = book.getSheetAt(si); 
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);
                if (row == null) continue;
                count++;
            }
        }
        System.out.println(count);
    }

    private static void read(String path, Map<String, Set<String>> packageAPIs) {
        XSSFWorkbook book = getExcelWorkBook(path);
        XSSFSheet sheet = book.getSheetAt(0);
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            XSSFRow row = sheet.getRow(i);
            if (row == null) continue;
            String sig = row.getCell(0).getStringCellValue();
            String p = getPackage(sig);
            if (p.startsWith("java.")) {
                Set<String> apis = packageAPIs.get(p);
                if (apis == null) {
                    apis = new HashSet<>();
                    packageAPIs.put(p, apis);
                }
                apis.add(sig);
            }
        }
    }
    
    private static String getPackage(String sig) {
        int index = -1;
        while (true) {
            index = sig.indexOf('.', index+1);
            if (index == -1)
                return null;
            index++;
            if (Character.isUpperCase(sig.charAt(index)))
                return sig.substring(0, index-1);
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

}
