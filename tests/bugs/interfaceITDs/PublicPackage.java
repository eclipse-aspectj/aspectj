public interface PublicPackage {
	//empty interface
}


//aspectj introduce a method to this interface
privileged aspect aspectWorld {
	abstract void PublicPackage.world();
}

//class test implements hello interface, and
//method world
 class test implements PublicPackage {
    
	public void world() {
		 System.out.println("hello");
	}
	
	public static void main(String[] args) {
		test t = new test();
		t.world();
	}
}