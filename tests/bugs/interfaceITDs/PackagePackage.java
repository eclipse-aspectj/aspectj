interface PackagePackage {
	//empty interface
}


//aspectj introduce a method to this interface
 privileged aspect aspectWorld {
	abstract void PackagePackage.world();
//	 void test.andy() {
//	 	
//	 }

}

//class test implements hello interface, and
//method world
 class test implements PackagePackage{
    
	public void world() {
		 System.out.println("hello");
	}
	
	public static void main(String[] args) {
		test t = new test();
		t.world();
	}
}