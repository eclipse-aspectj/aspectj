public aspect AnnotationAspect01 {

  // ITDC - not allowed
  public SimpleAnnotation.new(int i) {
  }

  // ITDM - not allowed
  public int SimpleAnnotation.newMember(int i) {
    return 75346;
  }

  // ITDF - not allowed
  public int SimpleAnnotation.newField;
}
