/* *******************************************************************
 * Copyright (c) 2026 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Andy Clement     initial implementation
 * ******************************************************************/
package org.aspectj.bridge;

import junit.framework.TestCase;

public class WeaveMessageTest extends TestCase {

	public void testNormalSubstitution() {
		WeaveMessage.WeaveMessageKind kind = new WeaveMessage.WeaveMessageKind(99, "Hi %1, you are %2");
		WeaveMessage m = WeaveMessage.constructWeavingMessage(kind, new String[] { "Bob", "32" });
		assertEquals("Hi Bob, you are 32", m.getMessage());
	}

	public void testInsertContainingPercentDigitDoesNotReintroduceSubstitution() {
		WeaveMessage.WeaveMessageKind kind = new WeaveMessage.WeaveMessageKind(99, "Type %1 woven");
		WeaveMessage m = WeaveMessage.constructWeavingMessage(kind, new String[] { "com.something.Boo%1" });
		assertEquals("Type com.something.Boo%1 woven", m.getMessage());
	}

	public void testInsertContainingPercentLetterDoesNotCrash() {
		WeaveMessage.WeaveMessageKind kind = new WeaveMessage.WeaveMessageKind(99, "Type %1 woven");
		WeaveMessage m = WeaveMessage.constructWeavingMessage(kind, new String[] { "com.something.Boo%a" });
		assertEquals("Type com.something.Boo%a woven", m.getMessage());
	}

	public void testTrailingPercentDoesNotCrash() {
		// Trailing % in insert
		WeaveMessage.WeaveMessageKind kind = new WeaveMessage.WeaveMessageKind(99, "Type %1 woven");
		WeaveMessage m = WeaveMessage.constructWeavingMessage(kind, new String[] { "com.something.Boo%" });
		assertEquals("Type com.something.Boo% woven", m.getMessage());
		// Trailing % in message
		kind = new WeaveMessage.WeaveMessageKind(99, "Type %");
		m = WeaveMessage.constructWeavingMessage(kind, new String[] { "com.something.Boo%" });
		assertEquals("Type %", m.getMessage());
	}
}