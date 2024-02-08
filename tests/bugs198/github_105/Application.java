import java.lang.annotation.Annotation;

/**
 * {@code FooAspect} should add {@code @BarAnnotation(name = "from FooAspect")}.
 * <p>
 * This fails in AspectJ 1.9.5 to 1.9.8.RC2 due to a removed safeguard in JDT Core,
 * if the aspect is in a separate library on the aspectpath.
 * <p>
 * See https://github.com/eclipse-aspectj/aspectj/issues/105
 */
@FooAnnotation
public class Application {
  public static void main(String[] args) {
    for (Annotation annotation : Application.class.getDeclaredAnnotations()) {
      System.out.println(annotation);
    }
  }
}
