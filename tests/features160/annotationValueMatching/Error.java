import java.lang.annotation.*;

public class Error {
  public static void main(String[] args) { }

}

@interface Anno {
  int ival();
  float fval();
  double dval();
  byte bval();
  short sval();
  boolean zval();
  long jval();
  Color enumval();
  char cval();
  String stringval();
}

enum Color { RED,GREEN,BLUE };

aspect X {	
    before(): execution(@Anno(ival=Color.GREEN) * *(..)) {}
    before(): execution(@Anno(fval="hello") * *(..))  {} // invalid
    before(): execution(@Anno(bval="foo") * *(..)) {}
    before(): execution(@Anno(cval="no") * *(..))  {}
    before(): execution(@Anno(jval=30.0f) * *(..))  {}
    before(): execution(@Anno(dval="foo") * *(..)) {}
    before(): execution(@Anno(zval=123) * *(..)) {}
    before(): execution(@Anno(sval=4212312312) * *(..)) {}
    before(): execution(@Anno(enumval=12) * *(..)) {}
    before(): execution(@Anno(stringval=234) * *(..)) {}
//  before(): execution(@Anno(clazzval=String.class) * *(..)); 
//  before(): execution(@Anno(arrayval={1,2,3}) * *(..)); 
}
