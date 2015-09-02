package ajtest;

import java.lang.reflect.Field;

import ajtest.AjTarget;

public abstract aspect BaseAspect {

	protected pointcut mapped(Object obj) : get(@(AjTarget) Long *) && target(obj);

	Object around(Object obj) : mapped(obj) {
		Object value = proceed(obj);
		return value;
	}
}
