// CaseTwelve - binding anno and anno value
import java.lang.annotation.*;

public class CaseTwelve {

  public static void main(String []argv) {
   
    CaseTwelve o = new CaseTwelve();
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

  before(Level l,Anno a): execution(@Anno !@Anno(Level.NONE) * *(..)) && @annotation(Anno(l)) && @annotation(a) {
    System.out.println(l+":"+a.c());
  }
}
