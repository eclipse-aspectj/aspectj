aspect A1 {

	pointcut getPcd() : get(int C1.x);
	
	before() : getPcd() {
	}
	
}

class C1 {
	
	int x = 0;
	
	public void method1() {
		int y = x;
		System.out.println("y " + y);
		int z = x;
		System.out.println("z " + z);		
	}
}
