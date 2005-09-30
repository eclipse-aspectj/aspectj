class MyClass {
  protected Object method() {
    return null;
  }
}

abstract aspect A {

  interface C2 { }

  public void C2.hello() {
    new MyClass() {
      protected Object methodX() {
        return 
//super.
method();
      }
    };
  }

  // ok
  class C { }
  public void C.hello() {
    new MyClass() {
      protected Object methodX() {
        return super.method();
      }
    };
  }
  
  
}

