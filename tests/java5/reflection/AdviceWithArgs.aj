import org.aspectj.lang.annotation.*;
import java.lang.reflect.*;

public aspect AdviceWithArgs {
	
	@SuppressAjWarnings
	before(String s) : execution(* *(..)) && args(s) {
		System.out.println(s);
	}
	
	public static void main(String[] args) throws Exception {
		Method[] meths = AdviceWithArgs.class.getMethods();
		boolean found = false;
		for (Method meth : meths) {
			if (meth.isAnnotationPresent(Before.class)) {
				found = true;
				Before bAnn = meth.getAnnotation(Before.class);
				String argNames = bAnn.argNames();
				if (!argNames.equals("s")) {
					throw new RuntimeException("Expected 's' but got '" + argNames + "'");
				}
				break;
			}
		}
		if (!found) throw new RuntimeException("Did not find expected advice annotation");
	}	
}