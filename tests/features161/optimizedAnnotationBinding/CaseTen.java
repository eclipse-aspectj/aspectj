// CaseTen - binding multiple things
import java.lang.annotation.*;

public class CaseTen {

  public static void main(String []argv) {
   
    CaseTen o = new CaseTen();
    o.a(1);
    o.b(2);
    o.c(3);
    o.d(4);
    o.e(5);
  }

                     public void a(int i) {}
  @Anno(Level.NONE)  public void b(int i) {}
  @Anno(Level.ONE)   public void c(int i) {}
  @Anno(Level.TWO)   public void d(int i) {}
  @Anno(Level.THREE) public void e(int i) {}

}

enum Level { NONE, ONE, TWO, THREE; }

@Retention(RetentionPolicy.RUNTIME)
@interface Anno { Level value();}

aspect X {

  before(Level l,int i): execution(@Anno !@Anno(Level.NONE) * *(..)) && @annotation(Anno(l)) && args(i) {
    System.out.println(l+":"+i);
  }
}
