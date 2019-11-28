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

package org.aspectj.testingutil;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.aspectj.util.LangUtil;

/**
 * This is source for a sample .class file.
 * It is compiled and the corresponding .class files are
 * checked in under the testdata directory.
 * It has no other purpose.
 */
public class TestCompareClassFile implements Runnable {
    public static final String STATIC_CONST = "STATIC_CONST";
	public static void main(String[] args) {
        // tc static references
        long l = Math.abs(System.currentTimeMillis());
	   String s = STATIC_CONST + " is constant";
    }
    public static void runStatic() {
    }
    private static void privateRunStatic() {
    }
    static void defaultRunStatic() {
    }
    protected static void protectedRunStatic() {
    }

    private long privateLong;
    private final Object privateFinalObject;

    private TestCompareClassFile() {
        super();
        privateLong = System.currentTimeMillis();
        // method-local inner class
        privateFinalObject = new Runnable() { public void run(){}};
    }

    /** implement Runnable */
    public void run() {
    }
    private void privateRun() {
    }
    void defaultRun() {
    }
    protected void protectedRun() {
    }

    // ------- misc stolen utility code
    // Collections Util
    /*
    public static List getListInMap(Map<Object,List> map, Object key) {
        List list = map.get(key);
        if (list == null) {
            list = new ArrayList();
            map.put(key, list);
        }
        return list;
    }

    public static SortedSet getSortedSetInMap(Map<Object,SortedSet> map, Object key) {
        SortedSet list = map.get(key);
        if (list == null) {
            list = new TreeSet();
            map.put(key, list);
        }
        return list;
    }
    */

    // LangUtil
    /**
     * Make a copy of the array.
     * @return an array with the same component type as source
     * containing same elements, even if null.
     * @throws IllegalArgumentException if source is null
     */
    public static final Object[] copy(Object[] source) {
        final Class c = source.getClass().getComponentType();
        Object[] result = (Object[]) Array.newInstance(c, source.length);
        System.arraycopy(source, 0, result, 0, result.length);
        return result;
    }
    /**
     * Trim ending lines from a StringBuffer,
     * clipping to maxLines and further removing any number of
     * trailing lines accepted by checker.
     * @param stack StringBuffer with lines to elide
     * @param maxLines int for maximum number of resulting lines
     */
    static void elideEndingLines(StringBuffer stack, int maxLines) {
        if ((null == stack) || (0 == stack.length())) {
            return;
        }
        final LinkedList lines = new LinkedList();
        StringTokenizer st = new StringTokenizer(stack.toString(),"\n\r");
        while (st.hasMoreTokens() && (0 < --maxLines)) {
            lines.add(st.nextToken());
        }
        st = null;

        String line;
        int elided = 0;
        while (!lines.isEmpty()) {
            line = (String) lines.getLast();
            if (null == line) {
                break;
            } else {
                elided++;
                lines.removeLast();
            }
        }
        if ((elided > 0) || (maxLines < 1)) {
            final int EOL_LEN = LangUtil.EOL.length();
            int totalLength = 0;
            while (!lines.isEmpty()) {
                totalLength += EOL_LEN + ((String) lines.getFirst()).length();
                lines.removeFirst();
            }
            if (stack.length() > totalLength) {
                stack.setLength(totalLength);
                if (elided > 0) {
                    stack.append("    (... " + elided + " lines...)");
                }
            }
        }
    }

}
