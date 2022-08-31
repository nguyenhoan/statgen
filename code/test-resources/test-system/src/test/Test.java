package test;

import java.util.EmptyStackException;

public class Test {
    final int f = 10;
    final String STR = "String";
    boolean b;
    String s;
    
    void s() {
        s(STR);
    }
    
    void m1() {
        this.s = new String();
        m2(this.s);
    }
    
    void m2(String s) {
        if (s == null)
            throw new NullPointerException();
    }
    
    void s(String s) {
        s = new String();
        if (s.isEmpty())
            throw new EmptyStackException();
    }
    
    boolean getB() {
        return b;
    }
    
    void p2() {
        int i;
        if ((i = getF()) == -1)
            throw new NullPointerException();
    }
    
    void p1() {
        a = {8,8,8};
        int i = f > 0 ? f : 0;
        if (i < 0)
            throw new NullPointerException();
    }
    
    void p() {
        if (b = getB())
            throw new NullPointerException();
    }
    
    void q() {
        q(0.75);
    }
    
    void q(float f) {
        if (Float.isNaN(f))
            throw new NumberFormatException();
    }
    
    int getF() {
        return f;
    }
    
    int same(int i) {
        return i;
    }
    
    void n() {
        int i = same(f);
        m(i);
    }
    
    void m() {
        m(0);
        new Test();
    }
    
    void m(int i) {
        if (i < 0)
            throw new IndexOutOfBoundsException();
    }

    private static byte[] base64ToByteArray(String s, boolean alternate) {
        byte[] alphaToInt = (alternate ?  altBase64ToInt : base64ToInt);
        int sLen = s.length();
        int numGroups = sLen/4;
        if (4*numGroups != sLen)
            throw new IllegalArgumentException(
                "String length must be a multiple of four.");
    }

}
