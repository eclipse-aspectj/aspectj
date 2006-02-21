import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class AfterThrowingTest {

	public static void main(String[] args) {
		try {
			new B().start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// include "JoinPoint" in the argument list
	@AfterThrowing(pointcut = "execution(public void B.start())", throwing = "ex")
	public void handleExceptionJP(JoinPoint jp, Exception ex) {	
	}

	// include "JoinPoint.StaticPart" in the argument list
	@AfterThrowing(pointcut = "execution(public void B.start())", throwing = "ex")
	public void handleExceptionJPSP(JoinPoint.StaticPart jp, Exception ex) {	
	}
	
	// include "JoinPoint.EnclosingStaticPart" in the argument list
	@AfterThrowing(pointcut = "execution(public void B.start())", throwing = "ex")
	public void handleExceptionJPESP(JoinPoint.EnclosingStaticPart jp, Exception ex) {	
	}
	
	// include "JoinPoint" and "JoinPoint.EnclosingStaticPart" in the argument list
	@AfterThrowing(pointcut = "execution(public void B.start())", throwing = "ex")
	public void handleExceptionJPESP(JoinPoint jp1, JoinPoint.EnclosingStaticPart jp, Exception ex) {	
	}
	
	// make sure it still works if "JoinPoint" is second on the argument list
	@AfterThrowing(pointcut = "execution(public void B.start())", throwing = "ex")
	public void handleExceptionJP2(JoinPoint jp, Exception ex) {	
	}
}

class B implements I {
	public void start() throws Exception {
		throw new IllegalArgumentException();
	}	
}

interface I {
	public void start() throws Exception;
}
