package packagevisibility;
import org.aspectj.testing.Tester;

import packagevisibility.testPackage.*;

public class PackagesAndAdviceCf
{
    public static void main(String[] args) { test(); }

    public static void test() {
        packagevisibility.testPackage.Class1 c1 =
            new packagevisibility.testPackage.Class1();
        Tester.checkEqual(c1.doIt("-1"),
                          "-1-advised-advised1-1-class1",
                          "publically visible");
        Tester.checkEqual(c1.doItToClass2("-2"),
                          "-2-advised-advised1-2-class2",
                          "package visible");
    }

}

aspect A {
    static String message = "-advised";

    String around(String s):
        call(String doIt(String)) && args(s) &&
	    (target(packagevisibility.testPackage.Class1) ||
                  target(packagevisibility.testPackage.Class2)) {  // Cf type not visible
        String result = s + message;
	result += A1.message;
	return result + proceed(s);
    }

}

class A1 {
    static String message = "-advised1";
}
