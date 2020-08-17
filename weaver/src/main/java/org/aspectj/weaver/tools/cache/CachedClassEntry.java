/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   John Kew (vmware)          initial implementation
 *   Lyor Goldstein (vmware)	add support for weaved class being re-defined
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
    private final byte[] weavedBytes;
    private final EntryType type;

    public CachedClassEntry(CachedClassReference ref, byte[] weavedBytes, EntryType type) {
        this.weavedBytes = weavedBytes;
        this.ref = ref;
        this.type = type;
    }

    public String getClassName() {
        return ref.getClassName();
    }

    public byte[] getBytes() {
        return weavedBytes;
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

    @Override
    public int hashCode() {
        return getClassName().hashCode()
             + getKey().hashCode()
             + type.hashCode()
             ;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;

        CachedClassEntry    other=(CachedClassEntry) obj;
        if (getClassName().equals(other.getClassName())
         && getKey().equals(other.getKey())
         && (type == other.type)) {
             return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return getClassName() + "[" + type + "]";
    }
}
