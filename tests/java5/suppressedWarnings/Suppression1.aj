import org.aspectj.lang.annotation.*;

// 5 pieces of advice.  Numbers 2, 4, 5 should not report a warning for not matching
public class Suppression1 {

  public static void main(String []argv) {
  }
}


aspect A {

  before(): call(* *(..)) && !within(A) {//13
  }

  @SuppressAjWarnings
  before(): call(* *(..)) && !within(A) {//17
  }
  
  @SuppressAjWarnings("bananas")
  before(): call(* *(..)) && !within(A) {//21
  }

  @SuppressAjWarnings("adviceDidNotMatch")
  before(): call(* *(..)) && !within(A) {//25
  }

  @SuppressAjWarnings({"adviceDidNotMatch","custard"})
  before(): call(* *(..)) && !within(A) {//29
  }

}
