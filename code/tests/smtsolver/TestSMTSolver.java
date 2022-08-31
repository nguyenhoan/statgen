package smtsolver;

import org.eclipse.jdt.core.dom.Expression;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Solver;
import com.microsoft.z3.Status;

import static org.junit.Assert.*;

import java.io.File;

import utils.FileIO;
import utils.JavaASTUtil;
import utils.Utils;

public class TestSMTSolver {
    @Rule
    public TestName name = new TestName();
    
    private Context ctx = new Context();
    
    @BeforeClass
    public static void oneTimeSetUp() {
    }

    @Test
    public void test1() throws Exception {
        Expression expr = JavaASTUtil.parseExpression("!(!(a.length <= a.length) || true)");
        ExpressionExtractorParser eep = new ExpressionExtractorParser();
        BoolExpr z3Expr = (BoolExpr) eep.handleExpression(ctx, expr, true);
        Solver s = ctx.mkSolver();
        s.add(z3Expr);
        Status q = s.check();
        assertEquals(Status.UNSATISFIABLE, q);
    }

    @Test
    public void test2() throws Exception {
        Expression expr = JavaASTUtil.parseExpression("(beginIndex > length() - (length() - beginIndex))");
        ExpressionExtractorParser eep = new ExpressionExtractorParser();
        BoolExpr z3Expr = (BoolExpr) eep.handleExpression(ctx, expr, true);
        Solver s = ctx.mkSolver();
        s.add(z3Expr);
        Status q = s.check();
        assertEquals(Status.UNSATISFIABLE, q);
    }

    @Test
    public void test3() throws Exception {
        assertTrue(JavaASTUtil.equivalent("a", "a"));
        assertTrue(!JavaASTUtil.equivalent("a", "!a"));
        assertTrue(!JavaASTUtil.equivalent("a", "b"));
    }

    @Test
    public void test4() throws Exception {
        assertEquals(Status.UNKNOWN, Utils.satcheck("a ^ a"));
    }

    @Test
    public void test5() throws Exception {
        assertEquals(Status.SATISFIABLE, Utils.satcheck("a ^ !a"));
    }

    @Test
    public void test6() throws Exception {
        assertEquals(Status.UNKNOWN, Utils.satcheck("nanos / 1000_000"));
    }

    @Test
    public void test7() throws Exception {
        assertEquals(Status.SATISFIABLE, Utils.satcheck("new int[]{0}[0]"));
    }

    @Test
    public void test8() throws Exception {
        assertEquals("x == Long.MIN_VALUE && y == -1", JavaASTUtil.optimize("((y != 0) && ((x * y) / y != x)) || (x == Long.MIN_VALUE && y == -1)"));
    }

    @Test
    public void test9() throws Exception {
        assertEquals("false", JavaASTUtil.optimize("(int)((long)x * (long)y) != ((long)x * (long)y)"));
    }

    @Test
    public void test10() throws Exception {
        assertEquals("false", JavaASTUtil.optimize("(x * 2) / 2 != x"));
    }

    @Test
    public void test11() throws Exception {
        assertEquals("false", JavaASTUtil.optimize("(x + 1) == x"));
    }

    @Test
    public void test12() throws Exception {
        assertEquals("false", JavaASTUtil.optimize("(x * y) / y != x"));
    }

    @Test
    public void test13() throws Exception {
        assertEquals("x * z * w != x", JavaASTUtil.optimize("(x * y * z * w) / y != x"));
    }

    @Test
    public void test14() throws Exception {
        assertEquals("Long.parseLong(parsed) == Long.MIN_VALUE && multiplier == -1", JavaASTUtil.optimize("((multiplier != 0) && ((Long.parseLong(parsed) * multiplier) / multiplier != Long.parseLong(parsed))) || (Long.parseLong(parsed) == Long.MIN_VALUE && multiplier == -1)"));
    }

    @Test
    public void test16() throws Exception {
        ExpressionExtractorParser eep = new ExpressionExtractorParser();
        Expression expr1 = JavaASTUtil.parseExpression("count < 0");
        BoolExpr z3Expr1 = (BoolExpr) eep.handleExpression(ctx, expr1, true);
        Expression expr2 = JavaASTUtil.parseExpression("count >= 0");
        BoolExpr z3Expr2 = (BoolExpr) eep.handleExpression(ctx, expr2, true);
        Solver s = ctx.mkSolver();
        s.add(z3Expr1);
        s.add(z3Expr2);
        Status q = s.check();
        assertEquals(Status.UNSATISFIABLE, q);
    }

    @Test
    public void test17() throws Exception {
        File file = new File("T:/temp/condition.txt");
        if (!file.exists())
            return;
        String condition = FileIO.readStringFromFile(file.getAbsolutePath());
        condition = JavaASTUtil.optimize(condition);
        Expression e = JavaASTUtil.parseExpression(condition);
        if (e == null)
            System.err.println();
        String[] parts = condition.split("\\|\\|");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            e = JavaASTUtil.parseExpression(part);
            if (e == null)
                System.err.println();
        }
    }
    
    @Test
    public void test18() throws Exception {
        assertEquals(Status.SATISFIABLE, Utils.satcheck("java.lang.Enum.getClass() != (java.lang.Enum<?>)o.getClass() && java.lang.Enum.getDeclaringClass() != (java.lang.Enum<?>)o.getDeclaringClass()"));
    }
    
    @Test
    public void test19() throws Exception {
        assertEquals(Status.UNSATISFIABLE, Utils.satcheck("(i++) < i"));
    }
    
    @Test
    public void test20() throws Exception {
        assertEquals(Status.UNSATISFIABLE, Utils.satcheck("l.size() < 0 || c.width < 0 || d.height < 0 || size() < 0 || width < 0 || height < 0 || size < 0 || length + 1 < 1 || new C() == null"));
    }
    
    @Test
    public void test21() throws Exception {
        assertEquals("name.length() - 1 >= length()", JavaASTUtil.optimize("( name . length() - 1 > length() ) || ( name . length() - 1 >= length() )"));
    }
    
    @Test
    public void test22() throws Exception {
        assertTrue(JavaASTUtil.equivalent("s == null", "s == null || s == null"));
    }
    
    @Test
    public void test23() throws Exception {
        assertEquals(Status.UNSATISFIABLE, Utils.satcheck("4 * s . length() / 4 != s . length()"));
    }
    
    @Test
    public void test24() throws Exception {
        assertEquals(Status.SATISFIABLE, Utils.satcheck("4 * (s.length() / 4) != s.length()"));
    }
    
    @Test
    public void test25() throws Exception {
        assertEquals(Status.SATISFIABLE, Utils.satcheck("s . length() / 4 * 4 != s . length()"));
    }
    
    @Test
    public void test26() throws Exception {
        assertEquals("true", JavaASTUtil.optimize("( \"name\" . length() - 1 >= 0 ) || ( name . length() - 1 > length() )"));
    }
    
    public static void main(String[] args) throws Exception {
        new TestSMTSolver().test1();
    }

}
