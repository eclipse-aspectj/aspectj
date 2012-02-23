/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   John Kew (vmware)         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.tools.cache;

/**
 * Represents a class which has been cached
 */
public class CachedClassEntry {
	enum EntryType {
		GENERATED,
		WEAVED,
		IGNORED,
	}

	private final CachedClassReference ref;
	private final byte[] bytes;
	private final EntryType type;

	public CachedClassEntry(CachedClassReference ref, byte[] bytes, EntryType type) {
		this.bytes = bytes;
		this.ref = ref;
		this.type = type;
	}

	public String getClassName() {
		return ref.getClassName();
	}

	public byte[] getBytes() {
		return bytes;
	}

	public String getKey() {
		return ref.getKey();
	}

	public boolean isGenerated() {
		return type == EntryType.GENERATED;
	}

	public boolean isWeaved() {
		return type == EntryType.WEAVED;
	}

	public boolean isIgnored() {
		return type == EntryType.IGNORED;
	}
}
