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

package org.aspectj.testing.util;

import java.io.PrintStream;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.MessageUtil.IMessageRenderer;
import org.aspectj.testing.harness.bridge.AbstractRunSpec;
import org.aspectj.testing.harness.bridge.IRunSpec;
import org.aspectj.testing.run.IRunStatus;
import org.aspectj.testing.run.RunValidator;
import org.aspectj.util.LangUtil;

/**
 *
 */
public class RunUtils {

	/** enable verbose for this an any related AbstractRunSpec children */
	public static void enableVerbose(AbstractRunSpec spec) { // instanceof hack
		LangUtil.throwIaxIfNull(spec, "spec");
		spec.runtime.setVerbose(true);
		for (Object element : spec.getChildren()) {
			IRunSpec child = (IRunSpec) element;
			if (child instanceof AbstractRunSpec) {
				enableVerbose((AbstractRunSpec) child);
			}
		}
	}

	/**
	 * Calculate failures for this status.
	 * If the input status has no children and failed, the result is 1.
	 * If it has children and recurse is false, then
	 * the result is the number of children whose status has failed
	 * (so a failed status with some passing and no failing children
	 *  will return 0).
	 * If it has children and recurse is true,
	 * then return the number of leaf failures in the tree,
	 * ignoring (roll-up) node failures.
	 * @return number of failures in children of this status
	 */
	public static int numFailures(IRunStatus status, boolean recurse) {
		int numFails = 0;
		IRunStatus[] children = status.getChildren();
		int numChildren = (null == children? 0 : children.length);
		if (0 == numChildren) {
			if (!RunValidator.NORMAL.runPassed(status)) {
				return 1;
			}
		} else {
			//            int i = 0;
			for (IRunStatus element : children) {
				if (recurse) {
					numFails += numFailures(element, recurse);
				} else {
					if (!RunValidator.NORMAL.runPassed(element)) {
						numFails++;
					}
				}
			}
		}
		return numFails;
	}

	// ------------------------ printing status
	public static void printShort(PrintStream out, IRunStatus status) {
		if ((null == out) || (null == status)) {
			return;
		}
		printShort(out, "", status);
	}

	public static void printShort(PrintStream out, String prefix, IRunStatus status) {
		int numFails = numFailures(status, true);
		String fails = (0 == numFails ? "" : " - " + numFails + " failures");
		out.println(prefix + toShortString(status) + fails);
		IRunStatus[] children = status.getChildren();
		int numChildren = (null == children? 0 : children.length);
		if (0 < numChildren) {
			int i = 0;
			for (IRunStatus element : children) {
				printShort(out, prefix + "[" + LangUtil.toSizedString(i++, 3) + "]: ", element);
				if (!RunValidator.NORMAL.runPassed(element)) {
					numFails++;
				}
			}
		}
	}

	public static void print(PrintStream out, IRunStatus status) {
		if ((null == out) || (null == status)) {
			return;
		}
		print(out, "", status);
	}

	public static void print(PrintStream out, String prefix, IRunStatus status) {
		print(out, prefix, status, MessageUtil.MESSAGE_ALL, MessageUtil.PICK_ALL);
	}

	public static void print(PrintStream out, String prefix, IRunStatus status,
			IMessageRenderer renderer, IMessageHandler selector) {
		String label = status.getIdentifier()
				+ (status.runResult() ? "PASS" : "FAIL");
		out.println(prefix + label);
		out.println(prefix + debugString(status));
		IMessageHolder messageHolder = status;
		if ((null != messageHolder) && (0 < messageHolder.numMessages(null, true))) {
			MessageUtil.print(out, messageHolder, prefix, renderer, selector);
		}
		Throwable thrown = status.getThrown();
		if (null != thrown) {
			out.println(prefix + "--- printing stack trace for thrown");
			thrown.printStackTrace(out);
		}
		IRunStatus[] children = status.getChildren();
		int numChildren = (null == children? 0 : children.length);
		int numFails = 0;
		if (0 < numChildren) {
			out.println(prefix + "--- printing children [" + numChildren + "]");
			int i = 0;
			for (IRunStatus element : children) {
				print(out, prefix + "[" + LangUtil.toSizedString(i++, 3) + "]: ", element);
				if (!RunValidator.NORMAL.runPassed(element)) {
					numFails++;
				}
			}
		}
		if (0 < numFails) {
			label = numFails + " fails " + label;
		}
		out.println("");
	}


