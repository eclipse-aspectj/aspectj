/**
 * 
 */
package pack;

public aspect A {
	
	declare warning : (get(* System.out) || get(* System.err)) : "There should be no printlns"; 
	
	pointcut p() : call(* C.method2(..));
	
	before() : p() {
		System.out.println("blah");
	}
		
}
