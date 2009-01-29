package com;

import org.aspectj.lang.annotation.*;


@Aspect
public class OtherAspect {
	@Before("execution(* *(..))")
			public void b() {}
  //...
}

