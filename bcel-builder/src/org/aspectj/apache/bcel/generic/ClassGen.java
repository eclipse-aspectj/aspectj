package org.aspectj.apache.bcel.generic;

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
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Modifiers;
import org.aspectj.apache.bcel.classfile.SourceFile;
import org.aspectj.apache.bcel.classfile.Utility;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisAnnos;

/**
 * Template class for building up a java class. May be initialized with an existing java class.
 * 
 * @see JavaClass
 * @version $Id: ClassGen.java,v 1.15 2009/09/15 19:40:14 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * 
 *         Upgraded, Andy Clement 9th Mar 06 - calculates SUID
 */
public class ClassGen extends Modifiers implements Cloneable {

	private String classname;
	private String superclassname;
	private String filename;
	private int classnameIndex = -1;
	private int superclassnameIndex = -1;
	private int major = Constants.MAJOR_1_1;
	private int minor = Constants.MINOR_1_1;
	private ConstantPool cpool;
	private List<Field> fieldsList = new ArrayList<Field>();
	private List<Method> methodsList = new ArrayList<Method>();
	private List<Attribute> attributesList = new ArrayList<Attribute>();
	private List<String> interfaceList = new ArrayList<String>();
	private List<AnnotationGen> annotationsList = new ArrayList<AnnotationGen>();

	public ClassGen(String classname, String superclassname, String filename, int modifiers, String[] interfacenames,
			ConstantPool cpool) {
		this.classname = classname;
		this.superclassname = superclassname;
		this.filename = filename;
		this.modifiers = modifiers;
		this.cpool = cpool;
		if (filename != null) {
			addAttribute(new SourceFile(cpool.addUtf8("SourceFile"), 2, cpool.addUtf8(filename), cpool));
		}
		this.classnameIndex = cpool.addClass(classname);
		this.superclassnameIndex = cpool.addClass(superclassname);
		if (interfacenames != null) {
			for (String interfacename : interfacenames) {
				addInterface(interfacename);
			}
		}
	}

	public ClassGen(String classname, String superclassname, String filename, int modifiers, String[] interfacenames) {
		this(classname, superclassname, filename, modifiers, interfacenames, new ConstantPool());
	}

	public ClassGen(JavaClass clazz) {
		classnameIndex = clazz.getClassNameIndex();
		superclassnameIndex = clazz.getSuperclassNameIndex();
		classname = clazz.getClassName();
		superclassname = clazz.getSuperclassName();
		filename = clazz.getSourceFileName();
		modifiers = clazz.getModifiers();
		cpool = clazz.getConstantPool().copy();
		major = clazz.getMajor();
		minor = clazz.getMinor();

		Method[] methods = clazz.getMethods();
		Field[] fields = clazz.getFields();
		String[] interfaces = clazz.getInterfaceNames();

		for (int i = 0; i < interfaces.length; i++) {
			addInterface(interfaces[i]);
		}

		// OPTIMIZE Could make unpacking lazy, done on first reference
		Attribute[] attributes = clazz.getAttributes();
		for (Attribute attr : attributes) {
			if (attr instanceof RuntimeVisAnnos) {
				RuntimeVisAnnos rva = (RuntimeVisAnnos) attr;
				List<AnnotationGen> annos = rva.getAnnotations();
				for (AnnotationGen a : annos) {
					annotationsList.add(new AnnotationGen(a, cpool, false));
				}
			} else if (attr instanceof RuntimeInvisAnnos) {
				RuntimeInvisAnnos ria = (RuntimeInvisAnnos) attr;
				List<AnnotationGen> annos = ria.getAnnotations();
				for (AnnotationGen anno : annos) {
					annotationsList.add(new AnnotationGen(anno, cpool, false));
				}
			} else {
				attributesList.add(attr);
			}
		}

		for (int i = 0; i < methods.length; i++) {
			addMethod(methods[i]);
		}

		for (int i = 0; i < fields.length; i++) {
			addField(fields[i]);
		}
	}

	/**
	 * @return build and return a JavaClass
	 */
	public JavaClass getJavaClass() {
		int[] interfaces = getInterfaces();
		Field[] fields = getFields();
		Method[] methods = getMethods();

		Collection<Attribute> attributes = null;
		if (annotationsList.size() == 0) {
			attributes = attributesList;
		} else {
			// TODO: Sometime later, trash any attributes called 'RuntimeVisibleAnnotations' or 'RuntimeInvisibleAnnotations'
			attributes = new ArrayList<Attribute>();
			attributes.addAll(Utility.getAnnotationAttributes(cpool, annotationsList));
			attributes.addAll(attributesList);
		}

		// Must be last since the above calls may still add something to it
		ConstantPool cp = this.cpool.getFinalConstantPool();

		return new JavaClass(classnameIndex, superclassnameIndex, filename, major, minor, modifiers, cp, interfaces, fields,
				methods, attributes.toArray(new Attribute[attributes.size()]));// OPTIMIZE avoid toArray()?
	}

