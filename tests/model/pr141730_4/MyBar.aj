package bar;

import foo.*;

public aspect MyBar {

	before() : call(* main(..)) {}

	declare warning: get( * System.out ) : "System.out should not be called";
	
	declare parents : *Foo extends NewClass;
	
	declare @type : *Foo* : @MyAnnotation;
	declare @method : public * *Foo.anotMethod(..) : @MyAnnotation;
	declare @constructor : *Foo.new(String) : @MyAnnotation;
	declare @field : int *Foo.* : @MyAnnotation ;
	
}
