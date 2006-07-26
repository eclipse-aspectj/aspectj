import org.aspectj.lang.annotation.*;

@Aspect
public class AtAJAspect {
  @Before("staticinitialization(*)")
  public void m() { }
}
