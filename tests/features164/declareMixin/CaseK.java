// TESTING: too many arguments to the factory method
import org.aspectj.lang.annotation.*;

public class CaseK {
  public static void main(String[]argv) { }
}

aspect X {
  @DeclareMixin("A")
  public static I createImplementation1(String s,long foo) {return null;}

}

interface I {}

