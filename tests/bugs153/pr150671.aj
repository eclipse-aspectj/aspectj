class C {

	   private volatile int state;
	   private int test;
	   
	   public void foo() {
	      state = 0;
	      test = 0;
	   }
	
}

aspect FSM {
	   declare error: set(* C.state): "Changing state";
	   declare error: set(* C.test): "test";
}
