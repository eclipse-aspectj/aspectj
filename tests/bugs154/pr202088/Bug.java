package tracing;
import org.aspectj.lang.annotation.*;

@Aspect
public abstract class Bug {
	@Pointcut
	public abstract void traced(Object thiz);
}