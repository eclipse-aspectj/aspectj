// various forms of package name pattern matching work

import org.aspectj.testing.Tester;

import p1.C1;
import p1.p2.C2;

public class Driver {
    public static void test() {
        Top t = new Top();
        p1.C1 c1 = new p1.C1();
        p1.p2.C2 c2 = new p1.p2.C2();

        Tester.checkEqual(t.bar(), 11, "top.bar()");

        Tester.checkEqual(c1.bar(), 1111, "c1.bar()");

        Tester.checkEqual(c2.bar(), 1011, "c2.bar()");
    }
    public static void main(String[] args) { test(); }
}

class Top {
  public int bar() {
    return 1;
  }
}

aspect TopAdvice {
    int around(): target(*) && call(int *()) {
        int result = proceed();
        return result+10;
    }

    int around(): call(int p1.*.*()) {
        int result = proceed();
        return result+100;
    }

    int around(): call(int p1..*.*()) {
        int result = proceed();
        return result+1000;
    }
}
