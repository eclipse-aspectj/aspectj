public class AnnotatedParameters {


  public static void main(@SimpleAnnotation(id=1) String args[]) {
  }

  public void foo(@SimpleAnnotation(id=2) int arg1, 
                  @SimpleAnnotation(id=3) @AnnotationEnumElement(enumval=SimpleEnum.Red) String arg2) {
    try {
      throw new RuntimeException("eee");
    } catch (@SimpleAnnotation(id=5) Exception ex) {
    }
  }

}
