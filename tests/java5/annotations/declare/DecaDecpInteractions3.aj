import java.lang.annotation.*;

interface Marker {}

@Retention(RetentionPolicy.RUNTIME) @interface Color { String value();}

// Put the marker interface on a particular type and add the annotation on 
// types with that interface.
// deca specified first
public aspect DecaDecpInteractions3 {

  declare @type: Marker+ : @Color("red");

  declare parents: A* implements Marker;
}

aspect X {
  before(): execution(* *(..)) && this(Marker) {
    System.err.println("Marker interface identified on "+thisJoinPoint);
  }
  before(): execution(* *(..)) && @this(Color) {
    System.err.println("Color annotation identified on "+thisJoinPoint);
  }
}
