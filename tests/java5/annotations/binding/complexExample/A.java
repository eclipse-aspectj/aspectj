package a.b.c;
import g.h.i.B;

import d.e.f.*;

public class A {
  public static void main(String []argv) {
    new A().a();
    new B().b();
    if ((new A()) instanceof java.io.Serializable) 
      throw new RuntimeException("A should never be serializable");
  }

  @Color("blue")
  public void a() {
    System.err.println("a.b.c.A.a() running");
  }
}
