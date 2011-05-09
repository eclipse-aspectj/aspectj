import static java.lang.annotation.ElementType.*;
import java.lang.annotation.*;

public aspect InterType2 {

  @Retention(RetentionPolicy.RUNTIME)
  @Target({METHOD})
  public @interface MyAnnotation {
  }

  public static aspect AroundMethod {
    Object around() : execution(@MyAnnotation * * (..)) {
      System.out.println(thisJoinPointStaticPart);
      return proceed();
    }   
  }

  public interface InterTypeIfc {}

  // (1)
  @MyAnnotation
  public void InterTypeIfc.m1(int p1) {}

  // (2)
  public void InterTypeIfc.m1(int p1, int p2) {}

  // (3)
//  @MyAnnotation
//  public void m1(int p1) {}

  // (4)
//  public void m1(int p1, int p2) {}


  public static void main(String []argv) throws Exception {
    new Foo().m1(1);
    new Foo().m1(1,2);
  }

  declare parents: Foo implements InterTypeIfc;
 
  static class Foo {
  }
}
