import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface Annotation{};

public class PR113447d {

	public static void main(String[] args) {
		PR113447d me = new PR113447d();
		me.method4(1);
	}
	
	@Annotation public void method4(int i){}
	@Annotation public void method5(int i){}
}

aspect Super {
	
	pointcut p(Annotation a) : 
		@annotation(a) && (call(void method4(int)) 
				|| call(void method5(int)));

	before(Annotation a) : p(a) {}
}
