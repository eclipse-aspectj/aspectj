aspect X {
before(): execution(* *(..)) {
  System.out.println("X:"+thisJoinPointStaticPart);
}
}

