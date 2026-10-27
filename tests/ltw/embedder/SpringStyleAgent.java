/* *******************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * SPDX-License-Identifier: EPL-2.0
 * ******************************************************************/
import java.lang.instrument.Instrumentation;

import org.aspectj.weaver.loadtime.ClassPreProcessorAgentAdapter;
import org.springframework.instrument.InstrumentationSavingAgent;

/**
 * Behaves like an embedder of the weaver (like Spring's load-time weaving): it owns the agent, so the AspectJ weaver's
 * own Agent never runs, and it only registers a ClassPreProcessorAgentAdapter as a transformer.
 */
public class SpringStyleAgent {

	public static void premain(String agentArgs, Instrumentation inst) {
		InstrumentationSavingAgent.premain(agentArgs, inst);
		inst.addTransformer(new ClassPreProcessorAgentAdapter());
	}
}
