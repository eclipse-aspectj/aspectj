
public aspect Pr113368 {
    
    public static void main(String[] args) {
    	try {
    		aspectOf().hook();
    	}  catch (ExceptionInInitializerError ex) {
    		Throwable cause = ex.getCause();
    		if (! (cause instanceof org.aspectj.lang.NoAspectBoundException)) {
    			throw new RuntimeException("Unexpected exception: " + cause);
    		}
    	}
    }
    
    void hook() {}

    private pointcut managedBeanConstruction(ManagedBean bean) : 
        execution(ManagedBean+.new(..)) && this(bean); 
    
    //NPE's on the if pointcut below    
    private pointcut topLevelManagedBeanConstruction(ManagedBean bean) : 
        managedBeanConstruction(bean) && 
        if(thisJoinPointStaticPart.getSignature().getDeclaringType() == bean.getClass()); 

    after(ManagedBean bean) returning: topLevelManagedBeanConstruction(bean) {
            System.out.println("I just constructed " + bean);
    }
    
}

abstract aspect ManagedBean {
}


aspect ManagedSubBean extends ManagedBean {

    before() : execution(* hook()) {        
    }
    
}

aspect AutoStart {
    before() : staticinitialization(ManagedBean) {
        ManagedSubBean.aspectOf();
    }
}

aspect Tracer {
    before() : !within(Tracer) {
		System.out.println(thisJoinPoint);
}
}
