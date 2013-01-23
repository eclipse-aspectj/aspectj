import java.lang.annotation.*;

public class Code7 {
  public static boolean isTrue = true;
  public static boolean isTrue2 = true;

  public void m() {
  }

  public static void main(String []argv) {
    new Code7().m();
  }
}

aspect X {

 before(): execution(* Code*.*(..)) && if(Code7.isTrue) && if(Code7.isTrue2) {
    System.out.println("advice");
  }
}
