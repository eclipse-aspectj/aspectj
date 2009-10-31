aspect X {
before(): cflow(execution(* main(..))) && execution(* *(..)) {
  System.out.println("AspectX>>"+thisJoinPointStaticPart);
}

}

