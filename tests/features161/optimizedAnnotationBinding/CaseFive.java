// CaseFive - not an enum, compiler limitation
import java.lang.annotation.*;

public class CaseFive {

  public static void main(String []argv) {
    CaseFive o = new CaseFive();
    o.a();
  }

  @Anno(4) public void a() {}

}

@Retention(RetentionPolicy.RUNTIME)
@interface Anno { int value(); }

aspect X {

  before(int l): execution(@Anno * *(..)) && @annotation(Anno(l)) {
    System.out.println(l);
  }
}
