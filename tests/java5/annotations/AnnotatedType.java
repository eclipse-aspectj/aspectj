public class AnnotatedType {
  public static void main(String[] argv) {
    method1();
    method2();
  }

  @SimpleAnnotation(id=1)
  public static void method1() { }

  public static void method2() { }
}
