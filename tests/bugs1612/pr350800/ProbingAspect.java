package test.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class ProbingAspect extends AbstractProbingAspect<String> {

    @Pointcut("execution(String test.aop.Adapter.execute(String))")
    protected void adapterMethodExecution() {};
       
    @Override
    protected String extractFunctionName(String command) {   
        return String.valueOf(command);
    }
}
