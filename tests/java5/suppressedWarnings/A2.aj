import org.aspectj.lang.annotation.*;
public aspect A2 {

  before(): execution(* foo(..)) { // wont match
  }

  @SuppressAjWarnings
  before(): execution(* foo(..)) { // wont match - but suppressed
  }
  @SuppressAjWarnings("Bananas") // this wont prevent the lint advice not match warning
  before(): execution(* foo(..)) { // wont match
  }
}
