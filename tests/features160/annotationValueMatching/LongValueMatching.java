import java.lang.annotation.*;

public class LongValueMatching {
  public static void main(String[] args) { }

  @Anno(jval=123123123) public void methodOne() {}
  @Anno(jval=8) public void methodTwo() {}
}

@interface Anno {
  long jval();
}


aspect X {	
  before(): execution(@Anno(jval=123123123) * *(..))  {}
}
