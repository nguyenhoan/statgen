package eval;

import translation.Translator;

public class ConstructCodeFromClauses {

    public static void main(String[] args) {
        String path = "T:/spectrans/jdk8-java-javax";
        Translator.construct(path, path +"/test.trans");
    }

}
