package xxx.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TaskModification {

	String value() default "abc";

	int i() default 1;

}
