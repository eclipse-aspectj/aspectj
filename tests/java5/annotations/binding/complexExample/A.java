package a.b.c;
import g.h.i.B;

import d.e.f.*;

public class A {
  public static void main(String []argv) {
    new A().a();
    new B().b();
  }

  @Color("blue")
  public void a() {
    System.err.println("a.b.c.A.a() running");
  }
}
