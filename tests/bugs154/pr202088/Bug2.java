package tracing;
import org.aspectj.lang.annotation.*;

@Aspect abstract class Bug {
	@Pointcut
	public abstract void traced(Object thiz);
	
	@Before("traced(o) && execution(* m(..))")
	public void b1(Object o) {
		System.out.println("o is '"+o+"'");
	}
	
}

public @Aspect class Bug2 extends Bug {
	@Pointcut("this(thiz)")
	public void traced(Object thiz) {}
	
	public static void main(String []argv) {
		C.main(argv);
	}
}

class C {
	public static void main(String []argv) {
		new C().m();
	}
	public void m() {
		
	}
	
	public String toString() { return "instance of C";}
}