package com.example;

 import org.aspectj.lang.ProceedingJoinPoint;
 import org.aspectj.lang.annotation.Around;
 import org.aspectj.lang.annotation.Aspect;
 import org.aspectj.lang.annotation.Pointcut;

 @Aspect
 public class MyAspect {

     @Pointcut("execution(* *(..,@MyParameterAnnotation (String),..))")
     public void anyMethodCallWithMyParameterAnnotation() {
     }

     @Around("anyMethodCallWithMyParameterAnnotation()")
     public Object aroundMethodWithMyParameterAnnotation(ProceedingJoinPoint pjp) throws Throwable {
         throw new RuntimeException("OK");
     }
 }

