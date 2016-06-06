/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.aspectj.apache.bcel.classfile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Module.Export;

/**
 * This class is derived from <em>Attribute</em> and represents the module
 * information captured in a class file.
 * http://cr.openjdk.java.net/~mr/jigsaw/spec/lang-vm.html
 * 
 * @author Andy Clement
 */
public final class Module extends Attribute {

	private static final String[] NO_MODULE_NAMES = {};
	
	private byte[] moduleInfo;
	private int ptr;
	private boolean unpacked = false;
	private Require[] requires;
	private Export[] exports;
	private Uses[] uses;
	private Provide[] provides;

	/**
	 * Build a Module attribute from a previously Unknown attribute.
	 */
	public Module(Unknown unknown) {
		super(unknown.getTag(), unknown.getNameIndex(), unknown.getLength(), unknown.getConstantPool());
		moduleInfo = unknown.getBytes();
	}

	public class Require {

		private final int moduleNameIndex;
		private final int requiresFlags;

		public Require(int moduleNameIndex, int requiresFlags) {
			this.moduleNameIndex = moduleNameIndex;
			this.requiresFlags = requiresFlags;
		}
		
		public String getModuleName() {
			return cpool.getConstantUtf8(moduleNameIndex).getStringValue();
		}
		
		public int getRequiresFlags() {
			return requiresFlags;
		}

		public String getRequiresFlagsAsString() {
			StringBuilder s = new StringBuilder();
			if ((requiresFlags & Constants.MODULE_ACC_PUBLIC)!=0) {
				s.append("public ");
			}
			if ((requiresFlags & Constants.MODULE_ACC_SYNTHETIC)!=0) {
				s.append("synthetic ");
			}
			if ((requiresFlags & Constants.MODULE_ACC_MANDATED)!=0) {
				s.append("mandated ");
			}
			return s.toString();
		}
		
		public String toString() {
			return "requires "+getRequiresFlagsAsString()+getModuleName();
		}
		
	}

	
	public class Export {

		private final int exportedPackageNameIndex;
		private final int[] toModuleNameIndices;

		public Export(int exportedPackageNameIndex, int[] toModuleNameIndices) {
			this.exportedPackageNameIndex = exportedPackageNameIndex;
			this.toModuleNameIndices = toModuleNameIndices;
		}

		public String getExportedPackage() {
			return cpool.getConstantUtf8(exportedPackageNameIndex).getStringValue();
		}

		public String[] getToModuleNames() {
			if (toModuleNameIndices==null) {
				return NO_MODULE_NAMES;
			}
			String[] toModuleNames = new String[toModuleNameIndices.length];
			for (int i=0;i<toModuleNameIndices.length;i++) {
				toModuleNames[i] = cpool.getConstantUtf8(toModuleNameIndices[i]).getStringValue();
			}
			return toModuleNames;
		}
		
		public String toString() {
			StringBuilder s =new StringBuilder();
			s.append("exports ").append(getExportedPackage().replace('/', '.'));
			String[] toModules = getToModuleNames();
			if (toModules.length!=0) {
				s.append(" to ");
				for (int i=0;i<toModules.length;i++) {
					if (i>0) {
						s.append(", ");
					}
					s.append(toModules[i]);
				}
			}
			return s.toString().trim();
		}
	}

	public class Provide {
		private final int providedTypeIndex;
		private final int withTypeIndex;

		public Provide(int providedTypeIndex, int withTypeIndex) {
			this.providedTypeIndex = providedTypeIndex;
			this.withTypeIndex = withTypeIndex;
		}
		
		public String getProvidedType() {
			return cpool.getConstantString_CONSTANTClass(providedTypeIndex);
		}
		
		public int getProvidedTypeIndex() {
			return providedTypeIndex;
		}

		public String getWithType() {
			return  cpool.getConstantString_CONSTANTClass(withTypeIndex);
		}

		public int getWithTypeIndex() {
			return withTypeIndex;
		}

		public String toString() {
			StringBuilder s =new StringBuilder();
			s.append("provides ").append(getProvidedType().replace('/', '.'));
			s.append(" with ").append(getWithType().replace('/','.'));
			return s.toString();
		}
	}

	public class Uses {
		private final int typeNameIndex;
		
		public Uses(int typeNameIndex) {
			this.typeNameIndex = typeNameIndex;
		}

		public String getTypeName() {
			return  cpool.getConstantString_CONSTANTClass(typeNameIndex);
		}

		public int getTypeNameIndex() {
			return typeNameIndex;
		}
		
		public String toString() {
			StringBuilder s =new StringBuilder();
			s.append("uses ").append(getTypeName().replace('/', '.'));
			return s.toString().trim();
		}
	}
	
	private final int readInt() {
		return ((moduleInfo[ptr++] & 0xFF) << 24) + ((moduleInfo[ptr++] & 0xFF) << 16)
				+ ((moduleInfo[ptr++] & 0xFF) << 8) + (moduleInfo[ptr++] & 0xFF);
	}

	private final int readUnsignedShort() {
		return ((moduleInfo[ptr++] & 0xff) << 8) + (moduleInfo[ptr++] & 0xff);
	}

