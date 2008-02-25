import java.lang.annotation.*;

public class AllKinds {
	public static void main(String[] args) {
		
	}
}

enum Color { RED, GREEN, AMBER }

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
  Color enumval();
  String strval();
  Class clazzval();
  int[] arrayval();
}

aspect X {	
  pointcut p1(): execution(@ComplexAnnotation(ival=5) * *(..)); 
  pointcut p2(): execution(@ComplexAnnotation(bval=5) * *(..)); 
  pointcut p3(): execution(@ComplexAnnotation(cval='5') * *(..)); 
  pointcut p4(): execution(@ComplexAnnotation(jval=32232323) * *(..)); 
  pointcut p5(): execution(@ComplexAnnotation(dval=5.0) * *(..)); 
  pointcut p6(): execution(@ComplexAnnotation(zval=true) * *(..)); 
  pointcut p7(): execution(@ComplexAnnotation(sval=42) * *(..)); 
  pointcut p8(): execution(@ComplexAnnotation(enumval=Color.GREEN) * *(..)); 
  pointcut p9(): execution(@ComplexAnnotation(strval="Hello") * *(..)); 
//  pointcut pa(): execution(@ComplexAnnotation(clazzval=String.class) * *(..)); 
//  pointcut pb(): execution(@ComplexAnnotation(arrayval={1,2,3}) * *(..)); 
}
