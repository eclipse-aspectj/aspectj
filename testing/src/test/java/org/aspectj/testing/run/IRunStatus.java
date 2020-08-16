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


package org.aspectj.testing.run;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageUtil;

/**
 * Encapsulate status and results for a run.
 * A run starts and then completes normally
 * (finished(Object result)),
 * abruptly (thrown(Throwable thrown)),
 * or by user request (abort(Object request)).
 * @author isberg
 */
public interface IRunStatus extends IMessageHolder {
    /** clients use this when signalling completion without a specific result */
	Object VOID = Boolean.TRUE;

    /** result object for successful (unset) boolean run result */
	Boolean PASS = Boolean.TRUE;

    /** result object for failed (unset) boolean run result */
	Boolean FAIL = Boolean.FALSE;

    /** clients use this when signalling abort without any specific request */
	Object ABORT = Boolean.FALSE;

    /** clients use this when signalling abort because no object to run */
	Object ABORT_NORUN = MessageUtil.ABORT_NOTHING_TO_RUN;

    /** returned from getChildren when there are no children */
	IRunStatus[] EMPTY_NEST = new IRunStatus[0];

    //------------------- process controls
    /**
     * Set identifier associated with this run, if any
     * @throws IllegalArgumentException if id is null
     * @throws IllegalStateException if id has already been set
     */
    void setIdentifier(Object id);

    //------------------- process controls
    /**
     * Call before any start() or after isCompleted() would return true
     * to reset this to its pre-start state
     * @throws IllegalStateException if start() has been called
     * and isCompleted() is not true.
     */
    void reset();

    /**
     * Call only once to signal this run has started.
     * @throws IllegalStateException if start() has been called
     */
    void start();

    /**
     * Call this or thrown only once after start()
     * to signal this run has ended.
     * If this represents a void process, use VOID.
     * @param result the Object returned by this run.
     * @throws IllegalStateException if start() was not called first
     *  or if either completed(Object) or thrown(Throwable) have been called.
     */
    void finish(Object result);

    /**
     * Call to signal this run is ending by request.
     * If there is no message, use ABORT.
     * @param request the Object request to abort,
     *         or ABORT if none is available.
     * @throws IllegalStateException if start() was not called first
     *  or if either completed(Object) or thrown(Throwable) have been called.
     */
    void abort(Object request);

    /**
     * Call this or completed only once after start()
     * to signal this run has ended.
     * @throws IllegalStateException if start() was not called first
     *  or if either completed(Object) or thrown(Throwable) have been called.
     */
    void thrown(Throwable thrown);


    /**
     * Call this for the status to throw an unchecked exception
     * of the type that its controller understands.
     * It is an error for a IRunStatus to continue normally
     * after this is invoked.
     */
    void completeAbruptly();
    //------------------- process messages
    /**
     * Detect whether a message of a given kind has been handled.
     * @param kind the IMessage.Kind of the message to detect
     * @param orGreater if true, then also accept any message of a greater kind
     * @param includeChildren if true, then also search in any child IRunStatus
     * @return true if any such message is detected
     */
    boolean hasAnyMessage(IMessage.Kind kind, boolean orGreater, boolean includeChildren);

    /**
     * Get all messages or those of a specific kind, optionally in children as well
     * Pass null to get all kinds.
     * @param kind the IMessage.Kind expected, or null for all messages
     * @param orGreater if true, also get any greater than the target kind
     *         as determined by IMessage.Kind.COMPARATOR
     * @param includeChildren if true, then also search in any child IRunStatus
     * @return IMessage[] of messages of the right kind, or IMessage.NONE
     */
    IMessage[] getMessages(IMessage.Kind kind, boolean orGreater, boolean includeChildren);

    /**
     * Call this any time to signal any messages.
     * (In particular, the IRun caller may use this to register messages
     * about the mishandling of the run by the ResultStatusI by the callee.)
     * This is a shortcut for getMessageHandler().handleMessage(..);
     */
    //boolean handleMessage(IMessage message);

    //------------------- process display
    /** @return true if this run has started */
    boolean started();

    /** @return true if one of the result, abort request, or thrown is available */
    boolean isCompleted();

    /** @return true if this got an abort request */
    boolean aborted();

    /**
     * @return true if completed and not aborted and no thrown
     * or messages with kind ABORT or FAIL or ERROR
     */
    boolean runResult();

    /** get the invoker for any subruns */
    Runner getRunner();

    /** @return the Object result, if any, of this run */
    Object getResult();

    /** @return the Object abort request, if any, of this run */
    Object getAbortRequest();

    /** @return the Throwable thrown, if any, by this run */
    Throwable getThrown();

    /** @return any Message[] signalled, or SILENCE if none */
    IMessage[] getMessages();

    /** @return the identifier set for this run, if any */
    Object getIdentifier();

    //------------------- subprocess
    /**
     * Add a record for a child run
     * and install self as parent.
     * @throws IllegalArgumentException if child is null
     */
    void addChild(IRunStatus child);

    /**
     * Register this as the run parent.
     * (Any run that does addChild(IRunStatus) should register as parent.)
     * @throws IllegalArgumentException if parent is null
     * @throws IllegalStateException if parent exists already
     */
    void registerParent(IRunStatus parent);

    /**
     * @return the current children of this run, or EMPTY_NEST if none
     */
    IRunStatus[] getChildren();

    /**
     * @return the currently-registered parent, or null if none
     */
    IRunStatus getParent();
}
