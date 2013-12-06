package com.foo.bar;

import org.aspectj.lang.annotation.*;

@Aspect
public class AspectX {
    @Before("execution(* com.foo.bar.Test.foo())")
    public void advice() {
        System.out.println("Hello");
    }
}
