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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
//import org.aspectj.bridge.IMessage.Kind;
import org.aspectj.testing.util.BridgeUtil;
import org.aspectj.testing.util.Diffs;
import org.aspectj.util.LangUtil;

/**
 * Handle messages during test and calculate differences
 * between expected and actual messages.
 */
public class AjcMessageHandler extends MessageHandler {

	/** Comparator for enclosed IMessage diffs */
	public static final Comparator COMP_IMessage =
		BridgeUtil.Comparators.MEDIUM_IMessage;

	/** Comparator for enclosed File diffs */
	public static final Comparator COMP_File = BridgeUtil.Comparators.WEAK_File;

	/** unmodifiable list of IMessage messages of any type */
	private final List expectedMessagesAsList;

	/** IMessageHolder variant of expectedMessagesAsList */
	private final IMessageHolder expectedMessages;

	/** number of messages FAIL or worse */
	private final int numExpectedFailed;

	/** true if there were no error or worse messages expected */
	private final boolean expectingCommandTrue;

	/** unmodifiable list of File expected to be recompiled */
	private final List expectedRecompiled;
	// Unused now, but reinstate when supported

	/** if true, ignore warnings when calculating diffs and passed() */
	private final boolean ignoreWarnings;

	/** list of File actually recompiled */
	private List actualRecompiled;

	/** cache expected/actual diffs, nullify if any new message */
	private transient CompilerDiffs diffs;

	AjcMessageHandler(IMessageHolder expectedMessages) {
		this(expectedMessages, false);
	}
	/** 
	 * @param messages the (constant) IMessageHolder with expected messages
	 */
	AjcMessageHandler(
		IMessageHolder expectedMessages,
		boolean ignoreWarnings) {
		LangUtil.throwIaxIfNull(messages, "messages");
		this.expectedMessages = expectedMessages;
		expectedMessagesAsList = expectedMessages.getUnmodifiableListView();
		expectedRecompiled = Collections.EMPTY_LIST;
		this.ignoreWarnings = ignoreWarnings;
		int fails = 0;
		int errors = 0;
		for (Object o : expectedMessagesAsList) {
			IMessage m = (IMessage) o;
			IMessage.Kind kind = m.getKind();
			if (IMessage.FAIL.isSameOrLessThan(kind)) {
				fails++;
			} else if (m.isError()) {
				errors++;
			}
		}
		expectingCommandTrue = (0 == (errors + fails));
		numExpectedFailed = fails;
	}

	/** clear out any actual values to be re-run */
	public void init() {
		super.init();
		actualRecompiled = null;
		diffs = null;
	}

	/**
	 * Return true if we have this kind of
	 * message for the same line and store all messages.
	 * @see bridge.tools.impl.ErrorHandlerAdapter#doShowMessage(IMessage)
	 * @return true if message handled (processing should abort)
	 */
	public boolean handleMessage(IMessage message) {
		if (null == message) {
			throw new IllegalArgumentException("null message");
		}
		super.handleMessage(message);
		return expecting(message);
	}

	/** 
	 * Set the actual files recompiled.
	 * @param List of File recompiled - may be null; adopted but not modified
	 * @throws IllegalStateException if they have been set already.
	 */
	public void setRecompiled(List list) {
		if (null != actualRecompiled) {
			throw new IllegalStateException("actual recompiled already set");
		}
		this.actualRecompiled = LangUtil.safeList(list);
	}

	/** Generate differences between expected and actual errors and warnings */
	public CompilerDiffs getCompilerDiffs() {
		if (null == diffs) {
			final List<IMessage> expected;
			final List<IMessage> actual;
			if (!ignoreWarnings) {
				expected = expectedMessages.getUnmodifiableListView();
				actual = this.getUnmodifiableListView();
			} else {
				expected =
					Arrays.asList(
						expectedMessages.getMessages(IMessage.ERROR, true));
				actual = Arrays.asList(this.getMessages(IMessage.ERROR, true));
			}
			// we ignore unexpected info messages,
			// but we do test for expected ones
			final Diffs messages;
            boolean usingNew = true; // XXX extract old API's after shake-out period
            if (usingNew) {
                final IMessage.Kind[] NOSKIPS = new IMessage.Kind[0];
                IMessage.Kind[] skipActual =  new IMessage.Kind[] { IMessage.INFO };
                int expectedInfo 
                    = MessageUtil.numMessages(expected, IMessage.INFO, false);
                if (0 < expectedInfo) {
                    // fyi, when expecting any info messages, have to expect all
                    skipActual = NOSKIPS;
                }
                messages = Diffs.makeDiffs(
                    "message",
                    (IMessage[]) expected.toArray(new IMessage[0]),
                    (IMessage[]) actual.toArray(new IMessage[0]),
                    NOSKIPS,
                    skipActual);
            } else {
                messages = Diffs.makeDiffs(
                    "message", 
                    expected, 
                    actual, 
                    COMP_IMessage, 
                    Diffs.ACCEPT_ALL, 
                    CompilerDiffs.SKIP_UNEXPECTED_INFO);
            }
			Diffs recompiled =
				Diffs.makeDiffs(
					"recompiled",
					expectedRecompiled,
					actualRecompiled,
					COMP_File);
			diffs = new CompilerDiffs(messages, recompiled);
		}
		return diffs;
	}

