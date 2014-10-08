/* *******************************************************************
 * Copyright (c) 2002, 2010 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Nieraj Singh
 * ******************************************************************/

package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.Map;

import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.World;

public class BindingTypePattern extends ExactTypePattern implements BindingPattern {
	private int formalIndex;
	private String bindingName;

	public BindingTypePattern(UnresolvedType type, int index, boolean isVarArgs) {
		super(type, false, isVarArgs);
		this.formalIndex = index;
	}

	public BindingTypePattern(FormalBinding binding, boolean isVarArgs) {
		this(binding.getType(), binding.getIndex(), isVarArgs);
		this.bindingName = binding.getName();
	}

	public int getFormalIndex() {
		return formalIndex;
	}
	
	public String getBindingName() {
		return bindingName;
	}

	public boolean equals(Object other) {
		if (!(other instanceof BindingTypePattern)) {
			return false;
		}
		BindingTypePattern o = (BindingTypePattern) other;
		if (includeSubtypes != o.includeSubtypes) {
			return false;
		}
		if (isVarArgs != o.isVarArgs) {
			return false;
		}
		return o.type.equals(this.type) && o.formalIndex == this.formalIndex;
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + super.hashCode();
		result = 37 * result + formalIndex;
		return result;
	}

	public void write(CompressingDataOutputStream out) throws IOException {
		out.writeByte(TypePattern.BINDING);
		type.write(out);
		out.writeShort((short) formalIndex);
		out.writeBoolean(isVarArgs);
		writeLocation(out);
	}

	public static TypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		UnresolvedType type = UnresolvedType.read(s);
		int index = s.readShort();
		boolean isVarargs = false;
		if (s.getMajorVersion() >= AjAttribute.WeaverVersionInfo.WEAVER_VERSION_MAJOR_AJ150) {
			isVarargs = s.readBoolean();
		}
		TypePattern ret = new BindingTypePattern(type, index, isVarargs);
		ret.readLocation(context, s);
		return ret;
	}

	public TypePattern remapAdviceFormals(IntMap bindings) {
		if (!bindings.hasKey(formalIndex)) {
			return new ExactTypePattern(type, false, isVarArgs);
		} else {
			int newFormalIndex = bindings.get(formalIndex);
			return new BindingTypePattern(type, newFormalIndex, isVarArgs);
		}
	}

	public TypePattern parameterizeWith(Map<String,UnresolvedType> typeVariableMap, World w) {
		ExactTypePattern superParameterized = (ExactTypePattern) super.parameterizeWith(typeVariableMap, w);
		BindingTypePattern ret = new BindingTypePattern(superParameterized.getExactType(), this.formalIndex, this.isVarArgs);
		ret.copyLocationFrom(this);
		return ret;
	}

	public String toString() {
		// Thread.currentThread().dumpStack();
		return "BindingTypePattern(" + super.toString() + ", " + formalIndex + ")";
	}
}
