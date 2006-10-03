package pkg;

public aspect A {

	pointcut innerpointcut() : execution( * Outer.Inner.myMethod() );

	before() : innerpointcut() {
	   System.out.println( "executing!" );
	}
	
}
