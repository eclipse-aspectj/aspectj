import java.lang.annotation.*;

@MyAnnotation
public class Program {

  public static void main(String []argv) {
    Program p = new Program();
    p.m1();
    p.m2();
  }


  @MyAnnotation
  public void m1() {
    System.err.println("m1 method");
  }

  public void m2() {
    System.err.println("m2 method");
  }
}

@Retention(RetentionPolicy.RUNTIME)
@interface MyAnnotation {}

