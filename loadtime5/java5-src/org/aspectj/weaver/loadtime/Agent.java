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

import java.lang.instrument.Instrumentation;
import java.lang.instrument.ClassFileTransformer;

/**
 * Java 1.5 preMain agent to hook in the class pre processor
 * Can be used with -javaagent:aspectjweaver.jar
 *
 * @author <a href="mailto:alex@gnilux.com">Alexandre Vasseur</a>
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

    /**
     * Returns the Instrumentation system level instance
     */
    public static Instrumentation getInstrumentation() {
        if (s_instrumentation == null) {
            throw new UnsupportedOperationException("Java 5 was not started with preMain -javaagent for AspectJ");
        }
        return s_instrumentation;
    }

}
