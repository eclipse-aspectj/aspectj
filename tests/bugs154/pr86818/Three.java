// Three.m() is ITD from another aspect. Aspects other way round to case Two

public class Three {
//  public void m() {} --> gone to aspect
  public static void main(String []argv) {
   if (!(new Three() instanceof java.io.Serializable)) System.err.println("declare parents failed");
  }
}

aspect Y {
  public void Three.m() {}
}
aspect X {
  declare parents: hasmethod(public void m()) implements java.io.Serializable;
}
