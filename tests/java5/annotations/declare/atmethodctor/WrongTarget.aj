// trying to put wrong annotations onto a field
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.FIELD) @interface MethodColoring { String value(); }
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)   @interface TypeColoring   { String value(); }

public aspect WrongTarget {
  declare @method: void m1(..) : @MethodColoring("red");
  declare @method: void m1(..) : @TypeColoring("blue");
  declare @constructor: new(..) : @MethodColoring("red");
  declare @constructor: new(..) : @TypeColoring("blue");
}

