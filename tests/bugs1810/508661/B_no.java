class B_no {
  public void m() {
    System.out.println("B_no.m()");
    Class[] itfs = B_no.class.getInterfaces();
    System.out.println("B_no has interface? "+((itfs==null||itfs.length==0)?"no":itfs[0].getName()));
  }
}
