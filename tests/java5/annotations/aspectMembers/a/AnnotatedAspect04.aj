package a;

@TypeAnnotation
public aspect AnnotatedAspect04 {
	
	@ConstructorAnnotation private int ITDMe.goo = 3;
	
	@FieldAnnotation private int ITDMe.getGoo() { return goo; }
	
	@TypeAnnotation public ITDMe.new(int x) { goo = x; }
	
	@MethodAnnotation int ITDMe.foo = 2;  // known limitation - no warning
}

class ITDMe {}
