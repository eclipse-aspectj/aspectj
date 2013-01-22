import org.aspectj.lang.annotation.control.*;
import java.lang.annotation.*;

public class Code {
  public static boolean isTrue = true;

  public void m() {
  }
  public static void main(String []argv) {
    new Code().m();
  }
}

aspect X {
  @CodeGenerationHint(ifNameSuffix="andy")
  before(): execution(* Code.*(..)) && if(Code.isTrue) {
    System.out.println("advice");
  }
}
