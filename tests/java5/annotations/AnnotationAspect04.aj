import java.lang.annotation.Annotation;

public aspect AnnotationAspect04 {
  declare parents: SimpleAnnotation implements java.io.Serializable;

  class C extends Annotation { }
  declare parents: SimpleAnnotation extends C;

  class D {}
  declare parents: D extends Annotation;
}
