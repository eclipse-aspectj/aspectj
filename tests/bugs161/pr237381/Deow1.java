import org.aspectj.lang.annotation.*;

aspect X {
  @DeclareWarning("execution(* *.foo(..))")
  public static final String argleBargle = "fromX";
  
  public void foo() {}
}

@Aspect
class Y {
  @DeclareWarning("execution(* *.goo(..))")
  public static final String argleBargle = "fromY";
  
  public void goo() {}
}
