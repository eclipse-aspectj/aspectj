import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface TestAnnotation {
  public boolean value() default true;
}

aspect TestAspect {
  declare parents: Failing implements TestInterface;
// this also does not work (even when removing annotation in the following ITD)
//          declare @method: public void TestInterface.foo(): @TestAnnotation;
  @TestAnnotation
  public void TestInterface.foo() {
    System.err.println("foo");
  }
}

interface TestInterface {
  public void foo();
}

public class Failing {
  public static void main(String[] args) throws Exception {
	System.err.println("On TestInterface:"+TestInterface.class.getDeclaredMethod("foo").getAnnotation(TestAnnotation.class)); 
    System.err.println("On Failing:"+Failing.class.getDeclaredMethod("foo").getAnnotation(TestAnnotation.class)); 
  }
}