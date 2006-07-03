package pkg;

public aspect A9 {
	
	public int C.x = 5;
	
	private void C.method() {
	}
	
	public String C.methodWithArgs(int i) {
		return "";
	}
}

class C {
}
