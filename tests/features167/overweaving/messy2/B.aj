public aspect B {
  before(): execution(* *(..)) {System.out.println("B:"+thisJoinPointStaticPart);}
}
