package testdata;

@SomeAnnotation
public class AnnotatedClass {

  @MethodLevelAnnotation
  public void annotatedMethod() { }

  public void nonAnnotatedMethod() {
  }
}
