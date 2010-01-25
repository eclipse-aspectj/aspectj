
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class ConcreteAspect extends AbstractAspect {
    @Override
    protected Boolean getValueReplacement() {
        return Boolean.TRUE;
    }
}
