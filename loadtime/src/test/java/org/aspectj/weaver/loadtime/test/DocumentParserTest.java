/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime.test;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
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

    /**
     * An external DTD can declare a default for the aspect name attribute, so fetching it would let whoever
     * controls that DTD pick the aspect to be woven in.
     */
    public void testExternalDtdIsNotFetched() throws Throwable {
        File dtd = write("aopXxe", ".dtd", "<!ELEMENT aspectj ANY>\n<!ATTLIST aspect name CDATA \"evil.Injected\">\n");
        File aopXml = write("aopXxe", ".xml",
                "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE aspectj PUBLIC \"-//Evil//DTD//EN\" \"" + dtd.toURI().toURL() + "\">\n" +
                "<aspectj><aspects><aspect/></aspects></aspectj>\n");
        try {
            Definition def = DocumentParser.parse(aopXml.toURI().toURL());
            assertFalse("aspect name came from the external DTD", def.getAspectClassNames().contains("evil.Injected"));
        } finally {
            dtd.delete();
            aopXml.delete();
        }
    }

    /**
     * Same for a general entity: its replacement text must not be read off disk, so a missing target is
     * simply ignored rather than failing the parse.
     */
    public void testExternalEntityIsNotResolved() throws Throwable {
        File missing = new File(System.getProperty("java.io.tmpdir"), "aopXxeNoSuchFile.txt");
        assertFalse(missing.exists());
        File aopXml = write("aopXxe", ".xml",
                "<?xml version=\"1.0\"?>\n" +
                "<!DOCTYPE aspectj [\n" +
                "  <!ENTITY xxe PUBLIC \"-//Evil//EN\" \"" + missing.toURI().toURL() + "\">\n" +
                "]>\n" +
                "<aspectj><weaver options=\"-showWeaveInfo\"/>&xxe;</aspectj>\n");
        try {
            Definition def = DocumentParser.parse(aopXml.toURI().toURL());
            assertEquals("-showWeaveInfo", def.getWeaverOptions().trim());
        } finally {
            aopXml.delete();
        }
    }

    private static File write(String prefix, String suffix, String content) throws Exception {
        File file = File.createTempFile(prefix, suffix);
        try (Writer writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

}
