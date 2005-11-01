public class LazyTjpTest5 {

  public void test1 () { }
  
  private static aspect Aspect1 {

    private static boolean enabled = true;
    
    after () : if(enabled) && execution(public void LazyTjpTest5.test1()) {
      System.out.println(thisJoinPoint);
    }

    before() : execution(public void LazyTjpTest5.test1()) {
      System.out.println(thisJoinPoint);
    }

    void around() : execution(public void LazyTjpTest5.test1()) {
      System.out.println(thisJoinPoint);
    }
  }

}
