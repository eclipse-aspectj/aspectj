package p;

public aspect Asp {

 before(): execution(* fo*(..)) {}
}
