package mypackage;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class MyAbstractClass implements MyInterface {

	@Retention(RetentionPolicy.RUNTIME)
	private @interface MyAspectPresent {
	}

	public MyAbstractClass() {
		if (!getClass().isAnnotationPresent(MyAspectPresent.class)) {
			throw new RuntimeException("MyAspect has not been woven into "
					+ getClass());
		}
	}

}
