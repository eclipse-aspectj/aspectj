// CaseFive - not an enum, compiler limitation
import java.lang.annotation.*;

public class CaseFive {

  public static void main(String []argv) {
    CaseFive o = new CaseFive();
    o.a();
  }

  @Anno("hello") public void a() {}

}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno { String value(); }

aspect X {

  before(String l): execution(@Anno * *(..)) && @annotation(Anno(l)) {
    System.out.println(l);
  }
}
