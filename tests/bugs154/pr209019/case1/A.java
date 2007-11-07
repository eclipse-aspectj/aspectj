package a.b;

public class A {
  public Object m1() {
    return m2();
  }

  public Object m2() {
    return m3();
  }
 
  public Object m3() {return "x";}
}
