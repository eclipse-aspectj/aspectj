package com.aspectj.test;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public abstract class Base {

    @Pointcut //
    abstract void method();


    public static void main(String args[]){
    }
}
