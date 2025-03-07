import org.aspectj.lang.annotation.SuppressAjWarnings;

aspect SimpleTracing
{
	pointcut traceCall():
		call (void SampleClass.foo(..));
		
	@SuppressAjWarnings("adviceDidNotMatch") before(): traceCall()
	{
		System.out.println ("Entering: " + thisJoinPoint);
	}
	
	@SuppressAjWarnings("adviceDidNotMatch") after(): traceCall()
	{
		System.out.println ("Exiting: " + thisJoinPoint);
	}
}
