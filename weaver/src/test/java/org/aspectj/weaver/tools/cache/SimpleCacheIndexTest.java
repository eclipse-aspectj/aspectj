/*******************************************************************************
 * Copyright (c) 2024 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 ********************************************************************************/

package org.aspectj.weaver.tools.cache;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Tests how {@link SimpleCache} treats the index file ({@code cache.idx}) it finds in its folder, which may have
 * been tampered with if the folder is shared.
 */
public class SimpleCacheIndexTest extends TestCase {

	private static final byte[] ORIGINAL = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
	private static final byte[] WOVEN = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	/** Same as the (private) cache map, with a storing timer of 0 so that every put writes the index straight away. */
	@SuppressWarnings("unchecked")
	private static Map<String, Object> openIndex(Path folder) throws Exception {
		Class<?> mapClass = Class.forName(SimpleCache.class.getName() + "$StoreableCachingMap");
		Method init = mapClass.getDeclaredMethod("init", String.class, int.class);
		init.setAccessible(true);
		return (Map<String, Object>) init.invoke(null, folder.toString(), 0);
	}

	public void testIndexIsReadBack() throws Exception {
		Path folder = Files.createTempDirectory("ajSimpleCache");
		Map<String, Object> index = openIndex(folder);
		index.put("key", WOVEN);

		assertTrue("index file should have been written", new File(folder.toFile(), "cache.idx").exists());
		assertEquals(WOVEN.length, ((byte[]) openIndex(folder).get("key")).length);
	}

	public void testIndexRejectsUnexpectedClass() throws Exception {
		Path folder = Files.createTempDirectory("ajSimpleCache");
		Gadget.deserialized = false;
		try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(folder.resolve("cache.idx")))) {
			out.writeObject(new Gadget());
		}

		SimpleCache cache = new SimpleCache(folder.toString(), true);

		assertFalse("Disallowed class was deserialized from the cache index", Gadget.deserialized);
		assertNull("a rejected index leaves an empty cache", cache.getAndInitialize("a.B", ORIGINAL, null, null));
	}

	public void testIndexForAnotherFolderIsIgnored() throws Exception {
		Path original = Files.createTempDirectory("ajSimpleCache");
		openIndex(original).put("key", WOVEN);

		// The index stores absolute paths into the folder it was written for; copying it elsewhere must not
		// make the new cache read those files
		Path copy = Files.createTempDirectory("ajSimpleCache");
		Files.copy(original.resolve("cache.idx"), copy.resolve("cache.idx"));

		assertNull("entries of an index that belongs to another folder must be ignored", openIndex(copy).get("key"));
	}

	public void testEntryPointingOutsideTheFolderIsNotRead() throws Exception {
		Path folder = Files.createTempDirectory("ajSimpleCache");
		Path outside = Files.createTempFile("ajOutside", ".class");
		Files.write(outside, WOVEN);
		Map<String, Object> index = openIndex(folder);

		// putAll does not go through the overridden put(), so the map ends up holding a path of our choosing
		index.putAll(Collections.singletonMap("key", outside.toString()));

		assertNull("bytes outside the cache folder must not be returned", index.get("key"));
	}

	private static class Gadget implements Serializable {
		private static final long serialVersionUID = 1L;
		static boolean deserialized;

		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			in.defaultReadObject();
			deserialized = true;
		}
	}
}
