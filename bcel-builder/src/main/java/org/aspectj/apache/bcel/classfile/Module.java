/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2016-17 The Apache Software Foundation.  All rights
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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.Constants;

/**
 * This class is derived from <em>Attribute</em> and represents the module
 * information captured in a class file.
 * http://cr.openjdk.java.net/~mr/jigsaw/spec/lang-vm.html
 * http://cr.openjdk.java.net/~mr/jigsaw/spec/java-se-9-jvms-diffs.pdf 4.7.25
 * 
 * @author Andy Clement
 */
public final class Module extends Attribute {

	private static final String[] NO_MODULE_NAMES = {};
	
	private int moduleNameIndex;    // u2 module_name_index
	private int moduleFlags;        // u2 module_flags
	private int moduleVersionIndex; // u2 module_version_index
	private Require[] requires;
	private Export[] exports;
	private Open[] opens;
	private Uses[] uses;
	private Provide[] provides;

	private byte[] moduleInfo;
	private int ptr;
	private boolean unpacked = false;

	public Module(Module module) {
		super(module.getTag(), module.getNameIndex(), module.getLength(), module.getConstantPool());
		moduleInfo = module.getBytes();
	}
	
	public Module(int nameIndex, int length, byte[] data, ConstantPool cp) {
		super(Constants.ATTR_MODULE, nameIndex, length, cp);
	}
	
	Module(int nameIndex, int length, DataInputStream stream, ConstantPool cp) throws IOException {
		this(nameIndex, length, (byte[])null, cp);
		moduleInfo = new byte[length];
		stream.read(moduleInfo);
		unpacked = false;
	}
	
	public class Require {

		private final int moduleIndex;
		private final int flags;
		private final int versionIndex;

		public Require(int moduleIndex, int flags, int versionIndex) {
			this.moduleIndex = moduleIndex;
			this.flags = flags;
			this.versionIndex = versionIndex;
		}
		
		public String getModuleName() {
			return cpool.getModuleName(moduleIndex);
		}
		
		public int getFlags() {
			return flags;
		}
		
		public int getVersionIndex() {
			return versionIndex;
		}
		
		public String getVersionString() {
			if (versionIndex == 0) {
				return null;
			} else {
				return cpool.getConstantUtf8(versionIndex).getValue();
			}
		}

		public String getFlagsAsString() {
			StringBuilder s = new StringBuilder();
			if ((flags & Constants.MODULE_ACC_TRANSITIVE)!=0) {
				s.append(" transitive");
			}
			if ((flags & Constants.MODULE_ACC_STATIC_PHASE)!=0) {
				s.append(" static");
			}
			if ((flags & Constants.MODULE_ACC_SYNTHETIC)!=0) {
				s.append(" synthetic");
			}
			if ((flags & Constants.MODULE_ACC_MANDATED)!=0) {
				s.append(" mandated");
			}
			return s.toString();
		}
		
		public String toString() {
			return "requires"+getFlagsAsString()+" "+getModuleName()+(versionIndex==0?"":" "+getVersionString());
		}
	}

	
	public class Export {

		private final int packageIndex;
		private final int flags;
		private final int[] toModuleIndices;

		public Export(int packageIndex, int flags, int[] toModuleIndices) {
			this.packageIndex = packageIndex;
			this.flags = flags;
			this.toModuleIndices = toModuleIndices;
		}

		public int getPackageIndex() {
			return packageIndex;
		}
		
		public int getFlags() {
			return flags;
		}
		
		public int[] getToModuleIndices() {
			return toModuleIndices;
		}
		
		public String getPackage() {
			return cpool.getPackageName(packageIndex);
		}		
		
		public String getFlagsAsString() {
			StringBuilder s = new StringBuilder();
			if ((flags & Constants.MODULE_ACC_SYNTHETIC)!=0) {
				s.append(" synthetic");
			}
			if ((flags & Constants.MODULE_ACC_MANDATED)!=0) {
				s.append(" synthetic");
			}
			return s.toString();
		}

		public String[] getToModuleNames() {
			if (toModuleIndices==null) {
				return NO_MODULE_NAMES;
			}
			String[] toModuleNames = new String[toModuleIndices.length];
			for (int i=0;i<toModuleIndices.length;i++) {
				toModuleNames[i] = cpool.getModuleName(toModuleIndices[i]);
			}
			return toModuleNames;
		}
		
