/*******************************************************************************
 * Copyright (c) 2012 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   John Kew (vmware)         	initial implementation
 *   Lyor Goldstein (vmware)	add support for weaved class being re-defined
 *******************************************************************************/
package org.aspectj.weaver.tools.cache;

import java.util.zip.CRC32;

import org.aspectj.weaver.tools.Trace;
import org.aspectj.weaver.tools.TraceFactory;

/**
 * Basic &quot;common&quot; {@link CacheBacking} implementation
 */
public abstract class AbstractCacheBacking implements CacheBacking {
    protected final Trace  logger=TraceFactory.getTraceFactory().getTrace(getClass());

	protected AbstractCacheBacking () {
		super();
	}

    /**
     * Calculates CRC32 on the provided bytes
     * @param bytes The bytes array - ignored if <code>null</code>/empty
     * @return Calculated CRC
     * @see {@link CRC32}
     */
    public static final long crc (byte[] bytes) {
        if ((bytes == null) || (bytes.length <= 0)) {
            return 0L;
        }

        CRC32   crc32=new CRC32();
        crc32.update(bytes);
        return crc32.getValue();
    }
}
