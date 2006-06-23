// target() is used, but not in a binding capacity, so dont need to supply
// in proceed

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class Break1 {

  @Around("call(void M.method(String)) && args(p) && target(M)")
  public void a( ProceedingJoinPoint pjp, String p) throws Throwable {
    System.err.println("advice from ataj aspect");
    pjp.proceed(new Object[]{"faked"});
  }

  public static void main(String []argv) {
    M.main(argv);
  }
}

class M {

  String prefix;

  public M(String prefix) { this.prefix = prefix; }

  public static void main( String[] args ) {
    M m = new M("1");
    m.method("real");
  }

  public void method(String s) { System.err.println(prefix+s); }
}
