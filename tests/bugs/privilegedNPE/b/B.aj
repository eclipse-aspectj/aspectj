package b;

import a.*;

privileged aspect B {
  void blah(ITD x) { x.returnNothing("y"); }

  public static void main(String[]argv) {
    ITD a = new ITD();
    a.returnNothing("a");
    System.err.println("Call returned OK!");
  }
}
