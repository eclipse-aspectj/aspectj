/* *******************************************************************
 * Copyright (c) 2013 VMware
 * 
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *    Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.apache.bcel.classfile.annotation;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.classfile.ConstantPool;

public class TypeAnnotationGen {
	public static final TypeAnnotationGen[] NO_TYPE_ANNOTATIONS = new TypeAnnotationGen[0];
	public static final int[] NO_TYPE_PATH = new int[0];
	
	private ConstantPool cpool;

	private TypeAnnotationGen(ConstantPool cpool) {
		this.cpool = cpool;
	}

	private int targetType;
	private int[] typePath;
	private AnnotationGen annotation;
	private int info; // meaning varies depending on target type
	private int info2; // meaning varies depending on target type
	private int[] localVarTarget;
	
	// target type constants
	public final static int CLASS_TYPE_PARAMETER = 0x00;
	public final static int METHOD_TYPE_PARAMETER = 0x01;
	
	public final static int CLASS_EXTENDS = 0x10;
	public final static int CLASS_TYPE_PARAMETER_BOUND = 0x11;
	public final static int METHOD_TYPE_PARAMETER_BOUND = 0x12;
	public final static int FIELD = 0x13;
	public final static int METHOD_RETURN = 0x14;	
	public final static int METHOD_RECEIVER = 0x15;
	public final static int METHOD_FORMAL_PARAMETER = 0x16;
	public final static int THROWS = 0x17;
	
	public final static int LOCAL_VARIABLE = 0x40;
	public final static int RESOURCE_VARIABLE = 0x41;
	public final static int EXCEPTION_PARAMETER = 0x42;
	public final static int INSTANCEOF = 0x43;
	public final static int NEW = 0x44;
	public final static int CONSTRUCTOR_REFERENCE = 0x45;
	public final static int METHOD_REFERENCE = 0x46;
	public final static int CAST = 0x47;
	public final static int CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT = 0x48;
	public final static int METHOD_INVOCATION_TYPE_ARGUMENT = 0x49;
	public final static int CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT = 0x4A;
	public final static int METHOD_REFERENCE_TYPE_ARGUMENT = 0x4B;
	
	// type path entry kinds
	public final static int TYPE_PATH_ENTRY_KIND_ARRAY = 0;
	public final static int TYPE_PATH_ENTRY_KIND_INNER_TYPE = 1;
	public final static int TYPE_PATH_ENTRY_KIND_WILDCARD = 2;
	public final static int TYPE_PATH_ENTRY_KIND_TYPE_ARGUMENT = 3;

	
	public static TypeAnnotationGen read(DataInputStream dis, ConstantPool cpool, boolean isVisible) throws IOException {
		TypeAnnotationGen typeAnno = new TypeAnnotationGen(cpool);
		typeAnno.targetType = dis.readUnsignedByte();
		// read target_info
		switch (typeAnno.targetType) {
			case CLASS_TYPE_PARAMETER:
				typeAnno.info = dis.readUnsignedByte();// type_parameter_index
				break;
			case METHOD_TYPE_PARAMETER:
				typeAnno.info = dis.readUnsignedByte(); // type_parameter_index
				break;
			case CLASS_EXTENDS:
				int superTypeIndex = dis.readUnsignedShort();
				if (superTypeIndex == 65535) {
					typeAnno.info = -1;
				} else {
					typeAnno.info = superTypeIndex;
				}
				break;
			case CLASS_TYPE_PARAMETER_BOUND:
			case METHOD_TYPE_PARAMETER_BOUND:
				typeAnno.info = dis.readUnsignedByte(); // type_parameter_index
				typeAnno.info2 = dis.readUnsignedByte(); // bound_index;
				break;
			case FIELD:
			case METHOD_RETURN:
			case METHOD_RECEIVER:
				break;
			case METHOD_FORMAL_PARAMETER:
				typeAnno.info = dis.readUnsignedByte(); // method_formal_parameter_index
				break;
			case THROWS:
				typeAnno.info = dis.readUnsignedShort(); // throws_type_index
				break;
			case LOCAL_VARIABLE:
			case RESOURCE_VARIABLE:
				typeAnno.localVarTarget = readLocalVarTarget(dis);
				break;
			case EXCEPTION_PARAMETER:
				// TODO should be a SHORT according to the spec but byte for now because of javac (b90)
				typeAnno.info = dis.readUnsignedByte(); // exception_table_index
				break;
			case INSTANCEOF:
			case NEW:
			case CONSTRUCTOR_REFERENCE:
			case METHOD_REFERENCE:
				typeAnno.info = dis.readUnsignedShort(); // offset
				break;
			case CAST:
			case CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT:
			case METHOD_INVOCATION_TYPE_ARGUMENT:
			case CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT:
			case METHOD_REFERENCE_TYPE_ARGUMENT:
				typeAnno.info = dis.readUnsignedShort(); // offset
				typeAnno.info2 = dis.readUnsignedByte(); // type_argument_index
				break;
			default:
				throw new IllegalStateException("nyi "+typeAnno.targetType);
		}
		int typepathlength = dis.readUnsignedByte();
		if (typepathlength==0) {
			typeAnno.typePath = NO_TYPE_PATH;
		} else {
			typeAnno.typePath = new int[typepathlength*2];
			for (int i=0, max = typepathlength*2; i<max; i++) {
				typeAnno.typePath[i] = dis.readUnsignedByte();
			}
		}
		typeAnno.annotation = AnnotationGen.read(dis, cpool, isVisible);
		return typeAnno;
	}
	
	public static int[] readLocalVarTarget(DataInputStream dis) throws IOException {
		int tableLength = dis.readUnsignedShort();
		int[] table = new int[tableLength*3];
		int count = 0;
		for (int i=0;i<tableLength;i++) {
			table[count++]=dis.readUnsignedShort(); // start_pc
			table[count++]=dis.readUnsignedShort(); // length
			table[count++]=dis.readUnsignedShort(); // index
		}
		return table;
	}

	public void dump(DataOutputStream dos) throws IOException {
		dos.writeByte(targetType);
		switch (targetType) {
		case CLASS_TYPE_PARAMETER:
			dos.writeByte(this.info); // type_parameter_index
			break;
		case METHOD_TYPE_PARAMETER:
			dos.writeByte(info); // type_parameter_index
			break;
		case CLASS_EXTENDS:
			dos.writeShort(info); // supertype_index
			break;
		case CLASS_TYPE_PARAMETER_BOUND:
		case METHOD_TYPE_PARAMETER_BOUND:
			dos.writeByte(info); // type_parameter_index
			dos.writeByte(info2); // bound_index;
			break;
		case FIELD:
		case METHOD_RETURN:
		case METHOD_RECEIVER:
			break;
		case METHOD_FORMAL_PARAMETER:
			dos.writeByte(info); // method_formal_parameter_index
			break;
		case THROWS:
			dos.writeShort(info); // throws_type_index
			break;
		case LOCAL_VARIABLE:
		case RESOURCE_VARIABLE:
			dos.writeShort(localVarTarget.length/3);
			for (int j : localVarTarget) {
				dos.writeShort(j);
			}
			break;
		case EXCEPTION_PARAMETER:
			// TODO should be a SHORT according to the spec but byte for now because of javac (b90)
			dos.writeByte(info); // exception_table_index
			break;
		case INSTANCEOF:
		case NEW:
		case CONSTRUCTOR_REFERENCE:
		case METHOD_REFERENCE:
			dos.writeShort(info); // offset
			break;
		case CAST:
		case CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT:
		case METHOD_INVOCATION_TYPE_ARGUMENT:
		case CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT:
		case METHOD_REFERENCE_TYPE_ARGUMENT:
			dos.writeShort(info); // offset
			dos.writeByte(info); // type_argument_index
			break;
		default:
			throw new IllegalStateException("nyi "+targetType);
		}
		dos.writeByte(typePath.length);
		for (int j : typePath) {
			dos.writeByte(j);
		}
		annotation.dump(dos);
	}
	
	public int getSupertypeIndex() {
		assert (targetType==CLASS_EXTENDS);
		return info;
	}
	
	public int getOffset() {
		assert (targetType==INSTANCEOF || targetType==NEW || targetType==CONSTRUCTOR_REFERENCE || targetType==METHOD_REFERENCE ||
				targetType==CAST || targetType==CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT || 
				targetType==METHOD_INVOCATION_TYPE_ARGUMENT || targetType==CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT || 
				targetType==METHOD_REFERENCE_TYPE_ARGUMENT);
		return info;
	}
	
	public int getTypeParameterIndex() {
		assert (targetType==CLASS_TYPE_PARAMETER || targetType==METHOD_TYPE_PARAMETER || 
				targetType==CLASS_TYPE_PARAMETER_BOUND || targetType==METHOD_TYPE_PARAMETER_BOUND);
		return info;
	}
	
	public int getTypeArgumentIndex() {
		assert (targetType==CAST || targetType==CONSTRUCTOR_INVOCATION_TYPE_ARGUMENT || 
				targetType==METHOD_INVOCATION_TYPE_ARGUMENT || targetType==CONSTRUCTOR_REFERENCE_TYPE_ARGUMENT || targetType==METHOD_REFERENCE_TYPE_ARGUMENT);
		return info2;
	}
	
	public int getBoundIndex() {
		assert (targetType==CLASS_TYPE_PARAMETER_BOUND || targetType==METHOD_TYPE_PARAMETER_BOUND);
		return info2;
	}

	public int getMethodFormalParameterIndex() {
		assert (targetType==METHOD_FORMAL_PARAMETER);
		return info;
	}

	public int getThrowsTypeIndex() {
		assert (targetType==THROWS);
		return info;
	}

	public int[] getLocalVarTarget() {
		assert (targetType==LOCAL_VARIABLE||targetType==RESOURCE_VARIABLE);
		return localVarTarget;
	}

	public int getExceptionTableIndex() {
		assert (targetType==EXCEPTION_PARAMETER);
		return info;
	}


	
	public int getTargetType() {
		return targetType;
	}
	
	public AnnotationGen getAnnotation() {
		return annotation;
	}

//	@Override
//	public String toString() {
//		StringBuffer s = new StringBuffer();
//		s.append("AnnotationGen:[" + getTypeName() + " #" + pairs.size() + " {");
//		for (int i = 0; i < pairs.size(); i++) {
//			s.append(pairs.get(i));
//			if (i + 1 < pairs.size())
//				s.append(",");
//		}
//		s.append("}]");
//		return s.toString();
//	}
//
//	public String toShortString() {
//		StringBuffer s = new StringBuffer();
//		s.append("@" + getTypeName() + "(");
//		for (int i = 0; i < pairs.size(); i++) {
//			s.append(pairs.get(i));
//			if (i + 1 < pairs.size())
//				s.append(",");
//		}
//		s.append(")");
//		return s.toString();
//	}
//
//	private void isRuntimeVisible(boolean b) {
//		isRuntimeVisible = b;
//	}
//
//	public boolean isRuntimeVisible() {
//		return isRuntimeVisible;
//	}
//
//	/**
//	 * @return true if the annotation has a value with the specified name and (toString'd) value
//	 */
//	public boolean hasNameValuePair(String name, String value) {
//		for (NameValuePair pair : pairs) {
//			if (pair.getNameString().equals(name)) {
//				if (pair.getValue().stringifyValue().equals(value)) {
//					return true;
//				}
//			}
//		}
//		return false;
//	}
//
//	/**
//	 * @return true if the annotation has a value with the specified name
//	 */
//	public boolean hasNamedValue(String name) {
//		for (NameValuePair pair : pairs) {
//			if (pair.getNameString().equals(name)) {
//				return true;
//			}
//		}
//		return false;
//	}
	
