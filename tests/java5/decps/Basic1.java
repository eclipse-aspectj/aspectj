public class Basic1 {
  public static void main(String []argv) {
    Basic1 b = new Basic1();
    if (!(b instanceof X.I)) throw new RuntimeException("Basic1 should implement I");
  }
}



aspect X {

  interface I { 
  }

  declare parents: Basic1 implements I;

}
  
