public aspect C {
before(): execution(* m(..)) { System.out.println("c"); }
}
