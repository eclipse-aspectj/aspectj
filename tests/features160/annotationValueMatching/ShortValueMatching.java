import java.lang.annotation.*;

public class ShortValueMatching {
  public static void main(String[] args) { }

  @Anno(sval=32700) public void methodOne() {}
  @Anno(sval=27) public void methodTwo() {}
}

@interface Anno {
  short sval();
}


aspect X {	
  before(): execution(@Anno(sval=32700) * *(..))  {}
}
