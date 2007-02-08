public class Basic {

  public static void main(String []argv) {
    Basic b = new Basic();
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
  before(): execution(trivial * *m(..)) {
    System.err.println("Trivial method executing:"+thisJoinPoint.getSignature());
  }
}

