import java.lang.annotation.*;

interface Marker {}

@Retention(RetentionPolicy.RUNTIME) @interface Color { String value();}

// Put the marker interface on a particular type and add the annotation on 
// types with that interface.
// decp specified first
public aspect DecaDecpInteractions4 {

  declare parents: A* implements Marker;

  declare @type: Marker+ : @Color("red");
}

aspect X {
  before(): execution(* *(..)) && this(Marker) {
    System.err.println("Marker interface identified on "+thisJoinPoint);
  }
  before(): execution(* *(..)) && @this(Color) {
    System.err.println("Color annotation identified on "+thisJoinPoint);
  }
}