//	public TypeAnnotationGen(TypeAnnotationGen a, ConstantPool cpool, boolean copyPoolEntries) {
//		this.cpool = cpool;
//		if (copyPoolEntries) {
//			typeIndex = cpool.addUtf8(a.getTypeSignature());
//		} else {
//			typeIndex = a.getTypeIndex();
//		}
//		isRuntimeVisible = a.isRuntimeVisible();
//		pairs = copyValues(a.getValues(), cpool, copyPoolEntries);
//	}
//
//	private List<NameValuePair> copyValues(List<NameValuePair> in, ConstantPool cpool, boolean copyPoolEntries) {
//		List<NameValuePair> out = new ArrayList<NameValuePair>();
//		for (NameValuePair nvp : in) {
//			out.add(new NameValuePair(nvp, cpool, copyPoolEntries));
//		}
//		return out;
//	}
//
//
//	/**
//	 * Retrieve an immutable version of this AnnotationGen
//	 */
//	public TypeAnnotationGen(ObjectType type, List<NameValuePair> pairs, boolean runtimeVisible, ConstantPool cpool) {
//		this.cpool = cpool;
//		if (type != null) {
//			this.typeIndex = cpool.addUtf8(type.getSignature()); // Only null for funky *temporary* FakeAnnotation objects
//		}
//		this.pairs = pairs;
//		isRuntimeVisible = runtimeVisible;
//	}
//

	public int[] getTypePath() {
		return typePath;
	}
	
	public String getTypePathString() {
		return toTypePathString(typePath);
	}
	
	public static String toTypePathString(int[] typepath) {
		StringBuilder sb = new StringBuilder();
		int count = 0;
		sb.append("[");
		while (count < typepath.length) {
			if (count>0) sb.append(",");
			switch (typepath[count++]) {
			case TYPE_PATH_ENTRY_KIND_ARRAY:
				sb.append("ARRAY");
				count++;
				break;
			case TYPE_PATH_ENTRY_KIND_INNER_TYPE:
				sb.append("INNER_TYPE");
				count++;
				break;
			case TYPE_PATH_ENTRY_KIND_WILDCARD:
				sb.append("WILDCARD");
				count++;
				break;
			case TYPE_PATH_ENTRY_KIND_TYPE_ARGUMENT:
				sb.append("TYPE_ARGUMENT(").append(typepath[count++]).append(")");
				break;				
			}
		}
		sb.append("]");
		return sb.toString();
	}

}
