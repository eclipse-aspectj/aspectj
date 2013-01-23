import java.lang.annotation.*;

public class Code5 {
  public static boolean isTrue = true;

  public void m() {
  }
  public static void main(String []argv) {
    new Code5().m();
  }
}

aspect X {

  pointcut p(): execution(* Code*.*(..)) && if(Code5.isTrue);

  before(): p() {
    System.out.println("advice");
  }
}
