// "Two Xlint warnings wth cflow?"

aspect A {
	  before(): call(* *(..)) && cflow(execution(* *(..))) {
	  }
}
