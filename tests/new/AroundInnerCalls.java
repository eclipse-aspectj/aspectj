import org.aspectj.testing.Tester;

public class AroundInnerCalls {
    public static void main(String[] args) {
	new Outer().foo();

	Tester.check("Outer.foo() calls Outer.Inner.mi()");
	Tester.check("Outer.foo() calls Outer.InnerRandom.nextHook(..)");
	Tester.check("Outer.InnerRandom.nextHook(..) calls Outer.InnerRandom.next(..)");
	Tester.check("Outer.Inner.mi() calls PrintStream.println(..)");

        Tester.check("X.toString()");
	Tester.check("Outer.foo() calls Outer.1.nextInt(..)");
    }
}

class Outer {
    private class Inner extends Object {
	public void mi() {
	    System.out.println(".");
	}
    }

    public void foo() {
	new Inner().mi();
	new InnerRandom().nextHook(2);

        new java.util.Random() { public String toString() { Tester.note("X.toString()"); return "X"; } }.nextInt(2);
    }

    private class InnerRandom extends java.util.Random {
	public int nextHook(int bits) {
	    return next(bits);
	}
    }
}

aspect A {
    Object around(): call(* *(..)) && !within(A) {
//	System.out.println
        Tester.note
            (thisEnclosingJoinPointStaticPart.getSignature().toShortString() +
             " calls " + thisJoinPointStaticPart.getSignature().toShortString());
	return proceed();
    }
    
    before(Object caller, Object callee):
       this(caller) && target(callee) && call(* *(..)) && !within(A)
    {
        System.out.println(thisEnclosingJoinPointStaticPart.getSignature().toShortString() +
             " calls " + thisJoinPointStaticPart.getSignature().toShortString());
	System.out.println
            (caller + "." + thisEnclosingJoinPointStaticPart.getSignature().getName() + 
             " calls " + callee + "." + thisJoinPoint.getSignature().getName());
    }
}
