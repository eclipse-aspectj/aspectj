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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.testing.util.TestDiffs.TestResult;
import org.aspectj.util.FileUtil;
import org.aspectj.util.LangUtil;

/**
 * Result struct for expected/actual diffs for Collection
 */
public class Diffs {

	/**
	 * Compare IMessage.Kind based on kind priority.
	 */
	public static final Comparator KIND_PRIORITY = new Comparator() {
		/**
		 * Compare IMessage.Kind based on kind priority.
		 * @throws NullPointerException if anything is null
		 */
		public int compare(Object lhs, Object rhs) {
			return ((IMessage.Kind) lhs).compareTo((IMessage.Kind) rhs);
		}
	};
	/**
	 * Sort ISourceLocation based on line, file path.
	 */
	public static final Comparator SORT_SOURCELOC = new Comparator() {
		/**
		 * Compare ISourceLocation based on line, file path.
		 * @throws NullPointerException if anything is null
		 */
		public int compare(Object lhs, Object rhs) {
			ISourceLocation l = (ISourceLocation) lhs;
			ISourceLocation r = (ISourceLocation) rhs;
			int result = getLine(l) - getLine(r);
			if (0 != result) {
				return result;
			}
			String lp = getSourceFile(l).getPath();
			String rp = getSourceFile(r).getPath();
			return lp.compareTo(rp);
		}
	};

	/**
	 * Compare IMessages based on kind and source location line (only).
	 */
	public static final Comparator<IMessage> MESSAGE_LINEKIND = new Comparator<IMessage>() {
		/**
		 * Compare IMessages based on kind and source location line (only).
		 * @throws NullPointerException if anything is null
		 */
		public int compare(IMessage lhs, IMessage rhs) {
			IMessage lm = lhs;
			IMessage rm = rhs;
            ISourceLocation ls = (lm == null ? null : lm.getSourceLocation());
            ISourceLocation rs = (rm == null ? null : rm.getSourceLocation());
            int left = (ls == null ? -1 : ls.getLine());
            int right = (rs == null ? -1 : rs.getLine());
			int result = left - right;
			if (0 == result) {
				result = lm.getKind().compareTo(rm.getKind());
			}
			return result;
		}
	};
	public static final Filter ACCEPT_ALL = new Filter() {
		public boolean accept(Object o) {
			return true;
		}
	};
	//     // XXX List -> Collection b/c comparator orders
	//    public static final Diffs NONE
	//        = new Diffs("NONE", Collections.EMPTY_LIST, Collections.EMPTY_LIST);

	public static Diffs makeDiffs(
		String label,
		List expected,
		List actual,
		Comparator comparator) {
		return makeDiffs(
			label,
			expected,
			actual,
			comparator,
			ACCEPT_ALL,
			ACCEPT_ALL);
	}

	public static Diffs makeDiffs(
		String label,
		IMessage[] expected,
		IMessage[] actual) {
		return makeDiffs(label, expected, actual, null, null);
	}

    private static int getLine(ISourceLocation loc) {
        int result = -1;
        if (null != loc) {
            result = loc.getLine();
        }
        return result;
    }
    private static int getLine(IMessage message) {
        int result = -1;
        if ((null != message)) {
            result = getLine(message.getSourceLocation());
        }
        return result;
    }

    private static File getSourceFile(ISourceLocation loc) {
        File result = ISourceLocation.NO_FILE;
        if (null != loc) {
            result = loc.getSourceFile();
        }
        return result;
    }

