import org.aspectj.lang.annotation.*;
public aspect A3 {

  before(): execution(* foo(..)) { // wont match
  }

  @SuppressAjWarnings("adviceDidNotMatch")
  before(): execution(* foo(..)) { // wont match - but suppressed
  }

  before(): execution(* foo(..)) { // wont match
  }
}
