import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface Annotation{};

@Annotation
public class PR113447b {

	public static void main(String[] args) {
		PR113447b me = new PR113447b();
		me.method4(1);
	}
	
	public void method4(int i){}
	public void method5(int i){}
}

aspect Super {

	pointcut p(Annotation a) : 
		@within(a) && (call(void method4(int)) 
				|| call(void method5(int)));

	before(Annotation a) : p(a) {}

}
