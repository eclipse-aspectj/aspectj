import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface SimpleStringAnnotation {
  String fruit();
}
