package org.aspectj.apache.bcel.classfile;

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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.StringTokenizer;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnos;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.util.SyntheticRepository;

/**
 * Represents a Java class, i.e., the data structures, constant pool, fields, methods and commands contained in a Java .class file.
 * See <a href="ftp://java.sun.com/docs/specs/">JVM specification</a> for details.
 * 
 * The intent of this class is to represent a parsed or otherwise existing class file. Those interested in programatically
 * generating classes should see the <a href="../generic/ClassGen.html">ClassGen</a> class.
 * 
 * @version $Id: JavaClass.java,v 1.22 2009/09/15 19:40:14 aclement Exp $
 * @see org.aspectj.apache.bcel.generic.ClassGen
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public class JavaClass extends Modifiers implements Cloneable, Node {

	private static final String[] NoInterfaceNames = new String[0];
	private static final Field[] NoFields = new Field[0];
	private static final Method[] NoMethod = new Method[0];
	private static final int[] NoInterfaceIndices = new int[0];
	private static final Attribute[] NoAttributes = new Attribute[0];

	private String fileName;
	private String packageName;
	private String sourcefileName;
	private int classnameIdx;
	private int superclassnameIdx;
	private String classname;
	private String superclassname;
	private int major, minor;
	private ConstantPool cpool;
	private int[] interfaces;
	private String[] interfacenames;
	private Field[] fields;
	private Method[] methods;
	private Attribute[] attributes;
	private AnnotationGen[] annotations;

	private boolean isGeneric = false;
	private boolean isAnonymous = false;
	private boolean isNested = false;
	private boolean computedNestedTypeStatus = false;

	// Annotations are collected from certain attributes, don't do it more than necessary!
	private boolean annotationsOutOfDate = true;

	// state for dealing with generic signature string
	private String signatureAttributeString = null;
	private Signature signatureAttribute = null;
	private boolean searchedForSignatureAttribute = false;

	/**
	 * In cases where we go ahead and create something, use the default SyntheticRepository, because we don't know any better.
	 */
	private transient org.aspectj.apache.bcel.util.Repository repository = null;

	public JavaClass(int classnameIndex, int superclassnameIndex, String filename, int major, int minor, int access_flags,
			ConstantPool cpool, int[] interfaces, Field[] fields, Method[] methods, Attribute[] attributes) {
		if (interfaces == null) {
			interfaces = NoInterfaceIndices;
		}

		this.classnameIdx = classnameIndex;
		this.superclassnameIdx = superclassnameIndex;
		this.fileName = filename;
		this.major = major;
		this.minor = minor;
		this.modifiers = access_flags;
		this.cpool = cpool;
		this.interfaces = interfaces;
		this.fields = (fields == null ? NoFields : fields);
		this.methods = (methods == null ? NoMethod : methods);
		this.attributes = (attributes == null ? NoAttributes : attributes);
		annotationsOutOfDate = true;

		// Get source file name if available
		SourceFile sfAttribute = AttributeUtils.getSourceFileAttribute(attributes);
		sourcefileName = sfAttribute == null ? "<Unknown>" : sfAttribute.getSourceFileName();

		/*
		 * According to the specification the following entries must be of type `ConstantClass' but we check that anyway via the
		 * `ConstPool.getConstant' method.
		 */
		classname = cpool.getConstantString(classnameIndex, Constants.CONSTANT_Class);
		classname = Utility.compactClassName(classname, false);

		int index = classname.lastIndexOf('.');
		if (index < 0) {
			packageName = "";
		} else {
			packageName = classname.substring(0, index);
		}

		if (superclassnameIndex > 0) { // May be zero -> class is java.lang.Object
			superclassname = cpool.getConstantString(superclassnameIndex, Constants.CONSTANT_Class);
			superclassname = Utility.compactClassName(superclassname, false);
		} else {
			superclassname = "java.lang.Object";
		}

		if (interfaces.length == 0) {
			interfacenames = NoInterfaceNames;
		} else {
			interfacenames = new String[interfaces.length];
			for (int i = 0; i < interfaces.length; i++) {
				String str = cpool.getConstantString(interfaces[i], Constants.CONSTANT_Class);
				interfacenames[i] = Utility.compactClassName(str, false);
			}
		}
	}

	/**
	 * Called by objects that are traversing the nodes of the tree implicitely defined by the contents of a Java class. I.e., the
	 * hierarchy of methods, fields, attributes, etc. spawns a tree of objects.
	 * 
	 * @param v Visitor object
	 */
	public void accept(ClassVisitor v) {
		v.visitJavaClass(this);
	}

	/**
	 * Dump class to a file.
	 * 
	 * @param file Output file
	 * @throws IOException
	 */
	public void dump(File file) throws IOException {
		String parent = file.getParent();
		if (parent != null) {
			File dir = new File(parent);
			dir.mkdirs();
		}
		dump(new DataOutputStream(new FileOutputStream(file)));
	}

	/**
	 * Dump class to a file named file_name.
	 * 
	 * @param file_name Output file name
	 * @exception IOException
	 */
	public void dump(String file_name) throws IOException {
		dump(new File(file_name));
	}

	/**
	 * @return class in binary format
	 */
	public byte[] getBytes() {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		DataOutputStream ds = new DataOutputStream(s);

		try {
			dump(ds);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				ds.close();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		}

		return s.toByteArray();
	}

	/**
	 * Dump Java class to output stream in binary format.
	 */
	public void dump(OutputStream file) throws IOException {
		dump(new DataOutputStream(file));
	}

	/**
	 * Dump Java class to output stream in binary format.
	 */
	public void dump(DataOutputStream file) throws IOException {
		file.writeInt(0xcafebabe);
		file.writeShort(minor);
		file.writeShort(major);

		cpool.dump(file);

		file.writeShort(modifiers);
		file.writeShort(classnameIdx);
		file.writeShort(superclassnameIdx);

		file.writeShort(interfaces.length);
		for (int anInterface : interfaces) {
			file.writeShort(anInterface);
		}

		file.writeShort(fields.length);
		for (Field field : fields) {
			field.dump(file);
		}

		file.writeShort(methods.length);
		for (Method method : methods) {
			method.dump(file);
		}

		AttributeUtils.writeAttributes(attributes, file);

		file.close();
	}

	public Attribute[] getAttributes() {
		return attributes;
	}

	public AnnotationGen[] getAnnotations() {
		if (annotationsOutOfDate) {
			// Find attributes that contain annotation data
			List<AnnotationGen> accumulatedAnnotations = new ArrayList<>();
			for (Attribute attribute : attributes) {
				if (attribute instanceof RuntimeAnnos) {
					RuntimeAnnos runtimeAnnotations = (RuntimeAnnos) attribute;
					accumulatedAnnotations.addAll(runtimeAnnotations.getAnnotations());
				}
			}
			annotations = accumulatedAnnotations.toArray(new AnnotationGen[] {});
			annotationsOutOfDate = false;
		}
		return annotations;
	}

	/**
	 * @return Class name.
	 */
	public String getClassName() {
		return classname;
	}

	/**
	 * @return Package name.
	 */
	public String getPackageName() {
		return packageName;
	}

	public int getClassNameIndex() {
		return classnameIdx;
	}

	public ConstantPool getConstantPool() {
		return cpool;
	}

	/**
	 * @return Fields, i.e., variables of the class. Like the JVM spec mandates for the classfile format, these fields are those
	 *         specific to this class, and not those of the superclass or superinterfaces.
	 */
	public Field[] getFields() {
		return fields;
	}

	/**
	 * @return File name of class, aka SourceFile attribute value
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return Names of implemented interfaces.
	 */
	public String[] getInterfaceNames() {
		return interfacenames;
	}

	/**
	 * @return Indices in constant pool of implemented interfaces.
	 */
	public int[] getInterfaceIndices() {
		return interfaces;
	}

	public int getMajor() {
		return major;
	}

	/**
	 * @return Methods of the class.
	 */
	public Method[] getMethods() {
		return methods;
	}

	/**
	 * @return A org.aspectj.apache.bcel.classfile.Method corresponding to java.lang.reflect.Method if any
	 */
	public Method getMethod(java.lang.reflect.Method m) {
		for (Method method : methods) {
			if (m.getName().equals(method.getName()) && m.getModifiers() == method.getModifiers()
					&& Type.getSignature(m).equals(method.getSignature())) {
				return method;
			}
		}

		return null;
	}

	public Method getMethod(java.lang.reflect.Constructor<?> c) {
		for (Method method : methods) {
			if (method.getName().equals("<init>") && c.getModifiers() == method.getModifiers()
					&& Type.getSignature(c).equals(method.getSignature())) {
				return method;
			}
		}

		return null;
	}

	public Field getField(java.lang.reflect.Field field) {
		String fieldName = field.getName();
		for (Field f : fields) {
			if (f.getName().equals(fieldName)) {
				return f;
			}
		}
		return null;
	}

	/**
	 * @return Minor number of class file version.
	 */
	public int getMinor() {
		return minor;
	}

	/**
	 * @return sbsolute path to file where this class was read from
	 */
	public String getSourceFileName() {
		return sourcefileName;
	}

	/**
	 * @return Superclass name.
	 */
	public String getSuperclassName() {
		return superclassname;
	}

	/**
	 * @return Class name index.
	 */
	public int getSuperclassNameIndex() {
		return superclassnameIdx;
	}

	/**
	 * @param attributes .
	 */
	public void setAttributes(Attribute[] attributes) {
		this.attributes = attributes;
		annotationsOutOfDate = true;
	}

	/**
	 * @param class_name .
	 */
	public void setClassName(String class_name) {
		this.classname = class_name;
	}

	/**
	 * @param class_name_index .
	 */
	public void setClassNameIndex(int class_name_index) {
		this.classnameIdx = class_name_index;
	}

	/**
	 * @param constant_pool .
	 */
	public void setConstantPool(ConstantPool constant_pool) {
		this.cpool = constant_pool;
	}

	/**
	 * @param fields .
	 */
	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	/**
	 * Set File name of class, aka SourceFile attribute value
	 */
	public void setFileName(String file_name) {
		this.fileName = file_name;
	}

	/**
	 * @param interface_names .
	 */
	public void setInterfaceNames(String[] interface_names) {
		this.interfacenames = interface_names;
	}

	/**
	 * @param interfaces .
	 */
	public void setInterfaces(int[] interfaces) {
		this.interfaces = interfaces;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public void setMethods(Method[] methods) {
		this.methods = methods;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	/**
	 * Set absolute path to file this class was read from.
	 */
	public void setSourceFileName(String source_file_name) {
		this.sourcefileName = source_file_name;
	}

	/**
	 * @param superclass_name .
	 */
	public void setSuperclassName(String superclass_name) {
		this.superclassname = superclass_name;
	}

	/**
	 * @param superclass_name_index .
	 */
	public void setSuperclassNameIndex(int superclass_name_index) {
		this.superclassnameIdx = superclass_name_index;
	}

	/**
	 * @return String representing class contents.
	 */
	@Override
	public String toString() {
		String access = Utility.accessToString(modifiers, true);
		access = access.equals("") ? "" : access + " ";

		StringBuffer buf = new StringBuffer(access + Utility.classOrInterface(modifiers) + " " + classname + " extends "
				+ Utility.compactClassName(superclassname, false) + '\n');
		int size = interfaces.length;

		if (size > 0) {
			buf.append("implements\t\t");

			for (int i = 0; i < size; i++) {
				buf.append(interfacenames[i]);
				if (i < size - 1) {
					buf.append(", ");
				}
			}

			buf.append('\n');
		}

		buf.append("filename\t\t" + fileName + '\n');
		buf.append("compiled from\t\t" + sourcefileName + '\n');
		buf.append("compiler version\t" + major + "." + minor + '\n');
		buf.append("access flags\t\t" + modifiers + '\n');
		buf.append("constant pool\t\t" + cpool.getLength() + " entries\n");
		buf.append("ACC_SUPER flag\t\t" + isSuper() + "\n");

		if (attributes.length > 0) {
			buf.append("\nAttribute(s):\n");
			for (Attribute attribute : attributes) {
				buf.append(indent(attribute));
			}
		}

		if (annotations != null && annotations.length > 0) {
			buf.append("\nAnnotation(s):\n");
			for (AnnotationGen annotation : annotations) {
				buf.append(indent(annotation));
			}
		}

		if (fields.length > 0) {
			buf.append("\n" + fields.length + " fields:\n");
			for (Field field : fields) {
				buf.append("\t" + field + '\n');
			}
		}

		if (methods.length > 0) {
			buf.append("\n" + methods.length + " methods:\n");
			for (Method method : methods) {
				buf.append("\t" + method + '\n');
			}
		}

		return buf.toString();
	}

	private static final String indent(Object obj) {
		StringTokenizer tok = new StringTokenizer(obj.toString(), "\n");
		StringBuffer buf = new StringBuffer();

		while (tok.hasMoreTokens()) {
			buf.append("\t" + tok.nextToken() + "\n");
		}

		return buf.toString();
	}

	public final boolean isSuper() {
		return (modifiers & Constants.ACC_SUPER) != 0;
	}

	public final boolean isClass() {
		return (modifiers & Constants.ACC_INTERFACE) == 0;
	}

	public final boolean isAnonymous() {
		computeNestedTypeStatus();
		return this.isAnonymous;
	}

	public final boolean isNested() {
		computeNestedTypeStatus();
		return this.isNested;
	}

	private final void computeNestedTypeStatus() {
		if (computedNestedTypeStatus) {
			return;
		}
		// Attribute[] attrs = attributes.getAttributes();
		for (Attribute attribute : attributes) {
			if (attribute instanceof InnerClasses) {
				InnerClass[] innerClasses = ((InnerClasses) attribute).getInnerClasses();
				for (InnerClass innerClass : innerClasses) {
					boolean innerClassAttributeRefersToMe = false;
					String inner_class_name = cpool.getConstantString(innerClass.getInnerClassIndex(),
							Constants.CONSTANT_Class);
					inner_class_name = Utility.compactClassName(inner_class_name);
					if (inner_class_name.equals(getClassName())) {
						innerClassAttributeRefersToMe = true;
					}
					if (innerClassAttributeRefersToMe) {
						this.isNested = true;
						if (innerClass.getInnerNameIndex() == 0) {
							this.isAnonymous = true;
						}
					}
				}
			}
		}
		this.computedNestedTypeStatus = true;
	}

	// J5SUPPORT:
	/**
	 * Returns true if this class represents an annotation, i.e. it was a 'public @interface blahblah' declaration
	 */
	public final boolean isAnnotation() {
		return (modifiers & Constants.ACC_ANNOTATION) != 0;
	}

	/**
	 * Returns true if this class represents an enum type
	 */
	public final boolean isEnum() {
		return (modifiers & Constants.ACC_ENUM) != 0;
	}

	/********************* New repository functionality *********************/

	/**
	 * Gets the ClassRepository which holds its definition. By default this is the same as SyntheticRepository.getInstance();
	 */
	public org.aspectj.apache.bcel.util.Repository getRepository() {
		if (repository == null) {
			repository = SyntheticRepository.getInstance();
		}
		return repository;
	}

	/**
	 * Sets the ClassRepository which loaded the JavaClass. Should be called immediately after parsing is done.
	 */
	public void setRepository(org.aspectj.apache.bcel.util.Repository repository) {
		this.repository = repository;
	}

	/**
	 * Equivalent to runtime "instanceof" operator.
	 * 
	 * @return true if this JavaClass is derived from teh super class
	 */
	public final boolean instanceOf(JavaClass super_class) {
		if (this.equals(super_class)) {
			return true;
		}

		JavaClass[] super_classes = getSuperClasses();

		for (JavaClass superClass : super_classes) {
			if (superClass.equals(super_class)) {
				return true;
			}
		}

		if (super_class.isInterface()) {
			return implementationOf(super_class);
		}

		return false;
	}

	/**
	 * @return true, if clazz is an implementation of interface inter
	 */
	public boolean implementationOf(JavaClass inter) {
		if (!inter.isInterface()) {
			throw new IllegalArgumentException(inter.getClassName() + " is no interface");
		}

		if (this.equals(inter)) {
			return true;
		}

		Collection<JavaClass> superInterfaces = getAllInterfaces();

		for (JavaClass superInterface : superInterfaces) {
			if (superInterface.equals(inter)) {
				return true;
			}
		}
		// for (int i = 0; i < super_interfaces.length; i++) {
		// if (super_interfaces[i].equals(inter)) {
		// return true;
		// }
		// }

		return false;
	}

	/**
	 * @return the superclass for this JavaClass object, or null if this is java.lang.Object
	 */
	public JavaClass getSuperClass() {
		if ("java.lang.Object".equals(getClassName())) {
			return null;
		}

		try {
			return getRepository().loadClass(getSuperclassName());
		} catch (ClassNotFoundException e) {
			System.err.println(e);
			return null;
		}
	}

	/**
	 * @return list of super classes of this class in ascending order, i.e., java.lang.Object is always the last element
	 */
	public JavaClass[] getSuperClasses() {
		JavaClass clazz = this;
		List<JavaClass> vec = new ArrayList<>();
		for (clazz = clazz.getSuperClass(); clazz != null; clazz = clazz.getSuperClass()) {
			vec.add(clazz);
		}
		return vec.toArray(new JavaClass[0]);
	}

	/**
	 * Get interfaces directly implemented by this JavaClass.
	 */
	public JavaClass[] getInterfaces() {
		String[] interfaces = getInterfaceNames();
		JavaClass[] classes = new JavaClass[interfaces.length];

		try {
			for (int i = 0; i < interfaces.length; i++) {
				classes[i] = getRepository().loadClass(interfaces[i]);
			}
		} catch (ClassNotFoundException e) {
			System.err.println(e);
			return null;
		}

		return classes;
	}

	/**
	 * Get all interfaces implemented by this JavaClass (transitively).
	 */
	public Collection<JavaClass> getAllInterfaces() {
		Queue<JavaClass> queue = new LinkedList<>();
		List<JavaClass> interfaceList = new ArrayList<>();

		queue.add(this);

		while (!queue.isEmpty()) {
			JavaClass clazz = queue.remove();

			JavaClass souper = clazz.getSuperClass();
			JavaClass[] interfaces = clazz.getInterfaces();

			if (clazz.isInterface()) {
				interfaceList.add(clazz);
			} else {
				if (souper != null) {
					queue.add(souper);
				}
			}

			Collections.addAll(queue, interfaces);
		}

		return interfaceList;
		// return interfaceList.toArray(new JavaClass[interfaceList.size()]);
	}

	/**
	 * Hunts for a signature attribute on the member and returns its contents. So where the 'regular' signature may be
	 * Ljava/util/Vector; the signature attribute will tell us e.g. "<E:>Ljava/lang/Object". We can learn the type variable names,
	 * their bounds, and the true superclass and superinterface types (including any parameterizations) Coded for performance -
	 * searches for the attribute only when requested - only searches for it once.
	 */
	public final String getGenericSignature() {
		loadGenericSignatureInfoIfNecessary();
		return signatureAttributeString;
	}

	public boolean isGeneric() {
		loadGenericSignatureInfoIfNecessary();
		return isGeneric;
	}

	private void loadGenericSignatureInfoIfNecessary() {
		if (!searchedForSignatureAttribute) {
			signatureAttribute = AttributeUtils.getSignatureAttribute(attributes);
			signatureAttributeString = signatureAttribute == null ? null : signatureAttribute.getSignature();
			isGeneric = signatureAttribute != null && signatureAttributeString.charAt(0) == '<';
			searchedForSignatureAttribute = true;
		}
	}

	public final Signature getSignatureAttribute() {
		loadGenericSignatureInfoIfNecessary();
		return signatureAttribute;
	}

}
