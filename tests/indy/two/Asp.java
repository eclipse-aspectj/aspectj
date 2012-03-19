aspect Aspect {

  before(): execution(* *.*(..)) && !within(Aspect) {
    System.out.println(thisJoinPointStaticPart);
  }
}
