public interface PublicPublic {
	//empty interface
}


//aspectj introduce a method to this interface
privileged aspect aspectWorld {
	public abstract void PublicPublic.world();
}

//class test implements hello interface, and
//method world
 class test implements PublicPublic {
    
	public void world() {
		 System.out.println("hello");
	}
	
	public static void main(String[] args) {
		test t = new test();
		t.world();
	}
}