import java.lang.annotation.*;

public class FloatValueMatching {
  public static void main(String[] args) { }

  @Anno(fval=37.0f) public void methodOne() {}
  @Anno(fval=52.1f) public void methodTwo() {}
}

@interface Anno {
  float fval();
}


aspect X {	
//  before(): execution(@Anno(ival=5) * *(..)) {}
    before(): execution(@Anno(fval=52.1f) * *(..))  {}
//  before(): execution(@Anno(bval=5) * *(..)) {}
//  before(): execution(@Anno(cval='5') * *(..))  {}
//  before(): execution(@Anno(jval=32232323) * *(..))  {}
//  before(): execution(@Anno(dval=5.0) * *(..)) {}
//  before(): execution(@Anno(zval=true) * *(..)) {}
//  before(): execution(@Anno(sval=42) * *(..)) {}
//  before(): execution(@Anno(enumval=Color.GREEN) * *(..)) {}
//  before(): execution(@Anno(strval="Hello") * *(..)) {}
//  before(): execution(@Anno(clazzval=String.class) * *(..)); 
//  before(): execution(@Anno(arrayval={1,2,3}) * *(..)); 
}
