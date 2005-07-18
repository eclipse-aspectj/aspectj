abstract aspect WorkerExample {

    after() returning (RequestContext newContext) : call(RequestContext+.new(..)) {        
        System.out.println("constructing "+newContext+" at "+thisJoinPoint.toLongString()+" from "+thisEnclosingJoinPointStaticPart+":");
    }

     abstract class RequestContext {
        public final Object execute() {
            return doExecute();
        }
        
        /** template method */
        public abstract Object doExecute();
    }

    public static void main(String args[]) {
        new Runnable() {
            public void run() {}
        }.run();
    };
}

aspect ConcreteAlpha extends WorkerExample {

    Object around(final Object runnable) : execution(void Runnable.run()) && this(runnable) {
        System.out.println("monitoring operation: "+runnable+" at "+thisJoinPoint+", for "+thisJoinPoint.getThis());
        RequestContext requestContext = new RequestContext() {
            public Object doExecute() {
                return proceed(runnable);
            }
            
        };
        return requestContext.execute();
    }
    
}

aspect ConcreteBeta extends WorkerExample {
    
    Object around() : call(void awqeyuwqer()) {
        RequestContext requestContext = new ConnectionRequestContext() {
            public Object doExecute() {                
                return proceed();
            }
            
        };
        return requestContext.execute();
    }

    
}