import java.lang.annotation.*;

public class DecaType2 {
  public static void main(String[] argv) {
  	Annotation a = DecaType2.class.getAnnotation(MyAnnotation.class);
        System.err.println("annotation on DecaType2 is "+a);
  	a = X.class.getAnnotation(MyAnnotation.class);
        System.err.println("annotation on X is "+a);
  	a = MyAnnotation.class.getAnnotation(MyAnnotation.class);
        System.err.println("annotation on MyAnnotation is "+a);
  }
}

aspect X {
  declare @type: * : @MyAnnotation;
}

@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@interface MyAnnotation {}

