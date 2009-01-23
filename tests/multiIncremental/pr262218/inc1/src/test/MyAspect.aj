package test;
import java.util.List;


public aspect MyAspect {
	List<String> Demo.list = null;
	declare @type : Demo : @Deprecated;  
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
    
    @interface MyAnnotation { }

    // try out declare annotation
    declare @field: int Demo.x: @MyAnnotation;
    declare @method: void Demo.foo(..): @MyAnnotation;
    declare @constructor: public Demo.new(int): @MyAnnotation; 

}
