package com.foo.bar;

public privileged aspect TestAspect {

      pointcut TestToArray(Test mt) :
                target(mt) &&
                !within(TestAspect);


    Object[] around(Test mt, Object[] objs) :
            TestToArray(mt) &&
            args(objs) &&
            execution(Object[] Test.getObjs(Object[])) {

        objs = proceed(mt, objs);
        System.out.println("GO Aspects!");
        return objs;
    }
}
