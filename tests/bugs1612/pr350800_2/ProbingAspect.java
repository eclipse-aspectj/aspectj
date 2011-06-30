package test.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

public aspect ProbingAspect extends AbstractProbingAspect<String> {

    pointcut adapterMethodExecution(): execution(String test.aop.Adapter.execute(String));

    @Override
    protected String extractFunctionName(String command) {   
        return String.valueOf(command);
    }
}
