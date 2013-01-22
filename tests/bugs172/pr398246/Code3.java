import java.lang.annotation.*;

public class Code3 {
  public static boolean isTrue = true;

  public void m() {
  }
  public static void main(String []argv) {
    new Code3().m();
  }
}

aspect X {

  @org.aspectj.lang.annotation.control.CodeGenerationHint(ifNameSuffix="barney")
  pointcut p(): execution(* Code3.*(..)) && if(Code3.isTrue);

  before(): p() {
    System.out.println("advice");
  }
}
