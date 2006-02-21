import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class AfterReturningTest {

	public static void main(String[] args) {
		new B1().start();
	}
	
	// include "JoinPoint" in the argument list
	@AfterReturning(pointcut = "execution(public B1 B1.start())", returning = "r")
	public void afterJP(JoinPoint jp, B1 r) {
		r.stop();
	}
	
	// include "JoinPoint.StaticPart" in the argument list
	@AfterReturning(pointcut = "execution(public B1 B1.start())", returning = "r")
	public void afterJPSP(JoinPoint.StaticPart jp, B1 r) {
		r.stop();
	}
	
	// include "JoinPoint.EnclosingStaticPart" in the argument list
	@AfterReturning(pointcut = "execution(public B1 B1.start())", returning = "r")
	public void afterJPESP(JoinPoint.EnclosingStaticPart jp, B1 r) {
		r.stop();
	}
	
	// include "JoinPoint and JoinPoint.EnclosingStaticPart" in the argument list
	@AfterReturning(pointcut = "execution(public B1 B1.start())", returning = "r")
	public void afterJPESP2(JoinPoint jp1, JoinPoint.EnclosingStaticPart jp, B1 r) {
		r.stop();
	}
	
	// make sure it still works if "JoinPoint" is second in the argument list
	@AfterReturning(pointcut = "execution(public B1 B1.start())", returning = "r")
	public void afterJP2(B1 r, JoinPoint jp) {
		r.stop();
	}
}

class B1 {
	
	public B1 start() {
		return new B1();
	}
	
	public void stop() {
	}
	
}
