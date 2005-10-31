public class LazyTjpTest4 {

  public void test1 () { }
  
  private static aspect Aspect1 {

    private static boolean enabled = true;
    
    after () : if(enabled) && execution(public void LazyTjpTest4.test1()) {
      System.out.println(thisJoinPoint);
    }

    before() : execution(public void LazyTjpTest4.test1()) {
      System.out.println(thisJoinPoint);
    }
  }

}
