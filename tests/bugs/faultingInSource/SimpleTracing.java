aspect SimpleTracing
{
	pointcut traceCall():
		call (void SampleClass.foo(..));
		
	before(): traceCall()
	{
		System.out.println ("Entering: " + thisJoinPoint);
	}
	
	after(): traceCall()
	{
		System.out.println ("Exiting: " + thisJoinPoint);
	}
}
