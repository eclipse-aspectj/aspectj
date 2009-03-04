// TESTING: invalid return type for factory method
import org.aspectj.lang.annotation.*;

public class CaseJ {
  public static void main(String[]argv) { }
}

aspect X {
  @DeclareMixin("A")
  public static void createImplementation1() {}

  @DeclareMixin("A")
  public static int createImplementation2(Object o) {return 2;}
}

interface I {}

