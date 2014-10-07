import org.aspectj.lang.annotation.*;

aspect XA {
  @SuppressAjWarnings("adviceDidNotMatch")
  before(): execution(* A.*(..)) {}
}

