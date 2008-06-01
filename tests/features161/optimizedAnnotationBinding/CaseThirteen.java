import java.lang.annotation.*;

public class CaseThirteen {

  public static void main(String []argv) {

    CaseThirteen o = new CaseThirteen();
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
 
  before(String l): execution(@Anno !@Anno(Level.NONE) * *(..)) && @annotation(Anno(l)) && @annotation(Anno(c)) {
    System.out.println(l+""+c);
  }
}

