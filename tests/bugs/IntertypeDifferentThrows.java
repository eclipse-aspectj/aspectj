class A {
  public A(){}
  public void m() throws Exception{}
}

class B extends A {
  public B(){}
  public void some_code(){
	m();
  }
}

// B.m() introduced here does not throw 'Exception' so class B above
// should compile OK!
aspect C {
  public void B.m(){}
}