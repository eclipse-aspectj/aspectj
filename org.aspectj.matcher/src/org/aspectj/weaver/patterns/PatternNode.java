/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.IHasSourceLocation;
import org.aspectj.weaver.ISourceContext;

public abstract class PatternNode implements IHasSourceLocation {
	protected int start, end;
	protected ISourceContext sourceContext;

	public PatternNode() {
		super();
		start = end = -1;
	}

	public int getStart() {
		return start + (sourceContext != null ? sourceContext.getOffset() : 0);
	}

	public int getEnd() {
		return end + (sourceContext != null ? sourceContext.getOffset() : 0);
	}

	public ISourceContext getSourceContext() {
		return sourceContext;
	}

	public String getFileName() {
		return "unknown";
	}

	public void setLocation(ISourceContext sourceContext, int start, int end) {
		this.sourceContext = sourceContext;
		this.start = start;
		this.end = end;
	}

	public void copyLocationFrom(PatternNode other) {
		this.start = other.start;
		this.end = other.end;
		this.sourceContext = other.sourceContext;
	}

	public ISourceLocation getSourceLocation() {
		// System.out.println("get context: " + this + " is " + sourceContext);
		if (sourceContext == null) {
			// System.err.println("no context: " + this);
			return null;
		}
		return sourceContext.makeSourceLocation(this);
	}

	public abstract void write(CompressingDataOutputStream s) throws IOException;

	public void writeLocation(DataOutputStream s) throws IOException {
		s.writeInt(start);
		s.writeInt(end);
	}

	public void readLocation(ISourceContext context, DataInputStream s) throws IOException {
		start = s.readInt();
		end = s.readInt();
		this.sourceContext = context;
	}

	public abstract Object accept(PatternNodeVisitor visitor, Object data);

	public Object traverse(PatternNodeVisitor visitor, Object data) {
		return accept(visitor, data);
	}
}
