public aspect A {
  before(): !cflow(adviceexecution()) && call(* *(..)) {System.out.println("A:"+thisJoinPointStaticPart);}
}