	public static String debugString(IRunStatus status) {
		if (null == status) {
			return "null";
		}
		final String[] LABELS =
				new String[] {
						"runResult",
						"id",
						"result",
						"numChildren",
						"completed",
						//"parent",
						"abort",
						"started",
						"thrown",
		"messages" };
		String runResult = status.runResult() ? "PASS" : "FAIL";
		Throwable thrown = status.getThrown();
		String thrownString = LangUtil.unqualifiedClassName(thrown);
		IRunStatus[] children = status.getChildren();
		String numChildren = (null == children? "0" : ""+children.length);
		String numMessages = ""+status.numMessages(null, IMessageHolder.EQUAL);
		Object[] values =
				new Object[] {
						runResult,
						status.getIdentifier(),
						status.getResult(),
						numChildren,
						status.isCompleted(),
						//status.getParent(),               // costly if parent printing us
						status.getAbortRequest(),
						status.started(),
						thrownString,
						numMessages };
		return org.aspectj.testing.util.LangUtil.debugStr(status.getClass(), LABELS, values);
	}

	public static String toShortString(IRunStatus status) {
		if (null == status) {
			return "null";
		}
		String runResult = status.runResult() ? " PASS: " : " FAIL: ";
		return (runResult + status.getIdentifier());
	}

	/** renderer for IRunStatus */
	public interface IRunStatusPrinter {
		void printRunStatus(PrintStream out, IRunStatus status);
	}

