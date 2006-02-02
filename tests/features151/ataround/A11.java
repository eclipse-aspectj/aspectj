// only bind arg subset and change that subset
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;

@Aspect
public class A11 {
  M newM2 = new M("2");
  M newM3 = new M("3");

  @Around("call(void M.method(..)) && args(*,p,*) && this(t) && target(t2)")
  public void a( ProceedingJoinPoint pjp, M t,String p, M t2) throws Throwable {
    System.err.println("advice from ataj aspect");
    pjp.proceed(new Object[]{newM2,"_",newM3});
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
    m.methodCaller("x","y","z");
  }

  public void methodCaller(String param,String param2,String param3) {
    method(param,param2,param3);
  }

  public void method(String a,String b,String c) { System.err.println(prefix+a+b+c); }
}
