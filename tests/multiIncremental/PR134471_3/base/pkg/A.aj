package pkg;

public aspect A {

  pointcut p() : call(* foo(..));
	
  before() : p() { } 


  declare warning: call (* goo(..)): "goo called!!";
}

