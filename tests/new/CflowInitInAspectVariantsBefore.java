import org.aspectj.testing.Tester; 

public class CflowInitInAspectVariantsBefore {
	public static void main(String[] args) { 
		new C().a();
		Tester.checkAllEventsIgnoreDups();
	}
	static {
		Tester.expectEvent("cflow before pc()");
		Tester.expectEvent("cflow before execution(void C.a())");
		Tester.expectEvent("cflowbelow before pc()");
		Tester.expectEvent("cflowbelow before execution(void C.a())");
	}
}
class C {
	void a() {b();}
	private void b() {int i = 1;} // avoid later optimizations
}

aspect A {
	
	pointcut pc() : execution(void C.a());
    // ---- works
    pointcut safety() : !within(A) ;
    // ---- cannot use cflow to exclude itself - reference thereto fails
    //pointcut safety() : !(within(A) && cflow(pc()));
    // ---- cannot use dynamic calculation, because before call happens first?
    //pointcut safety() : !within(A) || (within(A) && if(touched)) ;
    static boolean touched = false;
    // ---- does not address the before advice methods advising themselves
    //pointcut safety() : !(initialization(A.new(..)) || staticinitialization(A));
    // ---- worse way of saying !within(A)
    //pointcut safety() : this(C) || this(CflowInitInAspectVariantsBefore) || this(null);
    //pointcut safety() : this(C) || this(CflowInitInAspectVariantsBefore) || this(null);

    // no bug if ": within(C) && ..." 
	// @testTarget cflow.before.namedpc
	before() : safety() && cflow(pc()) { 
        if (!touched) touched = true;
		Tester.event("cflow before pc()");
	}
	// @testTarget cflow.before.anonpc
	before() : safety() && cflow(execution(void C.a())) { 
        if (!touched) touched = true;
		Tester.event("cflow before execution(void C.a())");
	}
	// @testTarget cflowbelow.before.namedpc
	before() : safety() && cflowbelow(pc()) { 
        if (!touched) touched = true;
		Tester.event("cflowbelow before pc()");
	}
	// @testTarget cflowbelow.before.anonpc
	before() : safety() && cflowbelow(execution(void C.a())) { 
        if (!touched) touched = true;
		Tester.event("cflowbelow before execution(void C.a())");
	}
}
