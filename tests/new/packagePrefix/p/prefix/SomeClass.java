
package p.prefix;

import org.aspectj.testing.Tester;

/** @testcase PUREJAVA PR#574 compile problems loading classes whose package names are suffixes of class names */
public class SomeClass {
    public static void main(String[] args) {
        int i = new p.prefix().hashCode();
        Tester.check(i!=0, "int i = new p.prefix().hashCode()");
    }
}
