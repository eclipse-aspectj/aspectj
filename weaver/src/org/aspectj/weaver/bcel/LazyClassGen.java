/* *******************************************************************
 * Copyright (c) 2002-2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC                 initial implementation 
 *     Andy Clement  6Jul05 generics - signature attribute
 *     Abraham Nevado
 * ******************************************************************/

package org.aspectj.weaver.bcel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.Synthetic;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.generic.BasicType;
import org.aspectj.apache.bcel.generic.ClassGen;
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjAttribute.WeaverState;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.SignatureUtils;
import org.aspectj.weaver.TypeVariable;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.UnresolvedType.TypeKind;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.WeaverStateInfo;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.asm.AsmDetector;
import org.aspectj.weaver.bcel.asm.StackMapAdder;

/**
 * Lazy lazy lazy. We don't unpack the underlying class unless necessary. Things like new methods and annotations accumulate in here
 * until they must be written out, don't add them to the underlying MethodGen! Things are slightly different if this represents an
 * Aspect.
 */
public final class LazyClassGen {

	private static final int ACC_SYNTHETIC = 0x1000;

	private static final String[] NO_STRINGS = new String[0];

	int highestLineNumber = 0; // ---- JSR 45 info

	private final SortedMap<String, InlinedSourceFileInfo> inlinedFiles = new TreeMap<String, InlinedSourceFileInfo>();

	private boolean regenerateGenericSignatureAttribute = false;

	private BcelObjectType myType; // XXX is not set for types we create
	private ClassGen myGen;
	private final ConstantPool cp;
	private final World world;
	private final String packageName = null;

	private final List<BcelField> fields = new ArrayList<BcelField>();
	private final List<LazyMethodGen> methodGens = new ArrayList<LazyMethodGen>();
	private final List<LazyClassGen> classGens = new ArrayList<LazyClassGen>();
	private final List<AnnotationGen> annotations = new ArrayList<AnnotationGen>();
	private int childCounter = 0;

	private final InstructionFactory fact;

	private boolean isSerializable = false;
	private boolean hasSerialVersionUIDField = false;
	private boolean serialVersionUIDRequiresInitialization = false;
	private long calculatedSerialVersionUID;
	private boolean hasClinit = false;

	private ResolvedType[] extraSuperInterfaces = null;
	private ResolvedType superclass = null;

	// ---

	static class InlinedSourceFileInfo {
		int highestLineNumber;
		int offset; // calculated

		InlinedSourceFileInfo(int highestLineNumber) {
			this.highestLineNumber = highestLineNumber;
		}
	}

	void addInlinedSourceFileInfo(String fullpath, int highestLineNumber) {
		Object o = inlinedFiles.get(fullpath);
		if (o != null) {
			InlinedSourceFileInfo info = (InlinedSourceFileInfo) o;
			if (info.highestLineNumber < highestLineNumber) {
				info.highestLineNumber = highestLineNumber;
			}
		} else {
			inlinedFiles.put(fullpath, new InlinedSourceFileInfo(highestLineNumber));
		}
	}

	void calculateSourceDebugExtensionOffsets() {
		int i = roundUpToHundreds(highestLineNumber);
		for (InlinedSourceFileInfo element : inlinedFiles.values()) {
			element.offset = i;
			i = roundUpToHundreds(i + element.highestLineNumber);
		}
	}

	private static int roundUpToHundreds(int i) {
		return ((i / 100) + 1) * 100;
	}

	int getSourceDebugExtensionOffset(String fullpath) {
		return inlinedFiles.get(fullpath).offset;
	}

	// private Unknown getSourceDebugExtensionAttribute() {
	// int nameIndex = cp.addUtf8("SourceDebugExtension");
	// String data = getSourceDebugExtensionString();
	// //System.err.println(data);
	// byte[] bytes = Utility.stringToUTF(data);
	// int length = bytes.length;
	//
	// return new Unknown(nameIndex, length, bytes, cp);
	// }

	// private LazyClassGen() {}
	// public static void main(String[] args) {
	// LazyClassGen m = new LazyClassGen();
	// m.highestLineNumber = 37;
	// m.inlinedFiles.put("boo/baz/foo.java", new InlinedSourceFileInfo( 83));
	// m.inlinedFiles.put("boo/barz/foo.java", new InlinedSourceFileInfo(292));
	// m.inlinedFiles.put("boo/baz/moo.java", new InlinedSourceFileInfo(128));
	// m.calculateSourceDebugExtensionOffsets();
	// System.err.println(m.getSourceDebugExtensionString());
	// }

	// For the entire pathname, we're using package names. This is probably
	// wrong.
	// private String getSourceDebugExtensionString() {
	// StringBuffer out = new StringBuffer();
	// String myFileName = getFileName();
	// // header section
	// out.append("SMAP\n");
	// out.append(myFileName);
	// out.append("\nAspectJ\n");
	// // stratum section
	// out.append("*S AspectJ\n");
	// // file section
	// out.append("*F\n");
	// out.append("1 ");
	// out.append(myFileName);
	// out.append("\n");
	// int i = 2;
	// for (Iterator iter = inlinedFiles.keySet().iterator(); iter.hasNext();) {
	// String element = (String) iter.next();
	// int ii = element.lastIndexOf('/');
	// if (ii == -1) {
	// out.append(i++); out.append(' ');
	// out.append(element); out.append('\n');
	// } else {
	// out.append("+ "); out.append(i++); out.append(' ');
	// out.append(element.substring(ii+1)); out.append('\n');
	// out.append(element); out.append('\n');
	// }
	// }
	// // emit line section
	// out.append("*L\n");
	// out.append("1#1,");
	// out.append(highestLineNumber);
	// out.append(":1,1\n");
	// i = 2;
	// for (Iterator iter = inlinedFiles.values().iterator(); iter.hasNext();) {
	// InlinedSourceFileInfo element = (InlinedSourceFileInfo) iter.next();
	// out.append("1#");
	// out.append(i++); out.append(',');
	// out.append(element.highestLineNumber); out.append(":");
	// out.append(element.offset + 1); out.append(",1\n");
	// }
	// // end section
	// out.append("*E\n");
	// // and finish up...
	// return out.toString();
	// }

	// ---- end JSR45-related stuff

	/** Emit disassembled class and newline to out */
	public static void disassemble(String path, String name, PrintStream out) throws IOException {
		if (null == out) {
			return;
		}
		// out.println("classPath: " + classPath);

		BcelWorld world = new BcelWorld(path);

		UnresolvedType ut = UnresolvedType.forName(name);
		ut.setNeedsModifiableDelegate(true);
		LazyClassGen clazz = new LazyClassGen(BcelWorld.getBcelObjectType(world.resolve(ut)));
		clazz.print(out);
		out.println();
	}

	public String getNewGeneratedNameTag() {
		return new Integer(childCounter++).toString();
	}

	// ----

	public LazyClassGen(String class_name, String super_class_name, String file_name, int access_flags, String[] interfaces,
			World world) {
		myGen = new ClassGen(class_name, super_class_name, file_name, access_flags, interfaces);
		cp = myGen.getConstantPool();
		fact = new InstructionFactory(myGen, cp);
		regenerateGenericSignatureAttribute = true;
		this.world = world;
	}

