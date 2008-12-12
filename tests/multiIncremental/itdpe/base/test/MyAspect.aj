package test;
import java.util.List;

public aspect MyAspect {
    List<String> Demo.list = null;
	int Demo.x = 5;
	
	void Demo.foo(List<String> x) {
		
	}
	
	public Demo.new(int x) { }
	
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
    }
}
