public aspect TestITD {
	
	declare parents : AbstractSuperAspectWithInterface+ implements TestInterface;
	
	public void TestInterface.interfaceMethod () {
		System.out.println("? void TestITD.interfaceMethod()");
	}
}