import org.aspectj.lang.reflect.*;

aspect TJPAspect {
	before(): withincode(void ThisJoinPointLock.nonStaticMethod()) {
		if (thisJoinPoint.getSignature() instanceof LockSignature) {
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

public class ThisJoinPointLock {
	public static void main(String[] args) {
		ThisJoinPointLock b = new ThisJoinPointLock();
		b.nonStaticMethod();
		b.staticMethod();
	}
	
	public void nonStaticMethod() {
		synchronized (this) {
			staticMethod();
		}
	}

	public void staticMethod() {
		synchronized (ThisJoinPointLock.class) {
		}
	}
	
}
