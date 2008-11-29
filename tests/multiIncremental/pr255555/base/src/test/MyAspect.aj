package test;
import java.util.List;

public privileged aspect MyAspect {
	void Demo.foo(java.util.List<String> x) { }
        declare @method: (void Demo.foo(..): @Deprecated;
	declare @type: Demo: @Deprecated;
	
        //declare @field: (int Demo.x): @Deprecated;
        //declare @constructor: (public Demo.new(int)): @Deprecated;

}
