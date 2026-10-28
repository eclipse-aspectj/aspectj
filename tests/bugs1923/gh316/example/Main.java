package example;

import org.aspectj.lang.annotation.Aspect;

public class Main {
	public static void main(String[] args) {
		System.out.println("deprecated=" + AccessRightsAspect.class.isAnnotationPresent(Deprecated.class));
		System.out.println("aspect=" + AccessRightsAspect.class.isAnnotationPresent(Aspect.class));
	}
}
