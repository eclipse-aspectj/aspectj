import java.lang.annotation.*;

public class ByteValueMatching {
  public static void main(String[] args) { }

  @Anno(bval=123) public void methodOne() {}
  @Anno(bval=27) public void methodTwo() {}
}

@interface Anno {
  byte bval();
}


aspect X {	
  before(): execution(@Anno(bval=123) * *(..))  {}
}
