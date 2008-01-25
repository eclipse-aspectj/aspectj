class SimpleType {
  public void a(AnnotatedWithAnno1 p) {}
  public void b(@Anno1 String p) {}
  public void c(@Anno1 AnnotatedWithAnno2 p) {}
  public void d(@Anno1 @Anno2 String p) {}
  public void e(@Anno1 @Anno2 AnnotatedWithBoth p) {}
}
