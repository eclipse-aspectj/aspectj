package foo;
public class MyOtherClass {

	public static class MyInnerClass {
		
		public static aspect MyInnerInnerAspect {
			
			before(): execution(* MyClass.method1()) { 
				System.out.println("Before method1..");
			} 
		
		}
		
	}
	
}

