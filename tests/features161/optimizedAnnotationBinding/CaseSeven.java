// CaseSeven - annotations in packages
package p.q.r;

import java.lang.annotation.*;

public class CaseSeven {

  public static void main(String []argv) {
      CaseSeven o = new CaseSeven();
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

  enum Level { NONE, ONE, TWO, THREE; }

  @Retention(RetentionPolicy.RUNTIME)
  @interface Anno { Level value();}
  
aspect X {

  before(Level l): execution(@Anno !@Anno(Level.NONE) * *(..)) && @annotation(p.q.r.Anno(l)) {
    System.out.println(l);
  }
}
