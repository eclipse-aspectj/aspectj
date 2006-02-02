// Simple - don't attempt to alter target for proceed, just change the arg
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class A1 {

  @Around("call(void M.method(String)) && args(p)")
  public void a( ProceedingJoinPoint pjp, String p) throws Throwable {
    System.err.println("advice from ataj aspect");
    pjp.proceed( new Object[] { pjp.getTarget(),"faked" } );
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
