import java.lang.annotation.*;

public class Code5a {
  public static boolean isTrue = true;

  public void m() {
  }
  public static void main(String []argv) {
    new Code5a().m();
  }
}


// more white space, on purpose



aspect X2 {


  pointcut p(): execution(* Code*.*(..)) && if(Code5.isTrue);
  before(): p() {
    System.out.println("advice");
  }
}
