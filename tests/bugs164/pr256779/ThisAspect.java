package com;

import org.aspectj.lang.annotation.*;

@DeclarePrecedence("com.ThisAspect, com.OtherAspect")
@Aspect public class ThisAspect {
@Before("execution(* *(..))")
public void b() {}
  //...
  public static void main(String[] argv) {}
}

