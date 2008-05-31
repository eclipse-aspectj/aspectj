// CaseEleven - binding multiple annotation fields
import java.lang.annotation.*;

public class CaseEleven {

  public static void main(String []argv) {
   
    CaseEleven o = new CaseEleven();
    o.a();
    o.b();
    o.c();
    o.d();
    o.e();
  }

                     public void a() {}
  @Anno(value=Level.NONE,c=Color.RED)  public void b() {}
  @Anno(value=Level.ONE)   public void c() {}
  @Anno(value=Level.TWO,c=Color.GREEN)   public void d() {}
  @Anno(value=Level.THREE,c=Color.BLUE) public void e() {}

}

enum Level { NONE, ONE, TWO, THREE; }
enum Color { RED, GREEN, BLUE; }

@Retention(RetentionPolicy.RUNTIME)
@interface Anno { Level value(); Color c() default Color.GREEN; }

aspect X {

  before(Level l,Color color): execution(@Anno !@Anno(Level.NONE) * *(..)) && @annotation(Anno(l)) && @annotation(Anno(color)) {
    System.out.println(l+":"+color);
  }
}
