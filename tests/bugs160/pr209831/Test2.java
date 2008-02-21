import java.lang.annotation.*;

 aspect ExactAnnotationTypePatternBug {

  before(Throwable e) : handler(Throwable+)  && args(e) &&
!args(@NoDefaultHandler Throwable+) {

  }
}


@Retention(RetentionPolicy.CLASS)
//@Target(ElementType.PARAMETER)
 @interface NoDefaultHandler {
}

@NoDefaultHandler
class MyException extends Throwable {
	
}

public class Test2 {

  /**
   * @param args
   */
  public static void main(String[] args) {
    // TODO Auto-generated method stub

  }

  private void th() throws MyException {
    throw new MyException();
  }

  private void test() {
    try {
      th();
    } catch (MyException e) {

    }
  }

}

