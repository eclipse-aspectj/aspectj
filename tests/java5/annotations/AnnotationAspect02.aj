public aspect AnnotationAspect02 {
  @org.aspectj.lang.annotation.SuppressAjWarnings before(): call(@SimpleAnnotation * *(..)) {}

  @org.aspectj.lang.annotation.SuppressAjWarnings before(): call( * *(..)) {}
}
