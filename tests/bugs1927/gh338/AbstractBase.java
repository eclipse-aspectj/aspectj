/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

@Aspect
public abstract class AbstractBase {
	@Pointcut("if()")
	public static boolean isEnabled() {
		return true;
	}

	@Pointcut
	public abstract void pointCut();

	@Around("isEnabled() && pointCut()")
	public Object myAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
		System.out.println("around");
		return joinPoint.proceed();
	}
}
