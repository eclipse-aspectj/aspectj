// 2. child specifies override and there was a parent (no error)

class Parent {
  public void method() {}
  public Object method2() {return null;}
}

class Child extends Parent {

  @Override public void method() {} // OK
  @Override public String method2() {return null;} // OK, covariance at work

}

aspect Injector { }
