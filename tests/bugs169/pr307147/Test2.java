privileged
aspect Test {
	
	before(): call(* m(..)) {}
	
	public void B.getFoo() {
          m();
        }

}
