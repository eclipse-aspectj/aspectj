package a.b.c;

import org.aspectj.lang.annotation.*;

interface I { public void m() throws BeansException; }

class Impl implements I {
  public Impl() {}
  public void m() throws BeansException { }
}

@Aspect
class TestBeanAdvice {
  @DeclareParents(value="a.b.c.TestBean2", defaultImpl=a.b.c.Impl.class)
  private I implementationInterface;
}

public class TestBean2 {
  public static void main(String []argv) throws Exception {
    ((I)new TestBean2()).m();
  }
}

class BeansException extends Exception {}
