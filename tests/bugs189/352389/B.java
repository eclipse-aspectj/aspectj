public aspect B {
before(): execution(* m(..)) { System.out.println("b"); }
}
