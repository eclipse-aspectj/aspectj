package pkg;

public class ClassWithNestedAspect2 {
	
	public void amethod() {
	}
	
	class InnerClass {

		aspect NestedAspect { 
			pointcut p() : execution(* ClassWithNestedAspect.amethod(..));
			
			before() : p() {
			}
		} 

	}
	
	
}
