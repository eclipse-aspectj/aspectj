public class LazyTjpTest1 {

  public void test1 () { }
  public void test2 () { }
  
  private static aspect Aspect2 {

    // OK, no tjp used in the advice
    void around () : execution(public void test1()) {
      System.out.println("Aspect2.around() "); 
      proceed();
    }

    // Warning: tjp used in around advice so can't apply lazyTjp
    void around () : execution(public void test2()) {
      System.out.println("Aspect2.around() " + thisJoinPoint);
      proceed();
    }
  }
  
}