	public static Diffs makeDiffs(
		String label,
		IMessage[] expected,
		IMessage[] actual,
		IMessage.Kind[] ignoreExpectedKinds,
		IMessage.Kind[] ignoreActualKinds) {
		List<IMessage> exp = getExcept(expected, ignoreExpectedKinds);
		List<IMessage> act = getExcept(actual, ignoreActualKinds);

		List<IMessage> missing = new ArrayList<>();
		List<IMessage> unexpected = new ArrayList<>();

		if (LangUtil.isEmpty(expected)) {
			unexpected.addAll(act);
		} else if (LangUtil.isEmpty(actual)) {
			missing.addAll(exp);
		} else {
			ListIterator expectedIterator = exp.listIterator();
			int lastLine = Integer.MIN_VALUE + 1;
			List expectedFound = new ArrayList();
			List expectedForLine = new ArrayList();
			for (ListIterator iter = act.listIterator(); iter.hasNext();) {
				IMessage actualMessage = (IMessage) iter.next();
				int actualLine = getLine(actualMessage);
				if (actualLine != lastLine) {
					// new line - get all messages expected for it
					if (lastLine > actualLine) {
						throw new Error("sort error");
					}
					lastLine = actualLine;
					expectedForLine.clear();
					while (expectedIterator.hasNext()) {
						IMessage curExpected =
							(IMessage) expectedIterator.next();
                        int curExpectedLine = getLine(curExpected);
						if (actualLine == curExpectedLine) {
							expectedForLine.add(curExpected);
						} else {
							expectedIterator.previous();
							break;
						}
					}
				}
				// now check actual against all expected on that line
				boolean found = false;
				IMessage expectedMessage = null;
				for (Iterator iterator = expectedForLine.iterator();
					!found && iterator.hasNext();
					) {
					expectedMessage = (IMessage) iterator.next();
					found = expectingMessage(expectedMessage, actualMessage);
				}
				if (found) {
					iter.remove();
					if (expectedFound.contains(expectedMessage)) {
						// XXX warn: expected message matched two actual
					} else {
						expectedFound.add(expectedMessage);
					}
				} else {
					// unexpected: any actual result not found
					unexpected.add(actualMessage);
				}
			}
			// missing: all expected results not found
			exp.removeAll(expectedFound);
			missing.addAll(exp);
		}
		return new Diffs(label, missing, unexpected);
	}

	public static Diffs makeDiffs(
		String label,
		List expected,
		List actual,
		Comparator comparator,
		Filter missingFilter,
		Filter unexpectedFilter) {
		label = label.trim();
		if (null == label) {
			label = ": ";
		} else if (!label.endsWith(":")) {
			label += ": ";
		}
		final String thisLabel = " " + label;
		ArrayList miss = new ArrayList();
		ArrayList unexpect = new ArrayList();

		org.aspectj.testing.util.LangUtil.makeSoftDiffs(
			expected,
			actual,
			miss,
			unexpect,
			comparator);
		if (null != missingFilter) {
			for (ListIterator iter = miss.listIterator(); iter.hasNext();) {
				if (!missingFilter.accept(iter.next())) {
					iter.remove();
				}
			}
		}
		if (null != unexpectedFilter) {
			for (ListIterator iter = unexpect.listIterator();
				iter.hasNext();
				) {
				if (!unexpectedFilter.accept(iter.next())) {
					iter.remove();
				}
			}
		}
		return new Diffs(thisLabel, miss, unexpect);
	}

	//    /**
	//     * Shift over elements in sink if they are of one of the specified kinds.
	//     * @param sink the IMessage[] to shift elements from
	//     * @param kinds
	//     * @return length of sink after shifting
	//     *      (same as input length if nothing shifted)
	//     */
	//    public static int removeKinds(IMessage[] sink, IMessage.Kind[] kinds) {
	//        if (LangUtil.isEmpty(kinds)) {
	//            return sink.length;
	//        } else if (LangUtil.isEmpty(sink)) {
	//            return 0;
	//        }
	//        int from = -1;
	//        int to = -1;
	//        for (int j = 0; j < sink.length; j++) {
	//            from++;
	//            if (null == sink[j]) {
	//                continue;
	//            }
	//            boolean remove = false;
	//            for (int i = 0; !remove && (i < kinds.length); i++) {
	//                IMessage.Kind kind = kinds[i];
	//                if (null == kind) {
	//                    continue;
	//                }
	//                if (0 == kind.compareTo(sink[j].getKind())) {
	//                    remove = true;
	//                }
	//            }
	//            if (!remove) {
	//                to++;
	//                if (to != from) {
	//                    sink[to] = sink[from];
	//                }
	//            }
	//        }
	//        return to+1;
	//    }

