// CaseFour - default value specified and should be extracted
import java.lang.annotation.*;

public class CaseFour {

  public static void main(String []argv) {
    CaseFour o = new CaseFour();
    o.a();
    o.b();
    o.c();
  }

  @Anno public void a() {}
  @Anno(Level.TWO) public void b() {}
  @Anno public void c() {}

}

enum Level { NONE, ONE, TWO, THREE; }

@Retention(RetentionPolicy.RUNTIME)
@interface Anno { Level value() default Level.ONE;}

aspect X {

  before(Level l): execution(@Anno !@Anno(Level.NONE) * *(..)) && @annotation(Anno(l)) {
    System.out.println(l);
  }
}
