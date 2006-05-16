package test;

import test1.PointcutProvider;

public aspect PointcutConsumer percflow(flow()) {

        // compiler issues the following line with
        // can not find pointcut test on test.PointcutConsumer
        pointcut mytest(): PointcutProvider.test();
 
        // this also does not work with the same error message
        pointcut mytest2(): test1.PointcutProvider.test();

        pointcut flow(): mytest() || mytest2();
}