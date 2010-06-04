import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Wibble {
  String value();
}
