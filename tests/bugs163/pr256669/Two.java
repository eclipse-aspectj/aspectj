import java.lang.reflect.*;
import java.lang.annotation.*;


class Destination {}

aspect Introduction {
  // static ITD
  public static String Destination.helloWorld(@SomeAnnotation("xyz") String who) {
    return "Hello " + who;
  }
}

public class Two {
  public static void main(String[] argv) throws Exception {
    Class<Destination> clazz = Destination.class;
    Method m = clazz.getMethod("helloWorld", String.class);
    Annotation[] ann = m.getAnnotations();
    for (int i = 0; i < m.getParameterAnnotations().length; i++) {
      int count = m.getParameterAnnotations()[i].length;
      System.out.println("Parameter " + i + " has " + count + " parameter annotations");
    }

  }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface SomeAnnotation {
        String value() default "";
}
