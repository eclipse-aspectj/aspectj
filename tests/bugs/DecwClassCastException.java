/*
 * From:
 * 
 * http://dev.eclipse.org/viewcvs/indextech.cgi/~checkout~/aspectj-home/doc/progguide/semantics-declare.html#d0e6499
 *
 * Pointcuts that appear inside of declare forms have certain restrictions. 
 * Like other pointcuts, these pick out join points, but they do so in a 
 * way that is statically determinable. 
 * 
 * Consequently, such pointcuts may not include, directly or indirectly 
 * (through user-defined pointcut declarations) pointcuts that discriminate 
 * based on dynamic (runtime) context. Therefore, such pointcuts may not be 
 * defined in terms of
 * 
 * cflow
 * cflowbelow
 * this
 * target
 * args
 * if
 * 
 * all of which can discriminate on runtime information. 
 */

public aspect DecwClassCastException {

	declare warning : if(true) : "if(true) directly against checker";
	pointcut p(): if(false);
	declare warning : p() : "if(false) through defined pointcut";
	
	declare error : cflow(execution(* main(..))): "cflow(execution(* main(..))) directly against checker";
	pointcut p2(): cflow(execution(* main(..)));
	declare error : p2() : "cflow(execution(* main(..))) through defined pointcut";
	
	declare warning : cflowbelow(execution(* main(..))): "cflowbelow(execution(* main(..))) directly against checker";
	pointcut p3(): cflowbelow(execution(* main(..)));
	declare error : p3() : "cflowbelow(execution(* main(..))) through defined pointcut";
	
	declare warning : this(Object): "this(Object) directly against checker";
	pointcut p4(): this(Object);
	declare warning : p4(): "this(Object) through defined pointcut";
	
	declare warning : target(Object): "target(Object) directly against checker";
	pointcut p5(): target(Object);
	declare warning : p5(): "target(Object) through defined pointcut";
		
	declare warning : args(Object): "args(Object) directly against checker";
	pointcut p6(): args(Object);
	declare warning : p6(): "args(Object) through defined pointcut";
	

    public static void main(String[] args) {
	    System.err.println("In main!");
    }
    
}