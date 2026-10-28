package example;

// Not deprecated itself, so the deprecated use of AccessRightsAspect.class is checked (the original report's stack)
public aspect UsesIt {
	private Class<?> found;

	before(): execution(* example.AccessRightsHelper.*(..)) {
		if (thisJoinPoint != null) {
			if (found == null) {
				found = AccessRightsAspect.class;
			}
		}
	}
}
