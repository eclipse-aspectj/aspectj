import java.lang.annotation.*;

interface Marker {}

@Retention(RetentionPolicy.RUNTIME) @interface Color { String value();}

// Color put on a particular type, marker interface added to any types with Color annotation
// Deca specified first
public aspect DecaDecpInteractions1 {

  declare @type: A : @Color("red");

  declare parents: @Color *  implements Marker;
}

aspect X {
  before(): execution(* *(..)) && this(Marker) {
    System.err.println("Marker interface identified on "+thisJoinPoint);
  }
  before(): execution(* *(..)) && @this(Color) {
    System.err.println("Color annotation identified on "+thisJoinPoint);
  }
}
