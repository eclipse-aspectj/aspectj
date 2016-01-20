public aspect A {
before(): execution(* m(..)) { System.out.println("a"); }
}
