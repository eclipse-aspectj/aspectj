import java.io.InvalidClassException;
import org.aspectj.testing.Tester;

public privileged aspect PrivilegedAspect {
	
	pointcut run (Test test) :
		execution(public void run()) && this(test);
		
	before (Test test) : run (test) {
		System.out.println("? PrivilegedAspect.run() i=" + test.i);
	}

}
