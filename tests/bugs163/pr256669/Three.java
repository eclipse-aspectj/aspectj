import java.lang.reflect.*;
import java.lang.annotation.*;


class Destination {}

aspect Introduction {
  // multiple parameters, not all annotated
  public static String Destination.helloWorld(int i, @SomeAnnotation("xyz") String who, long l, @SomeAnnotation("abc") String what) {
    return "Hello " + who;
  }
}

public class Three {
  public static void main(String[] argv) throws Exception {
    Class<Destination> clazz = Destination.class;
    Method m = clazz.getMethod("helloWorld", Integer.TYPE,String.class,Long.TYPE,String.class);
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
