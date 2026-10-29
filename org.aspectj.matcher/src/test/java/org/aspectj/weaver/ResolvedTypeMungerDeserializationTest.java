/* *******************************************************************
 * Copyright (c) 2025 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.aspectj.bridge.ISourceLocation;

import junit.framework.TestCase;

/**
 * Checks that ResolvedTypeMunger.readSourceLocation only deserializes the types that
 * writeSourceLocation produces. The source location is embedded in the
 * org.aspectj.weaver.TypeMunger class file attribute, so the serialized stream it reads
 * is taken from whatever class files the weaver is given.
 */
public class ResolvedTypeMungerDeserializationTest extends TestCase {

	public static class Hostile implements Serializable {
		private static final long serialVersionUID = 1L;
		public static boolean deserialized;

		private void readObject(ObjectInputStream in) {
			deserialized = true;
		}
	}

	public void testHostilePayloadIsNotDeserialized() throws Exception {
		Hostile.deserialized = false;
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bytes.write(0); // 0 = uncompressed location, selects the ObjectInputStream branch
		ObjectOutputStream oos = new ObjectOutputStream(bytes);
		oos.writeObject(new Hostile());
		oos.close();
		VersionedDataInputStream s = new VersionedDataInputStream(new ByteArrayInputStream(bytes.toByteArray()), null);
		try {
			ResolvedTypeMunger.readSourceLocation(s);
		} catch (RuntimeException re) {
			// only interested in whether the payload was deserialized
		}
		assertFalse("hostile payload should be rejected before it is deserialized", Hostile.deserialized);
	}

	public void testValidSourceLocationStillReadable() throws Exception {
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		bytes.write(0);
		ObjectOutputStream oos = new ObjectOutputStream(bytes);
		oos.writeObject(Boolean.TRUE);
		oos.writeObject(new File("Foo.java"));
		oos.writeObject(42);
		oos.writeObject(7);
		oos.close();
		VersionedDataInputStream s = new VersionedDataInputStream(new ByteArrayInputStream(bytes.toByteArray()), null);
		ISourceLocation location = ResolvedTypeMunger.readSourceLocation(s);
		assertNotNull(location);
		assertEquals(new File("Foo.java"), location.getSourceFile());
		assertEquals(42, location.getLine());
		assertEquals(7, location.getOffset());
	}
}
