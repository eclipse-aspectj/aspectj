aspect Targeter {
  intertype Basic {
    public void foo() {} // declared on Basic
  }
  //static aspect C {}
}

class Basic {
  public static void main(String[] argv) {
    new Basic().foo();
  }
}
