package b;

import a.*;

privileged aspect B {
  public void ITD.newFun() {
      returnNothing("a");
  }

  public static void main(String [] argv) {
    new ITD().newFun();
  }
}
