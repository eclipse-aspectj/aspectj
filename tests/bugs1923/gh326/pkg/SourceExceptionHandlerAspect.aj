package pkg;

import java.lang.annotation.Annotation;

import org.aspectj.lang.reflect.MethodSignature;

public aspect SourceExceptionHandlerAspect {

	pointcut handleSourceExceptionPointcut(): @annotation(HandleSourceException);

	declare soft : SourceException : handleSourceExceptionPointcut();

	Object around() throws TargetException: handleSourceExceptionPointcut() {
		try {
			return proceed();
		} catch (Throwable exception) {
			String message = "";
			Annotation[] annotations = ((MethodSignature) thisJoinPoint.getSignature()).getMethod().getAnnotationsByType(HandleSourceException.class);
			if (annotations.length == 1) {
				message = ((HandleSourceException) annotations[0]).message();
			}
			if (message.isBlank()) {
				message = exception.getMessage();
			}
			throw new TargetException(message, exception);
		}
	}

}
