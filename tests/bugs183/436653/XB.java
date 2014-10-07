import org.aspectj.lang.annotation.*;

aspect XB {
  before(): execution(* B.*(..)) {}
}

