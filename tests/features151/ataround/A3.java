// Bind the target but pass args in wrong order on proceed
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class A3 {

  @Around("call(void M.method(String)) && args(p) && target(t)")
  public void a( ProceedingJoinPoint pjp, M t, String p) throws Throwable {
    System.err.println("advice from ataj aspect");
    pjp.proceed(new Object[]{"faked",t});
    // Type mismatch: cannot convert from String to M
    // Type mismatch: cannot convert from M to String
  }

  public static void main(String []argv) {
    M.main(argv);
  }
}

class M {

  String prefix;

  public M(String prefix) { this.prefix = prefix; }

  public static void main( String[] args ) {
    M m = new M(">");
    m.method("real");
  }

  public void method(String s) { System.err.println(s); }
}
