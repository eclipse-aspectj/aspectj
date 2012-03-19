aspect Aspect {

  before(): within(Code1) {
    System.out.println(thisJoinPointStaticPart);
  }
}
