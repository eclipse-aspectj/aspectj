// 3. child specifies override and parent was placed via ITD (no error)


class Parent {
}

class Child extends Parent {

  @Override public void method() {} // OK
  @Override public String method2() {return null;} // OK, covariance at work

}

aspect Injector { 

  public void Parent.method() {}

  public Object Parent.method2() {return null;}

}
