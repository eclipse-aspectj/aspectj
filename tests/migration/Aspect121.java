// Aspect should be built with AspectJ 1.2.1 into aspects121.jar
// We are testing the new AspectJ can recognize old aspects

public aspect Aspect121 {

  pointcut anyMethodCall(): execution(* main(..));

  pointcut setIntField(): set(int *);

  pointcut complex(): call(* *(..)) && cflow(execution(* toplevel(..)));

  pointcut moreComplex(): call(* *(..)) && !(call(* foo*(..)) || call(* bar*(..)));

  before(): complex() { }

  after():  complex() { }

  void around(): anyMethodCall() {
     proceed();
  }
}
