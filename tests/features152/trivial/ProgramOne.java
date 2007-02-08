public class ProgramOne {

  public static void main(String []argv) {
    ProgramOne b = new ProgramOne();
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
  before(): ((execution(* *.*(..)) && !execution(trivial * *(..)))) {
    System.err.println("Non trivial method executing:"+thisJoinPoint.getSignature());
  }
}

