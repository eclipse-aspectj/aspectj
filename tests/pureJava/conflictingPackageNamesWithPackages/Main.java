package conflictingPackageNamesWithPackages;

import org.aspectj.testing.*;

public class Main {
    public static void main(String[] args) {
        new Main().go();
        Tester.checkAllEvents();
    }

    public void go() {

        String str0 = new String("String:String");
        java.lang.String str1 = new String("java.lang.String:String");
        String str2 = new java.lang.String("String:java.lang.String");
        java.lang.String str3 = new java.lang.String("String:java.lang.java.lang.String");

        Tester.checkEqual(str0, "String:String");
        Tester.checkEqual(str1, "java.lang.String:String");
        Tester.checkEqual(str2, "String:java.lang.String");
        Tester.checkEqual(str3, "String:java.lang.java.lang.String");

        Tester.event("Driver.go");
    }    

    static {
        Tester.expectEvent("Driver.go");
    }
}
