import org.aspectj.lang.*;

public class AfterThrowingAdviceSyntaxError {

	public static void main(String[] args) {
		perform();
	}

	private static void perform() {
		Object nullObj = null;
		nullObj.toString();
	}
}

aspect ExceptionLoggerAspectV2 
{

	pointcut exceptionLogMethods()
		: call(* *.*(..)) && !within(ExceptionLoggerAspectV2);

	after() thowing(Throwable ex) : exceptionLogMethods() {
		Signature sig = thisJoinPointStaticPart.getSignature();
		System.out.printl("WARNING: " 
			+ sig.getDeclaringType().getName() + " "
			+ sig.getName() + " "
			+ "Exception logger aspect " + ex);
	}
}