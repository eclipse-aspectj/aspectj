// this() is used for matching but not binding
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class Break2 {
  M newM2 = new M("2");
  M newM3 = new M("3");

  @Around("execution(void M.method(String)) && args(p) && this(M)")
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
   m.methodCaller("real");
 }

 public void methodCaller(String param) {
   method(param);
 }

 public void method(String s) { System.err.println(prefix+s); }

}
