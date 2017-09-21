//package no.kantega;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public abstract class AbstractLoggerAspect {

    @Pointcut
    public abstract void debugJoinPoints();

    @Pointcut
    public abstract void infoJoinPoints();

    @Pointcut
    public abstract void excludedJoinPoints();


    @Pointcut("execution(public String Object+.toString()) || call(public String Object.toString())"
            + "|| call(public int Object.hashCode()) || call(public boolean Object.equals())"
            + "|| execution(public static void Object+.main(String[])) ")
    public void defaultExcludedJoinPoints() {}

    @Pointcut("(infoJoinPoints() && !defaultExcludedJoinPoints() && !excludedJoinPoints())")
    public void infoJoinPointsToLog() {}

/*
    @Pointcut("(debugJoinPoints() && !defaultExcludedJoinPoints() && !infoJoinPointsToLog() && !excludedJoinPoints())")
    public void debugJoinPointsToLog() {}


    @Around("debugJoinPointsToLog()")
    public Object handleDebugJoinPointsToLog(ProceedingJoinPoint thisJoinPoint)
    {
        // do stuff
        try {
            return thisJoinPoint.proceed();
        } catch (Throwable throwable) {
            throw new RuntimeException("foo");
        }
        // then do other stuff
    }
*/

    @Around("infoJoinPoints()")
    public Object handleInfoJoinPointsToLog(ProceedingJoinPoint thisJoinPoint)
    {
        // first do stuff
        try {
            return thisJoinPoint.proceed();
        } catch (Throwable throwable) {
            throw new RuntimeException("foo");
        }
        // then do other stuff
    }
}

