// Bind the target but make it the third arg rather than the second
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class A6 {
  M newM = new M("2");

  @Around("call(void M.method(String)) && args(p) && target(t)")
  public void a( ProceedingJoinPoint pjp, String p, M t) throws Throwable {
//    System.err.println("advice from ataj aspect");
    pjp.proceed(new Object[]{"faked",newM});
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
