aspect Aspect {

  before(): call(* *.*(..)) && !within(Aspect) {
    System.out.println(thisJoinPointStaticPart);
  }
}
