public aspect BasicParseTest {
	
	declare warning : set(* *) : "setting";
	
	declare @field: * foo : @MyAnnotation;
	
	declare @method: * foo() : @MyAnnotation;
	
	declare @constructor: Foo*.new(..) : @MyAnnotation;
	
	declare @type: org.xyz..* : @MyAnnotation;
	
}

@interface MyAnnotation {}