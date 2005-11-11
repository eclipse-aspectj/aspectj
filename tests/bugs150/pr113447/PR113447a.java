import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface Annotation{};

@Annotation
public class PR113447a {

	public static void main(String[] args) {
		PR113447a me = new PR113447a();
		me.method1();
		me.method3();
		me.method4(2);
	}
	
	public void method1(){}

	public void method3(){}
	
	public void method4(int i){}
	public void method5(int i){}
}

aspect Super {

	// second method doesn't exist
	pointcut pc1(Annotation a) : 
		(@this(a) && execution(void method1()))
		|| (@this(a) && execution(void method2()));

	before(Annotation a) : pc1(a) {}
	
	// second method does exist
	pointcut pc2(Annotation a) : 
		(@this(a) && execution(void method1()))
		|| (@this(a) && execution(void method3()));

	before(Annotation a) : pc2(a) {}
	
	// second method doesn't exist
	pointcut pc3(Annotation a) : 
		(@target(a) && call(void method1()))
		|| (@target(a) && call(void method2()));

	before(Annotation a) : pc3(a) {
	}
	
	// second method does exist
	pointcut pc4(Annotation a) : 
		(@target(a) && call(void method1()))
		|| (@target(a) && call(void method3()));

	before(Annotation a) : pc4(a) {
	}
	
	// @this equivalent of BaseTests.test024 which was affected by
	// the fix for the non annotation version
	pointcut p(Annotation a) : 
		@target(a) && (call(void method4(int)) 
				|| call(void method5(int)));

	before(Annotation a) : p(a) {}
	after(Annotation a): p(a) {}
}
