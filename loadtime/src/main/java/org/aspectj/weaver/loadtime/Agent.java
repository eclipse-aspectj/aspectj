/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;

/**
 * Java 1.5 preMain agent to hook in the class pre processor
 * Can be used with -javaagent:aspectjweaver.jar
 *
 * @author Alexandre Vasseur
 * @author Alexander Kriegisch
 */
public class Agent { 

    /**
     * The instrumentation instance
     */
    private static Instrumentation s_instrumentation;

    /**
     * The ClassFileTransformer wrapping the weaver
     */
    private static ClassFileTransformer s_transformer = new ClassPreProcessorAgentAdapter();

    /**
     * JSR-163 preMain Agent entry method
     *
     * @param options
     * @param instrumentation 
     */
    public static void premain(String options, Instrumentation instrumentation) {
    	/* Handle duplicate agents */
    	if (s_instrumentation != null) {
    		return;
    	}
        s_instrumentation = instrumentation;
        s_instrumentation.addTransformer(s_transformer);
    }

    public static void agentmain(String options, Instrumentation instrumentation) {
        premain(options, instrumentation);
    }

    /**
     * Returns the Instrumentation system level instance
     */
    public static Instrumentation getInstrumentation() {
        if (s_instrumentation == null) {
            throw new UnsupportedOperationException(
                "AspectJ weaving agent was neither started via '-javaagent' (preMain) " +
                "nor attached via 'VirtualMachine.loadAgent' (agentMain)");
        }
        return s_instrumentation;
    }

}
