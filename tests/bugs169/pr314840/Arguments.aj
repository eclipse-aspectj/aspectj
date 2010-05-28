package com.test;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.annotation.Pointcut;

public aspect Arguments {

//    @Pointcut("execution(@Test * *(..)) && @annotation(test) && @within( test1) && this(test2)")
 //   public void unit(Test2 test2, Test1 test1, Test test) {
  //  }

    @Around("execution(@Test * *(..)) && @annotation(test) && @within( test1 ) && this(test2)")
    public void test( ProceedingJoinPoint pjp,
              Test test2,
                      Test1 test1,
                      Test test){

    }
}

