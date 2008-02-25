import java.lang.annotation.*;

public class CharValueMatching {
  public static void main(String[] args) { }

  @Anno(cval='e') public void methodOne() {}
  @Anno(cval='a') public void methodTwo() {}
}

@interface Anno {
  char cval();
}


aspect X {	
  before(): execution(@Anno(cval='a') * *(..))  {}
}
