import org.aspectj.lang.annotation.control.*;
import java.lang.annotation.*;

public class Code2 {
  public static boolean isTrue = true;

  public void m() {
  }
  public static void main(String []argv) {
    new Code2().m();
  }
}

aspect X {
  @CodeGenerationHint(ifNameSuffix="fred")
  pointcut p(): execution(* Code2.*(..)) && if(Code2.isTrue);
  before(): p() {
    System.out.println("advice");
  }
}
