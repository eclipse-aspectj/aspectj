package test135.pack;

import org.aspectj.lang.*;
import org.aspectj.lang.reflect.*;

// a first try at a library class in the test suite
public abstract aspect JoinPointFields {
    public String className;
    public String methodName;
    public String[] parameterNames;
    public Class[] parameterTypes;
    public Object[] parameters;

    protected int protectedField = 42;

    abstract protected pointcut onTypes();

    before(): call(!static * *(..)) && onTypes() && !within(JoinPointFields+) {
        System.out.println(thisJoinPoint);

        Signature sig           = thisJoinPoint.getSignature();
        CodeSignature codeSig   = (CodeSignature) sig;
        //ReceptionJoinPoint rjp  = (ReceptionJoinPoint) thisJoinPoint;
        
        className       = sig.getDeclaringType().getName();
        System.out.println(className);

        methodName      = sig.getName();
        parameterNames  = codeSig.getParameterNames();
        parameterTypes  = codeSig.getParameterTypes();
        //parameters      = rjp.getParameters();
        parameters      = thisJoinPoint.getArgs();
        System.out.println("DONE: " + thisJoinPoint);
    }
}
