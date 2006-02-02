// Bind the target but forget to pass it on the proceed call
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class A2 {

  @Around("call(void M.method(String)) && args(p) && target(t)")
  public void a( ProceedingJoinPoint pjp, M t, String p) throws Throwable {
    System.err.println("advice from ataj aspect");
    pjp.proceed( new Object[] { "faked" } ); // too few arguments to proceed?
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
