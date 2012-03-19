aspect Aspect {

  before(): staticinitialization(!Aspect) {
    System.out.println(thisJoinPointStaticPart);
  }
}
