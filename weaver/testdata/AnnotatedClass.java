import p.SimpleAnnotation;

@SimpleAnnotation(id=2)
public class AnnotatedClass {

  @SimpleAnnotation(id=3)
  public void m1() { }

  @SimpleAnnotation(id=4)
  int i;
}

