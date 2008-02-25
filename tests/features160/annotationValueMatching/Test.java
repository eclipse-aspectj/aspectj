//@Retention(RetentionPolicy.RUNTIME)
@interface ComplexAnnotation {
  int ival();
  byte bval();
  char cval();
  long jval();
  double dval();
  boolean zval();
  short sval();
  float fval();
//  Color enumval();
  String strval();
  Class classval();
  int[] arrayval();
}

