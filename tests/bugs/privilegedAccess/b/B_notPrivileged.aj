package b;

import a.*;

aspect B {
  public void ITD.newFun() {
      returnNothing("a");
  }

  public static void main(String [] argv) {
    new ITD().newFun();
  }
}
