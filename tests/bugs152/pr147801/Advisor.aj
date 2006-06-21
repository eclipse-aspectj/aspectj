public aspect Advisor {

  before(): staticinitialization(*oo) {
    System.err.println("x");
  }

  declare parents: Foo implements java.io.Serializable;
}
