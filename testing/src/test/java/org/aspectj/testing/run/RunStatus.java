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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.testing.util.BridgeUtil;
import org.aspectj.util.LangUtil;

/**
 * Default implementation of {@link IRunStatus}.
 * 
 * @author isberg
 */
public class RunStatus implements IRunStatus {
	private static int INDEX;

	private final String name = "RunStatus[" + INDEX++ + "]";

	/** true after isCompleted() evaluated true */
	private boolean evaluated;

	/** true after starting() called */
	private boolean started; // set only in starting()

	/** true after finished(int) or thrown(Throwable) called */
	private boolean completed; // set only in completed(boolean)

	/** contains any id set */
	private Object id;

	/** after finished(Object) called, contains that parameter */
	private Object result;

	/** after aborted(Object) called, contains that parameter */
	private Object abortRequest;

	/** use to set exception thrown, if any */
	private Throwable thrown;

	/** list of any messages submitted */
	private IMessageHolder messageHolder;

	/** list of any child status */
	private ArrayList children;

	/** parent of this status */
	private IRunStatus parent;

	/** invoker for any subruns */
	private Runner runner;

	/** controls runResult() */
	private IRunValidator validator;

	// public RunStatus() {
	// reset();
	// validator = RunValidator.NORMAL;
	// }

	public RunStatus(IMessageHolder holder, Runner runner) {
		reset(holder, runner);
		validator = RunValidator.NORMAL;
	}

	// ------------------- process controls

	/**
	 * Set identifier associated with this run, if any
	 * 
	 * @throws IllegalArgumentException if id is null
	 * @throws IllegalStateException if id has already been set
	 */
	public void setIdentifier(Object id) {
		if (null == id) {
			throw new IllegalArgumentException("null id");
		} else if ((null != this.id) && (id != this.id)) {
			throw new IllegalStateException("attempt to set id " + this.id + " to " + id);
		}
		this.id = id;
	}

	/**
	 * Set the current validator.
	 * 
	 * @param delegate the RunValidatorI to use when calculating runStatus
	 * @throws IllegalArgumentException if delegate is null
	 */
	public void setValidator(IRunValidator delegate) {
		if (null == delegate) {
			throw new IllegalArgumentException("null delegate");
		}
		if (validator != delegate) {
			validator = delegate;
		}
	}

	/**
	 * Call before any start() or after isCompleted() would return true to reset this to its pre-start state
	 * 
	 * @throws IllegalStateException if start() has been called and isCompleted() is not true.
	 */
	public void reset() {
		reset((IMessageHolder) null, (Runner) null);
	}

	/**
	 * Call before any start() or after isCompleted() would return true to reset this to its pre-start state. Does not affect
	 * validator.
	 * 
	 * @param holder the IMessageHolder to use after resetting.
	 * @throws IllegalStateException if start() has been called and isCompleted() is not true.
	 */
	public void reset(IMessageHolder holder, Runner runner) {
		if (null == runner) {
			throw new IllegalArgumentException("null runner");
		}
		if (started && (!isCompleted())) {
			throw new IllegalStateException("no reset() until isCompleted");
		}
		started = false;
		completed = false;
		result = null;
		abortRequest = null;
		thrown = null;
		parent = null;
		id = null;
		messageHolder = (null != holder ? holder : new MessageHandler());
		if (null != children) {
			children.clear();
		}
		this.runner = runner;
		evaluated = false;
	}

	/**
	 * Call only once to signal this run has started.
	 * 
	 * @throws IllegalStateException if start() has been called
	 */
	public void start() {
		if (started) {
			throw new IllegalStateException("started already");
		} else if (isCompleted()) {
			throw new IllegalStateException("start after completed (do reset)");
		}
		started = true;
	}

	/**
	 * Call this or thrown only once after start() to signal this run has ended. If this represents a void process, use VOID.
	 * 
	 * @param result the Object returned by this run.
	 * @throws IllegalStateException if start() was not called first or if either completed(Object) or thrown(Throwable) have been
	 *         called.
	 */
	public void finish(Object result) {
		if (null == result) {
			throw new IllegalArgumentException("null result");
		} else if (isCompleted()) {
			throw new IllegalStateException("completed then finish " + result);
		}
		this.result = result;
	}

