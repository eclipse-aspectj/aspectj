import java.lang.annotation.*;

public class DecaType3 {
  public static void main(String[] argv) {
    new DecaType3().sayhello();
  }
  public void sayhello() {
    System.err.println("hello world");
  }
}

aspect X {
  declare @type: DecaType3 : @MyAnnotation;

  after(): call(* println(..)) && @this(MyAnnotation) {
    System.err.println("advice running");
  }
}

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@interface MyAnnotation {}
