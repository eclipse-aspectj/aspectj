public aspect Basic {

/*
  before(): execution(* a(..)) {}
  after(): execution(* a(..)) {}
  before(): execution(* a(..)) {}
  after(): execution(* a(..)) {}

*/
  public void foo() {
	  new Runnable() {
		  public void run() {}
	  };
  }
}
