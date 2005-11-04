public aspect AJ5FeaturesAtJ14 {
	
	pointcut p1() : @annotation(Foo);
	
	pointcut p2() : @this(Foo);
	
	pointcut p3() : @target(Foo);
	
	pointcut p4() : @args(Foo);
	
	pointcut p5() : @within(Foo);
	
	pointcut p6() : @withincode(Foo);
	
	declare @type : Bar : @Foo;
	
	declare @method : * *.main() : @Foo;
	
	declare @field : * main : @Foo;
	
	declare @constructor : Bar.new(..) : @Foo;
	
	// annotations within type and signature patterns...
	
	pointcut p7() : execution(@Foo * *(..));
	
	declare parents : (@Foo *) extends Bar;
}

class Foo {}

class Bar {}