package com.foo.bar;

public class Ten {
  public static void main(String[] argv) {
    Ten a = new Ten();
    a.m();
  }

  public void m() {
    System.out.println("Method m() running");
  }
}

aspect X {
  boolean doit() {
    System.out.println("In instance check method doit() class="+this.getClass().getName());
    return true;
  }

  before():execution(* m(..))  && if(thisAspectInstance.doit()){ 
    System.out.println("In advice()");
  }
}
