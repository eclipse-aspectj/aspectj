
/*
 * To test some stuff
 */

import org.aspectj.testing.*;

public class AdviceOnAdvice {
  public static void main(String[] args) {
    new Class1().a();
    Tester.check(Class1.calledB, "Aspect2 did not get advice");
  }
}

class Class1 {
    public void a() { }
    public void b() { }
    public static boolean calledA = false;
    public static boolean calledB = false;
}


aspect Aspect1 {
    pointcut a(Class1 c1) :
        target(c1) && call(public void a());
    
    void around(Class1 c1) : a(c1) {
        proceed(c1);
        c1.b();
    }
}


aspect Aspect2b {
    
    pointcut b() :
        call(public void Class1.b()) && within(Aspect1);
    
    after () : b() {
        Class1.calledB = true;
    }
}
