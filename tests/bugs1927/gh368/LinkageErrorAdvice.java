/*******************************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *******************************************************************************/
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

/** Advice for a different one of the catch types that share a handler. */
@Aspect
public class LinkageErrorAdvice {
	@Before("handler(LinkageError+) && args(e)")
	public void before(LinkageError e) {
		System.out.println("  advice for LinkageError: " + e.getMessage());
	}
}
