import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface TestAnnotation1 {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@interface TestAnnotation2{}

@TestAnnotation1
class Annotated {}

interface Marker {}

public aspect AnnotationAspect {

  declare @type: @TestAnnotation1 *: @TestAnnotation2;

  // of cource this matches
  // declare parents: (@TestAnnotation1 *)  implements Marker;
  // this matches, too
  // declare parents: (@TestAnnotation2 *)  implements Marker;

  // this does not match on Annotated
  declare parents: (@TestAnnotation2 *) && !java.lang.annotation.Annotation implements Marker;
  // but this does match on annotated
  // declare parents: (@TestAnnotation1 *) && !java.lang.annotation.Annotation implements Marker;
}

