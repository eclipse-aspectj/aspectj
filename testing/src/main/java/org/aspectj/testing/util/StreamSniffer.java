/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


/*
 * StreamGrabber.java created on May 16, 2002
 *
 */
package org.aspectj.testing.util;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
  * Listen to a stream using StringBuffer.
  * Clients install and remove buffer to enable/disable listening.
  * Does not affect data passed to underlying stream
  */
public class StreamSniffer extends FilterOutputStream {
    StringBuffer buffer;
    /** have to use delegate, not super, because super we will double-count input */
    final OutputStream delegate;
    
    public StreamSniffer(OutputStream stream) {
        super(stream);
        delegate = stream;
    }

    /** set to null to stop copying */
    public void setBuffer(StringBuffer sb) {
        buffer = sb;
    }

    //---------------- FilterOutputStream 
    public void write(int b) throws IOException {
        StringBuffer sb = buffer;
        if (null != sb) {
            if ((b > Character.MAX_VALUE) 
                || (b < Character.MIN_VALUE)) {
                throw new Error("don't know double-byte"); // XXX
            } else {
                sb.append((char) b);
            }
        }
        delegate.write(b);
    }
    
    public void write(byte[] b) throws IOException {
        StringBuffer sb = buffer;
        if (null != sb) {
            String s = new String(b);
            sb.append(s);
        }
        delegate.write(b);
    }
    
    public void write(byte[] b, int offset, int length) throws IOException {
        StringBuffer sb = buffer;
        if (null != sb) {
            String s = new String(b, offset, length);
            sb.append(s);
        }
        delegate.write(b, offset, length);
    }
}
