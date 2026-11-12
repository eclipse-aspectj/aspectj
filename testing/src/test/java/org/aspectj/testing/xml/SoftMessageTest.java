/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/
package org.aspectj.testing.xml;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.SourceLocation;

import junit.framework.TestCase;

public class SoftMessageTest extends TestCase {

	public void testExtraSourceLocationsAreWritten() {
		ISourceLocation location = new SourceLocation(new File("Main.java"), 1);
		ISourceLocation[] extras = {
			new SourceLocation(new File("A.java"), 2),
			new SourceLocation(new File("B.java"), 3) };
		StringWriter written = new StringWriter();
		XMLWriter out = new XMLWriter(new PrintWriter(written));
		SoftMessage.writeXml(out, new Message("text", location, false, extras));
		out.close();
		String xml = written.toString();
		assertTrue(xml, xml.contains("<source line=\"2\" file=\"A.java\"/>"));
		assertTrue(xml, xml.contains("<source line=\"3\" file=\"B.java\"/>"));
		assertFalse("the location of the message is an attribute, not a child: " + xml, xml.contains("<source line=\"1\""));
	}
}
