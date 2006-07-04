import org.aspectj.lang.JoinPoint;

public abstract aspect SimpleTracing {
    private Tracer tracer = new Tracer();
	
    public abstract pointcut scope();
	
    public pointcut anyExec() :
        execution(* *(..)) || execution(new(..)) || adviceexecution();
	
    public pointcut inTracing() : anyExec() && cflow(within(Tracer));
    
    public pointcut trace() : scope() && anyExec() && !inTracing();

    before() : trace() {
    	tracer.enter(thisJoinPoint);
    }
    
    after() : trace() {
    	tracer.exit(thisJoinPoint);
    }
    
    class Tracer {
    	public void enter(JoinPoint jp) {
    		System.out.println("trace enter: " + jp.getSignature().toString());
    	}
    	
    	public void exit(JoinPoint jp) {
    		System.out.println("trace exit: " + jp.getSignature().toString());
    	}
    }
}
