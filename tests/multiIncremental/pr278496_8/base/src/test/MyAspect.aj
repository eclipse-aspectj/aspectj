package test;
import java.util.List;

public aspect MyAspect {
	
	@MyAnnotation(val = 5)
	private Integer Demo.version;    

	
	List<String> Demo.list = null;
	int Demo.x = 5;
	
	void Demo.foo(List<String> x) {
		MyAspect.hasAspect();
	}
	 
	public Demo.new(int x) { 
		this();
	}
	
    declare warning : execution(* *.nothing(..)) : "blah";
    
    declare error : execution(* *.nothing(..)) : "blah";
    
	declare soft : Exception : execution(* *.nothing(..));
	
	
    protected pointcut s():
        call(String Demo.toString(..));

    before (): s() {
    }
    after (): s() {
    }
    void around (): s() {
        proceed();
        return;
    }
    after () returning(): s() {
    }
    after () throwing(): s() {
    	thisEnclosingJoinPointStaticPart.getClass();
    	thisJoinPoint.getClass();
    	thisJoinPointStaticPart.getClass();  
    }
    
    @interface MyAnnotation {
    	int val() default 5;
    }

    // try out declare annotation
	declare @type : Demo : @MyAnnotation;   
    declare @field: int Demo.x: @MyAnnotation;
    declare @method: void Demo.foo(..): @MyAnnotation;
    declare @constructor: public Demo.new(int): @MyAnnotation; 

    // try out abstract ITDs
    public abstract long Abstract.nothing();

    public static abstract class Abstract { }

    
}
