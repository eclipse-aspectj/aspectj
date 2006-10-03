package pkg;

public aspect A {

	pointcut innerpointcut() : execution( * Outer.myMethod() );

	before() : innerpointcut() {
	   System.out.println( "executing!" );
	}
}
