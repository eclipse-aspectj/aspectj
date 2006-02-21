import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
class AbstractTracer 
{
	
    @Pointcut("(execution(public * Foo.anotherMethod*(..)) || execution(public * Foo.methodA(..))) && this(obj)")
    protected void methodExec(Object obj){};
        
    @Before("methodExec(obj)")
    public void beforeMethodExec(JoinPoint thisJoinPoint, Object obj) {
      	System.out.println("Before " + thisJoinPoint.getSignature().toString());
    }
	
}


class Foo {

	public void methodA() {
	}
	
	public void anotherMethod() {
	}
	
}
