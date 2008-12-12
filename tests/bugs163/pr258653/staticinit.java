import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;

@Aspect
class staticinit {
	@After("staticinitialization(MyType)")
	public void print(){
		System.out.println("INITIALIZED");		
	}
}

//public
class MyType {
	public static void main(){
		new MyType();
	}
}


