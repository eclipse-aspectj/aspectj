package psub;

import org.aspectj.lang.annotation.SuppressAjWarnings;
import org.aspectj.testing.Tester;
import pc.C;

aspect A {
	private static interface Marker {}
	@SuppressAjWarnings("adviceDidNotMatch")
    before(): call(String SubC.getMyPackage()) {
    	Tester.checkFailed("shouldn't ever run this " + thisJoinPoint);
    }
    before(): !target(Marker) && !this(Marker) && !args(Marker, ..) && call(String C.getMyPackage()) {
    	Tester.note("C.getMyPackage on " + thisJoinPoint.getTarget().getClass().getName());
    }
}