	public void addInterface(String name) {
		interfaceList.add(name);
	}

	public void removeInterface(String name) {
		interfaceList.remove(name);
	}

	public int getMajor() {
		return major;
	}

	public void setMajor(int major) {
		this.major = major;
	}

	public void setMinor(int minor) {
		this.minor = minor;
	}

	public int getMinor() {
		return minor;
	}

	public void addAttribute(Attribute a) {
		attributesList.add(a);
	}

	public void addAnnotation(AnnotationGen a) {
		annotationsList.add(a);
	}

	public void addMethod(Method m) {
		methodsList.add(m);
	}

	/**
	 * Convenience method.
	 * 
	 * Add an empty constructor to this class that does nothing but calling super().
	 * 
	 * @param access rights for constructor
	 */
	public void addEmptyConstructor(int access_flags) {
		InstructionList il = new InstructionList();
		il.append(InstructionConstants.THIS); // Push `this'
		il.append(new InvokeInstruction(Constants.INVOKESPECIAL, cpool.addMethodref(superclassname, "<init>", "()V")));
		il.append(InstructionConstants.RETURN);

		MethodGen mg = new MethodGen(access_flags, Type.VOID, Type.NO_ARGS, null, "<init>", classname, il, cpool);
		mg.setMaxStack(1);
		mg.setMaxLocals();
		addMethod(mg.getMethod());
	}

	/**
	 * Add a field to this class.
	 * 
	 * @param f field to add
	 */
	public void addField(Field f) {
		fieldsList.add(f);
	}

	public boolean containsField(Field f) {
		return fieldsList.contains(f);
	}

	/**
	 * @return field object with given name, or null if not found
	 */
	public Field containsField(String name) {
		for (Field field : fieldsList) {
			if (field.getName().equals(name)) {
				return field;
			}
		}
		return null;
	}

	/**
	 * @return method object with given name and signature, or null if not found
	 */
	public Method containsMethod(String name, String signature) {
		for (Method method : methodsList) {
			if (method.getName().equals(name) && method.getSignature().equals(signature)) {
				return method;
			}
		}
		return null;
	}

	public void removeAttribute(Attribute a) {
		attributesList.remove(a);
	}

	public void removeAnnotation(AnnotationGen a) {
		annotationsList.remove(a);
	}

	public void removeMethod(Method m) {
		methodsList.remove(m);
	}

	/**
	 * Replace given method with new one. If the old one does not exist add the new_ method to the class anyway.
	 */
	public void replaceMethod(Method old, Method new_) {
		if (new_ == null)
			throw new ClassGenException("Replacement method must not be null");

		int i = methodsList.indexOf(old);

		if (i < 0)
			methodsList.add(new_);
		else
			methodsList.set(i, new_);
	}

	/**
	 * Replace given field with new one. If the old one does not exist add the new_ field to the class anyway.
	 */
	public void replaceField(Field old, Field new_) {
		if (new_ == null)
			throw new ClassGenException("Replacement method must not be null");

		int i = fieldsList.indexOf(old);

		if (i < 0)
			fieldsList.add(new_);
		else
			fieldsList.set(i, new_);
	}

	public void removeField(Field f) {
		fieldsList.remove(f);
	}

	public String getClassName() {
		return classname;
	}

	public String getSuperclassName() {
		return superclassname;
	}

	public String getFileName() {
		return filename;
	}

	public void setClassName(String name) {
		classname = name.replace('/', '.');
		classnameIndex = cpool.addClass(name);
	}

	public void setSuperclassName(String name) {
		superclassname = name.replace('/', '.');
		superclassnameIndex = cpool.addClass(name);
	}

	public Method[] getMethods() {
		Method[] methods = new Method[methodsList.size()];
		methodsList.toArray(methods);
		return methods;
	}

	public void setMethods(Method[] methods) {
		methodsList.clear();
		for (int m = 0; m < methods.length; m++)
			addMethod(methods[m]);
	}

