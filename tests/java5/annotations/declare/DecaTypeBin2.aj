import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface ComplexAnnotation {
  int ival();
  byte bval();
  char cval();
  long jval();
  double dval();
  boolean zval();
  short sval();
  float fval();
  String s();
}

public aspect DecaTypeBin2 {
  declare @type: A : @ComplexAnnotation(ival=4,bval=2,cval=5,fval=3.0f,dval=33.4,zval=false,jval=56,sval=99,s="s");
}

aspect X {
  before(): execution(* *(..)) && @this(ComplexAnnotation) {
    System.err.println("ComplexAnnotation identified on "+thisJoinPoint);
  }
}
