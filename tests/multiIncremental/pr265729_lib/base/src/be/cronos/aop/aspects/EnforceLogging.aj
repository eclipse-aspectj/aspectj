package be.cronos.aop.aspects;

//import junit.framework.TestCase;

public aspect EnforceLogging {
    pointcut scope():
	!within(*TestCase+);

    pointcut printing(): 
	get(* System.out) || get(* System.err) || call(* printStackTrace());

    declare error
	: scope() && printing()
	: "Don't print to Console, use logger";

}
