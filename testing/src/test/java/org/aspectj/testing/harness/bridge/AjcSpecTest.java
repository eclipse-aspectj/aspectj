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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.MessageUtil;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

/**
 * Primarily used by others to test AjcTest
 */
public class AjcSpecTest extends TestCase {
	public static final String NOTSAME = " != ";
	public static void sameAjcSuiteSpec(
			AjcTest.Suite.Spec lhsSpec,
			AjcTest.Suite.Spec rhsSpec,
			Assert a) {
		assertNotNull(lhsSpec);
		assertNotNull(rhsSpec);
		Iterator lhs = lhsSpec.getChildren().iterator();
		Iterator rhs = rhsSpec.getChildren().iterator();
		while (lhs.hasNext() && rhs.hasNext()) {
			AjcTest.Spec lhsTest = (AjcTest.Spec) lhs.next();
			AjcTest.Spec rhsTest = (AjcTest.Spec) rhs.next();
			AjcSpecTest.sameAjcTestSpec(lhsTest, rhsTest, a);
		}
		assertTrue(!lhs.hasNext());
		assertTrue(!rhs.hasNext());
	}

	public static void sameAjcTestSpec(
			AjcTest.Spec lhsTest,
			AjcTest.Spec rhsTest,
			Assert a) {
		assertNotNull(lhsTest);
		assertNotNull(rhsTest);
		assertEquals(lhsTest.getBugId(), rhsTest.getBugId());
		assertEquals(lhsTest.getTestDirOffset(), rhsTest.getTestDirOffset());
		// XXX suiteDir varies by run..
		// description, options, paths, comments, keywords
		sameAbstractRunSpec(lhsTest, rhsTest, a);
	}

	public static void sameAbstractRunSpec(
			AbstractRunSpec lhs,
			AbstractRunSpec rhs,
			Assert a) {
		assertEquals(lhs.description, rhs.description);
		// XXX keywords added in .txt reading -
		//sameList(lhs.getKeywordsList(), rhs.getKeywordsList(), a);
		// XXX sameList(lhs.globalOptions, rhs.globalOptions, a);
		sameList(lhs.getOptionsList(), rhs.getOptionsList(), a);
		sameList(lhs.getPathsList(), rhs.getPathsList(), a);
		assertEquals(lhs.isStaging(), rhs.isStaging());
		sameList(lhs.keywords, rhs.keywords, a);
		assertEquals(lhs.comment, rhs.comment);
		assertEquals(lhs.badInput, rhs.badInput);
		// xml adds sourceloc?
		//sameSourceLocation(lhs.getSourceLocation(), rhs.getSourceLocation(), a);
		// XXX also sourceLocations?
		sameMessages(lhs.getMessages(), rhs.getMessages(), a);
	}

	/** @return normal form - null is "", "" is "", and others are {fully.qualified.class}.toString().trim() */
	static String normal(Object input) {
		if ((null == input) || ("".equals(input))) {
			return "";
		} else {
			return input.getClass().getName() + "." + input.toString().trim();
		}
	}

	/** @return true if these match after normalizing */
	public static void same(Object lhs, Object rhs, Assert a) {
		lhs = normal(lhs);
		rhs = normal(rhs);
		assertTrue(lhs + NOTSAME + rhs, lhs.equals(rhs));
	}

	/** @return true if both are empty (null or no entries) or if all match */
	public static void sameRA(String[] lhs, String[] rhs, Assert a) {
		if (null == lhs) {
			assertTrue((null == rhs) || (0 == rhs.length));
		} else if (null == rhs) {
			assertTrue(0 == lhs.length);
		} else {
			String l = normal(lhs);
			String r = normal(rhs);
			assertTrue(l + NOTSAME + r, l.equals(r));
		}
	}

	/** @return normal form for String[] items*/
	static String normal(String[] items) {
		return (null == items ? "[]" : normal(Arrays.asList(items)));
	}

	/** @return normal form for list items */
	static String normal(List list) {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		boolean first = true;
		for (Object o : list) {
			if (!first) {
				sb.append(", ");
			} else {
				first = false;
			}
			sb.append(normal(o));
		}
		sb.append("]");
		return sb.toString();
	}

	/** @return true if both are empty (null or no entries) or if all match after trimming */
	public static void sameListSize(List lhs, List rhs) {
		if (null == lhs) {
			assertTrue((null == rhs) || (0 == rhs.size()));
		} else if (null == rhs) {
			assertTrue(0 == lhs.size());
		} else {
			assertTrue(rhs.size() == lhs.size());
		}
	}

	/** @return true if both are empty (null or no entries) or if all match after trimming */
	public static void sameList(List lhs, List rhs, Assert a) {
		sameListSize(lhs, rhs);
		String l = normal(lhs);
		String r = normal(rhs);
		String label = l + NOTSAME + r;
		assertTrue(label, l.equals(r));
	}

