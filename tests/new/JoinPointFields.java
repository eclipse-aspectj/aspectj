import org.aspectj.testing.Tester;

import org.aspectj.lang.reflect.*;

public class JoinPointFields {
    public static void main(String[] args) {
        Tester.checkEqual(new JoinPointFields().foo("xxx-"), "xxx-arg", "parameterNames");
    }

    public String foo(String arg) {
        return arg;
    }
}

aspect A {
    String around(String arg):
        this(JoinPointFields) &&
        execution(String foo(String)) &&
        args(arg)
        {
            return arg + (((MethodSignature)thisJoinPoint.getSignature()).
                          getParameterNames())[0];
        }
}
