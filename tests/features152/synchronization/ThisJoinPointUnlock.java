import org.aspectj.lang.reflect.*;

aspect TJPAspect {
	before(): withincode(void ThisJoinPointUnlock.nonStaticMethod()) {
		if (thisJoinPoint.getSignature() instanceof UnlockSignature) {
			System.err.println("match.toString(): "+thisJoinPoint.toString());
			System.err.println("match.toShortString(): "+thisJoinPoint.toShortString());
			System.err.println("match.toLongString(): "+thisJoinPoint.toLongString());
		}
		
		// SHORT => shorttypenames, no args, no throws, no modifiers, short type names
		// MIDDLE=> args included
		// LONG  => modifiers included
    }
	
//	before(): withincode(void ThisJoinPointLock.nonStaticMethod()) {
//		if (thisJoinPoint.getSignature() instanceof MethodSignature) {
//			System.err.println("match.toString(): "+thisJoinPoint.toString());
//			System.err.println("match.toShortString(): "+thisJoinPoint.toShortString());
//			System.err.println("match.toLongString(): "+thisJoinPoint.toLongString());
//		}
//		
//		// SHORT => shorttypenames, no args, no throws, no modifiers, short type names
//		// MIDDLE=> args included
//		// LONG  => modifiers included
//    }

}

public class ThisJoinPointUnlock {
	public static void main(String[] args) {
		ThisJoinPointUnlock b = new ThisJoinPointUnlock();
		b.nonStaticMethod();
		b.staticMethod();
	}
	
	public void nonStaticMethod() {
		synchronized (this) {
			staticMethod();
		}
	}

	public void staticMethod() {
		synchronized (ThisJoinPointUnlock.class) {
		}
	}
	
}
