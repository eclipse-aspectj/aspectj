/* *******************************************************************
 * Copyright (c) 2009 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     initial implementation   Andy Clement
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.List;

import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.VersionedDataInputStream;

/**
 * Constructed based on an @DeclareMixin being found in an aspect.
 * 
 * @author Andy Clement
 */
public class DeclareParentsMixin extends DeclareParents {
	private int bitflags = 0x0000; // for future expansion

	public DeclareParentsMixin(TypePattern child, List parents) {
		super(child, parents, true);
	}

	public DeclareParentsMixin(TypePattern child, TypePatternList parents) {
		super(child, parents, true);
	}

	public boolean equals(Object other) {
		if (!(other instanceof DeclareParentsMixin)) {
			return false;
		}
		DeclareParentsMixin o = (DeclareParentsMixin) other;
		return o.child.equals(child) && o.parents.equals(parents) && o.bitflags == bitflags;
	}

	public int hashCode() {
		int result = 23;
		result = 37 * result + child.hashCode();
		result = 37 * result + parents.hashCode();
		result = 37 * result + bitflags;
		return result;
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(Declare.PARENTSMIXIN);
		child.write(s);
		parents.write(s);
		writeLocation(s);
		s.writeInt(bitflags);
	}

	public static Declare read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		DeclareParentsMixin ret = new DeclareParentsMixin(TypePattern.read(s, context), TypePatternList.read(s, context));
		ret.readLocation(context, s);
		ret.bitflags = s.readInt();
		return ret;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("declare parents mixin: ");
		buf.append(child);
		buf.append(" implements ");
		buf.append(parents);
		buf.append(";");
		buf.append("bits=0x").append(Integer.toHexString(bitflags));
		return buf.toString();
	}

	public boolean isMixin() {
		return true;
	}

}
