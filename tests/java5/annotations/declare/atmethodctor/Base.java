import java.util.*;

public class Base {

  public        void m1() { System.err.println("m1() running");}
  protected     void m2() { System.err.println("m2() running");}
  public static void m3() { System.err.println("m3() running");}

  public static void main(String[]argv) {
 //   new Base().x();
    new Base(3).x();
  }

  public void x() {
    m1();
    m2();
    m3();
  }

  public Base() {}

  public Base(int i) {}

}
