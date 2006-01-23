import java.lang.annotation.*;

public aspect TestSubAspect extends GenericAnnotation<MyAnnotation> {

  public static void main(String []argv) {
    new BasicType().run();
  }
}

@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation {}


class BasicType {
  @MyAnnotation
  public void run() {
    System.err.println("run running");
  }
}
