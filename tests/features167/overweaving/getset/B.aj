public aspect B {
  before(): !cflow(adviceexecution()) && set(* *) {System.out.println("B:"+thisJoinPointStaticPart);}
}
