public aspect A {
  before(): execution(* *(..)) {System.out.println("A:"+thisJoinPointStaticPart);}
}
