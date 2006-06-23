import java.util.*;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

@Aspect
public class MultipleArgs {


  @Around("call(* callone(..)) && !within((MultipleArgs)) && args(a,b,c)") 
  public void a1(ProceedingJoinPoint pjp,int a,String b,List c) {
    System.err.println("advice running");
    pjp.proceed(new Object[]{a,b,c});
  }

  @Around("call(* calltwo(..)) && !within((MultipleArgs)) && args(a,b,c)") 
  public void a2(ProceedingJoinPoint pjp,String b,List c,int a) {
    System.err.println("advice running");
    pjp.proceed(new Object[]{a,b,c});
  }

  @Around("call(* callone(..)) && !within((MultipleArgs)) && args(a,b,c) && this(o)") 
  public void a3(ProceedingJoinPoint pjp,int a,String b,List c,Object o) {
    System.err.println("advice running");
    pjp.proceed(new Object[]{o,a,b,c});
  }

  public static void main(String []argv) {
    new Test().doit();
  }
}


class Test {
  public void doit() {
    List l = new ArrayList();
    callone(5,"hello",l);
    calltwo(5,"hello",l);
    callthree(5,"hello",l);
    callfour(5,"hello",l);
  }

  public void callone(int i,String s, List l) {}
  public void calltwo(int i,String s, List l) {}
  public void callthree(int i,String s, List l) {}
  public void callfour(int i,String s, List l) {}
}
