import org.aspectj.testing.Tester;
import java.util.*;

public class CallTypesI {
    public static void main(String[] args) {
	C1a c1a = new C1a();

	preTest("c1a.mI()");
	c1a.mI();
	test("static c1a, static i0, static i1a, instanceof c0, instanceof c1a, instanceof i0, instanceof i1a, ");

	preTest("c1a.mC()");
	c1a.mC();
	test("static c0, static c1a, instanceof c0, instanceof c1a, instanceof i0, instanceof i1a, ");

	C0 c0 = c1a;

	preTest("(C c = c1a).mC()");
	c0.mC();
	test("static c0, instanceof c0, instanceof c1a, instanceof i0, instanceof i1a, ");
    }

    public static void preTest(String str) {
        A.noteAdvice = A.noteAdviceStar = "";
        msg = str;
    }

    static String msg;
    public static void test(String t1) {
	test(t1, t1);
    }

    public static void test(String baseString, String starString) {
	Tester.checkEqual(sort(A.noteAdvice),     sort(baseString), "base: "+msg);
	//Tester.checkEqual(sort(A.noteAdviceStar), sort(starString), "star: "+msg);
    }
    private final static Collection sort(String str) {
        SortedSet sort = new TreeSet();
        for (StringTokenizer t = new StringTokenizer(str, ",", false);
             t.hasMoreTokens();) {
            String s = t.nextToken().trim();
            if (s.length() > 0) sort.add(s);
        }
        return sort;
    }
}

interface I0 {
    public void mI();
}

interface I1a extends I0 { }

interface I1b extends I0 { }

////interface I2 extends I1a, I1b {}


class C0 {
    public void mC() { }
}

class C1a extends C0 implements I1a {
    public void mI() { }
}
class C1b extends C0 implements I1b {
    public void mI() { }
}

aspect A {
    static String noteAdvice = "";
    static String noteAdviceStar = "";

    before(): call(void C0.mC()) {
	noteAdvice += "static c0, ";
    }
    
    before(): call(void C1a.mC()) || call(void C1a.mI()) {
	noteAdvice += "static c1a, ";
    }
    
    before(): call(void C1b.mC()) || call(void C1b.mI()) {
	noteAdvice += "static c1b, ";
    }

    before(): call(void I0.mI()) {
	noteAdvice += "static i0, ";
    }
    before(): call(void I1a.mI()) {
	noteAdvice += "static i1a, ";
    }
    before(): call(void I1b.mI()) {
	noteAdvice += "static i1b, ";
    }

    before(): target(C0) && call(* *(..)) { noteAdvice += "instanceof c0, "; }
    before(): target(C1a) && call(* *(..)) { noteAdvice += "instanceof c1a, "; }
    before(): target(C1b) && call(* *(..)) { noteAdvice += "instanceof c1b, "; }
    before(): target(I0) && call(* *(..)) { noteAdvice += "instanceof i0, "; }
    before(): target(I1a) && call(* *(..)) { noteAdvice += "instanceof i1a, "; }
    before(): target(I1b) && call(* *(..)) { noteAdvice += "instanceof i1b, "; }
}
