import org.aspectj.lang.annotation.*;

// Aspect deactivated if A is missing
@RequiredTypes("A")
aspect XA2 {
  @SuppressAjWarnings("adviceDidNotMatch")
  before(): execution(* A.*(..)) {}
}

