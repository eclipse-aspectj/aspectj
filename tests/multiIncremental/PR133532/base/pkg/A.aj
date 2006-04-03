package pkg;

public privileged aspect A {

	public static int F_PRIVILEGED = 0x8000;
	
	before() : execution(int AbstractClass.getModifiers()) {
		int i = F_PRIVILEGED;
	}
}
