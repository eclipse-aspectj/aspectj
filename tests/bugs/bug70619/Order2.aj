aspect Conflict2 {
	after(): execution(* *(..)) { }
  declare precedence: Conflict2, Conflict1;

 
}