import java.lang.annotation.*;

public class DoubleValueMatching {
  public static void main(String[] args) { }

  @Anno(dval=37.01) public void methodOne() {}
  @Anno(dval=52.123) public void methodTwo() {}
}

@interface Anno {
  double dval();
}


aspect X {	
  before(): execution(@Anno(dval=37.01) * *(..))  {}
}
