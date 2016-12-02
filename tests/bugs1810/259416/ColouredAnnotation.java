import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface ColouredAnnotation {
  RGB value();
}
