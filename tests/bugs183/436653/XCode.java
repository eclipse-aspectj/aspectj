import org.aspectj.lang.annotation.*;

aspect XCode {
  @SuppressAjWarnings("adviceDidNotMatch")
  before(): execution(* Cod*.*(..)) {}
}

