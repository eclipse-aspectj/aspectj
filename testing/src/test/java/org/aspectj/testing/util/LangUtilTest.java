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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.textui.TestRunner;

/**
 * 
 * @author isberg
 */
public class LangUtilTest extends TestCase {

    private static final String ME 
        = "org.aspectj.testing.util.LangUtilTest";

    /** @param args ignored */
    public static void main(String[] args) throws Exception {
        class C extends TestRunner {
            public TestResult go(String[] a) throws Exception {
                return start(a);
            }
        }
        TestResult r = new C().go(new String[] {ME});
        if (!r.wasSuccessful()) {
            System.err.println(r.errorCount() + "/" + r.failureCount());
        }
    }
    
	/**
	 * Constructor for LangUtilTest.
	 * @param name
	 */
	public LangUtilTest(String name) {
		super(name);
	}

	void check(String l, StringTokenizer st, int max, String delim) {
        for (int i = 0; i < max; i++) {
            if ((i > 0) && (null != delim)) {
                assertEquals(l, delim, st.nextToken());
            }
            assertEquals(l, ""+i, st.nextToken());
		}
        assertTrue(l, !st.hasMoreTokens());
    }
    

	void checkUnflatten(FTest test) {
        String[] exp = test.unflattened;
        List result = LangUtil.unflatten(test.toUnflatten, test.spec);
        String label = test + " -> " + result;
        assertNotNull(label, result);
        
        assertEquals(label, exp.length, result.size()); 
        for (int i = 0; i < exp.length; i++) {
			assertEquals(label, exp[i], result.get(i));
		}
    }
    

	public void skiptestUnflatten() {
//        LangUtil.FlattenSpec COMMA = LangUtil.FlattenSpec.COMMA;
        LangUtil.FlattenSpec LIST = LangUtil.FlattenSpec.LIST;
            
        FTest[] tests = new FTest[]
        { new FTest("[]", new String[0], LIST)
        , new FTest("[1]", new String[] {"1"}, LIST)
        , new FTest("[1, 2]", new String[] {"1", "2"}, LIST)
        , new FTest("[1,2]", new String[] {"1,2"}, LIST)
        , new FTest("[1, 2, 3]", new String[] {"1","2","3"}, LIST)        
        };
		for (FTest test : tests) {
			checkUnflatten(test);
		}
    }
    

	public void testArrayList() {
        List l = new ArrayList();
        l.add(null);
        l.add(null);
        assertTrue(null == l.get(0));
        assertTrue(null == l.get(1));
        assertTrue(2 == l.size());
        assertEquals("[null, null]", "" + l);
    }
    
    public void testCombineStrings() {
        String[] one = new String[]{};
        String[] two = new String[]{};
        String[] expect = new String[]{};
        checkCombineStrings(one, two, expect);
        
        one = new String[]{null};
        two = new String[]{null};
        expect = new String[]{};
        checkCombineStrings(one, two, expect);
        
        one = new String[]{"1"};
        two = new String[]{null};
        expect = new String[]{"1"};
        checkCombineStrings(one, two, expect);
        
        one = new String[]{null};
        two = new String[]{"2"};
        expect = new String[]{"2"};
        checkCombineStrings(one, two, expect);
        
        one = new String[]{"1"};
        two = new String[]{"2"};
        expect = new String[]{"1", "2"};
        checkCombineStrings(one, two, expect);
        
        one = new String[]{null, null, "1", null, null};
        two = new String[]{null, "2", null};
        expect = new String[]{"1", "2"};
        checkCombineStrings(one, two, expect);

        one = new String[]{"1", "2", "3", "4"};
        two = new String[]{"5", null, "6"};
        expect = new String[]{"1", "2", "3", "4", "5", "6"};
        checkCombineStrings(one, two, expect);
        
    }
    void checkCombineStrings(String[] one, String[] two, String[] expected) {
        String[] actual = LangUtil.combine(one, two);
        String aString = LangUtil.arrayAsList(actual).toString();
        String eString = LangUtil.arrayAsList(expected).toString();
        String both = "actual=\"" + aString + "\" expected=\"" + eString + "\"";
        assertTrue(both, aString.equals(eString));
    }
    
