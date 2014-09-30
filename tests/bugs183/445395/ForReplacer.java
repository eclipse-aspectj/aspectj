//package org.acmsl.pocs.lambdafor;

import java.util.Arrays;

public aspect ForReplacer
{
    /**
     * Intercepting for loop constructs.
     */
    pointcut forLoopPointcut():
//        execution(* Sample.sampleCode(..)); // -> error
        execution(* forloop(..));
//        && args(Collect, ..);

    /**
     * Replacing the loop construct.
     */
    Object around() : forLoopPointcut()
    {
//        return proceed();
//        Collection<Integer> result = new ArrayList<>();
//        result.addAll(new ControlFlow().externallyDrivenForloop(new ControlFlowDriver(), Arrays.asList(4, 5, 6), (i) -> { System.out.println(i); return i;}));
//        return result;
        return new ControlFlow().externallyDrivenForloop(new ControlFlowDriver(), Arrays.asList(4, 5, 6), (i) -> { System.out.println(i); return i;});
    }

    /**
     * Intercepting for loop constructs.
     *
    pointcut forLoopPointcut(ControlFlow loop):
        call(* ControlFlow.forloop(..))
        && target(loop);
//        && args(items, ..);

    /**
     * Replacing the loop construct.
     *
    Collection around(ControlFlow loop) : forLoopPointcut(loop)
    {
        return loop.externallyDrivenForloop(new ControlFlowDriver(), Arrays.asList(4, 5, 6), (i) -> { System.out.println(i); return i;});
//        return new ControlFlow().externallyDrivenForloop(new ControlFlowDriver(), Arrays.asList(4, 5, 6), (i) -> { System.out.println(i); return i;});
    }
     */
}
