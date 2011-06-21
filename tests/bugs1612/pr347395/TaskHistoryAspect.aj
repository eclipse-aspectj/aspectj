package xxx.util;

import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

@Aspect
public class TaskHistoryAspect {

	@Around("execution(@xxx.util.TaskModification * *.*(..))")
	public Object aroundModification(ProceedingJoinPoint joinPoint) throws Throwable {
		Task task = null;
		List<Task> list = null;
		for (Object arg : joinPoint.getArgs()) {
			if (arg instanceof Task) {
				task = (Task) arg;
			} else if (arg instanceof List) {
				list = (List) arg;
			}
		}
		Object result = joinPoint.proceed(joinPoint.getArgs());
		if (task != null) {
			logModification(joinPoint, task);
		} else {
			logModification(joinPoint, list);
		}
		return result;
	}

	private void logModification(JoinPoint joinPoint, Task task) {
	}

	private void logModification(JoinPoint joinPoint, List<Task> tasks) {
	}
}
