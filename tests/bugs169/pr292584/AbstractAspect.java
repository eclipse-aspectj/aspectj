
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public abstract class AbstractAspect {
    @Around("execution(* ClassWithJoinPoint.getValue(..))")
    public Object ClassWithJoinPoint_getValue() {
        return getValueReplacement();
    }

    protected abstract Boolean getValueReplacement();
}