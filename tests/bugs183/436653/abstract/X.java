import org.aspectj.lang.annotation.*;

//@RequiredTypes("A")
aspect X extends AA {
  @SuppressAjWarnings("adviceDidNotMatch")
  before(): execution(* *(..)) { System.out.println("X.before"); }
}

