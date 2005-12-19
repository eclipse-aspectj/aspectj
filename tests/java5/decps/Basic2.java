public class Basic2 {
  public static void main(String []argv) {
    Basic2 b = new Basic2();
    if (!(b instanceof X.I)) throw new RuntimeException("Basic2 should implement I");
  }
}



aspect X {

  interface I { 
  }

  public void I.m2() {
    
  }


  declare parents: Basic2 implements I;


  before(): execution(* *(..)) {
  }

}
  
