import java.lang.annotation.*;

public class Code4 {
  public static boolean isTrue = true;

  public void m() {
  }
  public static void main(String []argv) {
    new Code4().m();
  }
}

aspect X {

  @org.aspectj.lang.annotation.control.CodeGenerationHint(ifNameSuffix="sid")
  pointcut p(): execution(* Code*.*(..)) && if(Code4.isTrue);

  before(): p() {
    System.out.println("advice");
  }
}
