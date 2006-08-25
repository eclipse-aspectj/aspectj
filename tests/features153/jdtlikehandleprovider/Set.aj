aspect A1 {

	pointcut setPcd() : set(int C1.x);
	
	before() : setPcd() {
	}
	
}

class C1 {
	
	int x = 0;
	
	public void method() {
		x = 1;
		x = 2;
	}

}
