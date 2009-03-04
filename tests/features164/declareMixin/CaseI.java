// TESTING: invalid entry in interfaces
import org.aspectj.lang.annotation.*;

public class CaseI {
  public static void main(String[]argv) { }
}

aspect X {
  @DeclareMixin(value="X",interfaces={String.class})
  public static I createImplementation() {
    return null;
  }
}

interface I {}

