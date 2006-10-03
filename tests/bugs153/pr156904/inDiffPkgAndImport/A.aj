package pkg;

import pkg1.*;

public aspect A {

	pointcut innerpointcut() : execution( * Outer.myMethod() );

	before() : innerpointcut() {
	   System.out.println( "executing!" );
	}
}
