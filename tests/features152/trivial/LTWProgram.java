public class LTWProgram {

  public static void main(String []argv) {
    LTWProgram b = new LTWProgram();
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

