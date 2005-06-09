/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.patterns;

import junit.framework.TestCase;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.io.LineNumberReader;
import java.io.FileReader;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class VisitorTestCase extends TestCase {

    private Set pointcuts = new HashSet();
    private Set typePatterns = new HashSet();

    protected void setUp() throws Exception {
        super.setUp();
        LineNumberReader rp = new LineNumberReader(new FileReader("../weaver/testdata/visitor.pointcuts.txt"));
        feed(rp, pointcuts);
        rp.close();
        LineNumberReader rt = new LineNumberReader(new FileReader("../weaver/testdata/visitor.typepatterns.txt"));
        feed(rt, typePatterns);
        rt.close();
    }

    private void feed(LineNumberReader r, Set set) throws Exception {
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            set.add(line);
        }
    }

    public void testPointcuts() {
        if (pointcuts.isEmpty()) {
            fail("Empty pointcuts file!");
        }
        for (Iterator iterator = pointcuts.iterator(); iterator.hasNext();) {
            String pointcut = (String) iterator.next();
            try  {
                PatternNodeVisitor.DumpPointcutVisitor.check(pointcut);
            } catch (Throwable t) {
                t.printStackTrace();
                fail("Failed on '"+pointcut+"': " +t.toString());
            }
        }
    }

    public void testTypePatterns() {
        if (typePatterns.isEmpty()) {
            fail("Empty typePatterns file!");
        }
        for (Iterator iterator = typePatterns.iterator(); iterator.hasNext();) {
            String tp = (String) iterator.next();
            try  {
                TypePattern p = new PatternParser(tp).parseTypePattern();
                PatternNodeVisitor.DumpPointcutVisitor.check(p, true);
            } catch (Throwable t) {
                fail("Failed on '"+tp+"': " +t.toString());
            }
        }
    }
}
