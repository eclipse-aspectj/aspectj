// trying to put annotations on that correctly use @Target
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface FieldColoring { String value(); }

public aspect RightTarget {
  declare @field: public int * : @FieldColoring("red");
}

aspect X {
  before(): set(@FieldColoring * *) {
    System.err.println("Colored field access at "+thisJoinPoint);
  }
}
