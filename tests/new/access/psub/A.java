package psub;

import org.aspectj.testing.Tester;
import pc.C;

aspect A {
    before(): call(String SubC.getMyPackage()) {
	//XXXTester.checkFailed("shouldn't ever run this " + thisJoinPoint);
    }
    before(): call(String C.getMyPackage()) {
	Tester.note("C.getMyPackage on " + thisJoinPoint.getTarget().getClass().getName());
    }
}