	public static final IRunStatusPrinter VERBOSE_PRINTER = new IRunStatusPrinter() {
		@Override
		public String toString() { return "VERBOSE_PRINTER"; }
		/** Render IRunStatus produced by running an AjcTest */
		@Override
		public void printRunStatus(PrintStream out, IRunStatus status) {
			printRunStatus(out, status, "");
		}
		private void printRunStatus(PrintStream out, IRunStatus status, String prefix) {
			LangUtil.throwIaxIfNull(out, "out");
			LangUtil.throwIaxIfNull(status, "status");
			String label = (status.runResult() ? " PASS: " : " FAIL: ")
					+ status.getIdentifier();
			out.println(prefix + "------------ " + label);
			out.println(prefix + "--- result: " + status.getResult());
			if (0 < status.numMessages(null, true)) {
				out.println(prefix + "--- messages ");
				MessageUtil.print(out, status, prefix, MessageUtil.MESSAGE_ALL, MessageUtil.PICK_ALL);
			}
			Throwable thrown = status.getThrown();
			if (null != thrown) {
				out.println(prefix + "--- thrown");
				thrown.printStackTrace(out);
			}
			IRunStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				String number = "[" + LangUtil.toSizedString(i,3) + "] ";
				printRunStatus(out, children[i], prefix + number);
			}
		}
	};

	/** print only status and fail/abort messages */
	public static final IRunStatusPrinter TERSE_PRINTER = new IRunStatusPrinter() {
		@Override
		public String toString() { return "TERSE_PRINTER"; }

		/** print only status and fail messages */
		@Override
		public void printRunStatus(PrintStream out, IRunStatus status) {
			printRunStatus(out, status, "");
		}
		private void printRunStatus(PrintStream out, IRunStatus status, String prefix) {
			LangUtil.throwIaxIfNull(out, "out");
			LangUtil.throwIaxIfNull(status, "status");
			String label = (status.runResult() ? "PASS: " : "FAIL: ")
					+ status.getIdentifier();
			out.println(prefix + label);
			Object result = status.getResult();
			if ((null != result) && (IRunStatus.PASS != result) && (IRunStatus.FAIL != result)) {
				out.println(prefix + "--- result: " + status.getResult());
			}
			if (0 < status.numMessages(IMessage.FAIL, true)) {
				MessageUtil.print(out, status, prefix, MessageUtil.MESSAGE_ALL, MessageUtil.PICK_FAIL_PLUS);
			}
			Throwable thrown = status.getThrown();
			if (null != thrown) {
				out.println(prefix + "--- thrown: " + LangUtil.renderException(thrown, true));
			}
			IRunStatus[] children = status.getChildren();
			for (int i = 0; i < children.length; i++) {
				if (!children[i].runResult()) {
					String number = "[" + LangUtil.toSizedString(i,3) + "] ";
					printRunStatus(out, children[i], prefix + number);
				}
			}
			out.println("");
		}
	};

	/** Render IRunStatus produced by running an AjcTest.Suite. */
	public static final IRunStatusPrinter AJCSUITE_PRINTER  = new IRunStatusPrinter() {
		@Override
		public String toString() { return "AJCSUITE_PRINTER"; }

		/**
		 * Render IRunStatus produced by running an AjcTest.Suite.
		 * This renders only test failures and
		 * a summary at the end.
		 */
		@Override
		public void printRunStatus(PrintStream out, IRunStatus status) {
			LangUtil.throwIaxIfNull(out, "out");
			LangUtil.throwIaxIfNull(status, "status");
			final String prefix = "";
			final boolean failed = status.runResult();
			String label = (status.runResult() ? "PASS: " : "FAIL: ")
					+ status.getIdentifier();
			out.println(prefix + label);
			// print all messages - these are validator comments
			if (0 < status.numMessages(null, true)) {
				MessageUtil.print(out, status, "init", MessageUtil.MESSAGE_ALL, MessageUtil.PICK_ALL);
			}
			// XXX ignore thrown if failed - will be printed as message anyway?
			Throwable thrown = status.getThrown();
			if ((null != thrown) && !failed) {
				out.println(prefix + "--- printing stack trace for thrown");
				thrown.printStackTrace(out);
			}
			IRunStatus[] children = status.getChildren();
			int numChildren = (null == children? 0 : children.length);
			int numFails = 0;
			if (0 < numChildren) {
				for (IRunStatus element : children) {
					if (!RunValidator.NORMAL.runPassed(element)) {
						numFails++;
					}
				}
			}
			if (0 < numFails) {
				out.println(prefix + "--- " + numFails + " failures when running " + children.length + " tests");
				for (int j = 0; j < children.length; j++) {
					if (!RunValidator.NORMAL.runPassed(children[j])) {
						print(out, prefix + "[" + LangUtil.toSizedString(j, 3) + "]: ", children[j]);
						out.println("");
					}
				}
			}
			label = "ran " + children.length + " tests"
					+ (numFails == 0 ? "" : "(" + numFails + " fails)");
			out.println("");
		}

	};
	/** Render IRunStatus produced by running an AjcTest (verbose) */
	public static final IRunStatusPrinter AJCTEST_PRINTER = VERBOSE_PRINTER;

	/** print this with messages, then children using AJCRUN_PRINTER */
	public static final IRunStatusPrinter AJC_PRINTER = new IRunStatusPrinter() {
		@Override
		public String toString() { return "AJC_PRINTER"; }
		/** Render IRunStatus produced by running an AjcTest */
		@Override
		public void printRunStatus(PrintStream out, IRunStatus status) {
			LangUtil.throwIaxIfNull(out, "out");
			LangUtil.throwIaxIfNull(status, "status");
			String label = (status.runResult() ? " PASS: " : " FAIL: ")
					+ status.getIdentifier();
			out.println("------------ " + label);
			MessageUtil.print(out, status, "", MessageUtil.MESSAGE_ALL, MessageUtil.PICK_ALL);
			IRunStatus[] children = status.getChildren();
			for (IRunStatus element : children) {
				AJCRUN_PRINTER.printRunStatus(out, element);
			}
			//out.println("------------   END "  + label);
			out.println("");
		}
	};


	/** print only fail messages */
	public static final IRunStatusPrinter AJCRUN_PRINTER = new IRunStatusPrinter() {
		@Override
		public String toString() { return "AJCRUN_PRINTER"; }
		/** Render IRunStatus produced by running an AjcTest child */
		@Override
		public void printRunStatus(PrintStream out, IRunStatus status) {
			LangUtil.throwIaxIfNull(out, "out");
			LangUtil.throwIaxIfNull(status, "status");
			final boolean orGreater = false;
			int numFails = status.numMessages(IMessage.FAIL, orGreater);
			if (0 < numFails) {
				out.println("--- " + status.getIdentifier());
				IMessage[] fails = status.getMessages(IMessage.FAIL, orGreater);
				for (int i = 0; i < fails.length; i++) {
					out.println("[fail " + LangUtil.toSizedString(i, 3) + "]: "
							+ MessageUtil.MESSAGE_ALL.renderToString(fails[i]));
				}
			}
		}
	};

	private RunUtils() {
	}

}
