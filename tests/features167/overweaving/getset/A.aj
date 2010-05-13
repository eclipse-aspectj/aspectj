public aspect A {
  before(): !cflow(adviceexecution()) && get(* *) {System.out.println("A:"+thisJoinPointStaticPart);}
}