	/**
	 * Get the (current) result of this run,
	 * ignoring differences in warnings on request.
	 * Note it may return passed (true) when there are expected error messages.
	 * @return false 
	 * if there are any fail or abort messages,
	 * or if the expected errors, warnings, or recompiled do not match actual.
	 */
	public boolean passed() {
		return !getCompilerDiffs().different;
	}

	/** @return true if we are expecting the command to fail - i.e., any expected errors */
	public boolean expectingCommandTrue() {
		return expectingCommandTrue;
	}

	/**
	 * Report results to a handler,
	 * adding all messages
	 * and creating fail messages for each diff.
	 */
	public void report(IMessageHandler handler) {
		if (null == handler) {
			MessageUtil.debug(this, "report got null handler");
		}
		// Report all messages except expected fail+ messages,
		// which will cause the reported-to handler client to gack.
		// XXX need some verbose way to report even expected fail+
		final boolean fastFail = false; // do all messages
		if (0 == numExpectedFailed) {
			MessageUtil.handleAll(handler, this, fastFail);
		} else {
			IMessage[] ra = getMessagesWithoutExpectedFails();
			MessageUtil.handleAll(handler, ra, fastFail);
		}

		CompilerDiffs diffs = getCompilerDiffs();
		if (diffs.different) {
			diffs.messages.report(handler, IMessage.FAIL);
			diffs.recompiled.report(handler, IMessage.FAIL);
		}
	}

	/** @return String consisting of differences and any other messages */
	public String toString() {
		CompilerDiffs diffs = getCompilerDiffs();
		StringBuffer sb = new StringBuffer(super.toString());
		final String EOL = "\n";
		sb.append(EOL);
		render(sb, " unexpected message ", EOL, diffs.messages.unexpected);
		render(sb, "    missing message ", EOL, diffs.messages.missing);
		render(sb, "               fail ", EOL, getList(IMessage.FAIL));
		render(sb, "              abort ", EOL, getList(IMessage.ABORT));
		render(sb, "               info ", EOL, getList(IMessage.INFO));
		return sb.toString(); // XXX cache toString
	}

	/** 
	 * Check if the message was expected, and clear diffs if not.
	 * @return true if we expect a message of this kind with this line number 
	 */
	private boolean expecting(IMessage message) {
		boolean match = false;
		if (null != message) {
			for (Object o : expectedMessagesAsList) {
				// amc - we have to compare against all messages to consume multiple
				// text matches on same line. Return true if any matches.
				if (0 == COMP_IMessage.compare(message, o)) {
					match = true;
				}
			}
		}
		if (!match) {
			diffs = null;
		}
		return match;
	}

	private IMessage[] getMessagesWithoutExpectedFails() {
		IMessage[] result = super.getMessages(null, true);
		// remove all expected fail+ (COSTLY)
		ArrayList<IMessage> list = new ArrayList<>();
		int leftToFilter = numExpectedFailed;
		for (IMessage iMessage : result) {
			if ((0 == leftToFilter)
					|| !IMessage.FAIL.isSameOrLessThan(iMessage.getKind())) {
				list.add(iMessage);
			} else {
				// see if this failure was expected
				if (expectedMessagesHasMatchFor(iMessage)) {
					leftToFilter--; // ok, don't add
				} else {
					list.add(iMessage);
				}
			}
		}
		result = (IMessage[]) list.toArray(new IMessage[0]);
		return result;
	}

	/**
	 * @param actual the actual IMessage to seek a match for in expected messages
	 * @return true if actual message is matched in the expected messages
	 */
	private boolean expectedMessagesHasMatchFor(IMessage actual) {
		for (Object o : expectedMessagesAsList) {
			IMessage expected = (IMessage) o;
			if (0 == COMP_IMessage.compare(expected, actual)) {
				return true;
			}
		}
		return false;
	}

	/** @return immutable list of a given kind - use  null for all kinds */
	private List getList(IMessage.Kind kind) {
		if ((null == kind) || (0 == numMessages(kind, IMessageHolder.EQUAL))) {
			return Collections.EMPTY_LIST;
		}
		return Arrays.asList(getMessages(kind, IMessageHolder.EQUAL));
	}

	/** @return "" if no items or {prefix}{item}{suffix}... otherwise */
	private void render(// LangUtil instead?
	StringBuffer result, String prefix, String suffix, List items) {
		if ((null != items)) {
			for (Object item : items) {
				result.append(prefix + item + suffix);
			}
		}
	}

	/** compiler results for errors, warnings, and recompiled files */
	public static class CompilerDiffs {
		/** Skip info messages when reporting unexpected messages */
		static final Diffs.Filter SKIP_UNEXPECTED_INFO = new Diffs.Filter() {
			public boolean accept(Object o) {
				return ((o instanceof IMessage) && !((IMessage) o).isInfo());
			}
		};
		public final Diffs messages;
		public final Diffs recompiled;
		public final boolean different;

		public CompilerDiffs(Diffs messages, Diffs recompiled) {
			this.recompiled = recompiled;
			this.messages = messages;
			different = (messages.different || recompiled.different);
		}
	}
}
