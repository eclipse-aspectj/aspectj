// TESTING: null target for mixin
import org.aspectj.lang.annotation.*;

public class CaseH {
  public static void main(String[]argv) { }
}

aspect X {
  @DeclareMixin(null)
  public static I createImplementation() {
    return null;
  }
}

interface I {}

