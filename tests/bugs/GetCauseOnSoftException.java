
import org.aspectj.lang.*;

public class GetCauseOnSoftException {

	public static void a(){
		b();
	}
	/**
	 * Method b.
	 */
	private static void b() {
		throw new MyException("secret");
	}
    
	public static void main(String[] args) {
		try {
			a();
		} catch (SoftException e) {
			System.out.println(e.getCause());
			if (e.getCause().getMessage().indexOf("secret")==-1) 
			  throw new RuntimeException("Didn't get expected cause of SoftException");
		}
	}
}
aspect Softner {
		declare soft : MyException : within(GetCauseOnSoftException);  
}

class MyException extends Exception {
	MyException(String s) { super(s);}
}