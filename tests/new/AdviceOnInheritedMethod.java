
import org.aspectj.testing.Tester;

import org.aspectj.lang.*;

public aspect AdviceOnInheritedMethod {
    public static void main(String[] args) { test(); }

    public static void test() {
        SuperC sc = new SubC();
        Tester.checkEqual(sc.doIt(":foo"), 
                           ":foo:beforeDoIt:inSubC:foo:beforeDoIt1:inSubC:doIt1",
                           "SubC.doIt");
         Tester.checkEqual(new SuperC().doIt(":foo"), 
                            ":foo:beforeDoIt1:inSuperC:doIt1",
                            "SuperC.doIt");
         Tester.checkEqual(new SubC().packageDoIt(":foo"), 
                            ":foo:beforePackageDoIt:inSubC:foo",
                            "SubC.packageDoIt");
    }
 
    String getTargetName(JoinPoint thisJoinPoint) {
        return thisJoinPoint.getTarget().getClass().getName();
    }

    String around(String a): 
        target(*) && call(String packageDoIt(String)) && args(a)
    {
	return a+
            ":beforePackageDoIt:in"+getTargetName(thisJoinPoint)
            + proceed(a);
    }
    String around(String a):
        target(SubC) && call(String doIt(String)) && args(a) {
	return a+
            ":beforeDoIt:in"+getTargetName(thisJoinPoint)
            + proceed(a);
    }
    String around(String a):
        target(SuperC) && call(String doIt1(String)) && args(a) {
	return a+
            ":beforeDoIt1:in"+getTargetName(thisJoinPoint)
            + proceed(a);
    }
}

class SuperC {
    public String doIt(String arg) {
        return doIt1(arg);
    }
    protected String doIt1(String arg) {
        return ":doIt1";
    }
    String packageDoIt(String arg) {
        return arg;
    }
}

class SubC extends SuperC {
    public String doIt(String arg) {
        return super.doIt(arg);
    }
}

