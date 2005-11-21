package com.aspectj.test;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public class Sub extends Base{

    @Pointcut( "execution(* com.aspectj.test.Base.main(..))" )
    void method(){};

    @Before("method()")
    public void test(){

    }
}
