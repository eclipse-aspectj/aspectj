package a;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
@TypeAnnotation
public class Foo {

	@FieldAnnotation int foo;
	
	@MethodAnnotation int getFoo() { return foo; }
	
	@MethodAnnotation int goo;
}
