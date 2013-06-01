import java.lang.annotation.*;
import java.lang.reflect.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
@interface Foo {
  int value() default 42;
}

interface I003 {}

class Foo003 implements @Foo(1) I003 { }

public class Code003 implements @Foo(2) I003 {
  void run() {
    AnnotatedType[] superinterfaces = Code003.class.getAnnotatedInterfaces();
    if (superinterfaces.length != 0) {
      System.out.println(superinterfaces[0].getAnnotations()[0]);
    }
  }

  public static void main(String []argv) {
    Code003 c = new Code003();
    c.run();
  }
}

