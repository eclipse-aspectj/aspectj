import org.aspectj.testing.*;

public class HasAspect {
    static boolean p = false;
    public static void main(String[] args) {
	HasAspect c = new HasAspect();
        c.someMethod();
        Tester.check(p, "p advice was not run");

	Tester.check(A.aspectOf(c).advised, "hasaspect(A)");
	Tester.check(B.aspectOf().advised, "hasaspect(B)");
	Tester.check(C.aspectOf().advised, "hasaspect(C)");
    }

    public void someMethod() {}        
}

aspect A pertarget(target(HasAspect)) {
    boolean advised = false;

    pointcut p(): call(void someMethod()) && hasaspect(A);
    //pointcut p(): receptions(void someMethod()) && instanceof(HasAspect);
    
    before() : p() {
        HasAspect.p = true;
    }
}

aspect B issingleton() {
    boolean advised = false;
}

aspect C percflow(entries()) {
    pointcut entries(): executions(void HasAspect.main(String[]));

    boolean advised = false;
}


aspect TestAspect {
    before(A a, B b, C c) : call(void someMethod()) 
                                    && hasaspect(a) && hasaspect(b) && hasaspect(c)
    {
	a.advised = true;
	b.advised = true;
	c.advised = true;
    }
}    
