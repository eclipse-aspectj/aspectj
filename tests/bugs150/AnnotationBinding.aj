import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.SuppressAjWarnings;
import java.lang.annotation.*;
public aspect AnnotationBinding {
	
	pointcut callToABeanMethod(Bean beanAnnotation) :
		call(@Bean * *(..)) && @annotation(beanAnnotation);
	
	@SuppressAjWarnings
	Object around(Bean beanAnnotation) : callToABeanMethod(beanAnnotation) {
		return proceed(beanAnnotation);
	}
	
	public static void main(String[] args) {
		D d = new D();
		d.bar();
	}
	
}

@Retention(RetentionPolicy.RUNTIME)
@interface Bean {
	
	boolean issingleton() default true;
}

class C {
	
	@Bean 
	public void foo() {}
	 
}

class D extends C {
	
	public void bar() { foo(); }
}