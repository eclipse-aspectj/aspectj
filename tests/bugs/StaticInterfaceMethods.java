interface StaticInterfaceMethods {
	
}

aspect A {
	
	static int StaticInterfaceMethods.aMethod() {
		return 1;
	}
}