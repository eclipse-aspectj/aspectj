import org.aspectj.testing.Tester;

import org.aspectj.lang.reflect.*;

public aspect CatchAdvice {
    public static void main(String[] args) { test(); }

    public static void test() {
	new C().foo();
	Tester.check(ranAdvice1, "advice on *'s");
	Tester.check(ranAdvice2, "advice on C");
	Tester.check(!ranAdvice3, "advice on C and Error");
    }

    private static boolean ranAdvice1 = false;
    before(Throwable t): handler(Throwable+) && args(t){
	//int n = thisJoinPoint.parameters.length;
	//System.out.println("caught "+t+" "+n+" params");
	Tester.check(((CatchClauseSignature)thisJoinPoint.getSignature()).
                     getParameterType() == ArithmeticException.class,
                     "right catch clause");
	ranAdvice1 = true;
   }
    after(Throwable t): handler(Throwable+) && args(t) {
	//System.out.println("after catch");
    }

    static boolean ranAdvice2 = false;
    after(ArithmeticException t):
        this(C) && handler(ArithmeticException) && args(t) {
	//System.out.println("(in C) caught "+t);
	ranAdvice2 = true;
    }

    static boolean ranAdvice3 = false;
    after(Error e):
        within(C) && handler(Error+) && args(e){
	//System.out.println("(in C) caught "+e);
	ranAdvice3 = true;
    }
}

class C {
    public void foo() {
	int x=0;
	try {
	    int y = 1/x;
	} catch (ArithmeticException e) {
	    //System.out.println("caught arithmetic exception");
	}
    }
}