	public void setFields(Field[] fs) {
		fieldsList.clear();
		for (int m = 0; m < fs.length; m++)
			addField(fs[m]);
	}

	public void setMethodAt(Method method, int pos) {
		methodsList.set(pos, method);
	}

	public Method getMethodAt(int pos) {
		return methodsList.get(pos);
	}

	public String[] getInterfaceNames() {
		int size = interfaceList.size();
		String[] interfaces = new String[size];

		interfaceList.toArray(interfaces);
		return interfaces;
	}

	public int[] getInterfaces() {
		int size = interfaceList.size();
		int[] interfaces = new int[size];

		for (int i = 0; i < size; i++)
			interfaces[i] = cpool.addClass(interfaceList.get(i));

		return interfaces;
	}

	public Field[] getFields() {
		Field[] fields = new Field[fieldsList.size()];
		fieldsList.toArray(fields);
		return fields;
	}

	public Collection<Attribute> getAttributes() {
		return attributesList;
	}

	// J5TODO: Should we make calling unpackAnnotations() lazy and put it in here?
	public AnnotationGen[] getAnnotations() {
		AnnotationGen[] annotations = new AnnotationGen[annotationsList.size()];
		annotationsList.toArray(annotations);
		return annotations;
	}

	public ConstantPool getConstantPool() {
		return cpool;
	}

	public void setConstantPool(ConstantPool constant_pool) {
		cpool = constant_pool;
	}

	public void setClassNameIndex(int class_name_index) {
		this.classnameIndex = class_name_index;
		classname = cpool.getConstantString(class_name_index, Constants.CONSTANT_Class).replace('/', '.');
	}

	public void setSuperclassNameIndex(int superclass_name_index) {
		this.superclassnameIndex = superclass_name_index;
		superclassname = cpool.getConstantString(superclass_name_index, Constants.CONSTANT_Class).replace('/', '.');
	}

	public int getSuperclassNameIndex() {
		return superclassnameIndex;
	}

	public int getClassNameIndex() {
		return classnameIndex;
	}

	@Override
	public Object clone() {
		try {
			return super.clone();
		} catch (CloneNotSupportedException e) {
			System.err.println(e);
			return null;
		}
	}

	public final boolean isAnnotation() {
		return (modifiers & Constants.ACC_ANNOTATION) != 0;
	}

	public final boolean isEnum() {
		return (modifiers & Constants.ACC_ENUM) != 0;
	}

