aspect Azpect {
  before(): within(HelloWorldEnumSwitch) {
    System.out.println(">"+thisJoinPointStaticPart.getSourceLocation().getLine());
  }
}
