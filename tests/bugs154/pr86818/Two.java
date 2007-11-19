// Two.m() is ITD from another aspect
public class Two {
//  public void m() {} --> gone to aspect
  public static void main(String []argv) {
   if (!(new Two() instanceof java.io.Serializable)) System.err.println("declare parents failed");
  }
}


aspect X {
  declare parents: hasmethod(public void m()) implements java.io.Serializable;
}
aspect Y {
  public void Two.m() {}
}