	/**
	 * Calculate the SerialVersionUID for a class.
	 */
	public long getSUID() {
		try {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);

			// 1. classname
			dos.writeUTF(getClassName());

			// 2. classmodifiers: ACC_PUBLIC, ACC_FINAL, ACC_INTERFACE, and ACC_ABSTRACT
			int classmods = 0;
			classmods |= (isPublic() ? Constants.ACC_PUBLIC : 0);
			classmods |= (isFinal() ? Constants.ACC_FINAL : 0);
			classmods |= (isInterface() ? Constants.ACC_INTERFACE : 0);

			if (isAbstract()) {
				// if an interface then abstract is only set if it has methods
				if (isInterface()) {
					if (methodsList.size() > 0)
						classmods |= Constants.ACC_ABSTRACT;
				} else {
					classmods |= Constants.ACC_ABSTRACT;
				}
			}

			dos.writeInt(classmods);

			// 3. ordered list of interfaces
			String[] names = getInterfaceNames();
			if (names != null) {
				Arrays.sort(names);
				for (int i = 0; i < names.length; i++)
					dos.writeUTF(names[i]);
			}

			// 4. ordered list of fields (ignoring private static and private transient fields):
			// (relevant modifiers are ACC_PUBLIC, ACC_PRIVATE,
			// ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_VOLATILE,
			// ACC_TRANSIENT)
			List<Field> relevantFields = new ArrayList<Field>();
			for (Field field : fieldsList) {
				if (!(field.isPrivate() && field.isStatic()) && !(field.isPrivate() && field.isTransient())) {
					relevantFields.add(field);
				}
			}
			Collections.sort(relevantFields, new FieldComparator());
			int relevantFlags = Constants.ACC_PUBLIC | Constants.ACC_PRIVATE | Constants.ACC_PROTECTED | Constants.ACC_STATIC
					| Constants.ACC_FINAL | Constants.ACC_VOLATILE | Constants.ACC_TRANSIENT;
			for (Field f : relevantFields) {
				dos.writeUTF(f.getName());
				dos.writeInt(relevantFlags & f.getModifiers());
				dos.writeUTF(f.getType().getSignature());
			}

			// some up front method processing: discover clinit, init and ordinary methods of interest:
			List<Method> relevantMethods = new ArrayList<Method>();
			List<Method> relevantCtors = new ArrayList<Method>();
			boolean hasClinit = false;
			for (Method m : methodsList) {
				boolean couldBeInitializer = m.getName().charAt(0) == '<';
				if (couldBeInitializer && m.getName().equals("<clinit>")) {
					hasClinit = true;
				} else if (couldBeInitializer && m.getName().equals("<init>")) {
					if (!m.isPrivate())
						relevantCtors.add(m);
				} else {
					if (!m.isPrivate())
						relevantMethods.add(m);
				}
			}
			Collections.sort(relevantCtors, new ConstructorComparator());
			Collections.sort(relevantMethods, new MethodComparator());

			// 5. If a class initializer exists, write out the following:
			// 1. The name of the method, <clinit>.
			// 2. The modifier of the method, java.lang.reflect.Modifier.STATIC, written as a 32-bit integer.
			// 3. The descriptor of the method, ()V.
			if (hasClinit) {
				dos.writeUTF("<clinit>");
				dos.writeInt(Modifier.STATIC);
				dos.writeUTF("()V");
			}

			// for methods and constructors:
			// ACC_PUBLIC, ACC_PRIVATE, ACC_PROTECTED, ACC_STATIC, ACC_FINAL, ACC_SYNCHRONIZED,
			// ACC_NATIVE, ACC_ABSTRACT and ACC_STRICT
			relevantFlags = Constants.ACC_PUBLIC | Constants.ACC_PRIVATE | Constants.ACC_PROTECTED | Constants.ACC_STATIC
					| Constants.ACC_FINAL | Constants.ACC_SYNCHRONIZED | Constants.ACC_NATIVE | Constants.ACC_ABSTRACT
					| Constants.ACC_STRICT;

			// 6. sorted non-private constructors
			for (Method ctor : relevantCtors) {
				dos.writeUTF(ctor.getName()); // <init>
				dos.writeInt(relevantFlags & ctor.getModifiers());
				dos.writeUTF(ctor.getSignature().replace('/', '.'));
			}

			// 7. sorted non-private methods
			for (Method m : relevantMethods) {
				dos.writeUTF(m.getName());
				dos.writeInt(relevantFlags & m.getModifiers());
				dos.writeUTF(m.getSignature().replace('/', '.'));
			}
			dos.flush();
			dos.close();
			byte[] bs = baos.toByteArray();
			MessageDigest md = MessageDigest.getInstance("SHA");
			byte[] result = md.digest(bs);

			long suid = 0L;
			int pos = result.length > 8 ? 7 : result.length - 1; // use the bytes we have
			while (pos >= 0) {
				suid = suid << 8 | ((long) result[pos--] & 0xff);
			}

			// if it was definetly 8 everytime...
			// long suid = ((long)(sha[0]&0xff) | (long)(sha[1]&0xff) << 8 |
			// (long)(sha[2]&0xff) << 16 | (long)(sha[3]&0xff) << 24 |
			// (long)(sha[4]&0xff) << 32 | (long)(sha[5]&0xff) << 40 |
			// (long)(sha[6]&0xff) << 48 | (long)(sha[7]&0xff) << 56);
			return suid;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Unable to calculate suid for " + getClassName() + ": " + e.toString());
		}
	}

	private static class FieldComparator implements Comparator<Field> {
		public int compare(Field f0, Field f1) {
			return f0.getName().compareTo(f1.getName());
		}
	}

	private static class ConstructorComparator implements Comparator<Method> {
		public int compare(Method m0, Method m1) {
			// can ignore the name...
			return (m0).getSignature().compareTo(m1.getSignature());
		}
	}

	private static class MethodComparator implements Comparator<Method> {
		public int compare(Method m0, Method m1) {
			int result = m0.getName().compareTo(m1.getName());
			if (result == 0) {
				result = m0.getSignature().compareTo(m1.getSignature());
			}
			return result;
		}
	}

	public boolean hasAttribute(String attributeName) {
		for (Attribute attr : attributesList) {
			if (attr.getName().equals(attributeName)) {
				return true;
			}
		}
		return false;
	}

	public Attribute getAttribute(String attributeName) {
		for (Attribute attr : attributesList) {
			if (attr.getName().equals(attributeName)) {
				return attr;
			}
		}
		return null;
	}
}
