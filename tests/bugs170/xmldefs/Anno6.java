import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@interface Annot {
  char a();
  boolean fred();
  String value();
}

