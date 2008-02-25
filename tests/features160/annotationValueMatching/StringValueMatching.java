import java.lang.annotation.*;

public class StringValueMatching {
  public static void main(String[] args) { }

  @Anno(stringval="foobar") public void methodOne() {}
  @Anno(stringval="goo") public void methodTwo() {}
}

@interface Anno {
  String stringval();
}


aspect X {	
  before(): execution(@Anno(stringval="foobar") * *(..))  {}
}
