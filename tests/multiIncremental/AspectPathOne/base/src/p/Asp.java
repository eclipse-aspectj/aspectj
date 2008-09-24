package pkg;

public aspect Asp {

 before(): execution(* fo*(..)) {}
}
