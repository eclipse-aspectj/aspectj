import org.aspectj.lang.annotation.*;

@Aspect
public class TestAspect {
  @DeclareParents("Test")
  public static Audit introduced = new AuditImpl();
}
