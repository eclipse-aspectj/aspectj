public class SampleClass
{
	void foo (String s)
	{
		System.out.println ("Printing " + s);
	}
	
	public static void main(String[] args)
	{
		SampleClass sc = new SampleClass();
		sc.foo ("hahaha");
	}
}
