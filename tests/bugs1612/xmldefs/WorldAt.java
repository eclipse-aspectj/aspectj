import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public abstract class WorldAt {

	@Pointcut("execution(* Hello.sayWorld(..))")
    void greeting() {}

}
