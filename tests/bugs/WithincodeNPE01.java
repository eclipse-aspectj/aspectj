aspect B {
  public A.new(String s) {this(); }
  public void A.foo() { int i = 1; }
  
  declare warning: withincode(void main(..)): "X"; // Would NPE without the fix for PR67774
  
  declare warning: withincode(A.new(String)): "In String ctor";
  
}

class A {
  private final static String name = A.class.getName();
}