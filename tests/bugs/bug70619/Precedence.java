class Conflict { public static void main(String[] args) { } }

aspect Conflict1 {
  declare precedence: Conflict1,Conflict2;

  before(): execution(* *(..)) { }
}

aspect Conflict2 {
  declare precedence: Conflict2, Conflict1;

  after(): execution(* *(..)) { }
}