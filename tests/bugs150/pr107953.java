import java.lang.annotation.*;
import org.aspectj.lang.annotation.*;

@Aspect
public class pr107953 {
	
	@AfterThrowing(pointcut="execution(* Foo.*(..))",throwing="RuntimeException")
	public void missingBindingOfThrowingFormal() {
		System.out.println("failure");
	}
		
}

class Foo {
	void bar() {
		throw new RuntimeException();
	}
}