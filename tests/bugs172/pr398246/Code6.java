import java.lang.annotation.*;

public class Code6 {
  public static boolean isTrue = true;
  public static boolean isTrue2 = true;

  public void m() {
  }

  public static void main(String []argv) {
    new Code6().m();
  }
}

aspect X {

  pointcut p(): execution(* Code*.*(..)) && if(Code6.isTrue) && if(Code6.isTrue2);

  before(): p() {
    System.out.println("advice");
  }
}
