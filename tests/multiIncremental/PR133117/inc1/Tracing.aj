public aspect Tracing {

    pointcut publicMethods() : execution(public * *(..));

    before() : publicMethods() {
        System.out.println("Entering "+thisJoinPoint);
    }
    
    after() : publicMethods() {
    	System.out.println("Exiting " + thisJoinPoint);
    }
}

class MainClass {

	
	public static void main(String[] args) {
		
	}
	
	public String toString() {
		return super.toString();
	}
	
	
	public int hashCode() {
		return super.hashCode();
	}
	
}
