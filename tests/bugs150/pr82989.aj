class A {

  public A(){}

  private void m1() throws E {
    throw new E(); 
  }
}

privileged aspect B {

  void A.m2() {
    try {
      m1(); 
    } catch(E e) { // accessor generated for m1() should be defined to throw E
    	System.err.println(e);
    }
  }
}

class E extends Exception{
  public E(){}
}
