public aspect AnnotationAspect02 {
  before(): call(@SimpleAnnotation * *(..)) {}

  before(): call( * *(..)) {}
}
