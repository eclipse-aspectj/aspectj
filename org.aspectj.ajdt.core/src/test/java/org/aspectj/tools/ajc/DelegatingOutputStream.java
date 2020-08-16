/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Matthew Webster - initial implementation
 *******************************************************************************/
package org.aspectj.tools.ajc;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class DelegatingOutputStream extends OutputStream {

	private boolean verbose = true;
	private OutputStream target;
	private List delegates;
	
	public DelegatingOutputStream (OutputStream os) {
		this.target = os;
		this.delegates = new LinkedList();
	}
	
	public void close() throws IOException {
		target.close();

		for (Object o : delegates) {
			OutputStream delegate = (OutputStream) o;
			delegate.close();
		}
	}

	public void flush() throws IOException {
		target.flush();

		for (Object o : delegates) {
			OutputStream delegate = (OutputStream) o;
			delegate.flush();
		}
	}

	public void write(byte[] b, int off, int len) throws IOException {
		if (verbose) target.write(b, off, len);

		for (Object o : delegates) {
			OutputStream delegate = (OutputStream) o;
			delegate.write(b, off, len);
		}
	}

	public void write(byte[] b) throws IOException {
		if (verbose) target.write(b);

		for (Object o : delegates) {
			OutputStream delegate = (OutputStream) o;
			delegate.write(b);
		}
	}

	public void write(int b) throws IOException {
		if (verbose) target.write(b);

		for (Object o : delegates) {
			OutputStream delegate = (OutputStream) o;
			delegate.write(b);
		}
	}
	
	public boolean add (OutputStream delegate) {
		return delegates.add(delegate);
	}
	
	public boolean remove (OutputStream delegate) {
		return delegates.remove(delegate);
	}

	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}
	
}
