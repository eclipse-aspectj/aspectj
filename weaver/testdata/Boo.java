import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Boo {
  String landmark();
}