	// Non child type, so it comes from a real type in the world.
	public LazyClassGen(BcelObjectType myType) {
		myGen = new ClassGen(myType.getJavaClass());
		cp = myGen.getConstantPool();
		fact = new InstructionFactory(myGen, cp);
		this.myType = myType;
		world = myType.getResolvedTypeX().getWorld();

		/* Does this class support serialization */
		if (implementsSerializable(getType())) {
			isSerializable = true;

			// ResolvedMember[] fields = getType().getDeclaredFields();
			// for (int i = 0; i < fields.length; i++) {
			// ResolvedMember field = fields[i];
			// if (field.getName().equals("serialVersionUID")
			// && field.isStatic() && field.getType().equals(UnresolvedType.LONG))
			// {
			// hasSerialVersionUIDField = true;
			// }
			// }
			hasSerialVersionUIDField = hasSerialVersionUIDField(getType());

			ResolvedMember[] methods = getType().getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				ResolvedMember method = methods[i];
				if (method.getName().equals("<clinit>")) {
					if (method.getKind() != Member.STATIC_INITIALIZATION) {
						throw new RuntimeException("qui?");
					}
					hasClinit = true;
				}
			}

			// Do we need to calculate an SUID and add it?
			if (!getType().isInterface() && !hasSerialVersionUIDField && world.isAddSerialVerUID()) {
				calculatedSerialVersionUID = myGen.getSUID();
				FieldGen fg = new FieldGen(Constants.ACC_PRIVATE | Constants.ACC_FINAL | Constants.ACC_STATIC, BasicType.LONG,
						"serialVersionUID", getConstantPool());
				addField(fg);
				hasSerialVersionUIDField = true;
				serialVersionUIDRequiresInitialization = true;
				// warn about what we've done?
				if (world.getLint().calculatingSerialVersionUID.isEnabled()) {
					world.getLint().calculatingSerialVersionUID.signal(
							new String[] { getClassName(), Long.toString(calculatedSerialVersionUID) + "L" }, null, null);
				}
			}
		}

