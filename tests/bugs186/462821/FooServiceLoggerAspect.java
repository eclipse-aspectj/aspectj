//package no.kantega;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class FooServiceLoggerAspect extends AbstractLoggerAspect {

    @Pointcut
    @Override
    public void debugJoinPoints() {
    }

    @Pointcut("execution(public * FooService.*(..)) && !debugJoinPoints()")
    @Override
    public void infoJoinPoints() {
    }

    @Pointcut
    @Override
    public void excludedJoinPoints() {
    }
}
