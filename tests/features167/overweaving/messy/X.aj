aspect X {
before(): execution(* *(..)) {
  System.out.println("AspectX>>"+thisJoinPointStaticPart);
}
}

