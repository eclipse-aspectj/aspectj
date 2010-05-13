public aspect B {
  declare precedence: B,*;
  before(): execution(* *(..)) {System.out.println("B:"+thisJoinPointStaticPart);}
}