	//    /**
	//     * Normalize and compare:
	//     * <li>bug id's are not compared since extracted during xml writing</li>
	//     * <li>keyword compare is disabled since keywords are generated during xml reading.</li>
	//     * <li>description compare is normalized by stripping bug ids</li>
	//     * <li>String and arrays are equal when empty (null or 0-length)</li>
	//     * @see Ajctest#stripBugId(String)
	//     */
	//    public static void sameAjcTest(AjcTest lhs, AjcTest rhs, Assert reporter) {
	//        Assert a = reporter;
	//        String label = lhs + NOTSAME + rhs;
	//        assertTrue(label, null != lhs);
	//        assertTrue(label, null != rhs);
	//        //assertTrue(label, lhs.ignoreWarnings == rhs.ignoreWarnings);
	//        // XXX disabled - not in .txt
	//        // sameStringList(lhs.keywords, rhs.keywords, a);
	//        // sameString(lhs.bugId, rhs.bugId, a);
	//        // argh - bugid stripped from description
	//        //same(AjcTest.stripBugId(lhs.description), AjcTest.stripBugId(lhs.description), a);
	//        //sameRA(lhs.globals, rhs.globals, a);
	//        //lhs.reset();
	//        //rhs.reset();
	//        boolean gotOne = false;
	//        IMessageHolder holder = new MessageHandler();
	//        assertTrue(label, !holder.hasAnyMessage(IMessage.FAIL, IMessageHolder.ORGREATER));
	//        while (lhs.hasNextRun() && rhs.hasNextRun()) {
	//            sameIAjcRun((IAjcRun) lhs.nextRun(holder), (IAjcRun) rhs.nextRun(holder), reporter);
	//            assertTrue(label, !holder.hasAnyMessage(IMessage.FAIL, IMessageHolder.ORGREATER));
	//            if (!gotOne) {
	//                gotOne = true;
	//            }
	//        }
	//        assertTrue(label, gotOne);
	//        assertTrue(label, !lhs.hasNextRun());
	//        assertTrue(label, !rhs.hasNextRun());
	//    }

	public static void sameIAjcRun(IAjcRun lhs, IAjcRun rhs, Assert reporter) {
		//        Assert a = reporter;
		assertTrue(lhs != null);
		assertTrue(rhs != null);
		Class c = lhs.getClass();
		assertTrue(c == rhs.getClass());
		AbstractRunSpec lhsSpec;
		AbstractRunSpec rhsSpec;

		if (c == CompilerRun.class) {
			CompilerRun.Spec l = ((CompilerRun) lhs).spec;
			CompilerRun.Spec r = ((CompilerRun) rhs).spec;
			lhsSpec = l;
			rhsSpec = r;
			assertEquals(l.argfiles, r.argfiles);
			assertEquals(l.aspectpath, r.aspectpath);
			assertEquals(l.testSrcDirOffset, r.testSrcDirOffset);
			assertEquals(l.compiler, r.compiler);
			assertEquals(l.includeClassesDir, r.includeClassesDir);
			assertEquals(l.reuseCompiler, r.reuseCompiler);
			assertEquals(l.sourceroots, r.sourceroots);
			assertEquals(l.extdirs, r.extdirs);
		} else if (c == JavaRun.class) {
			JavaRun.Spec l = ((JavaRun) lhs).spec;
			JavaRun.Spec r = ((JavaRun) rhs).spec;
			lhsSpec = l;
			rhsSpec = r;
			assertTrue(l.skipTester ==  r.skipTester);
			assertEquals(l.className, r.className);
			assertEquals(l.javaVersion, r.javaVersion);
			assertEquals(l.skipTester, r.skipTester);
			assertEquals(l.outStreamIsError, r.outStreamIsError);
			assertEquals(l.errStreamIsError, r.errStreamIsError);
		} else if (c == IncCompilerRun.class) {
			IncCompilerRun.Spec l = ((IncCompilerRun) lhs).spec;
			IncCompilerRun.Spec r = ((IncCompilerRun) rhs).spec;
			lhsSpec = l;
			rhsSpec = r;
			assertEquals(l.tag, r.tag);
			assertEquals(l.fresh, r.fresh);
		} else {
			assertTrue(lhs.equals(rhs));
			return;
		}
		sameSpec(lhsSpec, rhsSpec, reporter);
	}

	public static void sameSpec(AbstractRunSpec lhs, AbstractRunSpec rhs, Assert a) {
		if ((null == lhs) && (null == rhs)) {
			return;
		}
		assertTrue(lhs != null);
		assertTrue(rhs != null);
		assertEquals(""+lhs.getOptionsList(), ""+rhs.getOptionsList());
		sameList(lhs.getPathsList(), rhs.getPathsList(), a);
		sameMessages(lhs.getMessages(), rhs.getMessages(), a);
		sameDirChangesList(lhs.dirChanges, rhs.dirChanges, a);
	}

