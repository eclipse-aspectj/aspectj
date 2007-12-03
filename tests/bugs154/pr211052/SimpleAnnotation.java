package a.b;
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface SimpleAnnotation {
  String classname();
}

