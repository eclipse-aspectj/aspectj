// trying to put annotations on that correctly use @Target
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD) @interface MethodColoring { String value(); }
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.CONSTRUCTOR) @interface CtorColoring { String value(); }

public aspect RightTarget {
  declare @method: public void m1(..) : @MethodColoring("red");
  declare @constructor: public new(int) : @CtorColoring("red");
}

aspect X {
  before(): call(* *(..)) && @annotation(MethodColoring) {
    System.err.println("Colored method call at "+thisJoinPoint);
  }
  before(): call(new(..)) && @annotation(CtorColoring) {
    System.err.println("Colored ctor call at "+thisJoinPoint);
  }
}