    final String[] sABCDE     = new String[] {"A", "B", "C", "D", "E" };
    final String[] sABC       = new String[] {"A", "B", "C" };
    final String[] sDE        = new String[] {"D", "E" };
    final String[] sabcde     = new String[] {"a", "b", "c", "d", "e" };
    final String[] sabc       = new String[] {"a", "b", "c" };
    final String[] sde        = new String[] {"d", "e" };
    final String[] s12345     = new String[] {"1", "2", "3", "4", "5" };
    final String[] s13579     = new String[] {"1", "3", "5", "7", "9" };
    final String[] s02468     = new String[] {"0", "2", "4", "6", "8" };
    final String[] s135       = new String[] {"1", "3", "5" };
    final String[] s79        = new String[] {"7", "9" };
    final String[] s24        = new String[] {"2", "4" };
    final String[] s0         = new String[] {"0"};
    final String[] s068       = new String[] {"0", "6", "8" };
    final boolean unmodifiable = true;
    final boolean modifiable  = false;
    final List lABCDE         = makeList(unmodifiable, sABCDE);
    final List lABC           = makeList(unmodifiable, sABC);
    final List lDE            = makeList(unmodifiable, sDE);
    final List labcde         = makeList(unmodifiable, sabcde);
    final List labc           = makeList(unmodifiable, sabc);
    final List lde            = makeList(unmodifiable, sde);
    final List l12345         = makeList(unmodifiable, s12345);
    final List l13579         = makeList(unmodifiable, s13579);
    final List l02468         = makeList(unmodifiable, s02468);
    final List l135           = makeList(unmodifiable, s135);
    final List l79            = makeList(unmodifiable, s79);
    final List l24            = makeList(unmodifiable, s24);
    final List l0             = makeList(unmodifiable, s0);
    final List l068           = makeList(unmodifiable, s068);
    final List rlabcde        = makeList(modifiable, sabcde);
    final List rlabc          = makeList(modifiable, sabc);
    final List rlde           = makeList(modifiable, sde);
    final List rlABCDE        = makeList(modifiable, sABCDE);
    final List rlABC          = makeList(modifiable, sABC);
    final List rlDE           = makeList(modifiable, sDE);
    final List rl12345        = makeList(modifiable, s12345);
    final List rl13579        = makeList(modifiable, s13579);
    final List rl02468        = makeList(modifiable, s02468);
    final List rl135          = makeList(modifiable, s135);
    final List rl79           = makeList(modifiable, s79);
    final List rl24           = makeList(modifiable, s24);
    final List rl0            = makeList(modifiable, s0);
    final List rl068          = makeList(modifiable, s068);
    final List NONE = Collections.EMPTY_LIST;
    {
        Collections.shuffle(rlABCDE);
        Collections.shuffle(rlABC);
        Collections.shuffle(rlDE);
        Collections.shuffle(rlabcde);
        Collections.shuffle(rlabc);
        Collections.shuffle(rlde);
        Collections.shuffle(rl12345);
        Collections.shuffle(rl13579);
        Collections.shuffle(rl02468);
        Collections.shuffle(rl135);
        Collections.shuffle(rl79);
        Collections.shuffle(rl24);
        Collections.shuffle(rl0);
        Collections.shuffle(rl068);        
    }
       
   
    public void testDiffsEmptyIdentities() {
        checkDiff(l02468, null, l02468, NONE);
        checkDiff(null, l02468, NONE, l02468);
        checkDiff(l0, null, l0, NONE);
        checkDiff(null, l0, NONE, l0);
        checkDiff(l0, rl0, NONE, NONE);
        checkDiff(labc, rlabc, NONE, NONE);
    }

    public void testDiffsEmpties() {
        checkDiff(NONE, NONE, NONE, NONE);
        checkDiff(null, NONE, NONE, NONE);
        checkDiff(NONE, null, NONE, NONE);
        checkDiff(null, null, NONE, NONE);
        checkDiff(null, null, NONE, NONE);
    }

