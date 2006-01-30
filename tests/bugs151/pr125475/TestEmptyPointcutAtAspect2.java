import org.aspectj.lang.annotation.*;

@Aspect
public class TestEmptyPointcutAtAspect2 {

	@Pointcut("")
	protected void scope () {}
	
	@Before("within(*) && scope()")
	public void m() {
		System.err.println("Here!");
	}
}

class A {
	
	String s;
	int i;
	
	public static void main(String[] args) {
		new A().foo();
	} 
	
	public void foo() {
		i=4;
		s="hello";
	}
}