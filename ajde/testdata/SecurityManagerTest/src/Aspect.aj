/*
 * Created on 28-Sep-2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */



/**
 * @author websterm
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public aspect Aspect {

	pointcut method () :
		execution(* println(..)) && within(HelloWorld) && cflow(execution(* main(..)));
	
	before () : method () {
		System.out.println(thisJoinPoint.getSignature());
	}
}
