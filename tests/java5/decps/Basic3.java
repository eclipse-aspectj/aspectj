

public class Basic3 {
  public static void main(String []argv) {
    Basic3 b = new Basic3();
    if (!(b instanceof X.I)) throw new RuntimeException("Basic3 should implement I");
    ((X.I)b).m2();
    ((X.I)b).m3();
    ((X.I)b).m2();
    ((X.I)b).m4();
  }
}



aspect X {

  interface I { 
  }

  public void I.m2() { }
  public void I.m3() { }
  public void I.m4() { }


  declare parents: Basic3 implements I;


  before(): call(* m*(..)) {
  }

}
  
