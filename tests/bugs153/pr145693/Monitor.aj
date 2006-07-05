public aspect Monitor {
    public pointcut handleEvent(Event event):
        execution(* handleEvent(Event, ..)) && args(event);
    
    public pointcut inHandleEvent(Event event): cflow(handleEvent(event));

    before(Event event): 
    		set(* currentView) && 
    		inHandleEvent(event) {
System.err.println("advice running");
	}
    
}