		public String toString() {
			StringBuilder s =new StringBuilder();
			s.append("exports").append(getFlagsAsString()).append(" ").append(getPackage().replace('/', '.'));
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
	

	public class Open {

		private final int packageIndex;
		private final int flags;
		private final int[] toModuleIndices;

		public Open(int packageIndex, int flags, int[] toModuleIndices) {
			this.packageIndex = packageIndex;
			this.flags = flags;
			this.toModuleIndices = toModuleIndices;
		}

		public int getPackageIndex() {
			return packageIndex;
		}
		
		public int getFlags() {
			return flags;
		}
		
		public int[] getToModuleIndices() {
			return toModuleIndices;
		}
		
		public String getPackage() {
			return cpool.getPackageName(packageIndex);
		}		
		
		public String getFlagsAsString() {
			StringBuilder s = new StringBuilder();
			if ((flags & Constants.MODULE_ACC_SYNTHETIC)!=0) {
				s.append(" synthetic");
			}
			if ((flags & Constants.MODULE_ACC_MANDATED)!=0) {
				s.append(" synthetic");
			}
			return s.toString();
		}

		public String[] getToModuleNames() {
			if (toModuleIndices==null) {
				return NO_MODULE_NAMES;
			}
			String[] toModuleNames = new String[toModuleIndices.length];
			for (int i=0;i<toModuleIndices.length;i++) {
				toModuleNames[i] = cpool.getModuleName(toModuleIndices[i]);
			}
			return toModuleNames;
		}
		
		public String toString() {
			StringBuilder s =new StringBuilder();
			s.append("opens").append(getFlagsAsString()).append(" ").append(getPackage().replace('/', '.'));
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
		private final int[] withTypeIndices;

		public Provide(int providedTypeIndex, int[] withTypeIndices) {
			this.providedTypeIndex = providedTypeIndex;
			this.withTypeIndices = withTypeIndices;
		}
		
		public String getProvidedType() {
			return cpool.getConstantString_CONSTANTClass(providedTypeIndex);
		}
		
		public int getProvidedTypeIndex() {
			return providedTypeIndex;
		}

		public String[] getWithTypeStrings() {
			String[] result = new String[withTypeIndices.length];
			for (int i=0;i<withTypeIndices.length;i++) {
				result[i] = cpool.getConstantString_CONSTANTClass(withTypeIndices[i]);
			}
			return result;
		}

		public int[] getWithTypeIndices() {
			return withTypeIndices;
		}

		public String toString() {
			StringBuilder s =new StringBuilder();
			s.append("provides ").append(getProvidedType().replace('/', '.'));
			s.append(" with ");
			String[] withtypes = getWithTypeStrings();
			for (int i=0;i< withtypes.length;i++) {
				if (i>0) s.append(",");
				s.append(withtypes[i].replace('/','.'));
			}
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

	// Format: http://cr.openjdk.java.net/~mr/jigsaw/spec/java-se-9-jvms-diffs.pdf 4.7.25
	private void ensureUnpacked() {
		if (!unpacked) {
			ptr = 0;
			moduleNameIndex = readUnsignedShort();
			moduleFlags = readUnsignedShort();
			moduleVersionIndex = readUnsignedShort();
			
			int count = readUnsignedShort();
			requires = new Require[count];
			for (int i = 0; i < count; i++) {
				requires[i] = new Require(readUnsignedShort(), readUnsignedShort(), readUnsignedShort());
			}
			
			count = readUnsignedShort();
			exports = new Export[count];
			for (int i = 0; i < count; i++) {
				int index = readUnsignedShort();
				int flags = readUnsignedShort();
				int toCount = readUnsignedShort();
				int[] to = new int[toCount];
				for (int j = 0; j < toCount; j++) {
					to[j] = readUnsignedShort();
				}
				exports[i] = new Export(index, flags, to);
			}
			
			count = readUnsignedShort();
			opens = new Open[count];
			for (int i = 0; i < count; i++) {
				int index = readUnsignedShort();
				int flags = readUnsignedShort();
				int toCount = readUnsignedShort();
				int[] to = new int[toCount];
				for (int j = 0; j < toCount; j++) {
					to[j] = readUnsignedShort();
				}
				opens[i] = new Open(index, flags, to);
			}
			count = readUnsignedShort();
			uses = new Uses[count];
			for (int i = 0; i < count; i++) {
				uses[i] = new Uses(readUnsignedShort());
			}
			count = readUnsignedShort();
			provides = new Provide[count];
			for (int i = 0; i < count; i++) {
				int index = readUnsignedShort();
				int toCount = readUnsignedShort();
				int[] to = new int[toCount];
				for (int j = 0; j < toCount; j++) {
					to[j] = readUnsignedShort();
				}
				provides[i] = new Provide(index, to);
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

			file.writeShort(moduleNameIndex);
			file.writeShort(moduleFlags);
			file.writeShort(moduleVersionIndex);
			
			file.writeShort(requires.length);
			for (Require require : requires) {
				file.writeShort(require.moduleIndex);
				file.writeShort(require.flags);
				file.writeShort(require.versionIndex);
			}
			file.writeShort(exports.length);
			for (Export export : exports) {
				file.writeShort(export.packageIndex);
				int[] toIndices = export.toModuleIndices;
				file.writeShort(toIndices.length);
				for (int index : toIndices) {
					file.writeShort(index);
				}
			}
			file.writeShort(opens.length);
			for (Open open : opens) {
				file.writeShort(open.packageIndex);
				int[] toIndices = open.toModuleIndices;
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
				int[] toIndices = provide.withTypeIndices;
				file.writeShort(toIndices.length);
				for (int index : toIndices) {
					file.writeShort(index);
				}
			}
		}
	}

	public String toStringRequires() {
		StringBuilder s = new StringBuilder();
		s.append('#').append(requires.length);
		if (requires.length > 0) {
			for (Require require : requires) {
				s.append(' ');
				s.append(require.moduleIndex).append(':').append(require.flags);
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
				s.append(export.packageIndex).append(":[");
				int[] toIndices = export.toModuleIndices;
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
	
	public String toStringOpens() {
		StringBuilder s = new StringBuilder();
		s.append('#').append(opens.length);
		if (opens.length > 0) {
			for (Open open : opens) {
				s.append(' ');
				s.append(open.packageIndex).append(":[");
				int[] toIndices = open.toModuleIndices;
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
				s.append(provide.providedTypeIndex).append(":[");
				int[] indices = provide.withTypeIndices;
				for (int i = 0; i < indices.length; i++) {
					if (i > 0)
						s.append(',');
					s.append(indices[i]);
				}
				s.append("]");
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
		if (opens.length != 0) {
			s.append("opens=");
			s.append(toStringOpens());
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
//	 @Override
//	 public Attribute copy(ConstantPool constant_pool) {
//		 return (Module) clone();
//	 }
	
	@Override
	public void accept(ClassVisitor v) {
		v.visitModule(this);
	}
	
	public Require[] getRequires() {
		ensureUnpacked();
		return requires;
	}

	public String[] getRequiredModuleNames() {
		ensureUnpacked();
		String[] results = new String[requires.length];
		for (int i=0;i<requires.length;i++) {
			results[i] = cpool.getModuleName(requires[i].moduleIndex);
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
	
	public Open[] getOpens() {
		ensureUnpacked();
		return opens;
	}

	public Uses[] getUses() {
		ensureUnpacked();
		return uses;
	}

	public Provide[] getProvides() {
		ensureUnpacked();
		return provides;
	}
	
	public String getModuleName() {
		return ((ConstantModule)cpool.getConstant(moduleNameIndex)).getModuleName(cpool);
	}
	
	public int getModuleFlags() {
		// 0x0020 (ACC_OPEN) - Indicates that this module is open.
		// 0x1000 (ACC_SYNTHETIC) - Indicates that this module was not explicitly or implicitly declared.
		// 0x8000 (ACC_MANDATED) - Indicates that this module was implicitly declared
		return moduleFlags;
	}
	
	/** @return the module version or null if no version information specified */
	public String getModuleVersion() {
		if (moduleVersionIndex == 0) {
			return null;
		} else {
			return cpool.getConstantUtf8(moduleVersionIndex).getValue();
		}
	}
}
