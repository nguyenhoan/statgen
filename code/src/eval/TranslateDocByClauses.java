package eval;

import translation.Translator;

public class TranslateDocByClauses {

    public static void main(String[] args) {
        String path = "T:/spectrans/jdk8-java-javax";
        Translator.translate(path + "/test.doc", path, path +"/test.trans");
    }

}
