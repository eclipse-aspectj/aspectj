import p.*;

abstract aspect X {
		
	public void I.bar() {}
	
	public void pr99125.aMethod() {}
	
}

aspect Y extends X {
	
	public void I.goo() {};
	
	public void I.foo() { System.out.println("you got me"); }

}

aspect Z extends X {}