public aspect Asp2 {


before(): staticinitialization(C*) {}











 before(): execution(* fo*(..)) {}
}
