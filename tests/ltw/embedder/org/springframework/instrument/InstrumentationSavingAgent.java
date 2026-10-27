/* *******************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 * ******************************************************************/
package org.springframework.instrument;

import java.lang.instrument.Instrumentation;

/**
 * Test stand-in for Spring's class of the same name in spring-instrument, which is not a dependency of AspectJ.
 * The weaver looks it up by name (reflectively) to find the Instrumentation instance when it did not get started as an
 * agent itself but is embedded into a framework which owns the agent, like Spring's load-time weaving is.
 */
public class InstrumentationSavingAgent {

	private static volatile Instrumentation instrumentation;

	public static void premain(String agentArgs, Instrumentation inst) {
		instrumentation = inst;
	}

	public static Instrumentation getInstrumentation() {
		return instrumentation;
	}
}
