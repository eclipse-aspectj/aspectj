import org.aspectj.lang.annotation.*;

@RequiredTypes("A")
aspect X {
  before(): execution(* Code.*(..)) {System.out.println("x");}
}