	/**
	 * @param expected the File from the expected source location
	 * @param actual the File from the actual source location
	 * @return true if exp is ISourceLocation.NO_FILE
	 *  or exp path is a suffix of the actual path
	 *  (after using FileUtil.weakNormalize(..) on both)
	 */
	static boolean expectingFile(File expected, File actual) {
		if (null == expected) {
			return (null == actual);
		} else if (null == actual) {
			return false;
		}
		if (expected != ISourceLocation.NO_FILE) {
			String expPath = FileUtil.weakNormalize(expected.getPath());
			String actPath = FileUtil.weakNormalize(actual.getPath());
			if (!actPath.endsWith(expPath)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Soft comparison for expected message will not check a corresponding
	 * element in the actual message unless defined in the expected message.
	 * <pre>
	 *   message
	 *     kind                must match (constant/priority)
	 *     message             only requires substring
	 *     thrown              ignored
	 *     column              ignored
	 *     endline             ignored
	 *     details             only requires substring
	 *     sourceLocation
	 *       line              must match, unless expected < 0
	 *       file              ignored if ISourceLocation.NOFILE
	 *                         matches if expected is a suffix of actual
	 *                         after changing any \ to /
	 *     extraSourceLocation[]
	 *                         if any are defined in expected, then there
	 *                         must be exactly the actual elements as are
	 *                         defined in expected (so it is an error to
	 *                         not define all if you define any)
	 * <pre>
	 * @param expected
	 * @param actual
	 * @return true if we are expecting the line, kind, file, message,
	 *    details, and any extra source locations.
	 *    (ignores column/endline, thrown) XXX
	 */
	static boolean expectingMessage(IMessage expected, IMessage actual) {
		if (null == expected) {
			return (null == actual);
		} else if (null == actual) {
			return false;
		}
		if (0 != expected.getKind().compareTo(actual.getKind())) {
			return false;
		}
		if (!expectingSourceLocation(expected.getSourceLocation(),
			actual.getSourceLocation())) {
			return false;
		}
		if (!expectingText(expected.getMessage(), actual.getMessage())) {
			return false;
		}
		if (!expectingText(expected.getDetails(), actual.getDetails())) {
			return false;
		}
		ISourceLocation[] esl =
			expected.getExtraSourceLocations().toArray(
			new ISourceLocation[0]);
		ISourceLocation[] asl =
			actual.getExtraSourceLocations().toArray(
			new ISourceLocation[0]);

		Arrays.sort(esl, SORT_SOURCELOC);
		Arrays.sort(asl, SORT_SOURCELOC);
		if (!expectingSourceLocations(esl, asl)) {
			return false;
		}
		return true;
	}

	/**
	 * This returns true if no ISourceLocation are specified
	 * (i.e., it ignored any extra source locations if no expectations stated).
	 * XXX need const like NO_FILE.
	 * @param expected the sorted ISourceLocation[] expected
	 * @param expected the actual sorted ISourceLocation[]
	 * @return true if any expected element is expected by the corresponding actual element.
	 */
	static boolean expectingSourceLocations(
		ISourceLocation[] expected,
		ISourceLocation[] actual) {
		if (LangUtil.isEmpty(expected)) {
			return true;
		} else if (LangUtil.isEmpty(actual)) {
			return false;
		} else if (actual.length != expected.length) {
			return false;
		}
		for (int i = 0; i < actual.length; i++) {
			if (!expectingSourceLocation(expected[i], actual[i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @param expected
	 * @param actual
	 * @return true if any expected line/file matches the actual line/file,
	 *         accepting a substring as a file match
	 */
	static boolean expectingSourceLocation(
		ISourceLocation expected,
		ISourceLocation actual) {
        int eline = getLine(expected);
        int aline = getLine(actual);
		if ((-1 < eline) && (eline != aline)) {
			return false;
		}
		if (!expectingFile(getSourceFile(expected), getSourceFile(actual))) {
			return false;
		}
		return true;
	}

	/**
	 * @param expected the String in the expected message
	 * @param actual the String in the actual message
	 * @return true if both are null or actual contains expected
	 */
	static boolean expectingText(String expected, String actual) {
		if (null == expected) {
			return true; // no expectations
		} else if (null == actual) {
			return false; // expected something
		} else {
			return (actual.contains(expected));
		}
	}

	private static List<IMessage> getExcept(
		IMessage[] source,
		IMessage.Kind[] skip) {
		List<IMessage> sink = new ArrayList<>();
		if (LangUtil.isEmpty(source)) {
			return sink;
		}

		if (LangUtil.isEmpty(skip)) {
			sink.addAll(Arrays.asList(source));
			sink.sort(MESSAGE_LINEKIND);
			return sink;
		}
		for (IMessage message : source) {
			IMessage.Kind mkind = message.getKind();
			boolean skipping = false;
			for (int j = 0; !skipping && (j < skip.length); j++) {
				if (0 == mkind.compareTo(skip[j])) {
					skipping = true;
				}
			}
			if (!skipping) {
				sink.add(message);
			}
		}
		sink.sort(MESSAGE_LINEKIND);
		return sink;
	}

	private static List harden(List list) {
		return (
			LangUtil.isEmpty(list)
				? Collections.EMPTY_LIST
				: Collections.unmodifiableList(list));
	}

	/** name of the thing being diffed - used only for reporting */
	public final String label;

	/** immutable List */
	public final List<TestResult> missing;

	/** immutable List */
	public final List<TestResult> unexpected;

	/** true if there are any missing or unexpected */
	public final boolean different;

	/**
	 * Struct-constructor stores these values,
	 * wrapping the lists as unmodifiable.
	 * @param label the String label for these diffs
	 * @param missing the List of missing elements
	 * @param unexpected the List of unexpected elements
	 */
	public Diffs(String label, List missing, List unexpected) {
		this.label = label;
		this.missing = harden(missing);
		this.unexpected = harden(unexpected);
		different =
			((0 != this.missing.size()) || (0 != this.unexpected.size()));
	}

	/**
	 * Report missing and extra items to handler.
	 * For each item in missing or unexpected, this creates a {kind} IMessage with
	 * the text "{missing|unexpected} {label}: {message}"
	 * where {message} is the result of
	 * <code>MessageUtil.renderMessage(IMessage)</code>.
	 * @param handler where the messages go - not null
	 * @param kind the kind of message to construct - not null
	 * @param label the prefix for the message text - if null, "" used
	 * @see MessageUtil#renderMessage(IMessage)
	 */
	public void report(IMessageHandler handler, IMessage.Kind kind) {
		LangUtil.throwIaxIfNull(handler, "handler");
		LangUtil.throwIaxIfNull(kind, "kind");
		if (different) {
			for (Object value : missing) {
				String s = MessageUtil.renderMessage((IMessage) value);
				MessageUtil.fail(handler, "missing " + label + ": " + s);
			}
			for (Object o : unexpected) {
				String s = MessageUtil.renderMessage((IMessage) o);
				MessageUtil.fail(handler, "unexpected " + label + ": " + s);
			}
		}
	}

	/** @return "{label}: (unexpected={#}, missing={#})" */
	public String toString() {
		return label
			+ "(unexpected="
			+ unexpected.size()
			+ ", missing="
			+ missing.size()
			+ ")";
	}
	public interface Filter {
		/** @return true to keep input in list of messages */
		boolean accept(Object input);
	}
}
