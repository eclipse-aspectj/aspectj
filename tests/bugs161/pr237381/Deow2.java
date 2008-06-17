import org.aspectj.lang.annotation.*;

aspect X {
  @DeclareError("execution(* *.foo(..))")
  public static final String argleBargle = "fromX";
  
  public void foo() {}
}

@Aspect
class Y {
  @DeclareError("execution(* *.goo(..))")
  public static final String argleBargle = "fromY";
  
  public void goo() {}
}
