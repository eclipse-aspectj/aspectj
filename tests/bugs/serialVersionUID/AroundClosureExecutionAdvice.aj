import java.io.InvalidClassException;
import org.aspectj.testing.Tester;

public aspect AroundClosureExecutionAdvice {
	
	pointcut run () :
		execution(public void run());
		
	void around () : run () {
		Runnable runnable = new Runnable() {
			public void run () {
				System.out.println("> AroundClosureExecutionAdvice.run()");
		
				proceed();
		
				System.out.println("< AroundClosureExecutionAdvice.run()");
			}
		};
		runnable.run();
	}

}
