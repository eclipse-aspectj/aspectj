/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation (derivative from AspectWerkz)
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import com.bea.jvm.JVMFactory;
import com.jrockit.management.rmp.RmpSocketListener;

/**
 * JRockit (tested with 7SP4 and 8.1) preprocessor Adapter based on JMAPI <p/>JRockit has a low
 * level API for hooking ClassPreProcessor, allowing the use of online weaving at full speed.
 * Moreover, JRockit does not allow java.lang.ClassLoader overriding thru -Xbootclasspath/p option.
 * <p/>The ClassPreProcessor
 * implementation and all third party jars CAN reside in the standard classpath. <p/>The command
 * line will look like:
 * <code>"%JAVA_COMMAND%" -Xmanagement:class=org.aspectj.weaver.loadtime.JRockitAgent -cp ...</code>
 * Note: there can be some NoClassDefFoundError due to classpath limitation - as described in
 * http://edocs.bea.com/wls/docs81/adminguide/winservice.html <p/>In order to use the BEA JRockit
 * management server (for further connection of management console or runtime analyzer), the regular
 * option -Xmanagement will not have any effect prior to JRockit 8.1 SP2. Instead, use <code>-Dmanagement</code>.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class JRockitAgent implements com.bea.jvm.ClassPreProcessor {

    /**
     * Concrete preprocessor
     */
    private final static ClassPreProcessor s_preProcessor;

    private static boolean START_RMP_SERVER = false;

    static {
        START_RMP_SERVER = System.getProperties().containsKey("management");
        try {
            s_preProcessor = new Aj();
            s_preProcessor.initialize();
        } catch (Exception e) {
            throw new ExceptionInInitializerError("could not initialize JRockitAgent preprocessor due to: " + e.toString());
        }
    }

    /**
     * The JMAPI ClassPreProcessor must be self registrating
     */
    public JRockitAgent() {
        if (START_RMP_SERVER) {
            // the management server will be spawned in a new thread
            /*RmpSocketListener management = */new RmpSocketListener();
        }
        JVMFactory.getJVM().getClassLibrary().setClassPreProcessor(this);
    }

    /**
     * Weave a class
     *
     * @param caller   classloader
     * @param name     of the class to weave
     * @param bytecode original
     * @return bytecode weaved
     */
    public byte[] preProcess(ClassLoader caller, String name, byte[] bytecode) {
        if (caller == null || caller.getParent() == null) {
            return bytecode;
        } else {
            return s_preProcessor.preProcess(name, bytecode, caller);
        }
    }
}