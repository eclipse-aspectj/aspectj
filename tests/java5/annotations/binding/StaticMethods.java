import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Colored {String value();}


public class StaticMethods {

  public static void main(String[] argv) {
    m1();
    m2();
  }


  @Colored("red")  static void m1() {System.err.println("m1 running");}
  @Colored("blue") static void m2() {System.err.println("m2 running");}

  static aspect X {
    before(Colored c): call(* m*(..)) && @annotation(c) {
      System.err.println("Color at "+thisJoinPoint+" is "+c.value());
    }
    before(Colored c): execution(* m*(..)) && @annotation(c) {
      System.err.println("Color at "+thisJoinPoint+" is "+c.value());
    }
  }

}

