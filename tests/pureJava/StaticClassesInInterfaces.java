import org.aspectj.testing.Tester;

import anotherPackage.*;

public class StaticClassesInInterfaces {
    public static void main(String[] args) {
        new StaticClassesInInterfaces().realMain(args);
    }
    public void realMain(String[] args) {
        AnotherPackageInterface.Inner inner = null;
        Tester.check(true, "compiled!");
    }
}
