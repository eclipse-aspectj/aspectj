/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;

import junit.framework.TestCase;

/**
 * 
 */
public class CompilerRunSpecTest extends TestCase {

    private static boolean PRINTING = true;
    
	/**
	 * Constructor for CompilerRunSpecTest.
	 * @param name
	 */
	public CompilerRunSpecTest(String name) {
		super(name);
	}

    public void testSetupArgs() {
        checkSetupArgs("verbose", false);
        // XXX skipping since eclipse is default
        // checkSetupArgs("lenient", false);
        // checkSetupArgs("strict", false);
        // checkSetupArgs("ajc", true);   // XXX need to predict/test compiler selection
        // eclipse-only
        checkSetupArgs("eclipse", true);
    }

    void checkSetupArgs(String arg, boolean isTestArg) {
        MessageHandler handler = new MessageHandler();
        try {
            CompilerRun.Spec spec = new CompilerRun.Spec();
            AbstractRunSpec.RT parentRuntime = new AbstractRunSpec.RT();
            String result;
            String expResult;
            
            // -------- local set
            // global ^ (force-off) to disable
            spec.setOptions("-" + arg);
            parentRuntime.setOptions(new String[] {"^" + arg});
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = ""+spec.testSetup.commandOptions;
            assertTrue(result, "[]".equals(result));
    
            // global ! (force-on) does not change local-set
            parentRuntime.setOptions(new String[] {"!" + arg});
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = ""+spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            assertTrue(result, expResult.equals(result));
    
            // global - (set) does not change local-set
            parentRuntime.setOptions(new String[] {"-" + arg});
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = ""+spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            assertTrue(result, expResult.equals(result));
    
            // global (unset) does not change local-set
            parentRuntime.setOptions(new String[] {""});
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = ""+spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            assertTrue(result, expResult.equals(result));
    
            // -------- local force-on
            // global ^ (force-off) conflicts with local force-on
            spec.setOptions("!" + arg);
            parentRuntime.setOptions(new String[] {"^" + arg});
            assertTrue(!spec.adoptParentValues(parentRuntime, handler));
            assertTrue(0 != handler.numMessages(null, true));
            handler.init();
    
            // global ! (force-on) does not change local force-on
            parentRuntime.setOptions(new String[] {"!" + arg});
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = ""+spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            assertTrue(result, expResult.equals(result));
    
            // global - (set) does not change local force-on
            parentRuntime.setOptions(new String[] {"-" + arg});
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = ""+spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            assertTrue(result, expResult.equals(result));
    
            // global (unset) does not change local force-on
            parentRuntime.setOptions(new String[] {""});
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = ""+spec.testSetup.commandOptions;
            expResult = (isTestArg ? "[]" : "[-" + arg + "]");
            assertTrue(result, expResult.equals(result));
    
    
            // -------- local force-off
            // global ^ (force-off) does not change local force-off
            spec.setOptions("^" + arg);
            parentRuntime.setOptions(new String[] {"^" + arg});
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = ""+spec.testSetup.commandOptions;
            assertTrue(result, ("[]").equals(result));
    
            // global ! (force-on) conflicts with local force-off
            parentRuntime.setOptions(new String[] {"!" + arg});
            assertTrue(!spec.adoptParentValues(parentRuntime, handler));
            assertTrue(0 != handler.numMessages(null, true));
            handler.init();
    
            // global - (set) overridden by local force-off // XXX??
            parentRuntime.setOptions(new String[] {"-" + arg});
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = ""+spec.testSetup.commandOptions;
            assertTrue(result, ("[]").equals(result));
    
            // global (unset) does not change local force-off
            parentRuntime.setOptions(new String[] {""});
            assertTrue(spec.adoptParentValues(parentRuntime, handler));
            if (0 != handler.numMessages(null, true)) {
                assertTrue(handler.toString(), false);
            }
            result = ""+spec.testSetup.commandOptions;
            assertTrue(result, ("[]").equals(result));
        } finally {
            if (PRINTING && (0 < handler.numMessages(null, true))) {
                MessageUtil.print(System.err, handler, "checkSetupArgs: ");
            }
        }
    }
}
