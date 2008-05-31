// CaseNine - everything in different packages
package p.q.r;

import java.lang.annotation.*;
import a.b.c.Anno;
import x.y.z.Level;

public class CaseNine {

  public static void main(String []argv) {
      CaseNine o = new CaseNine();
      o.a();
      o.b();
      o.c();
      o.d();
      o.e();
    }

                       public void a() {}
    @Anno(Level.NONE)  public void b() {}
    @Anno(Level.ONE)   public void c() {}
    @Anno(Level.TWO)   public void d() {}
    @Anno(Level.THREE) public void e() {}

  }

aspect X {

  before(Level l): execution(@Anno !@Anno(Level.NONE) * *(..)) && @annotation(Anno(l)) {
    System.out.println(l);
  }
}
