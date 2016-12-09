public class A_yes {
  @CacheMethodResult
  public void m() {
    System.out.println("A_yes.m()");
    Class[] itfs = A_yes.class.getInterfaces();
    System.out.println("A_yes has interface? "+((itfs==null||itfs.length==0)?"no":itfs[0].getName()));
  }
}