	/**
	 * Call to signal this run is ending by request. If this represents a void process, use VOID. If there is no message, use ABORT.
	 * 
	 * @param request the Object request to abort, or ABORT if none is available.
	 * @throws IllegalStateException if start() was not called first or if either completed(Object) or thrown(Throwable) have been
	 *         called.
	 */
	public void abort(Object request) {
		if (null == request) {
			throw new IllegalArgumentException("null request");
		} else if (isCompleted()) {
			throw new IllegalStateException("completed then abort " + request);
		}
		this.abortRequest = request;
	}

	/**
	 * Call this or completed only once after start() to signal this run has ended.
	 * 
	 * @throws IllegalStateException if start() was not called first or if either completed(Object) or thrown(Throwable) have been
	 *         called.
	 */
	public void thrown(Throwable thrown) {
		if (null == thrown) {
			throw new IllegalArgumentException("null thrown");
		} else if (isCompleted()) {
			throw new IllegalStateException("completed then thrown " + thrown);
		}
		this.thrown = thrown;
	}

	public void completeAbruptly() {
		throw new Error("completing abruptly"); // XXX configurable
	}

	/**
	 * @return true if completed, not aborted, no thrown, no messages of kind ERROR, FAIL or ABORT, and result object is not
	 *         IRunStatus.FAIL.
	 * @see org.aspectj.testing.run.IRunStatus#runResult()
	 */
	public boolean runResult() {
		return validator.runPassed(this);
	}

	public boolean hasAnyMessage(IMessage.Kind kind, boolean orGreater, boolean includeChildren) {
		if (messageHolder.hasAnyMessage(kind, orGreater)) {
			return true;
		}
		if (includeChildren) {
			IRunStatus[] kids = getChildren();
			for (IRunStatus kid : kids) {
				if (kid.hasAnyMessage(kind, orGreater, true)) {
					return true;
				}
			}
		}
		return false;
	}

	public IMessage[] getMessages(IMessage.Kind kind, boolean orGreater, boolean includeChildren) {
		IMessage[] result = getMessages(kind, orGreater);
		if (!includeChildren) {
			return result;
		}
		ArrayList sink = new ArrayList();
		if (!LangUtil.isEmpty(result)) {
			sink.addAll(Arrays.asList(result));
		}

		IRunStatus[] kids = getChildren();
		for (IRunStatus kid : kids) {
			result = kid.getMessages(kind, orGreater, includeChildren);
			if (!LangUtil.isEmpty(result)) {
				sink.addAll(Arrays.asList(result));
			}
		}
		return (IMessage[]) sink.toArray(new IMessage[0]);
	}

	// ------------------- process messages
	/**
	 * Call this any time before isCompleted() would return true to signal any messages.
	 * 
	 * @throws IllegalStateException if isCompleted().
	 */
	public boolean handleMessage(IMessage message) {
		return messageHolder.handleMessage(message);
	}

	public boolean isIgnoring(IMessage.Kind kind) {
		return messageHolder.isIgnoring(kind);
	}

	public void dontIgnore(IMessage.Kind kind) {
		messageHolder.dontIgnore(kind);
	}

	public void ignore(IMessage.Kind kind) {
		messageHolder.ignore(kind);
	}

	/**
	 * @see org.aspectj.bridge.IMessageHolder#hasAnyMessage(org.aspectj.bridge.IMessage.Kind, boolean)
	 */
	public boolean hasAnyMessage(IMessage.Kind kind, boolean orGreater) {
		return messageHolder.hasAnyMessage(kind, orGreater);
	}

	/**
	 * @see org.aspectj.bridge.IMessageHolder#getMessages(org.aspectj.bridge.IMessage.Kind, boolean)
	 */
	public IMessage[] getMessages(IMessage.Kind kind, boolean orGreater) {
		return messageHolder.getMessages(kind, orGreater);
	}

