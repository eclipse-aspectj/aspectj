// 4. parent and child placed by ITD, @override on child (no error)

class Parent {
}

class Child extends Parent {
}

aspect Injector { 

  public void Parent.method() {}  
  @Override public void Child.method() {}  // OK, parent ITD'd

  public Object Parent.method2() {return null;}  
  @Override public String Child.method2() {return null;}  // OK, parent ITD'd, covariance

}
