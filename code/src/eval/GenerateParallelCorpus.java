package eval;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import utils.FileIO;

public class GenerateParallelCorpus {

    public static void main(String[] args) {
        String path = "T:/spectrans/jdk8-java-javax";
        List<String> doc = read(path + "/doc.txt"), code = read(path + "/code.txt"), locs = read(path + "/locations.txt");
        int i = 0;
        while (i < doc.size()) {
            if (isNull(doc.get(i)) || isNull(code.get(i)) || isUnresolvable(code.get(i))) {
                doc.remove(i);
                code.remove(i);
                locs.remove(i);
            } else i++;
        }
        write(doc, path + "/corpus.doc");
        write(code, path + "/corpus.code");
        write(locs, path + "/corpus.loc");
    }

    private static void write(List<String> lines, String path) {
        StringBuilder sb = new StringBuilder();
        for (String l : lines)
            sb.append(l + "\n");
        FileIO.writeStringToFile(sb.toString(), path);
    }

    private static boolean isUnresolvable(String s) {
        int index = s.indexOf(' ');
        s = s.substring(index + 1).trim();
        return s.equals("( unresolvable )") || s.equals("unresolvable");
    }
    
    private static boolean isNull(String s) {
        if (s == null || s.equals("null"))
            return true;
        String[] parts = s.split("\\s");
        return parts.length == 2 && parts[1].equals("null");
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

}
