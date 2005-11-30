public aspect DefaultInterfaceImplementationRecipe
{
	declare parents : MyClass_ch16 implements MyInterface_ch16;
	
	// Declare the default implementation of the bar method
	public void MyInterface_ch16.bar(String name)
	{
		System.out.println("bar(String) called on " + this);
	}
}
