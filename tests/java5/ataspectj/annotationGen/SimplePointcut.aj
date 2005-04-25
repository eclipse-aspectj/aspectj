import java.lang.annotation.*;
import org.aspectj.lang.annotation.*;
import java.lang.reflect.*;

public aspect SimplePointcut {
	
	pointcut myPointcut() : call(* String.*(..));
	
	public static void main(String[] args) {
		Method[] methods = SimplePointcut.class.getDeclaredMethods();
		boolean foundit = false;
		for (Method method : methods) {
			if (method.getName().startsWith("ajc$pointcut$")) {
				if (foundit) throw new RuntimeException("Only expecting one pointcut method");
				foundit = true;
				if (method.getName().indexOf("$myPointcut$") == -1) {
					throw new RuntimeException("Pointcut name not captured");
				}
				if (method.getAnnotations().length != 1) {
					throw new RuntimeException("Expecting one annotation, found " + method.getAnnotations().length);
				}
				Pointcut pcAnn = method.getAnnotation(Pointcut.class);
				if (!pcAnn.value().equals("call(* java.lang.String.*(..))"))
					throw new RuntimeException("Pointcut expression not set correctly in annotation: " + pcAnn.value());
			}
		}
	}
	
}