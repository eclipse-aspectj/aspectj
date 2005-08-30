import java.io.Serializable;
import java.lang.annotation.*;
import java.lang.*;

class Bean implements Serializable{
	
	private String name;

	public String getName() {
		return name;
	}
	
	@propertyChanger()
	public void setName( String name ) {
		this.name = name;
	}
}



@Retention( RetentionPolicy.RUNTIME )
@Target({ ElementType.METHOD })
@interface propertyChanger {
}

aspect pr108245 {
	
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