    public void testDiffsIdentities() {
        checkDiff(l02468, l02468, NONE, NONE);
        checkDiff(rl02468, l02468, NONE, NONE);
        checkDiff(l02468, rl02468, NONE, NONE);
        checkDiff(l13579, l13579, NONE, NONE);
        checkDiff(rl13579, l13579, NONE, NONE);
        checkDiff(l13579, rl13579, NONE, NONE);
        checkDiff(l13579, rl13579, NONE, NONE);
    }
    public void testDiffsEvens() {
        checkDiff(l02468, l12345, l068, l135);
        checkDiff(rl02468, rl12345, rl068, rl135);
    }
    
    public void testDiffsOdds() {
        checkDiff(l13579, l12345, l79, l24);
        checkDiff(rl13579, rl12345, rl79, rl24);
        checkDiff(l13579, rl12345, l79, rl24);
        checkDiff(rl13579, l12345, rl79, l24);
    }
    
    public void testSoftDiffs() {
        checkDiffSoft(labcde, lABCDE, NONE, NONE);
        checkDiffSoft(lABC, labc, NONE, NONE);
        checkDiffSoft(lABCDE, lABC, lDE, NONE);
        checkDiffSoft(lDE, lABCDE, NONE, lABC);
        checkDiffSoft(rlABCDE, rlABC, rlDE, NONE);
        checkDiffSoft(rlDE, rlABCDE, NONE, rlABC);
        checkDiffSoft(labcde, lABC, lDE, NONE);
        checkDiffSoft(lde, lABCDE, NONE, lABC);
        checkDiffSoft(rlabcde, rlABC, rlDE, NONE);
        checkDiffSoft(rlde, rlABCDE, NONE, rlABC);
    }

    // ---------------------- utilities
    List makeList(boolean unmodifiable, String[] ra) {
        if (unmodifiable) {
            return Collections.unmodifiableList(Arrays.asList(ra));
        } else {
			List list = new ArrayList(Arrays.asList(ra));
            return list;
        }
    }
    
    /** check both hard and soft - assuming list contain String */
    void checkDiff(List expected, List actual, List missing, List extra) {
        List extraOut = new ArrayList();
        List missingOut = new ArrayList();
        LangUtil.makeDiffs(expected, actual, missingOut, extraOut);
        checkSame(missing, missingOut);
        checkSame(extra, extraOut);
        extraOut.clear();
        missingOut.clear();
        
        LangUtil.makeSoftDiffs(expected, actual, missingOut, extraOut,
                            String.CASE_INSENSITIVE_ORDER);
        checkSame(missing, missingOut); // XXX does not detect bad order
        checkSame(extra, extraOut);        
    }
    
    void checkSame(Collection one, Collection two) { // just convert and string-compare?
        String label = one + "?=" + two;
        assertTrue(label, (null == one) == (null == two));
        if (null != one) {
            assertTrue(label, one.containsAll(two));
            assertTrue(label, two.containsAll(one));
        }
    }

    /** check only soft - assuming list contain String */
    void checkDiffSoft(List expected, List actual, List missing, List extra) {
        List extraOut = new ArrayList();
        List missingOut = new ArrayList();
        LangUtil.makeSoftDiffs(expected, actual, missingOut, extraOut,
                            String.CASE_INSENSITIVE_ORDER);
        checkSameSoft(missing, missingOut);
        checkSameSoft(extra, extraOut);        
    }

    /** @param one modifiable List of String
     * @param two modifiable List of String
     */
    void checkSameSoft(List one, List two) { // assume String
        String label = one + "?=" + two;
        assertTrue(label, (null == one) == (null == two));
        if (null != one) {
			List aone = new ArrayList(one);
            List atwo = new ArrayList();
            aone.addAll(two);
            Collections.sort(aone);
            Collections.sort(atwo);
            String sone = (""+aone).toLowerCase();
            String stwo = (""+aone).toLowerCase();
            assertTrue(label, sone.equals(stwo));
        }
    }
    
    static class FTest {
        String toUnflatten;
        String[] unflattened;
        LangUtil.FlattenSpec spec;
        FTest(String in, String[] out, LangUtil.FlattenSpec spec) {
            toUnflatten = in;
            unflattened = out;
            this.spec = spec;
        }
        public String toString() {
            return "FTest(" 
                + "toUnflatten=" + toUnflatten
                + ", unflattened=" + Arrays.asList(unflattened)
                + ", spec=" + spec
                + ")";
        }
    }

    
}
