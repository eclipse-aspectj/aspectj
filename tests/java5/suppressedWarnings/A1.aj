import org.aspectj.lang.annotation.*;
public aspect A1 {

  before(): execution(* foo(..)) { // wont match
  }
}