	/**
	 * @see org.aspectj.bridge.IMessageHolder#numMessages(org.aspectj.bridge.IMessage.Kind, boolean)
	 */
	public int numMessages(IMessage.Kind kind, boolean orGreater) {
		return messageHolder.numMessages(kind, orGreater);
	}

	// ------------------- process display
	/** @return true if this run has started */
	public boolean started() {
		return started;
	}

	/** @return true if one of the result, abort request, or thrown is available */
	public boolean isCompleted() {
		if (!evaluated) {
			if (started && ((null != thrown) || (null != result) || (null != abortRequest))) {
				completed = true;
				evaluated = true;
			}
		}
		return completed;
	}

	/** @return true if this got an abort request */
	public boolean aborted() {
		return (completed && (null != abortRequest));
	}

	/** @return the Object result, if any, of this run */
	public Object getResult() {
		return result;
	}

	/** @return the Object abort request, if any, of this run */
	public Object getAbortRequest() {
		return abortRequest;
	}

	/** @return the Throwable thrown, if any, by this run */
	public Throwable getThrown() {
		return thrown;
	}

	/**
	 * @see org.aspectj.bridge.IMessageHolder#getUnmodifiableListView()
	 */
	public List<IMessage> getUnmodifiableListView() {
		return messageHolder.getUnmodifiableListView();
	}

	/** @return any Message[] signalled, or IMessage.NONE if none */
	public IMessage[] getMessages() {
		return messageHolder.getMessages(null, IMessageHolder.EQUAL);
	}

	/** @return the identifier set for this run, if any */
	public Object getIdentifier() {
		return id;
	}

	/**
	 * @see org.aspectj.bridge.IMessageHolder#clearMessages()
	 * @throws UnsupportedOperationException always
	 */
	public void clearMessages() throws UnsupportedOperationException {
		throw new UnsupportedOperationException("use reset");
	}

	// ------------------- subprocess

	/** get the invoker for any subrunners */
	public Runner getRunner() {
		return runner;
	}

	/**
	 * Add a record for a child run and install self as parent.
	 * 
	 * @throws IllegalArgumentException if child is null
	 */
	public void addChild(IRunStatus child) {
		if (null == child) {
			throw new IllegalArgumentException("null child");
		}
		if (null == children) {
			children = new ArrayList();
		}
		children.add(child);
	}

	/**
	 * Register this as the run parent. (Any run that does addChild(IRunStatus) should register as parent.)
	 * 
	 * @throws IllegalArgumentException if parent is null
	 * @throws IllegalStateException if parent exists already
	 */
	public void registerParent(IRunStatus parent) {
		if (null == parent) {
			throw new IllegalArgumentException("null parent");
		} else if (null != this.parent) {
			throw new IllegalStateException("adding parent " + parent + " to parent " + this.parent);
		}
		this.parent = parent;
	}

	/**
	 * @return the current children of this run, or EMPTY_NEST if none
	 */
	public IRunStatus[] getChildren() {
		if ((null == children) || (0 == children.size())) {
			return EMPTY_NEST;
		} else {
			return (IRunStatus[]) children.toArray(EMPTY_NEST);
		}
	}

	/**
	 * @return the currently-registered parent, or null if none
	 */
	public IRunStatus getParent() {
		return parent;
	}

	public String toString() {
		return BridgeUtil.toShortString(this);
	}

	public String toLongString() {
		StringBuffer sb = new StringBuffer();
		sb.append(BridgeUtil.toShortString(this));
		if ((null != children) && (0 < children.size())) {
			String label = "### --------- " + name;
			int index = 0;
			for (Object child : children) {
				IRunStatus childStatus = (IRunStatus) child;
				String childLabel = "\n" + label + " child[" + index++ + "] " + childStatus.getIdentifier();
				sb.append(childLabel + " ---- start\n");
				sb.append(childStatus.toString());
				sb.append(childLabel + " ---- end\n");
			}
		}
		return sb.toString();
	}
}
