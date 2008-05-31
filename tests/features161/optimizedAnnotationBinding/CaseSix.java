// CaseSix - not an execution join point - compiler limitation
import java.lang.annotation.*;

public class CaseSix {

  @Anno static String s;

  public static void main(String []argv) {
    s = "hello";
  }

}
enum Level { NONE; }

@Retention(RetentionPolicy.RUNTIME)
@interface Anno { Level value() default Level.NONE; }

aspect X {

  before(Level l): set(@Anno * *) && @annotation(Anno(l)) {
    System.out.println(l);
  }
}
