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
package org.aspectj.weaver.loadtime.test;

import java.net.URL;

import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.loadtime.definition.DocumentParser;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DocumentParserTest extends TestCase {

    public void testSimple() throws Throwable {
        URL url = DocumentParserTest.class.getResource("simple.xml");
        Definition def = DocumentParser.parse(url);
        assertEquals("-showWeaveInfo", def.getWeaverOptions().trim());
    }

    public void testSimpleWithDtd() throws Throwable {
        URL url = DocumentParserTest.class.getResource("simpleWithDtd.xml");
        Definition def = DocumentParser.parse(url);
        assertEquals("-showWeaveInfo", def.getWeaverOptions().trim());
        assertTrue(def.getAspectClassNames().contains("test.Aspect"));

        assertEquals("foo..bar.Goo+", def.getIncludePatterns().get(0));
        assertEquals("@Baz", def.getAspectExcludePatterns().get(0));
        assertEquals("@Whoo", def.getAspectIncludePatterns().get(0));
        assertEquals("foo..*", def.getDumpPatterns().get(0));
        assertEquals(true,def.shouldDumpBefore());
    }

}
