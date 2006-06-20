// simple version - we go bang if Impl doesnt implement I...

package a.b.c;

import org.aspectj.lang.annotation.*;

interface I { public void m(); }

class Impl implements I {
  public Impl() {}
  public void m() {}
}

@Aspect
class TestBeanAdvice {
  @DeclareParents(value="a.b.c.TestBean", defaultImpl=a.b.c.Impl.class)
  private I implementationInterface;
}

public class TestBean {
  public static void main(String []argv) throws Exception {
    ((I)new TestBean()).m();
  }
}

class BeansException extends Exception {}
