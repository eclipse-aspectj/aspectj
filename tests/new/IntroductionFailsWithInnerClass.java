
import org.aspectj.testing.Tester;

// PR#129

public aspect IntroductionFailsWithInnerClass {
    public static void main(String[] args) { test(); }
    public static void test() {
        Tester.checkEqual(new M().s, "m", "introduction");
    }
    //introduction M {
	public String M.s;
        //}
    /*static*/ after(M m): target(m) && execution(new (..)) {
	    m.s = "m";
	}
}

class M { 
    class LabeledPairLayout extends Object { }
}
