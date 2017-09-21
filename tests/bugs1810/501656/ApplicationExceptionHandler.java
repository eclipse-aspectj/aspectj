package com.myapp.aspect;

import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;

import com.myapp.ApplicationException;

@Aspect
public abstract class ApplicationExceptionHandler<EX extends ApplicationException> {
    @AfterThrowing(
        pointcut = "execution(* com.myapp.*.facade.*.*(..))",
        throwing = "exception"
, argNames="exception"
    )
    public abstract void handleFacadeException(EX exception);

}
