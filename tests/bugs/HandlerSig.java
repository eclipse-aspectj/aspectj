import org.aspectj.testing.Tester;

import org.aspectj.lang.reflect.*;

public class HandlerSig {

	public void doSomething() {
		// Get around "unreachable code error...
		if (true)
		{
			throw new BusinessException("Surprise!!");
		}
		System.out.println("Busy doing something.");
	}

	public static void main(String[] args) {
		try {
			HandlerSig m = new HandlerSig();
			m.doSomething();
		} catch (BusinessException be) {
			Tester.checkEqual(be.getMessage(), "Surprise!!");
		}
	}
}

class BusinessException extends RuntimeException {
	BusinessException(String message) {
		super(message);
	}
}

aspect AppMonitor {
	pointcut problemHandling() : handler(Throwable+);

	before() : problemHandling() {
		CatchClauseSignature cSig =
			(CatchClauseSignature) thisJoinPointStaticPart.getSignature();

		Tester.checkEqual(cSig.getParameterType(), BusinessException.class);
		Tester.checkEqual(cSig.getParameterName(), "be");
	}
}
