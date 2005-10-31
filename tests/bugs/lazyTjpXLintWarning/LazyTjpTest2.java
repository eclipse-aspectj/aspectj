public class LazyTjpTest2 {

  public void test1 () { }
  public void test2 () { }
  public void test3 () { }
  
  private static aspect Aspect1 {

    private static boolean enabled = true;
    
    // OK, has an if() but doesnt use tjp anyway!
    before () : if(enabled) && execution(public void LazyTjpTest2.test1()) {
    }

    // Not ok, cant apply because no if() used
    before () : execution(public void LazyTjpTest2.test2()) {
      System.out.println(thisJoinPoint);
    }

    // OK, doesnt use tjp
    before () : execution(public void LazyTjpTest2.test3()) {
    }

    // OK, uses tjp but also has if()
    before () : if(enabled) && execution(public void LazyTjpTest2.test1()) {
      System.err.println(thisJoinPoint);
    }
  }

}
