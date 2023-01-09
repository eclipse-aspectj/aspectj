public aspect FuzzilyMatchingAspect {
  after() : execution(public MaybeMissing* MaybeMissing*.*()) {
    System.out.println(thisJoinPoint);
  }

  after() : execution(public MaybeMissing*[] MaybeMissing*.*()) {
    System.out.println(thisJoinPoint);
  }

  after() : execution(public MaybeMissing*[][] MaybeMissing*.*()) {
    System.out.println(thisJoinPoint);
  }

  after() : execution(public in* MaybeMissing*.*()) {
    System.out.println(thisJoinPoint);
  }

  after() : execution(public in*[] MaybeMissing*.*()) {
    System.out.println(thisJoinPoint);
  }

  after() : execution(public in*[][] MaybeMissing*.*()) {
    System.out.println(thisJoinPoint);
  }
}
