import org.aspectj.testing.Tester;

// aspect inheritance and advice, introduction

public class Driver {
    public static void test() {
        C1 c1 = new C1();
        
        Tester.checkEqual(c1.foo(), "from A2", "sub-aspects");
        Tester.checkEqual(c1.bar(), 
                           "from A0 #2", 
                           "sub-aspects and position");

	// the around advice lexically in A1 runs twice, once for each concrete
	// sub-aspects.  putting advice on a concrete pointcut in an abstract
	// aspect will always have this odd behavior, and possibly should be illegal
        Tester.checkEqual(c1.getString(), 
                           ":A2:A1:A1:C1",
                           "multiple arounds");
    }

    public static void main(String[] args) { test(); }
}

class C1 {
    String getString() {
        return ":C1";
    }
}

abstract aspect A1 {
    String C1.foo() {
	return "from A1";
    }
    
    String C1.bar() {
	return "from A1";
    }

    String around(): 
            call(String C1.getString()) {
        return ":A1" + proceed();
    }
}

aspect A2 extends A1 {
    String C1.foo() {
	return "from A2";
    }

    String around(): 
            call(String C1.getString()) {
        return ":A2" + proceed();
    }
    
}

aspect A0 extends A1 {
    // multiple conflicting declarations in the same aspect are now an error
    //String C1.bar() {
    //	return "from A0 #1";
    //}
    
    String C1.bar() {
	return "from A0 #2";
    }
}

