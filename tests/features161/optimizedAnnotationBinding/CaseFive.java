// CaseFive - not an enum, compiler limitation
import java.lang.annotation.*;

public class CaseFive {

  public static void main(String []argv) {
    CaseFive o = new CaseFive();
    o.a();
  }

  @Anno(4.0f) public void a() {}

}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno { float value(); }

aspect X {

  before(float l): execution(@Anno * *(..)) && @annotation(Anno(l)) {
    System.out.println(l);
  }
}
