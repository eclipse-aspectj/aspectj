import org.aspectj.lang.annotation.*;

// Aspect deactivated if A or B is missing (although aspect only really needs A)
@RequiredTypes({"A","B"})
aspect XA2 {
  @SuppressAjWarnings("adviceDidNotMatch")
  before(): execution(* A.*(..)) {}
}

