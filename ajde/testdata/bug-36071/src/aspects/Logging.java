/*
 * Created on 30-Jul-03
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package aspects;

/**
 * @author websterm
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public aspect Logging {
	
	pointcut methods () :
		execution(* *..*(..)) && !within(Logging);
	
	before () : methods () {
		System.err.println("> " + thisJoinPoint.getSignature().toLongString());
	}
	
	after () : methods () {
		System.err.println("< " + thisJoinPoint.getSignature().toLongString());
	}
}
