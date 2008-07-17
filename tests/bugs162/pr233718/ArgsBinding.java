public aspect ArgsBinding {
//  public void m() {}
//  public void m(String a) {}
  public void m(String a,String b) {}
//  public void m(int a,String b) {}


  before(String[] p): execution(* m(..)) && args(..,p,..) {}
}
