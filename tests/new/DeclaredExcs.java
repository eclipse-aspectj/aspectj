import org.aspectj.testing.Tester;

public class DeclaredExcs {
    public static void main(String[] args) { test(); }

    public static void test() {
	Foo foo = new Foo();

	Tester.checkEqual(foo.foo(), "success", "foo()");
	try {
	    foo.bar(false);
        } catch (Exception e) {
	    Tester.check(false, "shouldn't catch");
	}

	try {
	    Bar bar = new Bar(false);
	} catch (MyException e) {
	    Tester.check(false, "shouldn't catch");
	}

	try {
	    Bar bar = Bar.getNewBar(true);
	    Tester.check(false, "shouldn't get here");
	} catch (MyException e) {
	    Tester.check(true, "should catch");
	}
    }
}

class Foo {
    public void bar(boolean throwIt) throws Exception {
	if (throwIt) {
	    throw new MyException("you asked for it");
        }
    }

    public String foo() {
	try {
	    bar(false);
	} catch (Exception exc) {
	    Tester.check(false, "shouldn't catch anything");
	}

	try {
	    bar(true);
	} catch (MyException exc) {
	    return "success";
	} catch (Exception e1) {
	    return "failure";
	}
	
	return "failure";
    }
}

class Bar {
    String value;

    public static Bar getNewBar(boolean throwIt) throws MyException {
	return new Bar(throwIt);
    }

    public Bar(boolean throwIt) throws MyException {
	if (throwIt) {
	    throw new MyException("you asked for it from constructor");
	}
	value = "boolean";
    }

    public Bar(String value) {
	this.value = value;
    }
}


class MyException extends Exception {
    public MyException(String label) {
	super(label);
    }
}

aspect A {
    before (): (this(*) && execution(* *(..)) || execution(new(..))) && !within(A) {
	//System.out.println("entering: "+thisJoinPoint);
    }

    after (): (this(*) && execution(* *(..)) || execution(new(..))) && !within(A) {
	//System.out.println("exiting: "+thisJoinPoint);
    }  
    Object around(): this(*) && call(* *(..)) {    
	//System.out.println("start around: "+thisJoinPoint);
	Object ret = proceed();
	//System.out.println("end around: "+thisJoinPoint);
	return ret;
    }
}
