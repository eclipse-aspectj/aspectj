import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Color { String value();} 

@Color("red")
public class AnnotatedType {
  public static void main(String[] argv) {
    new AnnotatedType().m();
  }

  public void m() {
    System.err.println("m() running");
  }
}
