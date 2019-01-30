/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import java.util.ArrayList;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.Message;
import org.aspectj.testing.run.IRun;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.Runner;
import org.aspectj.testing.run.WrappedRunIterator;
import org.aspectj.util.LangUtil;


/** 
 * This wraps an AbstractRunSpec, which has children that
 * return IRunIterator, the results of which we return 
 * from nextRun(..)  
 * We extract global options from the AbstractRunSpec options 
 * and set the global options in the AbstractRunSpec,
 * which is responsible for setting them in any children
 * during makeRun(..).
 */
public class RunSpecIterator implements IRunIterator  {
    /*
     * When constructed, this gets its own spec
     * and a sandbox to be used for making all children.
     * In nextRun() this uses the spec's child specs
     * and the sandbox to create the next child run iterator.
     * This returns all the run provided by that child iterator
     * before going to the next child.
     */
 
    /** spec for this test */
    public final AbstractRunSpec spec; // XXX reconsider public after debugging
    
     /** current sandbox by default shared by all children */    
    Sandbox sandbox;
    
    /** keep our copy to avoid recopying */
    ArrayList childSpecs;
    
    /** index into child specs of next run */
    int specIndex;
    
    /** child creation until the start of each run */
    final Validator validator;
    
    /** current child iterator */
    IRunIterator childIterator;
    
    final boolean haltOnFailure;

	private int numIncomplete;

	private final IMessage.Kind failureKind;

//	private boolean didCleanup;
    
    /**
     * Create a RunSpecIterator.
     * Failure messages are of type IMessage.ABORT if abortOnFailure is true,
     * or IMessage.ERROR otherwise.
     * @param spec the AbstractRunSpec whose children we iterate - not null
     * @param sandbox the default Sandbox to use for children to make runs - may be null
     * @param haltOnFailure if true, stop after any failure in providing runs
     */
    public RunSpecIterator(
        AbstractRunSpec spec, 
        Sandbox sandbox,
        Validator validator, 
        boolean haltOnFailure) { 
        this(spec, sandbox, validator, haltOnFailure, 
            (haltOnFailure ? IMessage.ABORT : IMessage.ERROR));
    }
    
    /**
     * Create a RunSpecIterator, specifying any failure message kind.
     * @param spec the AbstractRunSpec whose children we iterate - not null
     * @param sandbox the default Sandbox to use for children to make runs - may be null
     * @param haltOnFailure if true, stop after any failure in providing runs
     * @param failureKind the IMessage.Kind for any failure messages - if null, no messages sent
     */
    public RunSpecIterator(
        AbstractRunSpec spec, 
        Sandbox sandbox,
        Validator validator, 
        boolean haltOnFailure,
        IMessage.Kind failureKind) { 
        LangUtil.throwIaxIfNull(spec, "spec");
        LangUtil.throwIaxIfNull(sandbox, "sandbox");
        LangUtil.throwIaxIfNull(validator, "validator");
        this.sandbox = sandbox;
        this.spec = spec;
        this.validator = validator;
        this.haltOnFailure = haltOnFailure;
        this.failureKind = failureKind;
        reset();
    }
    
    /**
     * @return value set on construction for abortOnError
	 * @see org.aspectj.testing.run.IRunIterator#abortOnFailure()
	 */
	public boolean abortOnFailure() {
        return haltOnFailure;
	}

    /** reset to start at the beginning of the child specs. */
    public void reset() { 
        specIndex = 0;
        childSpecs = spec.getWorkingChildren();
        childIterator = null;
        numIncomplete = 0;
    }
    
    /** @return int number of child run attempts that did not produce IRun */
    public int getNumIncomplete() {
        return numIncomplete;
    }
    
    /**
	 * @see org.aspectj.testing.run.IRunIterator#hasNextRun()
	 */
    public boolean hasNextRun() {
       return ((specIndex < childSpecs.size())
                    || ((null != childIterator) 
                        && childIterator.hasNextRun()));
    }

    /**
     * Get the next child IRunIterator as an IRun.
     * In case of failure to get the next child, 
     * numIncomplete is incremented, and
     * a message of type failureKind is passed to the handler 
     * (if failureKind was defined in the contructor).
     * @return next child IRunIterator wrapped as an IRun 
     */
    public IRun nextRun(final IMessageHandler handler, Runner runner) {
    	validator.pushHandler(handler);
		try {
	        IRun result = null;
	        IRunSpec childSpec = null;
	        String error = null;
	        String specLabel = "getting run for child of \"" + spec + "\" ";
	        while ((null == result) && hasNextRun() && (null == error)) {
	            if (null == childIterator) {
	                childSpec = (IRunSpec) childSpecs.get(specIndex++);
	                if (null == childSpec) {
	                    error = "unexpected - no more child specs at " + --specIndex;
	                } else {        
	                    Sandbox sandbox = makeSandbox(childSpec, validator);
	                    if (null == sandbox) {
	                        error = "unable to make sandbox for \"" + childSpec + "\"";
	                        childIterator = null;
	                    } else {      
	                        IRunIterator iter = childSpec.makeRunIterator(sandbox, validator);
	                        if (null == iter) {
	                            // client should read reason why from validator
	                            error = "child \"" + childSpec + "\".makeRunIterator(..) returned null";
	                        } else {
	                            // hoist: result not wrapped but single IRun
	                            if ((iter instanceof WrappedRunIterator)) {
	                                if (!iter.hasNextRun()) {
	                                    error = "child \"" + childSpec + "\".hasNextRun()"
	                                        + " is not true - should be exactly one run";
	                                } else {
	                                    result = iter.nextRun(handler, runner);
	                                    if (null == result) {
	                                        error = "child \"" + childSpec + "\".nextRun()"
	                                            + " returned null - should be exactly one run";
	                                    } else {
	                                        childIterator = null;
	                                        return result;
	                                    }
	                                }
	                            } else {
	                                childIterator = iter;
	                            }
	                        }
	                    }
	                }
	            }
	            if (null != childIterator) {
	                result = runner.wrap(childIterator, null);
	                childIterator = null;
	            } else if (null != error) {
	                numIncomplete++;
	                if (null != failureKind) {
	                    handler.handleMessage(new Message(specLabel + error, failureKind, null, null));
	                }
	                if (!haltOnFailure) {
	                    error = null;
	                } else if (result != null) {
	                    result = null; // do not return result if halting due to failure
	                }
	            }
	        }
	        return result;
		} finally {
			validator.popHandler(handler);
		}
    }

	/**
	 * @see org.aspectj.testing.run.IRunIterator#iterationCompleted()
	 */
	public void iterationCompleted() {
	}
   
    public String toString() {
        return "" + spec;
        //return "RunSpecIterator(" + specIndex + ", " + spec + ")" ;
    }
    
    /*
     * Subclasses may:
     * - set the sandbox on construction
     * - lazily set it on first use
     * - set it for each child
     */    

    /**
     * Create the sandbox used for each child.
     * This implementation always uses the sandbox set on construction.
     * Subclasses may decide to create one sandbox per child iterator.
     */
    protected Sandbox makeSandbox(IRunSpec child, Validator validator) {
        return getSandbox();
    }

    /** Get the sandbox currently in use */
    protected Sandbox getSandbox() {
        return sandbox;
    }

    /** Set the sandbox currently in use */
    protected void setSandbox(Sandbox sandbox) {
        LangUtil.throwIaxIfNull(sandbox, "sandbox");
        this.sandbox = sandbox;
    }
    
}