	private final int readUnsignedShort(int offset) {
		return ((moduleInfo[offset++] & 0xff) << 8) + (moduleInfo[offset] & 0xff);
	}

	private void ensureUnpacked() {
		if (!unpacked) {
			ptr = 0;
			int count = readUnsignedShort();
			requires = new Require[count];
			for (int i = 0; i < count; i++) {
				requires[i] = new Require(readUnsignedShort(), readUnsignedShort());
			}
			count = readUnsignedShort();
			exports = new Export[count];
			for (int i = 0; i < count; i++) {
				int index = readUnsignedShort();
				int toCount = readUnsignedShort();
				int[] to = new int[toCount];
				for (int j = 0; j < toCount; j++) {
					to[j] = readUnsignedShort();
				}
				exports[i] = new Export(index, to);
			}
			count = readUnsignedShort();
			uses = new Uses[count];
			for (int i = 0; i < count; i++) {
				uses[i] = new Uses(readUnsignedShort());
			}
			count = readUnsignedShort();
			provides = new Provide[count];
			for (int i = 0; i < count; i++) {
				provides[i] = new Provide(readUnsignedShort(), readUnsignedShort());
			}
			unpacked = true;
		}
	}

	@Override
	public final void dump(DataOutputStream file) throws IOException {
		super.dump(file);
		if (!unpacked) {
			file.write(moduleInfo);
		} else {
			file.writeShort(requires.length);
			for (int i = 0; i < requires.length; i++) {
				file.writeShort(requires[i].moduleNameIndex);
				file.writeShort(requires[i].requiresFlags);
			}
			file.writeShort(exports.length);
			for (Export export : exports) {
				file.writeShort(export.exportedPackageNameIndex);
				int[] toIndices = export.toModuleNameIndices;
				file.writeShort(toIndices.length);
				for (int index : toIndices) {
					file.writeShort(index);
				}
			}
			file.writeShort(uses.length);
			for (Uses use : uses) {
				file.writeShort(use.getTypeNameIndex());
			}
			file.writeShort(provides.length);
			for (Provide provide : provides) {
				file.writeShort(provide.providedTypeIndex);
				file.writeShort(provide.withTypeIndex);
			}
		}
	}

	public String toStringRequires() {
		StringBuilder s = new StringBuilder();
		s.append('#').append(requires.length);
		if (requires.length > 0) {
			for (Require require : requires) {
				s.append(' ');
				s.append(require.moduleNameIndex).append(':').append(require.requiresFlags);
			}
		}
		return s.toString();
	}

	public String toStringExports() {
		StringBuilder s = new StringBuilder();
		s.append('#').append(exports.length);
		if (exports.length > 0) {
			for (Export export : exports) {
				s.append(' ');
				s.append(export.exportedPackageNameIndex).append(":[");
				int[] toIndices = export.toModuleNameIndices;
				for (int i = 0; i < toIndices.length; i++) {
					if (i > 0)
						s.append(',');
					s.append(toIndices[i]);
				}
				s.append("]");
			}
		}
		return s.toString();
	}

	public String toStringUses() {
		StringBuilder s = new StringBuilder();
		s.append('#').append(uses.length);
		if (uses.length > 0) {
			for (Uses use : uses) {
				s.append(' ');
				s.append(use.getTypeName());
			}
		}
		return s.toString();
	}

	public String toStringProvides() {
		StringBuilder s = new StringBuilder();
		s.append('#').append(provides.length);
		if (provides.length > 0) {
			for (Provide provide : provides) {
				s.append(' ');
				s.append(provide.providedTypeIndex).append(':').append(provide.withTypeIndex);
			}
		}
		return s.toString();
	}

	@Override
	public final String toString() {
		StringBuilder s = new StringBuilder();
		ensureUnpacked();
		s.append("Module(");
		if (requires.length != 0) {
			s.append("requires=");
			s.append(toStringRequires());
			s.append(" ");
		}
		if (exports.length != 0) {
			s.append("exports=");
			s.append(toStringExports());
			s.append(" ");
		}
		if (uses.length != 0) {
			s.append("uses=");
			s.append(toStringUses());
			s.append(" ");
		}
		if (provides.length != 0) {
			s.append("provides=");
			s.append(toStringProvides());
			s.append(" ");
		}
		return s.toString().trim()+")";
	}

	/**
	 * @return deep copy of this attribute //
	 */
	// @Override
	// public Attribute copy(ConstantPool constant_pool) {
	// return (SourceFile) clone();
	// }
	@Override
	public void accept(ClassVisitor v) {
		v.visitSourceFile(this);
	}
	
	public Require[] getRequires() {
		ensureUnpacked();
		return requires;
	}

	public String[] getRequiredModuleNames() {
		ensureUnpacked();
		String[] results = new String[requires.length];
		for (int i=0;i<requires.length;i++) {
			results[i] = cpool.getConstantUtf8(requires[i].moduleNameIndex).getStringValue();
		}
		return results;
	}
	
	public byte[] getBytes() {
		return moduleInfo;
	}

	public Export[] getExports() {
		ensureUnpacked();
		return exports;
	}

	public Uses[] getUses() {
		ensureUnpacked();
		return uses;
	}

	public Provide[] getProvides() {
		ensureUnpacked();
		return provides;
	}
}
