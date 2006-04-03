package pkg;

public privileged aspect A {

	public static int AbstractClass.F_PRIVILEGED = 0x8000;

	before() : execution(int AbstractClass.getModifiers()) {
		int i = AbstractClass.F_PRIVILEGED;
	}
}
