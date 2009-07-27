package test;
import test.ChildInt;
import test.Parent;
import test.ParentInt;


public aspect AspectTrace {

	
	public pointcut p1(): execution(* test.Parent+.do*(..));	
	public pointcut p2(): withincode(* test.Parent+.do*(..)) && this(test.Parent+);
	
	public pointcut p3() : execution(* test.ParentInt.*(..));
	public pointcut p4() : execution(* test.ChildInt.*(..));
	
	before() : p1() {
		System.out.println("p1" + thisJoinPoint);
	}
	
//	before() : p2(){
//		System.out.println("p2" + thisJoinPoint);
//	}	
//	
	before() : p3() {
		System.out.println("p3" + thisJoinPoint);
	}
//	
//	before() : p4() {
//		System.out.println("p4" + thisJoinPoint);
//	}	 
}
