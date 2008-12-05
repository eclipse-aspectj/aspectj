import org.aspectj.lang.annotation.*;

@Aspect
public class Code {
  @Before("if(37!=42)")
  public void runme() {}
}
