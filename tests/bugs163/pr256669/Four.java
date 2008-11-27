import java.lang.reflect.*;
import java.lang.annotation.*;


interface I {}

class D implements I {}

aspect Introduction {
  // ITD onto interface
  public String I.helloWorld( @SomeAnnotation("xyz") String who) {
    return "Hello " + who;
  }
}

public class Four {
  public static void main(String[] argv) throws Exception {
    Class<D> clazz = D.class;
    Method m = clazz.getMethod("helloWorld", String.class);
    Annotation[] ann = m.getAnnotations();
    for (int i = 0; i < m.getParameterAnnotations().length; i++) {
      int count = m.getParameterAnnotations()[i].length;
      System.out.println("Class D parameter " + i + " has " + count + " parameter annotations");
    }
    Class<I> clazzI = I.class;
    m = clazzI.getMethod("helloWorld", String.class);
    ann = m.getAnnotations();
    for (int i = 0; i < m.getParameterAnnotations().length; i++) {
      int count = m.getParameterAnnotations()[i].length;
      System.out.println("Interface I parameter " + i + " has " + count + " parameter annotations");
    }

  }
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@interface SomeAnnotation {
        String value() default "";
}
