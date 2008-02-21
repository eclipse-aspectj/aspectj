import java.lang.annotation.*;

 aspect ExactAnnotationTypePatternBug {

  before(Throwable e) : handler(Throwable+)  && args(e) &&
!args(@NoDefaultHandler Throwable+) {

  }
}


@Retention(RetentionPolicy.CLASS)
@Target(ElementType.PARAMETER)
 @interface NoDefaultHandler {
}

public class Test {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  private void th() throws Throwable {
    throw new Throwable();
  }

  private void test() {
    try {
      th();
    } catch (Throwable e) {

    }
  }

}

