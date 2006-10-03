import java.lang.annotation.*;

aspect TestAspect {
  // works - Derived.func() omitted
//  declare warning : execution(@Annot * *(..)) && !within(@Annot *): "hi!";

  // fails - Derived.func() not omitted
  declare warning : execution(@Annot * *(..)) && within(!@Annot *) : "within includes negated annotation";
  pointcut hasMethod() : hasmethod(@Annot * *(..));
}


class Base {
  void func() { }
}

//@Annot
class AnnotTest {
  @Annot
  class Derived extends Base {
    @Annot void func() { }
  }
//
 // class NoAnnotClass {
  //  @Annot void func() { }
  //}
}

//@Retention(RetentionPolicy.RUNTIME)
@interface Annot { };
