public aspect pr109614 {  
    Object around() : call( NoClassDefFoundError.new(..)) {
      return proceed();
    } 
    
    public static void main(String []argv) {
    	new ContractChecking();
    }
}    

class ContractChecking {
    public static final boolean enabled = Boolean.getBoolean(ContractChecking.class.getName());
}