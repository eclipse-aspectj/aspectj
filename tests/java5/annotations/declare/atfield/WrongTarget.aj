// trying to put wrong annotations onto a field
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) @interface MethodColoring { String value(); }
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)   @interface TypeColoring   { String value(); }

public aspect WrongTarget {
  declare @field: public int * : @MethodColoring("red");
  declare @field: public int * : @TypeColoring("blue");
}

aspect X {
  before(): set(@MethodColoring * *) {
    System.err.println("Colored field access at "+thisJoinPoint);
  }
  before(): set(@TypeColoring * *) {
    System.err.println("Colored field access at "+thisJoinPoint);
  }
}
