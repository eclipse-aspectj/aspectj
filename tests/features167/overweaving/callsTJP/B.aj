public aspect B {
  declare precedence: B,*;
  before(): !cflow(adviceexecution()) && call(* *(..)) {System.out.println("B:"+thisJoinPoint);}
}
