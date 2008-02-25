import java.lang.annotation.*;

public class BooleanValueMatching {
  public static void main(String[] args) { }

  @Anno(zval=true) public void methodOne() {}
  @Anno(zval=false) public void methodTwo() {}
}

@interface Anno {
  boolean zval();
}


aspect X {	
  before(): execution(@Anno(zval=true) * *(..))  {}
}
