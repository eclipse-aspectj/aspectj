import org.aspectj.lang.annotation.*;

public class PointcutAssortment {
	
	@Pointcut("execution(* PointcutAssortment.*(..))")
	void pc1() {}
	
	@Pointcut("call(* PointcutAssortment.*(..))")
	String pc2() {}
	
	@Pointcut("foo()")
	void pc3() {}
	
	@Pointcut("pc1() || pc2()")
	void pc4() {
		System.out.println("hi there!");
	}
	
	@Pointcut("this(Integer)")
	void pc5() {}
	
	@Pointcut("this(i)") 
	void pc6(Integer i) {}
	
	@Pointcut("OtherClass.intSet() && pc6(myInt)")
	void pc7(Integer myInt) {}
	
	@Pointcut("get(* *)")
	@Pointcut("set(* *)")
	void pc8() {}
	
	@Pointcut("target(foo)")
	void pc9() {}
	
}

class OtherClass {
	
	@Pointcut("set(Integer *)")
	void intSet() {};
	
}