// Bind the this on a call and change it with proceed... works
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class A8 {
  M newM = new M("2");

  @Around("execution(void M.method(String)) && args(p) && this(t)")
  public void a( ProceedingJoinPoint pjp, String p, M t) throws Throwable {
    System.err.println("advice from ataj aspect");
    pjp.proceed(new Object[]{newM,"faked"});
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
   m.methodCaller("real");
 }

 public void methodCaller(String param) {
   method(param);
 }

 public void method(String s) { System.err.println(prefix+s); }

}
