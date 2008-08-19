public aspect SomeAspect {
 declare parents: SomeBaseClass implements SomeInterface;

 before() : execution(* (SomeInterface+).tag*(..)) {
  System.out.println("correct advice :-)");
 }

 before() : execution(* (!SomeInterface+).tag*(..)) {
  System.out.println("this advice should never run...");
 }
}

