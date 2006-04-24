package abc;
import org.aspectj.lang.annotation.*;

@Aspect
public class pr138215 {

  @DeclareWarning("fooExecution()")
  public static final String warning = "no foos please";

  @Pointcut("execution(* foo())")
  public void fooExecution() {}

}

class Fooey {

  public void foo() {}

}