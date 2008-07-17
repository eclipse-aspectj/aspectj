package test.aj;

import test.Base;


public aspect ModelAspect {
	  pointcut setter(Base o, Object v): set(* Base+.*) && target(o) && args(v);

	  void around(Base o, Object v) : setter(o, v) {
System.out.println("Advice changed");
		  System.out.println(o + ": " +thisJoinPoint.getSignature().getName()+", "+v);
	  }
}
