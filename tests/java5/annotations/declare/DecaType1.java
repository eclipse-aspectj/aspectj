import java.lang.annotation.*;

public class DecaType1 {
  public static void main(String[] argv) {
  	Annotation a = DecaType1.class.getAnnotation(MyAnnotation.class);
        System.err.println("annotation is "+a);
  }
}

aspect X {
  declare @type: DecaType1 : @MyAnnotation;
}

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@interface MyAnnotation {}
