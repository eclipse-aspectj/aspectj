import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;

aspect SampleAspect {
//@Aspect public class SampleAspect {
	//@Before("@annotation(logMe)") public void beforeAdvice(JoinPoint thisJoinPoint, LogMe logMe) {
before(LogMe logMe): @annotation(logMe) {
		System.out.println(thisJoinPoint);
	}
}
