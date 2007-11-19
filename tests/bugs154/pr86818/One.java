// Should work fine, One gets Serializable
public class One {
  public void m() {}
  public static void main(String []argv) {
   if (!(new One() instanceof java.io.Serializable)) System.err.println("declare parents failed");
  }
}

aspect X {
  declare parents: hasmethod(public void m()) implements java.io.Serializable;
}
