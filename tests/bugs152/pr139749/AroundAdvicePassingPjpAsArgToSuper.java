
package a.b.c;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

class ASuperClass {

	protected void takesApjp(ProceedingJoinPoint pjp) {
		System.out.println(pjp);
	}

}

@Aspect
public class AroundAdvicePassingPjpAsArgToSuper extends ASuperClass {

	@Around("execution(* foo())")
	public Object passesPjp(ProceedingJoinPoint pjp) throws Throwable {
		takesApjp(pjp);
		Object ret = pjp.proceed();
		return ret;
	}
	
	public static void main(String[] args) {
		new C().foo();
	}
}

class C {

	public Object foo() {
		return new Object();
    }
}