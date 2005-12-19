import org.aspectj.lang.annotation.*;

@Aspect
public class TestAspect {
  @DeclareParents(value="Test", defaultImpl=AuditImpl.class)
  Audit introduced;
}
