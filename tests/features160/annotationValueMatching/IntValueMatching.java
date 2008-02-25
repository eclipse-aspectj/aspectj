import java.lang.annotation.*;

public class IntValueMatching {
	public static void main(String[] args) {
		
	}

  @Anno(ival=3) public void a() {}
  @Anno(ival=5) public void b() {}
}

enum Color { RED, GREEN, AMBER }

@Retention(RetentionPolicy.RUNTIME)
@interface Anno {
  int ival();
}


aspect X {	
  before(): execution(@Anno(ival=5) * *(..)) {}
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
