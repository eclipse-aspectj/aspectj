public aspect TraceRecordComponents {
  before(): execution(public * *()) {
    System.out.println(thisJoinPointStaticPart);
  }
}
