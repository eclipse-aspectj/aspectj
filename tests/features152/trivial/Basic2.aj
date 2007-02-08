public class Basic2 {

  public static void main(String []argv) {
    Basic2 b = new Basic2();
    b.tm();
    b.ntm();
    b.tm();
  }

  public void tm() {
    // trivial : <10 instructions
  }

  public void ntm() {
    // nontrivial
    StringBuffer sb = new StringBuffer();
    for (int j=0;j<100;j++) {
      sb.append("a").append("b").append("c");
    }
  }

}

aspect X {
  before(): execution(!trivial * *m(..)) {
    System.err.println("Non trivial method executing:"+thisJoinPoint.getSignature());
  }
}

