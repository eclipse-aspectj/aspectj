// Putting the wrong annotations on types but specifying patterns as the target
// rather than exact types
import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME) @interface Color { String value();} 
@Retention(RetentionPolicy.RUNTIME) @interface Fruit { String value();} 
@Retention(RetentionPolicy.RUNTIME) @interface Chocolate { String value();} 

interface M1 {}
interface M2 {}
interface M3 {}

// sick and twisted
public aspect DecaTypeBin7 {
  declare parents: @Chocolate * implements M3;
  declare @type: A : @Color("Red");
  declare @type: M1+ : @Fruit("Banana");
  declare parents: @Color * implements M1;
  declare @type: M2+ : @Chocolate("maltesers");
  declare parents: @Fruit * implements M2;

  public void B.m() { System.err.println("B.m() running"); }
  public void C.m() { System.err.println("C.m() running"); }
}

aspect X {
  before(): execution(* *(..)) && @this(Color) { System.err.println("Color identified on "+thisJoinPoint); }
  before(): execution(* *(..)) && @this(Fruit) { System.err.println("Fruit identified on "+thisJoinPoint); }
  before(): execution(* *(..)) && @this(Chocolate) { System.err.println("Chocolate identified on "+thisJoinPoint); }
  before(): execution(* *(..)) && target(M1) { System.err.println("M1 at "+thisJoinPoint); }
  before(): execution(* *(..)) && target(M2) { System.err.println("M2 at "+thisJoinPoint); }
  before(): execution(* *(..)) && target(M3) { System.err.println("M3 at "+thisJoinPoint); }

}
