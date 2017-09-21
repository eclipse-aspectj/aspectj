import org.aspectj.lang.annotation.*;
import org.aspectj.lang.*;

@Aspect
public class Code3 {
 
    @Around(value = "args(s) && target(targeto) && call(* Foo.run1(String))")
    public void first(ProceedingJoinPoint proceedingJoinPoint, Foo targeto, String s) throws Throwable {
        System.out.println("first: binding target, just passing everything through: target=Foo(1)");
        proceedingJoinPoint.proceed(new Object[]{ targeto, s});
    }

    @Around(value = "args(s) && this(thiso) && target(targeto) && call(* run2(String))")
    public void second(ProceedingJoinPoint proceedingJoinPoint, Foo thiso, Foo targeto, String s) throws Throwable {
        System.out.println("second: binding this and target, just passing everything through: this=Foo(0) target=Foo(1)");
        proceedingJoinPoint.proceed(new Object[]{ thiso, targeto, s});
    }

    @Around(value = "args(s) && this(thiso) && call(* run3(String))")
    public void third(ProceedingJoinPoint proceedingJoinPoint, Foo thiso, String s) throws Throwable {
        System.out.println("third: binding this, just passing everything through: this=Foo(0)");
        proceedingJoinPoint.proceed(new Object[]{ thiso, s});
    }

    @Around(value = "args(s) && this(thiso) && call(* run4(String))")
    public void fourth(ProceedingJoinPoint proceedingJoinPoint, Foo thiso, String s) throws Throwable {
        System.out.println("fourth: binding this, switching from Foo(0) to Foo(3)");
        proceedingJoinPoint.proceed(new Object[]{ new Foo(3), s});
    }

    @Around(value = "args(s) && target(targeto) && call(* run5(String))")
    public void fifth(ProceedingJoinPoint proceedingJoinPoint, Foo targeto, String s) throws Throwable {
        System.out.println("fifth: binding target, switching from Foo(1) to Foo(4)");
        proceedingJoinPoint.proceed(new Object[]{ new Foo(4), s});
    }

    @Around(value = "args(s) && this(thiso) && target(targeto) && call(* run6(String))")
    public void sixth(ProceedingJoinPoint proceedingJoinPoint, Foo thiso, Foo targeto, String s) throws Throwable {
        System.out.println("sixth: binding this and target, switching them around (before this=Foo(0) target=Foo(1))");
        proceedingJoinPoint.proceed(new Object[]{ targeto, thiso, s});
    }

    public static void main(String []argv) {
      new Foo(0).execute1();
      new Foo(0).execute2();
      new Foo(0).execute3();
      new Foo(0).execute4();
      new Foo(0).execute5();
      new Foo(0).execute6();
    }
}

class Foo {
  int i;
  public Foo(int i) {
    this.i = i;
  }

  public void execute1() { new Foo(1).run1("abc"); }
  public void execute2() { new Foo(1).run2("abc"); }
  public void execute3() { new Foo(1).run3("abc"); }
  public void execute4() { new Foo(1).run4("abc"); }
  public void execute5() { new Foo(1).run5("abc"); }
  public void execute6() { new Foo(1).run6("abc"); }

  public void run1(String s) { System.out.println("Executing run("+s+") on "+this.toString()); }
  public void run2(String s) { System.out.println("Executing run("+s+") on "+this.toString()); }
  public void run3(String s) { System.out.println("Executing run("+s+") on "+this.toString()); }
  public void run4(String s) { System.out.println("Executing run("+s+") on "+this.toString()); }
  public void run5(String s) { System.out.println("Executing run("+s+") on "+this.toString()); }
  public void run6(String s) { System.out.println("Executing run("+s+") on "+this.toString()); }

  public String toString() {
    return ("Foo(i="+i+")");
  }
}
