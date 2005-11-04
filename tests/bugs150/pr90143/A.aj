class MyClass {
  protected Object method() {
    return null;
  }

}

abstract aspect A {

  interface C { }

  public void C.hello() {
    new MyClass() {
      protected Object methodX() {
        return super.method();
      }
    };
  }
  
  class C2 { }

  public void C2.hello() {
    new MyClass() {
      protected Object methodX() {
        return super.method();
      }
    };
  }

  
}

