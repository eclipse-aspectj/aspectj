public class MyClass_ch16
{
        public void foo(int number, String name)
	{
		System.out.println("Inside foo (int, String) with args: " + 
number + ":" + name);
	}

	public static void main(String[] args)
	{
		// Create an instance of MyClass
		MyInterface_ch16 myObject = (MyInterface_ch16)new MyClass_ch16
();
		
		// Make the call to foo
		myObject.bar("Russ");
	}
}
