package psub;

import org.aspectj.testing.Tester;
import pc.C;

aspect A {
	private static interface Marker {}
	
    before(): call(String SubC.getMyPackage()) {
	//XXXTester.checkFailed("shouldn't ever run this " + thisJoinPoint);
    }
    before(): !target(Marker) && !this(Marker) && !args(Marker, ..) && call(String C.getMyPackage()) {
	Tester.note("C.getMyPackage on " + thisJoinPoint.getTarget().getClass().getName());
    }
}
