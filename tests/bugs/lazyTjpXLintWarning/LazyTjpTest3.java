public class LazyTjpTest3 {

  public void test1 () { }
  public void test2 () { }
  public void test3 () { }
  public void test4 () { }
  
  private static aspect Aspect1 {

    private static boolean enabled = true;
    
    // OK, has an if() but doesnt use tjp anyway!
    after () : if(enabled) && execution(public void LazyTjpTest3.test1()) {
    }

    // Not ok, cant apply because no if() used
    after () : execution(public void LazyTjpTest3.test2()) {
      System.out.println(thisJoinPoint);
    }

    // OK, doesnt use tjp
    after () : execution(public void LazyTjpTest3.test3()) {
    }

    // OK, uses tjp but also has if()
    after () : if(enabled) && execution(public void LazyTjpTest3.test4()) {
      System.err.println(thisJoinPoint);
    }
  }

}
