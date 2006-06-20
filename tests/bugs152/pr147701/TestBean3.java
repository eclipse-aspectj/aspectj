// simple version - we go bang if Impl doesnt implement I...

package a.b.c;

import org.aspectj.lang.annotation.*;

interface I { public void m(); }

class Impl { // error!!!  implements I {
  public Impl() {}
  public void m() {}
}

@Aspect
class TestBeanAdvice {
  @DeclareParents(value="a.b.c.TestBean3", defaultImpl=a.b.c.Impl.class)
  private I implementationInterface;
}

public class TestBean3 {
  public static void main(String []argv) throws Exception {
    ((I)new TestBean3()).m();
  }
}

class BeansException extends Exception {}
