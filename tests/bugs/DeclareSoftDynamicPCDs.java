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

public aspect DeclareSoftDynamicPCDs {

	declare soft : MyException:if(true) ;
	pointcut p(): if(false);
	declare soft : MyException: p() ;
	
	declare soft : MyException:cflow(execution(* main(..)));
	pointcut p2(): cflow(execution(* main(..)));
	declare soft : MyException:p2();
	
	declare soft : MyException:cflowbelow(execution(* main(..)));
	pointcut p3(): cflowbelow(execution(* main(..)));
	declare soft : MyException:p3();
	
	declare soft : MyException: this(Object);
	pointcut p4(): this(Object);
	declare soft : MyException:p4();
	
	declare soft : MyException:target(Object);
	pointcut p5(): target(Object);
	declare soft : MyException:p5();
		
	declare soft : MyException:args(Object);
	pointcut p6(): args(Object);
	declare soft : MyException:p6();
	
	class MyException extends Exception {
	}

    public static void main(String[] args) {
	    System.err.println("In main!");
    }
    
}