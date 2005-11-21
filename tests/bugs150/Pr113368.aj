public aspect Pr113368 {
	
	private pointcut managedBeanConstruction(ManagedBean bean) : 
        execution(ManagedBean+.new(..)) && this(bean); 

    //NPE's on the if pointcut below    
    private pointcut topLevelManagedBeanConstruction(ManagedBean bean) : 
        managedBeanConstruction(bean) && 
        if(thisJoinPointStaticPart.getSignature().getDeclaringType() == bean.getClass()); 

    after(ManagedBean bean) returning: topLevelManagedBeanConstruction(bean) {
    		System.out.println("I just constructed " + bean);
    }
    
    public static void main(String[] args) {
    		new ManagedBean("super-bean");
    		new ManagedSubBean();
    }
    
}

class ManagedBean {
	
	public ManagedBean(String s) {
		System.out.println(s);
	}
}


class ManagedSubBean extends ManagedBean {
	
	public ManagedSubBean() {
		super("sub-bean");
	}
	
}