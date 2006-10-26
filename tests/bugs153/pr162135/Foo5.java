//package com.foliofn.infra.logging;

import java.lang.reflect.Field;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect public class Foo5 {

	public void m() {
	  new RuntimeException("hello");
	}
	  public static void main(String[] argv) {
		  
	  }
	
    @Pointcut("call(Throwable+.new(String, ..)) && this(caller) && args(exceptionMessage) && if()")
    public static boolean exceptionInitializer(Object caller, String exceptionMessage) {
        return true;
    }

    @Around("exceptionInitializer(caller, exceptionMessage)")
    public Object annotateException(ProceedingJoinPoint jp, Object caller, String exceptionMessage) {
        return null;
    }

}
