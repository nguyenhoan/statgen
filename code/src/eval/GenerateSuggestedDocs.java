package eval;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Stack;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import utils.FileIO;
import utils.JavaASTUtil;

public class GenerateSuggestedDocs {

    public static void main(String[] args) {
        String path = "T:/spectrans/apache-commons-collections";
        
        Map<String, List<String>> hierarchy = buildClassHierarchy("D:/data/apache/commons-collections-3.2.2/src/java");
        
        String content = FileIO.readStringFromFile(path + "/uses.csv");
        List<String> apis = new ArrayList<>();
        Scanner sc = new Scanner(content);
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] parts = line.split(",");
            apis.add(parts[0]);
        }
        sc.close();
        
        List<String> locs = new ArrayList<>();
        Map<String, List<Integer>> apiIndices = new HashMap<String, List<Integer>>();
        content = FileIO.readStringFromFile(path + "/test.loc");
        sc = new Scanner(content);
        int lid = 0;
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] parts = line.split("\t");
            String fqn = parts[1] + "." + parts[2] + "." + parts[3] + "(";
            String loc = parts[1] + "," + parts[2] + "," + parts[3] + "(";
            int i = 5;
            while (!parts[i].equals(")")) {
                loc += parts[i] + ";";
                i++;
            }
            fqn += (i-5) + ")";
            loc += ")";
            List<Integer> indices = apiIndices.get(fqn);
            if (indices == null) {
                indices = new ArrayList<>();
                apiIndices.put(fqn, indices);
            }
            indices.add(lid);
            locs.add(loc);
            lid++;
        }
        sc.close();

        List<String> trans = read(path + "/test.corpus.baseline.trans");
        
        StringBuilder sb = new StringBuilder();
        for (String api : apis) {
            List<Integer> indices = apiIndices.get(api);
            if (indices != null) {
                for (int index : indices)
                    sb.append(locs.get(index) + "," + trans.get(index) + "\n");
            } else {
                List<String> subAPIs = new ArrayList<>();
                getSubAPIs(api, hierarchy, subAPIs);
                for (String sub : subAPIs) {
                    indices = apiIndices.get(sub);
                    if (indices != null) {
                        for (int index : indices)
                            sb.append(locs.get(index) + "," + trans.get(index) + "\n");
                    }
                }
            }
        }
        FileIO.writeStringToFile(sb.toString(), path + "/suggestions.csv");
    }

    private static void getSubAPIs(String api, Map<String, List<String>> hierarchy, List<String> subAPIs) {
        int index = api.lastIndexOf('.');
        String type = api.substring(0, index);
        String method = api.substring(index + 1);
        if (hierarchy.containsKey(type)) {
            Stack<String> stk = new Stack<String>();
            for (String t : hierarchy.get(type))
                stk.push(t);
            while (!stk.isEmpty()) {
                String t = stk.pop();
                subAPIs.add(t + "." + method);
                List<String> subs = hierarchy.get(t);
                if (subs != null)
                    for (String sub : subs)
                        stk.push(sub);
            }
        }
    }

    private static Map<String, List<String>> buildClassHierarchy(String src) {
        String[] sourcePaths = JavaASTUtil.getSourcePaths(src, new String[]{".java"}, true);
        
        @SuppressWarnings("rawtypes")
        Map options = JavaCore.getOptions();
        options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
        options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setCompilerOptions(options);
        parser.setEnvironment(null, new String[]{}, new String[]{}, true);
        parser.setResolveBindings(true);
        parser.setBindingsRecovery(false);
        
        final HashMap<String, CompilationUnit> cus = new HashMap<>();
        final FileASTRequestor r = new FileASTRequestor() {
            @Override
            public void acceptAST(String sourceFilePath, CompilationUnit ast) {
                if (ast.getPackage() == null)
                    return;
                cus.put(sourceFilePath, ast);
            }
        };
        try {
            parser.createASTs(sourcePaths, null, new String[0], r, null);
        } catch (Throwable t) {
        }
        
        Map<String, List<String>> hierarchy = new HashMap<>();
        for (CompilationUnit cu : cus.values())
            buildClassHierarchy(cu, hierarchy);
        return hierarchy;
    }

    private static void buildClassHierarchy(CompilationUnit cu, Map<String, List<String>> hierarchy) {
        if (cu.getPackage() != null && cu.types() != null) {
            for (int i = 0; i < cu.types().size(); i++) {
                if (cu.types().get(i) instanceof TypeDeclaration) {
                    TypeDeclaration type = (TypeDeclaration) cu.types().get(i);
                    buildClassHierarchy(type.resolveBinding(), hierarchy);
                }
            }
        }
    }

    private static void buildClassHierarchy(ITypeBinding tb, Map<String, List<String>> hierarchy) {
        if (tb.isEnum() || tb.isAnnotation())
            return;
        if (tb.getSuperclass() != null) {
            ITypeBinding stb = tb.getSuperclass().getTypeDeclaration();
            if (stb != null)
                add(hierarchy, stb.getQualifiedName(), tb.getQualifiedName());
        }
        if (tb.getInterfaces() != null) {
            for (ITypeBinding stb : tb.getInterfaces()) {
                stb = stb.getTypeDeclaration();
                if (stb != null)
                    add(hierarchy, stb.getQualifiedName(), tb.getQualifiedName());
            }
        }
        if (tb.getDeclaredTypes() != null) {
            for (ITypeBinding inner : tb.getDeclaredTypes())
                buildClassHierarchy(inner, hierarchy);
        }
    }

    private static void add(Map<String, List<String>> hierarchy, String st, String t) {
        List<String> types = hierarchy.get(st);
        if (types == null) {
            types = new ArrayList<>();
            hierarchy.put(st, types);
        }
        types.add(t);
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
