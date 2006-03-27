import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface TestAnnotation {
  public boolean value() default true;
}

aspect TestAspect {
  declare parents: Failing2 implements TestInterface;
  
  declare @method: public void TestInterface.foo(): @TestAnnotation;

  public void TestInterface.foo() {
    System.err.println("foo");
  }
}

interface TestInterface {
  public void foo();
}

public class Failing2 {
  public static void main(String[] args) throws Exception {
	System.err.println("On TestInterface:"+TestInterface.class.getDeclaredMethod("foo").getAnnotation(TestAnnotation.class)); 
    System.err.println("On Failing2:"+Failing2.class.getDeclaredMethod("foo").getAnnotation(TestAnnotation.class)); 
  }
}