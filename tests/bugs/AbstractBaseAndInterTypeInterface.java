// pr 38824


public class AbstractBaseAndInterTypeInterface {

	interface I
	{
		public void foo();
	}

	/*abstract*/ class A implements I
	{
	}

	class B extends A
	{
	}

	private static aspect Test
	{
		protected interface ITest {};

		declare parents: A implements ITest;

		public void ITest.foo()
		{
			System.out.println("Hi");
		}
	}
}