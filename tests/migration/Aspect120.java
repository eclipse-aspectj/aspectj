// Aspect should be built with AspectJ 1.2.0 into aspects120.jar
// We are testing the new AspectJ can recognize old aspects

public aspect Aspect120 {

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
