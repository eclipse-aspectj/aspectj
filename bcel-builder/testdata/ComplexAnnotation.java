import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface ComplexAnnotation {
  int ival();
  byte bval();
  char cval();
  long jval();
  double dval();
  boolean zval();
  short sval();
  float fval();
}
