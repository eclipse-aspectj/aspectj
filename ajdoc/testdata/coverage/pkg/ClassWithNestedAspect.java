package pkg;

public class ClassWithNestedAspect {
	
	public void amethod() {
	}
	
	static aspect NestedAspect { 
		pointcut p() : execution(* ClassWithNestedAspect.amethod(..));
		
		before() : p() {
		}
	} 
	
}
