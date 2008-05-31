// CaseTwo - no such field in the annotation 
import java.lang.annotation.*;

public class CaseTwo {

  public static void main(String []argv) {
    CaseTwo o = new CaseTwo();
    o.a();
    o.b();
    o.c();
    o.d();
    o.e();
  }

                     public void a() {}
  @Anno("A")  public void b() {}
  @Anno("B")   public void c() {}
  @Anno("C")   public void d() {}
  @Anno public void e() {}

}

enum Level { NONE, ONE, TWO, THREE; }

@Retention(RetentionPolicy.RUNTIME)
@interface Anno { String value() default "";}

aspect X {

  before(Level l): execution(@Anno !@Anno(Level.NONE) * *(..)) && @annotation(Anno(l)) {
    System.out.println(l);
  }
}