		ResolvedMember[] methods = myType.getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			addMethodGen(new LazyMethodGen((BcelMethod) methods[i], this));
		}

		// Method[] methods = myGen.getMethods();
		// for (int i = 0; i < methods.length; i++) {
		// addMethodGen(new LazyMethodGen(methods[i], this));
		// }

		ResolvedMember[] fields = myType.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			this.fields.add((BcelField) fields[i]);
		}
	}

	public static boolean hasSerialVersionUIDField(ResolvedType type) {

		ResolvedMember[] fields = type.getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			ResolvedMember field = fields[i];
			if (field.getName().equals("serialVersionUID") && Modifier.isStatic(field.getModifiers())
					&& field.getType().equals(UnresolvedType.LONG)) {
				return true;
			}
		}

		return false;
	}

	// public void addAttribute(Attribute i) {
	// myGen.addAttribute(i);
	// }

	// ----

	public String getInternalClassName() {
		return getConstantPool().getConstantString_CONSTANTClass(myGen.getClassNameIndex());
		// getConstantPool().getConstantString(
		// myGen.getClassNameIndex(),
		// Constants.CONSTANT_Class);

	}

	public String getInternalFileName() {
		String str = getInternalClassName();
		int index = str.lastIndexOf('/');
		if (index == -1) {
			return getFileName();
		} else {
			return str.substring(0, index + 1) + getFileName();
		}
	}

	/**
	 * Returns the packagename - if its the default package we return an empty string
	 */
	public String getPackageName() {
		if (packageName != null) {
			return packageName;
		}
		String str = getInternalClassName();
		int index = str.indexOf("<");
		if (index != -1) {
			str = str.substring(0, index); // strip off the generics guff
		}
		index = str.lastIndexOf("/");
		if (index == -1) {
			return "";
		}
		return str.substring(0, index).replace('/', '.');
	}

	public void addMethodGen(LazyMethodGen gen) {
		// assert gen.getClassName() == super.getClassName();
		methodGens.add(gen);
		if (highestLineNumber < gen.highestLineNumber) {
			highestLineNumber = gen.highestLineNumber;
		}
	}

	public boolean removeMethodGen(LazyMethodGen gen) {
		return methodGens.remove(gen);
	}

	public void addMethodGen(LazyMethodGen gen, ISourceLocation sourceLocation) {
		addMethodGen(gen);
		if (!gen.getMethod().isPrivate()) {
			warnOnAddedMethod(gen.getMethod(), sourceLocation);
		}
	}

	public void errorOnAddedField(FieldGen field, ISourceLocation sourceLocation) {
		if (isSerializable && !hasSerialVersionUIDField) {
			getWorld().getLint().serialVersionUIDBroken.signal(
					new String[] { myType.getResolvedTypeX().getName(), field.getName() }, sourceLocation, null);
		}
	}

	public void warnOnAddedInterface(String name, ISourceLocation sourceLocation) {
		warnOnModifiedSerialVersionUID(sourceLocation, "added interface " + name);
	}

	public void warnOnAddedMethod(Method method, ISourceLocation sourceLocation) {
		warnOnModifiedSerialVersionUID(sourceLocation, "added non-private method " + method.getName());
	}

	public void warnOnAddedStaticInitializer(Shadow shadow, ISourceLocation sourceLocation) {
		if (!hasClinit) {
			warnOnModifiedSerialVersionUID(sourceLocation, "added static initializer");
		}
	}

	public void warnOnModifiedSerialVersionUID(ISourceLocation sourceLocation, String reason) {
		if (isSerializable && !hasSerialVersionUIDField) {
			getWorld().getLint().needsSerialVersionUIDField.signal(new String[] { myType.getResolvedTypeX().getName().toString(),
					reason }, sourceLocation, null);
		}
	}

	public World getWorld() {
		return world;
	}

	public List<LazyMethodGen> getMethodGens() {
		return methodGens; // ???Collections.unmodifiableList(methodGens);
	}

	public List<BcelField> getFieldGens() {
		return fields;
	}
	
	public boolean fieldExists(String name) {
//		Field[] allFields = myGen.getFields();
//		if (allFields!=null) { 
//			for (int i=0;i<allFields.length;i++) {
//				Field f = allFields[i];
//				if (f.getName().equals(name)) {
//					return f;
//				}
//			}
//		}
		for (BcelField f: fields) {
			if (f.getName().equals(name)) {
				return true;
			}
		}
		return false;
	}

	private void writeBack(BcelWorld world) {
		if (getConstantPool().getSize() > Short.MAX_VALUE) {
			reportClassTooBigProblem();
			return;
		}

		if (annotations.size() > 0) {
			for (AnnotationGen element : annotations) {
				myGen.addAnnotation(element);
			}
			// Attribute[] annAttributes =
			// org.aspectj.apache.bcel.classfile.Utility.getAnnotationAttributes(
			// getConstantPool(),annotations);
			// for (int i = 0; i < annAttributes.length; i++) {
			// Attribute attribute = annAttributes[i];
			// System.err.println("Adding attribute for "+attribute);
			// myGen.addAttribute(attribute);
			// }
		}

		// Add a weaver version attribute to the file being produced (if
		// necessary...)
		if (!myGen.hasAttribute("org.aspectj.weaver.WeaverVersion")) {
			myGen.addAttribute(Utility.bcelAttribute(new AjAttribute.WeaverVersionInfo(), getConstantPool()));
		}

		// 352389: don't add another one (there will already be one there and this new one won't deserialize correctly)
		if (!world.isOverWeaving() || !myGen.hasAttribute(WeaverState.AttributeName)) {
			if (myType != null && myType.getWeaverState() != null) {
				myGen.addAttribute(Utility.bcelAttribute(new AjAttribute.WeaverState(myType.getWeaverState()), getConstantPool()));
			}
		}

		// FIXME ATAJ needed only for slow Aspects.aspectOf() - keep or remove
		// make a lot of test fail since the test compare weaved class file
		// based on some test data as text files...
		// if (!myGen.isInterface()) {
		// addAjClassField();
		// }

		addAjcInitializers();

		// 17Feb05 - ASC - Skip this for now - it crashes IBM 1.4.2 jvms
		// (pr80430). Will be revisited when contents
		// of attribute are confirmed to be correct.
		boolean sourceDebugExtensionSupportSwitchedOn = false;

		if (sourceDebugExtensionSupportSwitchedOn) {
			calculateSourceDebugExtensionOffsets();
		}

		int len = methodGens.size();
		myGen.setMethods(Method.NoMethods);

		for (LazyMethodGen gen : methodGens) {
			// we skip empty clinits
			if (isEmptyClinit(gen)) {
				continue;
			}
			myGen.addMethod(gen.getMethod());
		}

		len = fields.size();
		myGen.setFields(Field.NoFields);
		for (int i = 0; i < len; i++) {
			BcelField gen = fields.get(i);
			myGen.addField(gen.getField(cp));
		}

		if (sourceDebugExtensionSupportSwitchedOn) {
			if (inlinedFiles.size() != 0) {
				if (hasSourceDebugExtensionAttribute(myGen)) {
					world.showMessage(IMessage.WARNING, WeaverMessages.format(WeaverMessages.OVERWRITE_JSR45, getFileName()), null,
							null);
				}
				// myGen.addAttribute(getSourceDebugExtensionAttribute());
			}
		}

		fixupGenericSignatureAttribute();
	}

	/**
	 * When working with Java generics, a signature attribute is attached to the type which indicates how it was declared. This
	 * routine ensures the signature attribute for the class we are about to write out is correct. Basically its responsibilities
	 * are:
	 * <ol>
	 * <li>
	 * Checking whether the attribute needs changing (ie. did weaving change the type hierarchy) - if it did, remove the old
	 * attribute
	 * <li>
	 * Check if we need an attribute at all, are we generic? are our supertypes parameterized/generic?
	 * <li>
	 * Build the new attribute which includes all typevariable, supertype and superinterface information
	 * </ol>
	 */
	private void fixupGenericSignatureAttribute() {

		if (getWorld() != null && !getWorld().isInJava5Mode()) {
			return;
		}

		// TODO asc generics Temporarily assume that types we generate dont need
		// a signature attribute (closure/etc).. will need
		// revisiting no doubt...
		// if (myType == null) {
		// return;
		// }

		// 1. Has anything changed that would require us to modify this
		// attribute?
		if (!regenerateGenericSignatureAttribute) {
			return;
		}

		// 2. Find the old attribute
		Signature sigAttr = null;
		if (myType != null) { // if null, this is a type built from scratch, it
			// won't already have a sig attribute
			sigAttr = (Signature) myGen.getAttribute("Signature");
		}

		// 3. Do we need an attribute?
		boolean needAttribute = false;
		// If we had one before, we definetly still need one as types can't be
		// 'removed' from the hierarchy
		if (sigAttr != null) {
			needAttribute = true;
		}

		// check the interfaces
		if (!needAttribute) {
			if (myType != null) {
				ResolvedType[] interfaceRTXs = myType.getDeclaredInterfaces();
				for (int i = 0; i < interfaceRTXs.length; i++) {
					ResolvedType typeX = interfaceRTXs[i];
					if (typeX.isGenericType() || typeX.isParameterizedType()) {
						needAttribute = true;
					}
				}
				if (extraSuperInterfaces != null) {
					for (int i = 0; i < extraSuperInterfaces.length; i++) {
						ResolvedType interfaceType = extraSuperInterfaces[i];
						if (interfaceType.isGenericType() || interfaceType.isParameterizedType()) {
							needAttribute = true;
						}
					}
				}
			}

			if (myType == null) {
				ResolvedType superclassRTX = superclass;
				if (superclassRTX != null) {
					if (superclassRTX.isGenericType() || superclassRTX.isParameterizedType()) {
						needAttribute = true;
					}
				}
			} else {
				// check the supertype
				ResolvedType superclassRTX = getSuperClass();
				if (superclassRTX.isGenericType() || superclassRTX.isParameterizedType()) {
					needAttribute = true;
				}
			}
		}

		if (needAttribute) {
			StringBuffer signature = new StringBuffer();
			// first, the type variables...
			if (myType != null) {
				TypeVariable[] tVars = myType.getTypeVariables();
				if (tVars.length > 0) {
					signature.append("<");
					for (int i = 0; i < tVars.length; i++) {
						TypeVariable variable = tVars[i];
						signature.append(variable.getSignatureForAttribute());
					}
					signature.append(">");
				}
			}
			// now the supertype
			String supersig = getSuperClass().getSignatureForAttribute();
			signature.append(supersig);
			if (myType != null) {
				ResolvedType[] interfaceRTXs = myType.getDeclaredInterfaces();
				for (int i = 0; i < interfaceRTXs.length; i++) {
					String s = interfaceRTXs[i].getSignatureForAttribute();
					signature.append(s);
				}
				if (extraSuperInterfaces != null) {
					for (int i = 0; i < extraSuperInterfaces.length; i++) {
						String s = extraSuperInterfaces[i].getSignatureForAttribute();
						signature.append(s);
					}
				}
			}
			if (sigAttr != null) {
				myGen.removeAttribute(sigAttr);
			}
			myGen.addAttribute(createSignatureAttribute(signature.toString()));
		}
	}

	/**
	 * Helper method to create a signature attribute based on a string signature: e.g. "Ljava/lang/Object;LI<Ljava/lang/Double;>;"
	 */
	private Signature createSignatureAttribute(String signature) {
		int nameIndex = cp.addUtf8("Signature");
		int sigIndex = cp.addUtf8(signature);
		return new Signature(nameIndex, 2, sigIndex, cp);
	}

	/**
	 * 
	 */
	private void reportClassTooBigProblem() {
		// PR 59208
		// we've generated a class that is just toooooooooo big (you've been
		// generating programs
		// again haven't you? come on, admit it, no-one writes classes this big
		// by hand).
		// create an empty myGen so that we can give back a return value that
		// doesn't upset the
		// rest of the process.
		myGen = new ClassGen(myGen.getClassName(), myGen.getSuperclassName(), myGen.getFileName(), myGen.getModifiers(),
				myGen.getInterfaceNames());
		// raise an error against this compilation unit.
		getWorld().showMessage(IMessage.ERROR, WeaverMessages.format(WeaverMessages.CLASS_TOO_BIG, this.getClassName()),
				new SourceLocation(new File(myGen.getFileName()), 0), null);
	}

	private static boolean hasSourceDebugExtensionAttribute(ClassGen gen) {
		return gen.hasAttribute("SourceDebugExtension");
	}

	public JavaClass getJavaClass(BcelWorld world) {
		writeBack(world);
		return myGen.getJavaClass();
	}

	public byte[] getJavaClassBytesIncludingReweavable(BcelWorld world) {
		writeBack(world);
		byte[] wovenClassFileData = myGen.getJavaClass().getBytes();
		// if is java 6 class file
		if (((myGen.getMajor() == Constants.MAJOR_1_6 && world.shouldGenerateStackMaps()) || myGen.getMajor() > Constants.MAJOR_1_6)
				&& AsmDetector.isAsmAround) {
			wovenClassFileData = StackMapAdder.addStackMaps(world, wovenClassFileData);
		}

		WeaverStateInfo wsi = myType.getWeaverState();// getOrCreateWeaverStateInfo();
		if (wsi != null && wsi.isReweavable()) { // && !reweavableDataInserted
			// reweavableDataInserted = true;
			return wsi.replaceKeyWithDiff(wovenClassFileData);
		} else {
			return wovenClassFileData;
		}
	}

	public void addGeneratedInner(LazyClassGen newClass) {
		classGens.add(newClass);
	}

	public void addInterface(ResolvedType newInterface, ISourceLocation sourceLocation) {
		regenerateGenericSignatureAttribute = true;

		if (extraSuperInterfaces == null) {
			extraSuperInterfaces = new ResolvedType[1];
			extraSuperInterfaces[0] = newInterface;
		} else {
			ResolvedType[] x = new ResolvedType[extraSuperInterfaces.length + 1];
			System.arraycopy(extraSuperInterfaces, 0, x, 1, extraSuperInterfaces.length);
			x[0] = newInterface;
			extraSuperInterfaces = x;
		}
		myGen.addInterface(newInterface.getRawName());
		if (!newInterface.equals(UnresolvedType.SERIALIZABLE)) {
			warnOnAddedInterface(newInterface.getName(), sourceLocation);
		}
	}

	public void setSuperClass(ResolvedType newSuperclass) {
		regenerateGenericSignatureAttribute = true;
		superclass = newSuperclass;
		// myType.addParent(typeX); // used for the attribute
		if (newSuperclass.getGenericType() != null) {
			newSuperclass = newSuperclass.getGenericType();
		}
		myGen.setSuperclassName(newSuperclass.getName()); // used in the real
		// class data
	}

	// public String getSuperClassname() {
	// return myGen.getSuperclassName();
	// }

	public ResolvedType getSuperClass() {
		if (superclass != null) {
			return superclass;
		}
		return myType.getSuperclass();
	}

	public String[] getInterfaceNames() {
		return myGen.getInterfaceNames();
	}

	// non-recursive, may be a bug, ha ha.
	private List<LazyClassGen> getClassGens() {
		List<LazyClassGen> ret = new ArrayList<LazyClassGen>();
		ret.add(this);
		ret.addAll(classGens);
		return ret;
	}

	public List<UnwovenClassFile.ChildClass> getChildClasses(BcelWorld world) {
		if (classGens.isEmpty()) {
			return Collections.emptyList();
		}
		List<UnwovenClassFile.ChildClass> ret = new ArrayList<UnwovenClassFile.ChildClass>();
		for (LazyClassGen clazz : classGens) {
			byte[] bytes = clazz.getJavaClass(world).getBytes();
			String name = clazz.getName();
			int index = name.lastIndexOf('$');
			// XXX this could be bad, check use of dollar signs.
			name = name.substring(index + 1);
			ret.add(new UnwovenClassFile.ChildClass(name, bytes));
		}
		return ret;
	}

	@Override
	public String toString() {
		return toShortString();
	}

	public String toShortString() {
		String s = org.aspectj.apache.bcel.classfile.Utility.accessToString(myGen.getModifiers(), true);
		if (s != "") {
			s += " ";
		}
		s += org.aspectj.apache.bcel.classfile.Utility.classOrInterface(myGen.getModifiers());
		s += " ";
		s += myGen.getClassName();
		return s;
	}

	public String toLongString() {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		print(new PrintStream(s));
		return new String(s.toByteArray());
	}

	public void print() {
		print(System.out);
	}

	public void print(PrintStream out) {
		List<LazyClassGen> classGens = getClassGens();
		for (Iterator<LazyClassGen> iter = classGens.iterator(); iter.hasNext();) {
			LazyClassGen element = iter.next();
			element.printOne(out);
			if (iter.hasNext()) {
				out.println();
			}
		}
	}

	private void printOne(PrintStream out) {
		out.print(toShortString());
		out.print(" extends ");
		out.print(org.aspectj.apache.bcel.classfile.Utility.compactClassName(myGen.getSuperclassName(), false));

		int size = myGen.getInterfaces().length;

		if (size > 0) {
			out.print(" implements ");
			for (int i = 0; i < size; i++) {
				out.print(myGen.getInterfaceNames()[i]);
				if (i < size - 1) {
					out.print(", ");
				}
			}
		}
		out.print(":");
		out.println();
		// XXX make sure to pass types correctly around, so this doesn't happen.
		if (myType != null) {
			myType.printWackyStuff(out);
		}
		Field[] fields = myGen.getFields();
		for (int i = 0, len = fields.length; i < len; i++) {
			out.print("  ");
			out.println(fields[i]);
		}
		List<LazyMethodGen> methodGens = getMethodGens();
		for (Iterator<LazyMethodGen> iter = methodGens.iterator(); iter.hasNext();) {
			LazyMethodGen gen = iter.next();
			// we skip empty clinits
			if (isEmptyClinit(gen)) {
				continue;
			}
			gen.print(out, (myType != null ? myType.getWeaverVersionAttribute() : WeaverVersionInfo.UNKNOWN));
			if (iter.hasNext()) {
				out.println();
			}
		}
		// out.println("  ATTRIBS: " + Arrays.asList(myGen.getAttributes()));

		out.println("end " + toShortString());
	}

	private boolean isEmptyClinit(LazyMethodGen gen) {

		if (!gen.getName().equals("<clinit>")) {
			return false;
		}
		// System.err.println("checking clinig: " + gen);
		InstructionHandle start = gen.getBody().getStart();
		while (start != null) {
			if (Range.isRangeHandle(start) || (start.getInstruction().opcode == Constants.RETURN)) {
				start = start.getNext();
			} else {
				return false;
			}
		}

		return true;
	}

	public ConstantPool getConstantPool() {
		return cp;
	}

	public String getName() {
		return myGen.getClassName();
	}

	public boolean isWoven() {
		return myType.getWeaverState() != null;
	}

	public boolean isReweavable() {
		if (myType.getWeaverState() == null) {
			return true;
		}
		return myType.getWeaverState().isReweavable();
	}

	public Set<String> getAspectsAffectingType() {
		if (myType.getWeaverState() == null) {
			return null;
		}
		return myType.getWeaverState().getAspectsAffectingType();
	}

	public WeaverStateInfo getOrCreateWeaverStateInfo(boolean inReweavableMode) {
		WeaverStateInfo ret = myType.getWeaverState();
		if (ret != null) {
			return ret;
		}
		ret = new WeaverStateInfo(inReweavableMode);
		myType.setWeaverState(ret);
		return ret;
	}

	public InstructionFactory getFactory() {
		return fact;
	}

	public LazyMethodGen getStaticInitializer() {
		for (LazyMethodGen gen : methodGens) {
			// OPTIMIZE persist kind of member into the gen object? for clinit
			if (gen.getName().equals("<clinit>")) {
				return gen;
			}
		}
		LazyMethodGen clinit = new LazyMethodGen(Modifier.STATIC, Type.VOID, "<clinit>", new Type[0], NO_STRINGS, this);
		clinit.getBody().insert(InstructionConstants.RETURN);
		methodGens.add(clinit);
		return clinit;
	}

	/**
	 * Retrieve the ajc$preClinit method - this method captures any initialization AspectJ wants to ensure happens in a class. It is
	 * called from the static initializer. Maintaining this separation enables overweaving to ignore join points added due to
	 * earlier weaves. If the ajc$preClinit method cannot be found, it is created and a call to it is placed in the real static
	 * initializer (the call is placed at the start of the static initializer).
	 * 
	 * @return the LazyMethodGen representing the ajc$ clinit
	 */
	public LazyMethodGen getAjcPreClinit() {
		if (this.isInterface()) {
			throw new IllegalStateException();
		}
		for (LazyMethodGen methodGen : methodGens) {
			if (methodGen.getName().equals(NameMangler.AJC_PRE_CLINIT_NAME)) {
				return methodGen;
			}
		}
		LazyMethodGen ajcPreClinit = new LazyMethodGen(Modifier.PRIVATE | Modifier.STATIC, Type.VOID,
				NameMangler.AJC_PRE_CLINIT_NAME, Type.NO_ARGS, NO_STRINGS, this);
		ajcPreClinit.getBody().insert(InstructionConstants.RETURN);
		methodGens.add(ajcPreClinit);
		getStaticInitializer().getBody().insert(Utility.createInvoke(fact, ajcPreClinit));
		return ajcPreClinit;
	}

	/**
	 * factory method for building multiple extended clinit methods. Constructs a new clinit method that invokes the previous one
	 * and then returns it. The index is used as a name suffix.
	 * 
	 * @param previousPreClinit
	 * @param i
	 */
	public LazyMethodGen createExtendedAjcPreClinit(LazyMethodGen previousPreClinit, int i) {
		LazyMethodGen ajcPreClinit = new LazyMethodGen(Modifier.PRIVATE | Modifier.STATIC, Type.VOID,
				NameMangler.AJC_PRE_CLINIT_NAME + i, Type.NO_ARGS, NO_STRINGS, this);
		ajcPreClinit.getBody().insert(InstructionConstants.RETURN);
		methodGens.add(ajcPreClinit);
		previousPreClinit.getBody().insert(Utility.createInvoke(fact, ajcPreClinit));
		return ajcPreClinit;
	}

	//

	// reflective thisJoinPoint support
	private Map<BcelShadow, Field> tjpFields = new HashMap<BcelShadow, Field>();
	Map<CacheKey, Field> annotationCachingFieldCache = new HashMap<CacheKey, Field>();
	private int tjpFieldsCounter = -1; // -1 means not yet initialized
	private int annoFieldsCounter = 0;
	public static final ObjectType proceedingTjpType = new ObjectType("org.aspectj.lang.ProceedingJoinPoint");
	public static final ObjectType tjpType = new ObjectType("org.aspectj.lang.JoinPoint");
	public static final ObjectType staticTjpType = new ObjectType("org.aspectj.lang.JoinPoint$StaticPart");
	public static final ObjectType typeForAnnotation = new ObjectType("java.lang.annotation.Annotation");
	public static final ObjectType enclosingStaticTjpType = new ObjectType("org.aspectj.lang.JoinPoint$EnclosingStaticPart");
	private static final ObjectType sigType = new ObjectType("org.aspectj.lang.Signature");
	// private static final ObjectType slType =
	// new ObjectType("org.aspectj.lang.reflect.SourceLocation");
	private static final ObjectType factoryType = new ObjectType("org.aspectj.runtime.reflect.Factory");
	private static final ObjectType classType = new ObjectType("java.lang.Class");

	public Field getTjpField(BcelShadow shadow, final boolean isEnclosingJp) {
		Field tjpField = tjpFields.get(shadow);
		if (tjpField != null) {
			return tjpField;
		}

		int modifiers = Modifier.STATIC | Modifier.FINAL ;

		// XXX - Do we ever inline before or after advice? If we do, then we
		// better include them in the check below. (or just change it to
		// shadow.getEnclosingMethod().getCanInline())

		// If the enclosing method is around advice, we could inline the join
		// point that has led to this shadow. If we do that then the TJP we are
		// creating here must be PUBLIC so it is visible to the type in which the
		// advice is inlined. (PR71377)
		LazyMethodGen encMethod = shadow.getEnclosingMethod();
		boolean shadowIsInAroundAdvice = false;
		if (encMethod != null && encMethod.getName().startsWith(NameMangler.PREFIX + "around")) {
			shadowIsInAroundAdvice = true;
		}

		if (getType().isInterface() || shadowIsInAroundAdvice) {
			modifiers |= Modifier.PUBLIC;
		} else {
			modifiers |= Modifier.PRIVATE;
		}
		ObjectType jpType = null;
		// Did not have different static joinpoint types in 1.2
		if (world.isTargettingAspectJRuntime12()) {
			jpType = staticTjpType;
		} else {
			jpType = isEnclosingJp ? enclosingStaticTjpType : staticTjpType;
		}
		if (tjpFieldsCounter == -1) {
			// not yet initialized, do it now
			if (!world.isOverWeaving()) {
				tjpFieldsCounter = 0;
			} else {
				List<BcelField> existingFields = getFieldGens();
				if (existingFields == null) {
					tjpFieldsCounter = 0;
				} else {
					BcelField lastField = null;
					// OPTIMIZE: go from last to first?
					for (BcelField field : existingFields) {
						if (field.getName().startsWith("ajc$tjp_")) {
							lastField = field;
						}
					}
					if (lastField == null) {
						tjpFieldsCounter = 0;
					} else {
						tjpFieldsCounter = Integer.parseInt(lastField.getName().substring(8)) + 1;
						// System.out.println("tjp counter starting at " + tjpFieldsCounter);
					}
				}
			}
		}
		if (!isInterface() && world.isTransientTjpFields()) {
			modifiers|=Modifier.TRANSIENT;
		}
		FieldGen fGen = new FieldGen(modifiers, jpType, "ajc$tjp_" + tjpFieldsCounter++, getConstantPool());
		addField(fGen);
		tjpField = fGen.getField();
		tjpFields.put(shadow, tjpField);
		return tjpField;
	}

	/**
	 * Create a field in the type containing the shadow where the annotation retrieved during binding can be stored - for later fast
	 * access.
	 * 
	 * @param shadow the shadow at which the @annotation result is being cached
	 * @return a field
	 */
	public Field getAnnotationCachingField(BcelShadow shadow, ResolvedType toType, boolean isWithin) {
		// Multiple annotation types at a shadow. A different field would be required for each
		CacheKey cacheKey = new CacheKey(shadow, toType, isWithin);
		Field field = annotationCachingFieldCache.get(cacheKey);
		// System.out.println(field + " for shadow " + shadow);
		if (field == null) {
			// private static Annotation ajc$anno$<nnn>
			StringBuilder sb = new StringBuilder();
			sb.append(NameMangler.ANNOTATION_CACHE_FIELD_NAME);
			sb.append(annoFieldsCounter++);
			FieldGen annotationCacheField = new FieldGen(Modifier.PRIVATE | Modifier.STATIC, typeForAnnotation, sb.toString(), cp);
			addField(annotationCacheField);
			field = annotationCacheField.getField();
			annotationCachingFieldCache.put(cacheKey, field);
		}
		return field;
	}

	static class CacheKey {
		private Object key;
		private ResolvedType annotationType;

		// If the annotation is being accessed via @annotation on a shadow then we can use the shadows toString() (so two shadows
		// the same share a variable), but if it is @withincode() or @within() we can't share them (as the shadows may look the same
		// but be occurring 'within' different things). In the within cases we continue to use the shadow itself as the key.
		CacheKey(BcelShadow shadow, ResolvedType annotationType, boolean isWithin) {
			this.key = isWithin ? shadow : shadow.toString();
			this.annotationType = annotationType;
		}

		@Override
		public int hashCode() {
			return key.hashCode() * 37 + annotationType.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (!(other instanceof CacheKey)) {
				return false;
			}
			CacheKey oCacheKey = (CacheKey) other;
			return key.equals(oCacheKey.key) && annotationType.equals(oCacheKey.annotationType);
		}
	}

	// FIXME ATAJ needed only for slow Aspects.aspectOf - keep or remove
	// private void addAjClassField() {
	// // Andy: Why build it again??
	// Field ajClassField = new FieldGen(
	// Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC,
	// classType,
	// "aj$class",
	// getConstantPool()).getField();
	// addField(ajClassField);
	//
	// InstructionList il = new InstructionList();
	// il.append(new PUSH(getConstantPool(), getClassName()));
	// il.append(fact.createInvoke("java.lang.Class", "forName", classType,
	// new Type[] {Type.STRING}, Constants.INVOKESTATIC));
	// il.append(fact.createFieldAccess(getClassName(), ajClassField.getName(),
	// classType, Constants.PUTSTATIC));
	//
	// getStaticInitializer().getBody().insert(il);
	// }

	private void addAjcInitializers() {
		if (tjpFields.size() == 0 && !serialVersionUIDRequiresInitialization) {
			return;
		}
		InstructionList[] il = null;

		if (tjpFields.size() > 0) {
			il = initializeAllTjps();
		}

		if (serialVersionUIDRequiresInitialization) {
			InstructionList[] ilSVUID = new InstructionList[1];
			ilSVUID[0] = new InstructionList();
			ilSVUID[0].append(InstructionFactory.PUSH(getConstantPool(), calculatedSerialVersionUID));
			ilSVUID[0].append(getFactory().createFieldAccess(getClassName(), "serialVersionUID", BasicType.LONG,
					Constants.PUTSTATIC));
			if (il == null) {
				il = ilSVUID;
			} else {
				InstructionList[] newIl = new InstructionList[il.length + ilSVUID.length];
				System.arraycopy(il, 0, newIl, 0, il.length);
				System.arraycopy(ilSVUID, 0, newIl, il.length, ilSVUID.length);
				il = newIl;
			}
		}

		LazyMethodGen prevMethod;
		LazyMethodGen nextMethod = null;
		if (this.isInterface()) { // Cannot sneak stuff into another static method in an interface
			prevMethod = getStaticInitializer();
		} else {
			prevMethod = getAjcPreClinit();
		}
		for (int counter = 1; counter <= il.length; counter++) {
			if (il.length > counter) {
				nextMethod = createExtendedAjcPreClinit(prevMethod, counter);
			}
			prevMethod.getBody().insert(il[counter - 1]);
			prevMethod = nextMethod;
		}
	}

	private InstructionList initInstructionList() {
		InstructionList list = new InstructionList();
		InstructionFactory fact = getFactory();

		// make a new factory
		list.append(fact.createNew(factoryType));
		list.append(InstructionFactory.createDup(1));

		list.append(InstructionFactory.PUSH(getConstantPool(), getFileName()));

		// load the current Class object
		// XXX check that this works correctly for inners/anonymous
		list.append(fact.PUSHCLASS(cp, myGen.getClassName()));
		// XXX do we need to worry about the fact the theorectically this could
		// throw
		// a ClassNotFoundException

		list.append(fact.createInvoke(factoryType.getClassName(), "<init>", Type.VOID, new Type[] { Type.STRING, classType },
				Constants.INVOKESPECIAL));

		list.append(InstructionFactory.createStore(factoryType, 0));
		return list;
	}

	private InstructionList[] initializeAllTjps() {
		Vector<InstructionList> lists = new Vector<InstructionList>();

		InstructionList list = initInstructionList();
		lists.add(list);

		List<Map.Entry<BcelShadow, Field>> entries = new ArrayList<Map.Entry<BcelShadow, Field>>(tjpFields.entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<BcelShadow, Field>>() {
			public int compare(Map.Entry<BcelShadow, Field> a, Map.Entry<BcelShadow, Field> b) {
				return (a.getValue()).getName().compareTo((b.getValue()).getName());
			}
		});

		long estimatedSize = 0;
		for (Iterator<Map.Entry<BcelShadow, Field>> i = entries.iterator(); i.hasNext();) {
			Map.Entry<BcelShadow, Field> entry = i.next();
			if (estimatedSize > Constants.MAX_CODE_SIZE) {
				estimatedSize = 0;
				list = initInstructionList();
				lists.add(list);
			}
			estimatedSize += entry.getValue().getSignature().getBytes().length;
			initializeTjp(fact, list, entry.getValue(), entry.getKey());
		}
		InstructionList listArrayModel[] = new InstructionList[1];
		return lists.toArray(listArrayModel);
	}

	private void initializeTjp(InstructionFactory fact, InstructionList list, Field field, BcelShadow shadow) {
		boolean fastSJP = false;
		// avoid fast SJP if it is for an enclosing joinpoint
		boolean isFastSJPAvailable = shadow.getWorld().isTargettingRuntime1_6_10()
				&& !enclosingStaticTjpType.equals(field.getType());

		Member sig = shadow.getSignature();

		// load the factory
		list.append(InstructionFactory.createLoad(factoryType, 0));

		// load the kind
		list.append(InstructionFactory.PUSH(getConstantPool(), shadow.getKind().getName()));

		// create the signature
		if (world.isTargettingAspectJRuntime12() || !isFastSJPAvailable || !sig.getKind().equals(Member.METHOD)) {
			list.append(InstructionFactory.createLoad(factoryType, 0));
		}

		String signatureMakerName = SignatureUtils.getSignatureMakerName(sig);
		ObjectType signatureType = new ObjectType(SignatureUtils.getSignatureType(sig));
		UnresolvedType[] exceptionTypes = null;
		if (world.isTargettingAspectJRuntime12()) { // TAG:SUPPORTING12: We
			// didn't have optimized
			// factory methods in 1.2
			list.append(InstructionFactory.PUSH(cp, SignatureUtils.getSignatureString(sig, shadow.getWorld())));
			list.append(fact.createInvoke(factoryType.getClassName(), signatureMakerName, signatureType, Type.STRINGARRAY1,
					Constants.INVOKEVIRTUAL));
		} else if (sig.getKind().equals(Member.METHOD)) {
			BcelWorld w = shadow.getWorld();

			// For methods, push the parts of the signature on.
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getModifiers(w))));
			list.append(InstructionFactory.PUSH(cp, sig.getName()));
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getDeclaringType())));
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getParameterTypes())));
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getParameterNames(w))));
			exceptionTypes = sig.getExceptions(w);
			if (isFastSJPAvailable && exceptionTypes.length == 0) {
				fastSJP = true;
			} else {
				list.append(InstructionFactory.PUSH(cp, makeString(exceptionTypes)));
			}
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getReturnType())));
			// And generate a call to the variant of makeMethodSig() that takes the strings
			if (isFastSJPAvailable) {
				fastSJP = true;
			} else {
				list.append(fact.createInvoke(factoryType.getClassName(), signatureMakerName, signatureType, Type.STRINGARRAY7,
						Constants.INVOKEVIRTUAL));
			}

		} else if (sig.getKind().equals(Member.MONITORENTER)) {
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getDeclaringType())));
			list.append(fact.createInvoke(factoryType.getClassName(), signatureMakerName, signatureType, Type.STRINGARRAY1,
					Constants.INVOKEVIRTUAL));
		} else if (sig.getKind().equals(Member.MONITOREXIT)) {
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getDeclaringType())));
			list.append(fact.createInvoke(factoryType.getClassName(), signatureMakerName, signatureType, Type.STRINGARRAY1,
					Constants.INVOKEVIRTUAL));
		} else if (sig.getKind().equals(Member.HANDLER)) {
			BcelWorld w = shadow.getWorld();
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getDeclaringType())));
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getParameterTypes())));
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getParameterNames(w))));
			list.append(fact.createInvoke(factoryType.getClassName(), signatureMakerName, signatureType, Type.STRINGARRAY3,
					Constants.INVOKEVIRTUAL));
		} else if (sig.getKind().equals(Member.CONSTRUCTOR)) {
			BcelWorld w = shadow.getWorld();
			if (w.isJoinpointArrayConstructionEnabled() && sig.getDeclaringType().isArray()) {
				// its the magical new jp
				list.append(InstructionFactory.PUSH(cp, makeString(Modifier.PUBLIC)));
				list.append(InstructionFactory.PUSH(cp, makeString(sig.getDeclaringType())));
				list.append(InstructionFactory.PUSH(cp, makeString(sig.getParameterTypes())));
				list.append(InstructionFactory.PUSH(cp, "")); // sig.getParameterNames?
				list.append(InstructionFactory.PUSH(cp, ""));// sig.getExceptions?
				list.append(fact.createInvoke(factoryType.getClassName(), signatureMakerName, signatureType, Type.STRINGARRAY5,
						Constants.INVOKEVIRTUAL));
			} else {
				list.append(InstructionFactory.PUSH(cp, makeString(sig.getModifiers(w))));
				list.append(InstructionFactory.PUSH(cp, makeString(sig.getDeclaringType())));
				list.append(InstructionFactory.PUSH(cp, makeString(sig.getParameterTypes())));
				list.append(InstructionFactory.PUSH(cp, makeString(sig.getParameterNames(w))));
				list.append(InstructionFactory.PUSH(cp, makeString(sig.getExceptions(w))));
				list.append(fact.createInvoke(factoryType.getClassName(), signatureMakerName, signatureType, Type.STRINGARRAY5,
						Constants.INVOKEVIRTUAL));
			}
		} else if (sig.getKind().equals(Member.FIELD)) {
			BcelWorld w = shadow.getWorld();
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getModifiers(w))));
			list.append(InstructionFactory.PUSH(cp, sig.getName()));
			// see pr227401
			UnresolvedType dType = sig.getDeclaringType();
			if (dType.getTypekind() == TypeKind.PARAMETERIZED || dType.getTypekind() == TypeKind.GENERIC) {
				dType = sig.getDeclaringType().resolve(world).getGenericType();
			}
			list.append(InstructionFactory.PUSH(cp, makeString(dType)));
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getReturnType())));
			list.append(fact.createInvoke(factoryType.getClassName(), signatureMakerName, signatureType, Type.STRINGARRAY4,
					Constants.INVOKEVIRTUAL));
		} else if (sig.getKind().equals(Member.ADVICE)) {
			BcelWorld w = shadow.getWorld();
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getModifiers(w))));
			list.append(InstructionFactory.PUSH(cp, sig.getName()));
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getDeclaringType())));
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getParameterTypes())));
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getParameterNames(w))));
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getExceptions(w))));
			list.append(InstructionFactory.PUSH(cp, makeString((sig.getReturnType()))));
			list.append(fact.createInvoke(factoryType.getClassName(), signatureMakerName, signatureType, new Type[] { Type.STRING,
					Type.STRING, Type.STRING, Type.STRING, Type.STRING, Type.STRING, Type.STRING }, Constants.INVOKEVIRTUAL));
		} else if (sig.getKind().equals(Member.STATIC_INITIALIZATION)) {
			BcelWorld w = shadow.getWorld();
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getModifiers(w))));
			list.append(InstructionFactory.PUSH(cp, makeString(sig.getDeclaringType())));
			list.append(fact.createInvoke(factoryType.getClassName(), signatureMakerName, signatureType, Type.STRINGARRAY2,
					Constants.INVOKEVIRTUAL));
		} else {
			list.append(InstructionFactory.PUSH(cp, SignatureUtils.getSignatureString(sig, shadow.getWorld())));
			list.append(fact.createInvoke(factoryType.getClassName(), signatureMakerName, signatureType, Type.STRINGARRAY1,
					Constants.INVOKEVIRTUAL));
		}

		// XXX should load source location from shadow
		list.append(Utility.createConstant(fact, shadow.getSourceLine()));

		final String factoryMethod;

		// TAG:SUPPORTING12: We didn't have makeESJP() in 1.2
		if (world.isTargettingAspectJRuntime12()) {
			list.append(fact.createInvoke(factoryType.getClassName(), "makeSJP", staticTjpType, new Type[] { Type.STRING, sigType,
					Type.INT }, Constants.INVOKEVIRTUAL));

			// put it in the field
			list.append(fact.createFieldAccess(getClassName(), field.getName(), staticTjpType, Constants.PUTSTATIC));

		} else {
			if (staticTjpType.equals(field.getType())) {
				factoryMethod = "makeSJP";
			} else if (enclosingStaticTjpType.equals(field.getType())) {
				factoryMethod = "makeESJP";
			} else {
				throw new Error("should not happen");
			}

			if (fastSJP) {
				if (exceptionTypes != null && exceptionTypes.length != 0) {
					list.append(fact.createInvoke(factoryType.getClassName(), factoryMethod, field.getType(), ARRAY_8STRING_INT,
							Constants.INVOKEVIRTUAL));
				} else {
					list.append(fact.createInvoke(factoryType.getClassName(), factoryMethod, field.getType(), ARRAY_7STRING_INT,
							Constants.INVOKEVIRTUAL));
				}
			} else {
				list.append(fact.createInvoke(factoryType.getClassName(), factoryMethod, field.getType(), new Type[] { Type.STRING,
						sigType, Type.INT }, Constants.INVOKEVIRTUAL));
			}

			// put it in the field
			list.append(fact.createFieldAccess(getClassName(), field.getName(), field.getType(), Constants.PUTSTATIC));
		}
	}

	private static final Type[] ARRAY_7STRING_INT = new Type[] { Type.STRING, Type.STRING, Type.STRING, Type.STRING, Type.STRING,
			Type.STRING, Type.STRING, Type.INT };
	private static final Type[] ARRAY_8STRING_INT = new Type[] { Type.STRING, Type.STRING, Type.STRING, Type.STRING, Type.STRING,
			Type.STRING, Type.STRING, Type.STRING, Type.INT };

	protected String makeString(int i) {
		return Integer.toString(i, 16); // ??? expensive
	}

	protected String makeString(UnresolvedType t) {
		// this is the inverse of the odd behavior for Class.forName w/ arrays
		if (t.isArray()) {
			// this behavior matches the string used by the eclipse compiler for
			// Foo.class literals
			return t.getSignature().replace('/', '.');
		} else {
			if (t.isParameterizedType()) {
				return t.getRawType().getName();
			} else {
				return t.getName();
			}
		}
	}

	protected String makeString(UnresolvedType[] types) {
		if (types == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		for (int i = 0, len = types.length; i < len; i++) {
			if (i > 0) {
				buf.append(':');
			}
			buf.append(makeString(types[i]));
		}
		return buf.toString();
	}

	protected String makeString(String[] names) {
		if (names == null) {
			return "";
		}
		StringBuilder buf = new StringBuilder();
		for (int i = 0, len = names.length; i < len; i++) {
			if (i > 0) {
				buf.append(':');
			}
			buf.append(names[i]);
		}
		return buf.toString();
	}

	public ResolvedType getType() {
		if (myType == null) {
			return null;
		}
		return myType.getResolvedTypeX();
	}

	public BcelObjectType getBcelObjectType() {
		return myType;
	}

	public String getFileName() {
		return myGen.getFileName();
	}

	// for *new* fields
	private void addField(FieldGen field) {
		makeSyntheticAndTransientIfNeeded(field);
		BcelField bcelField = null;
		if (getBcelObjectType() != null) {
			bcelField = new BcelField(getBcelObjectType(), field.getField());
		} else {
			bcelField = new BcelField(getName(), field.getField(), world);
		}
		fields.add(bcelField);
		// myGen.addField(field.getField());
	}

	private void makeSyntheticAndTransientIfNeeded(FieldGen field) {
		if (field.getName().startsWith(NameMangler.PREFIX) && !field.getName().startsWith("ajc$interField$")
				&& !field.getName().startsWith("ajc$instance$")) {
			// it's an aj added field
			// first do transient
			if (!field.isStatic()) {
				field.setModifiers(field.getModifiers() | Constants.ACC_TRANSIENT);
			}
			// then do synthetic
			if (getWorld().isInJava5Mode()) {
				// add the synthetic modifier flag
				field.setModifiers(field.getModifiers() | ACC_SYNTHETIC);
			}
			if (!hasSyntheticAttribute(field.getAttributes())) {
				// belt and braces, do the attribute even on Java 5 in addition
				// to the modifier flag
				// Attribute[] oldAttrs = field.getAttributes();
				// Attribute[] newAttrs = new Attribute[oldAttrs.length + 1];
				// System.arraycopy(oldAttrs, 0, newAttrs, 0, oldAttrs.length);
				ConstantPool cpg = myGen.getConstantPool();
				int index = cpg.addUtf8("Synthetic");
				Attribute synthetic = new Synthetic(index, 0, new byte[0], cpg);
				field.addAttribute(synthetic);
				// newAttrs[newAttrs.length - 1] = synthetic;
				// field.setAttributes(newAttrs);
			}
		}
	}

	private boolean hasSyntheticAttribute(List<Attribute> attributes) {
		for (int i = 0; i < attributes.size(); i++) {
			if ((attributes.get(i)).getName().equals("Synthetic")) {
				return true;
			}
		}
		return false;
	}

	public void addField(FieldGen field, ISourceLocation sourceLocation) {
		addField(field);
		if (!(field.isPrivate() && (field.isStatic() || field.isTransient()))) {
			errorOnAddedField(field, sourceLocation);
		}
	}

	public String getClassName() {
		return myGen.getClassName();
	}

	public boolean isInterface() {
		return myGen.isInterface();
	}

	public boolean isAbstract() {
		return myGen.isAbstract();
	}

	public LazyMethodGen getLazyMethodGen(Member m) {
		return getLazyMethodGen(m.getName(), m.getSignature(), false);
	}

	public LazyMethodGen getLazyMethodGen(String name, String signature) {
		return getLazyMethodGen(name, signature, false);
	}

	public LazyMethodGen getLazyMethodGen(String name, String signature, boolean allowMissing) {
		for (LazyMethodGen gen : methodGens) {
			if (gen.getName().equals(name) && gen.getSignature().equals(signature)) {
				return gen;
			}
		}

		if (!allowMissing) {
			throw new BCException("Class " + this.getName() + " does not have a method " + name + " with signature " + signature);
		}

		return null;
	}

	public void forcePublic() {
		myGen.setModifiers(Utility.makePublic(myGen.getModifiers()));
	}

	public boolean hasAnnotation(UnresolvedType t) {

		// annotations on the real thing
		AnnotationGen agens[] = myGen.getAnnotations();
		if (agens == null) {
			return false;
		}
		for (int i = 0; i < agens.length; i++) {
			AnnotationGen gen = agens[i];
			if (t.equals(UnresolvedType.forSignature(gen.getTypeSignature()))) {
				return true;
			}
		}

		// annotations added during this weave

		return false;
	}

	public void addAnnotation(AnnotationGen a) {
		if (!hasAnnotation(UnresolvedType.forSignature(a.getTypeSignature()))) {
			annotations.add(new AnnotationGen(a, getConstantPool(), true));
		}
	}
	
	public void addAttribute(AjAttribute attribute) {
		myGen.addAttribute(Utility.bcelAttribute(attribute, getConstantPool()));
	}

	// this test is like asking:
	// if
	// (UnresolvedType.SERIALIZABLE.resolve(getType().getWorld()).isAssignableFrom
	// (getType())) {
	// only we don't do that because this forces us to find all the supertypes
	// of the type,
	// and if one of them is missing we fail, and it's not worth failing just to
	// put out
	// a warning message!
	private boolean implementsSerializable(ResolvedType aType) {
		if (aType.getSignature().equals(UnresolvedType.SERIALIZABLE.getSignature())) {
			return true;
		}

		ResolvedType[] interfaces = aType.getDeclaredInterfaces();
		for (int i = 0; i < interfaces.length; i++) {
			if (interfaces[i].isMissing()) {
				continue;
			}
			if (implementsSerializable(interfaces[i])) {
				return true;
			}
		}
		ResolvedType superType = aType.getSuperclass();
		if (superType != null && !superType.isMissing()) {
			return implementsSerializable(superType);
		}
		return false;
	}

	public boolean isAtLeastJava5() {
		return (myGen.getMajor() >= Constants.MAJOR_1_5);
	}

	/**
	 * Return the next available field name with the specified 'prefix', e.g. for prefix 'class$' where class$0, class$1 exist then
	 * return class$2
	 */
	public String allocateField(String prefix) {
		int highestAllocated = -1;
		List<BcelField> fs = getFieldGens();
		for (BcelField field : fs) {
			if (field.getName().startsWith(prefix)) {
				try {
					int num = Integer.parseInt(field.getName().substring(prefix.length()));
					if (num > highestAllocated) {
						highestAllocated = num;
					}
				} catch (NumberFormatException nfe) {
					// something wrong with the number on the end of that
					// field...
				}
			}
		}
		return prefix + Integer.toString(highestAllocated + 1);
	}

}
