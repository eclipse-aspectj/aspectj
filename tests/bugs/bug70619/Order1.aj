aspect Conflict1 {
  
  
  
  declare precedence: Conflict1, Conflict2;

   before(): execution(* *(..)) { }
}