import org.aspectj.testing.Tester;

public class Within {
    public static void main(String[] args) {
	C c = new C();
	c.mi();

	Tester.check("I.mi within A1");
	Tester.check("I.mi instanceof C");
	Tester.check("I.mi instanceof I");

	c.mc();
    }
}


class C {
    void mc() {}
}

interface I {}

aspect A1 {
    void I.mi() {}
}

aspect A2 {
    declare parents: C implements I;
}



aspect Test {
    before (): execution(* I.*(..)) && within(C) {
	Tester.checkFailed(thisJoinPoint + " I.* within C");
    }
    before (): execution(* I.*(..)) && within(I) {
	Tester.checkFailed(thisJoinPoint + " I.* within I");
    }
    before (): execution(* I.*(..)) && within(A1) {
	Tester.checkEqual(thisJoinPoint.getSignature().getName(), "mi",
			   thisJoinPoint + " I.* within A1");
	Tester.note("I.mi within A1");
    }
    before (): execution(* I.*(..)) && within(A2) {
    }

    before (): execution(* I.*(..)) && this(C) {
	Tester.checkEqual(thisJoinPoint.getSignature().getName(), "mi",
			   thisJoinPoint + " I.* instanceof C");
	Tester.note("I.mi instanceof C");
    }
    before (): execution(* I.*(..)) && this(I) {
	Tester.checkEqual(thisJoinPoint.getSignature().getName(), "mi",
			   thisJoinPoint + " I.* instanceof I");
	Tester.note("I.mi instanceof I");
    }
    before (): execution(* I.*(..)) && this(A1) {
	Tester.checkFailed(thisJoinPoint + " I.* instanceof A1");
    }
    before (): execution(* I.*(..)) && this(A2) {
	Tester.checkFailed(thisJoinPoint + " I.* instanceof A2");
    }
}
