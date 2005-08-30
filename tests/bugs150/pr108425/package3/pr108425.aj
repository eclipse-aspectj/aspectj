package package3;
import package2.*;

public aspect pr108425 {
	
	public static void main(String[] args) {
		Bean b = new Bean();
		b.setName("hasBean");
	}
	
	pointcut callSetter( Bean b ) 
    	: call( @propertyChanger * *(..) ) && target( b );
	
	before(Bean b) : callSetter(b) {
		System.out.println("before " + b);
	}
	
}