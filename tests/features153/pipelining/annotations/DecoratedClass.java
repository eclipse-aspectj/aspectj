// Use all the variants of annotations - to exercise the 
// eclipse transform code in EclipseSourceType

import java.lang.annotation.*;

@AnnotationStringElement(stringval="hello")
@SimpleAnnotation(id=1)
@AnnotationClassElement(clz=Integer.class)
@CombinedAnnotation({@SimpleAnnotation(id=4)})
@AnnotationEnumElement(enumval=SimpleEnum.Red)
@ComplexAnnotation(ival=4,bval=2,cval='5',fval=3.0f,dval=33.4,zval=false,jval=56,sval=99)
public class DecoratedClass {
public void m() {}

}

@Target(value={ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@interface SimpleAnnotation {
  int id();
  String fruit() default "bananas";
}
enum SimpleEnum { Red,Orange,Yellow,Green,Blue,Indigo,Violet };

@Retention(RetentionPolicy.RUNTIME)
@interface SimpleStringAnnotation {
  String fruit();
}


@Target({ElementType.TYPE,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@interface AnnotationClassElement {
  Class clz();
}

@Retention(RetentionPolicy.RUNTIME)
@interface AnnotationEnumElement {
  SimpleEnum enumval();
}
@Target({ElementType.TYPE,ElementType.METHOD,ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@interface AnnotationStringElement {
  String stringval();
}
@Retention(RetentionPolicy.RUNTIME)
@interface CombinedAnnotation {
 SimpleAnnotation[] value();
}
@Retention(RetentionPolicy.RUNTIME)
@interface ComplexAnnotation {
  int ival();
  byte bval();
  char cval();
  long jval();
  double dval();
  boolean zval();
  short sval();
  float fval();
}

