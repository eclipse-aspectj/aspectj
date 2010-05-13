public aspect B {
  before(): !cflow(adviceexecution()) && call(* *(..)) {System.out.println("B:"+thisJoinPointStaticPart);}
}
