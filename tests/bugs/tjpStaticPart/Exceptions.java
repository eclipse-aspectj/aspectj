package tjpStaticPart;

import java.io.*;

import org.aspectj.lang.*;

public aspect Exceptions {
	
	pointcut exceptionMethods () :
		call(java.io.*.new(..) throws FileNotFoundException);
		
	Object around () throws FileNotFoundException : exceptionMethods() && !within(Exceptions) {
		System.err.println("before: " + thisJoinPoint.getStaticPart());
		Object obj = proceed();
		System.err.println("after: " + thisJoinPoint.getStaticPart());
		return obj;	
	}
}