	public static void sameDirChangesList(List<DirChanges.Spec> lhs, List<DirChanges.Spec> rhs, Assert a) {
		if ((null == lhs) && (null == rhs)) {
			return;
		}
		assertTrue(rhs != null);
		assertTrue(lhs != null);
		sameListSize(lhs, rhs);
		Iterator<DirChanges.Spec> lhsIter = lhs.iterator();
		Iterator<DirChanges.Spec> rhsIter = rhs.iterator();
		while (lhsIter.hasNext() && rhsIter.hasNext()) {
			sameDirChangesSpec(lhsIter.next(), rhsIter.next(), a);
		}
	}

	public static void sameDirChangesSpec(DirChanges.Spec lhs, DirChanges.Spec rhs, Assert a) {
		if ((null == lhs) && (null == rhs)) {
			return;
		}
		assertTrue(rhs != null);
		assertTrue(lhs != null);
		assertEquals(lhs.defaultSuffix, rhs.defaultSuffix);
		assertEquals(lhs.dirToken, rhs.dirToken);
		assertEquals(lhs.fastFail, rhs.fastFail);
		assertEquals(lhs.expDir, rhs.expDir); // XXX normalize?
		sameList(lhs.updated, rhs.updated, a);
		sameList(lhs.removed, rhs.removed, a);
		sameList(lhs.added, rhs.added, a);
	}

	public static void sameMessages(IMessageHolder one, IMessageHolder two, Assert a) {
		if ((null == one) && (null == two)) {
			return;
		}
		// order matters here
		ListIterator lhs = one.getUnmodifiableListView().listIterator();
		ListIterator rhs = two.getUnmodifiableListView().listIterator();
		while (lhs.hasNext() && rhs.hasNext()) {
			sameMessage((IMessage) lhs.next(), (IMessage) rhs.next(), a);
		}
		assertTrue(!lhs.hasNext());
		assertTrue(!rhs.hasNext());
	}

	public static void sameMessage(IMessage lhs, IMessage rhs, Assert a) {
		if ((null == lhs) && (null == rhs)) {
			return;
		}
		assertTrue(lhs != null);
		assertTrue(rhs != null);
		assertTrue(lhs.getKind() == rhs.getKind());
		same(lhs.getMessage(), rhs.getMessage(), a);
		same(lhs.getDetails(), rhs.getDetails(), a);
		assertEquals(lhs.getThrown(), rhs.getThrown());
		sameSourceLocation(lhs.getSourceLocation(), rhs.getSourceLocation());
		sameSourceLocations(lhs.getExtraSourceLocations(), rhs.getExtraSourceLocations());
	}
	public static void sameSourceLocations(List lhs, List rhs) {
		sameListSize(lhs, rhs);
		if ((null == lhs) || (0 == lhs.size())) {
			return;
		}
		// ok, do order-dependent check..
		ListIterator iterLeft = lhs.listIterator();
		ListIterator iterRight = rhs.listIterator();
		while (iterLeft.hasNext() && iterRight.hasNext()) {
			ISourceLocation left = (ISourceLocation) iterLeft.next();
			ISourceLocation right = (ISourceLocation) iterRight.next();
			sameSourceLocation(left, right);
		}
		assertTrue(!iterLeft.hasNext());
		assertTrue(!iterRight.hasNext());

	}

	public static void sameSourceLocation(ISourceLocation lhs, ISourceLocation rhs) {
		if ((null == lhs) && (null == rhs)) {
			return;
		}
		assertTrue(lhs != null);
		assertTrue(rhs != null);
		assertTrue(lhs.getLine() == rhs.getLine());
		assertTrue(lhs.getColumn() == rhs.getColumn());
		assertTrue(lhs.getOffset() == rhs.getOffset());
		assertTrue(lhs.getEndLine() == rhs.getEndLine());
		// XXX need to compare files, permitting null == NONE
	}

	/**
	 * Constructor for AjcSpecTest.
	 * @param name
	 */
	public AjcSpecTest(String name) {
		super(name);
	}

	public void testMinimal() {
		AjcTest.Spec one = new AjcTest.Spec();
		AjcTest.Spec two = new AjcTest.Spec();
		// empty/identity tests
		sameAjcTestSpec(one, two, this);

		one.addOption("-one");
		one.addKeyword("keyword");
		one.addPath("path");
		IMessage m = MessageUtil.info("info message");
		one.addMessage(m);
		DirChanges.Spec dcspec = new DirChanges.Spec();
		dcspec.setDirToken("dirToken");
		dcspec.setDefaultSuffix(".suffix");
		one.addDirChanges(dcspec);

		// full/identity tests
		sameAjcTestSpec(one, one, this);
		// XXX need to clone...

		// XXX need to test that more differences are detected
		boolean passed = false;
		try {
			sameAjcTestSpec(one, two, this);
		} catch (AssertionFailedError e) {
			passed = true;
		}
		assertTrue("did not get expected exception", passed);
	}
}
