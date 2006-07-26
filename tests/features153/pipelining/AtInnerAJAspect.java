import org.aspectj.lang.annotation.*;

public class AtInnerAJAspect {
  @Aspect
  public static class SimpleAspect {
    @Before("staticinitialization(*)")
    public void m() { }
  }
}
