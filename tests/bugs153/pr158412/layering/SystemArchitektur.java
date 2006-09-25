package layering;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;


@Aspect
public class SystemArchitektur {
    @Pointcut("within(dao.*)")
    public void inDAOLayer() {}
    
}


