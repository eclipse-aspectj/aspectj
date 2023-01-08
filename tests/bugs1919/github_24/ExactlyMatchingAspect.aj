public aspect ExactlyMatchingAspect {
  after() : execution(public MaybeMissingClass MaybeMissingClass.*()) {
    System.out.println(thisJoinPoint);
  }

  after() : execution(public MaybeMissingClass[] MaybeMissingClass.*()) {
    System.out.println(thisJoinPoint);
  }
}
