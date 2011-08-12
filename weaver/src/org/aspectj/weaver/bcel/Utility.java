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

package org.aspectj.weaver.bcel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Unknown;
import org.aspectj.apache.bcel.classfile.annotation.ArrayElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.SimpleElementValue;
import org.aspectj.apache.bcel.generic.ArrayType;
import org.aspectj.apache.bcel.generic.BasicType;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionByte;
import org.aspectj.apache.bcel.generic.InstructionCP;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.InstructionSelect;
import org.aspectj.apache.bcel.generic.InstructionShort;
import org.aspectj.apache.bcel.generic.InstructionTargeter;
import org.aspectj.apache.bcel.generic.LineNumberTag;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.ReferenceType;
import org.aspectj.apache.bcel.generic.SwitchBuilder;
import org.aspectj.apache.bcel.generic.TargetLostException;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ConstantPoolReader;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.Lint;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.Utils;
import org.aspectj.weaver.World;

public class Utility {

	public static List<AjAttribute> readAjAttributes(String classname, Attribute[] as, ISourceContext context, World w,
			AjAttribute.WeaverVersionInfo version, ConstantPoolReader dataDecompressor) {
		List<AjAttribute> attributes = new ArrayList<AjAttribute>();

		// first pass, look for version
		List<Unknown> forSecondPass = new ArrayList<Unknown>();
		for (int i = as.length - 1; i >= 0; i--) {
			Attribute a = as[i];
			if (a instanceof Unknown) {
				Unknown u = (Unknown) a;
				String name = u.getName();
				if (name.charAt(0) == 'o') { // 'o'rg.aspectj
					if (name.startsWith(AjAttribute.AttributePrefix)) {
						if (name.endsWith(WeaverVersionInfo.AttributeName)) {
							version = (AjAttribute.WeaverVersionInfo) AjAttribute.read(version, name, u.getBytes(), context, w,
									dataDecompressor);
							if (version.getMajorVersion() > WeaverVersionInfo.getCurrentWeaverMajorVersion()) {
								throw new BCException(
										"Unable to continue, this version of AspectJ supports classes built with weaver version "
												+ WeaverVersionInfo.toCurrentVersionString() + " but the class " + classname
												+ " is version " + version.toString() + ".  Please update your AspectJ.");
							}
						}
						forSecondPass.add(u);
					}
				}
			}
		}

		// FIXASC why going backwards? is it important
		for (int i = forSecondPass.size() - 1; i >= 0; i--) {
			Unknown a = forSecondPass.get(i);
			String name = a.getName();
			AjAttribute attr = AjAttribute.read(version, name, a.getBytes(), context, w, dataDecompressor);
			if (attr != null) {
				attributes.add(attr);
			}
		}
		return attributes;
	}

	/*
	 * Ensure we report a nice source location - particular in the case where the source info is missing (binary weave).
	 */
	public static String beautifyLocation(ISourceLocation isl) {
		StringBuffer nice = new StringBuffer();
		if (isl == null || isl.getSourceFile() == null || isl.getSourceFile().getName().indexOf("no debug info available") != -1) {
			nice.append("no debug info available");
		} else {
			// can't use File.getName() as this fails when a Linux box
			// encounters a path created on Windows and vice-versa
			int takeFrom = isl.getSourceFile().getPath().lastIndexOf('/');
			if (takeFrom == -1) {
				takeFrom = isl.getSourceFile().getPath().lastIndexOf('\\');
			}
			nice.append(isl.getSourceFile().getPath().substring(takeFrom + 1));
			if (isl.getLine() != 0) {
				nice.append(":").append(isl.getLine());
			}
		}
		return nice.toString();
	}

	public static Instruction createSuperInvoke(InstructionFactory fact, BcelWorld world, Member signature) {
		short kind;
		if (Modifier.isInterface(signature.getModifiers())) {
			throw new RuntimeException("bad");
		} else if (Modifier.isPrivate(signature.getModifiers()) || signature.getName().equals("<init>")) {
			throw new RuntimeException("unimplemented, possibly bad");
		} else if (Modifier.isStatic(signature.getModifiers())) {
			throw new RuntimeException("bad");
		} else {
			kind = Constants.INVOKESPECIAL;
		}

		return fact.createInvoke(signature.getDeclaringType().getName(), signature.getName(),
				BcelWorld.makeBcelType(signature.getReturnType()), BcelWorld.makeBcelTypes(signature.getParameterTypes()), kind);
	}

	public static Instruction createInvoke(InstructionFactory fact, BcelWorld world, Member signature) {
		short kind;
		int signatureModifiers = signature.getModifiers();
		if (Modifier.isInterface(signatureModifiers)) {
			kind = Constants.INVOKEINTERFACE;
		} else if (Modifier.isStatic(signatureModifiers)) {
			kind = Constants.INVOKESTATIC;
		} else if (Modifier.isPrivate(signatureModifiers) || signature.getName().equals("<init>")) {
			kind = Constants.INVOKESPECIAL;
		} else {
			kind = Constants.INVOKEVIRTUAL;
		}

		UnresolvedType targetType = signature.getDeclaringType();
		if (targetType.isParameterizedType()) {
			targetType = targetType.resolve(world).getGenericType();
		}
		return fact.createInvoke(targetType.getName(), signature.getName(), BcelWorld.makeBcelType(signature.getReturnType()),
				BcelWorld.makeBcelTypes(signature.getParameterTypes()), kind);
	}

	public static Instruction createGet(InstructionFactory fact, Member signature) {
		short kind;
		if (Modifier.isStatic(signature.getModifiers())) {
			kind = Constants.GETSTATIC;
		} else {
			kind = Constants.GETFIELD;
		}

		return fact.createFieldAccess(signature.getDeclaringType().getName(), signature.getName(),
				BcelWorld.makeBcelType(signature.getReturnType()), kind);
	}

	public static Instruction createSet(InstructionFactory fact, Member signature) {
		short kind;
		if (Modifier.isStatic(signature.getModifiers())) {
			kind = Constants.PUTSTATIC;
		} else {
			kind = Constants.PUTFIELD;
		}

		return fact.createFieldAccess(signature.getDeclaringType().getName(), signature.getName(),
				BcelWorld.makeBcelType(signature.getReturnType()), kind);
	}

	public static Instruction createInstanceof(InstructionFactory fact, ReferenceType t) {
		int cpoolEntry = (t instanceof ArrayType) ? fact.getConstantPool().addArrayClass((ArrayType) t) : fact.getConstantPool()
				.addClass((ObjectType) t);
		return new InstructionCP(Constants.INSTANCEOF, cpoolEntry);
	}

	public static Instruction createInvoke(InstructionFactory fact, LazyMethodGen m) {
		short kind;
		if (m.getEnclosingClass().isInterface()) {
			kind = Constants.INVOKEINTERFACE;
		} else if (m.isStatic()) {
			kind = Constants.INVOKESTATIC;
		} else if (m.isPrivate() || m.getName().equals("<init>")) {
			kind = Constants.INVOKESPECIAL;
		} else {
			kind = Constants.INVOKEVIRTUAL;
		}

		return fact.createInvoke(m.getClassName(), m.getName(), m.getReturnType(), m.getArgumentTypes(), kind);
	}

	/**
	 * Create an invoke instruction
	 * 
	 * @param fact
	 * @param kind INVOKEINTERFACE, INVOKEVIRTUAL..
	 * @param member
	 * @return
	 */
	public static Instruction createInvoke(InstructionFactory fact, short kind, Member member) {
		return fact.createInvoke(member.getDeclaringType().getName(), member.getName(),
				BcelWorld.makeBcelType(member.getReturnType()), BcelWorld.makeBcelTypes(member.getParameterTypes()), kind);
	}

	private static String[] argNames = new String[] { "arg0", "arg1", "arg2", "arg3", "arg4" };

	// ??? these should perhaps be cached. Remember to profile this to see if
	// it's a problem.
	public static String[] makeArgNames(int n) {
		String[] ret = new String[n];
		for (int i = 0; i < n; i++) {
			if (i < 5) {
				ret[i] = argNames[i];
			} else {
				ret[i] = "arg" + i;
			}
		}
		return ret;
	}

	// Lookup table, for converting between pairs of types, it gives
	// us the method name in the Conversions class
	private static Hashtable<String, String> validBoxing = new Hashtable<String, String>();

	static {
		validBoxing.put("Ljava/lang/Byte;B", "byteObject");
		validBoxing.put("Ljava/lang/Character;C", "charObject");
		validBoxing.put("Ljava/lang/Double;D", "doubleObject");
		validBoxing.put("Ljava/lang/Float;F", "floatObject");
		validBoxing.put("Ljava/lang/Integer;I", "intObject");
		validBoxing.put("Ljava/lang/Long;J", "longObject");
		validBoxing.put("Ljava/lang/Short;S", "shortObject");
		validBoxing.put("Ljava/lang/Boolean;Z", "booleanObject");
		validBoxing.put("BLjava/lang/Byte;", "byteValue");
		validBoxing.put("CLjava/lang/Character;", "charValue");
		validBoxing.put("DLjava/lang/Double;", "doubleValue");
		validBoxing.put("FLjava/lang/Float;", "floatValue");
		validBoxing.put("ILjava/lang/Integer;", "intValue");
		validBoxing.put("JLjava/lang/Long;", "longValue");
		validBoxing.put("SLjava/lang/Short;", "shortValue");
		validBoxing.put("ZLjava/lang/Boolean;", "booleanValue");
	}

	public static void appendConversion(InstructionList il, InstructionFactory fact, ResolvedType fromType, ResolvedType toType) {
		if (!toType.isConvertableFrom(fromType) && !fromType.isConvertableFrom(toType)) {
			throw new BCException("can't convert from " + fromType + " to " + toType);
		}
		// XXX I'm sure this test can be simpler but my brain hurts and this works
		World w = toType.getWorld();
		if (w == null) { // dbg349636
			throw new IllegalStateException("Debug349636: Unexpectedly found world null for type " + toType.getName());
		}

		if (!w.isInJava5Mode()) {
			if (toType.needsNoConversionFrom(fromType)) {
				return;
			}
		} else {
			if (toType.needsNoConversionFrom(fromType) && !(toType.isPrimitiveType() ^ fromType.isPrimitiveType())) {
				return;
			}
		}
		if (toType.equals(UnresolvedType.VOID)) {
			// assert fromType.equals(UnresolvedType.OBJECT)
			il.append(InstructionFactory.createPop(fromType.getSize()));
		} else if (fromType.equals(UnresolvedType.VOID)) {
			// assert toType.equals(UnresolvedType.OBJECT)
			il.append(InstructionFactory.createNull(Type.OBJECT));
			return;
		} else if (fromType.equals(UnresolvedType.OBJECT)) {
			Type to = BcelWorld.makeBcelType(toType);
			if (toType.isPrimitiveType()) {
				String name = toType.toString() + "Value";
				il.append(fact.createInvoke("org.aspectj.runtime.internal.Conversions", name, to, new Type[] { Type.OBJECT },
						Constants.INVOKESTATIC));
			} else {
				il.append(fact.createCheckCast((ReferenceType) to));
			}
		} else if (toType.equals(UnresolvedType.OBJECT)) {
			// assert fromType.isPrimitive()
			Type from = BcelWorld.makeBcelType(fromType);
			String name = fromType.toString() + "Object";
			il.append(fact.createInvoke("org.aspectj.runtime.internal.Conversions", name, Type.OBJECT, new Type[] { from },
					Constants.INVOKESTATIC));
		} else if (toType.getWorld().isInJava5Mode() && validBoxing.get(toType.getSignature() + fromType.getSignature()) != null) {
			// XXX could optimize by using any java boxing code that may be just
			// before the call...
			Type from = BcelWorld.makeBcelType(fromType);
			Type to = BcelWorld.makeBcelType(toType);
			String name = validBoxing.get(toType.getSignature() + fromType.getSignature());
			if (toType.isPrimitiveType()) {
				il.append(fact.createInvoke("org.aspectj.runtime.internal.Conversions", name, to, new Type[] { Type.OBJECT },
						Constants.INVOKESTATIC));
			} else {
				il.append(fact.createInvoke("org.aspectj.runtime.internal.Conversions", name, Type.OBJECT, new Type[] { from },
						Constants.INVOKESTATIC));
				il.append(fact.createCheckCast((ReferenceType) to));
			}
		} else if (fromType.isPrimitiveType()) {
			// assert toType.isPrimitive()
			Type from = BcelWorld.makeBcelType(fromType);
			Type to = BcelWorld.makeBcelType(toType);
			try {
				Instruction i = fact.createCast(from, to);
				if (i != null) {
					il.append(i);
				} else {
					il.append(fact.createCast(from, Type.INT));
					il.append(fact.createCast(Type.INT, to));
				}
			} catch (RuntimeException e) {
				il.append(fact.createCast(from, Type.INT));
				il.append(fact.createCast(Type.INT, to));
			}
		} else {
			Type to = BcelWorld.makeBcelType(toType);
			// assert ! fromType.isPrimitive() && ! toType.isPrimitive()
			il.append(fact.createCheckCast((ReferenceType) to));
		}
	}

	public static InstructionList createConversion(InstructionFactory factory, Type fromType, Type toType) {
		return createConversion(factory, fromType, toType, false);
	}

	public static InstructionList createConversion(InstructionFactory fact, Type fromType, Type toType, boolean allowAutoboxing) {
		// System.out.println("cast to: " + toType);

		InstructionList il = new InstructionList();

		// PR71273
		if ((fromType.equals(Type.BYTE) || fromType.equals(Type.CHAR) || fromType.equals(Type.SHORT)) && (toType.equals(Type.INT))) {
			return il;
		}

		if (fromType.equals(toType)) {
			return il;
		}
		if (toType.equals(Type.VOID)) {
			il.append(InstructionFactory.createPop(fromType.getSize()));
			return il;
		}

		if (fromType.equals(Type.VOID)) {
			if (toType instanceof BasicType) {
				throw new BCException("attempting to cast from void to basic type");
			}
			il.append(InstructionFactory.createNull(Type.OBJECT));
			return il;
		}

		if (fromType.equals(Type.OBJECT)) {
			if (toType instanceof BasicType) {
				String name = toType.toString() + "Value";
				il.append(fact.createInvoke("org.aspectj.runtime.internal.Conversions", name, toType, new Type[] { Type.OBJECT },
						Constants.INVOKESTATIC));
				return il;
			}
		}

		if (toType.equals(Type.OBJECT)) {
			if (fromType instanceof BasicType) {
				String name = fromType.toString() + "Object";
				il.append(fact.createInvoke("org.aspectj.runtime.internal.Conversions", name, Type.OBJECT, new Type[] { fromType },
						Constants.INVOKESTATIC));
				return il;
			} else if (fromType instanceof ReferenceType) {
				return il;
			} else {
				throw new RuntimeException();
			}
		}

		if (fromType instanceof ReferenceType && ((ReferenceType) fromType).isAssignmentCompatibleWith(toType)) {
			return il;
		}

		if (allowAutoboxing) {
			if (toType instanceof BasicType && fromType instanceof ReferenceType) {
				// unboxing
				String name = toType.toString() + "Value";
				il.append(fact.createInvoke("org.aspectj.runtime.internal.Conversions", name, toType, new Type[] { Type.OBJECT },
						Constants.INVOKESTATIC));
				return il;
			}

			if (fromType instanceof BasicType && toType instanceof ReferenceType) {
				// boxing
				String name = fromType.toString() + "Object";
				il.append(fact.createInvoke("org.aspectj.runtime.internal.Conversions", name, Type.OBJECT, new Type[] { fromType },
						Constants.INVOKESTATIC));
				il.append(fact.createCast(Type.OBJECT, toType));
				return il;
			}
		}

		il.append(fact.createCast(fromType, toType));
		return il;
	}

	public static Instruction createConstant(InstructionFactory fact, int value) {
		Instruction inst;
		switch (value) {
		case -1:
			inst = InstructionConstants.ICONST_M1;
			break;
		case 0:
			inst = InstructionConstants.ICONST_0;
			break;
		case 1:
			inst = InstructionConstants.ICONST_1;
			break;
		case 2:
			inst = InstructionConstants.ICONST_2;
			break;
		case 3:
			inst = InstructionConstants.ICONST_3;
			break;
		case 4:
			inst = InstructionConstants.ICONST_4;
			break;
		case 5:
			inst = InstructionConstants.ICONST_5;
			break;
		default:
			if (value <= Byte.MAX_VALUE && value >= Byte.MIN_VALUE) {
				inst = new InstructionByte(Constants.BIPUSH, (byte) value);
			} else if (value <= Short.MAX_VALUE && value >= Short.MIN_VALUE) {
				inst = new InstructionShort(Constants.SIPUSH, (short) value);
			} else {
				int ii = fact.getClassGen().getConstantPool().addInteger(value);
				inst = new InstructionCP(value <= Constants.MAX_BYTE ? Constants.LDC : Constants.LDC_W, ii);
			}
			break;
		}
		return inst;
	}

	/** For testing purposes: bit clunky but does work */
	public static int testingParseCounter = 0;

	public static JavaClass makeJavaClass(String filename, byte[] bytes) {
		try {
			testingParseCounter++;
			ClassParser parser = new ClassParser(new ByteArrayInputStream(bytes), filename);
			return parser.parse();
		} catch (IOException e) {
			throw new BCException("malformed class file");
		}
	}

	/**
	 * replace an instruction handle with another instruction, in this case, a branch instruction.
	 * 
	 * @param ih the instruction handle to replace.
	 * @param branchInstruction the branch instruction to replace ih with
	 * @param enclosingMethod where to find ih's instruction list.
	 */
	public static void replaceInstruction(InstructionHandle ih, InstructionList replacementInstructions,
			LazyMethodGen enclosingMethod) {
		InstructionList il = enclosingMethod.getBody();
		InstructionHandle fresh = il.append(ih, replacementInstructions);
		deleteInstruction(ih, fresh, enclosingMethod);
	}

	/**
	 * delete an instruction handle and retarget all targeters of the deleted instruction to the next instruction. Obviously, this
	 * should not be used to delete a control transfer instruction unless you know what you're doing.
	 * 
	 * @param ih the instruction handle to delete.
	 * @param enclosingMethod where to find ih's instruction list.
	 */
	public static void deleteInstruction(InstructionHandle ih, LazyMethodGen enclosingMethod) {
		deleteInstruction(ih, ih.getNext(), enclosingMethod);
	}

	/**
	 * delete an instruction handle and retarget all targeters of the deleted instruction to the provided target.
	 * 
	 * @param ih the instruction handle to delete
	 * @param retargetTo the instruction handle to retarget targeters of ih to.
	 * @param enclosingMethod where to find ih's instruction list.
	 */
	public static void deleteInstruction(InstructionHandle ih, InstructionHandle retargetTo, LazyMethodGen enclosingMethod) {
		InstructionList il = enclosingMethod.getBody();
		for (InstructionTargeter targeter : ih.getTargetersCopy()) {
			targeter.updateTarget(ih, retargetTo);
		}
		ih.removeAllTargeters();
		try {
			il.delete(ih);
		} catch (TargetLostException e) {
			throw new BCException("this really can't happen");
		}
	}

	/**
	 * Fix for Bugzilla #39479, #40109 patch contributed by Andy Clement
	 * 
	 * Need to manually copy Select instructions - if we rely on the the 'fresh' object created by copy(), the InstructionHandle
	 * array 'targets' inside the Select object will not have been deep copied, so modifying targets in fresh will modify the
	 * original Select - not what we want ! (It is a bug in BCEL to do with cloning Select objects).
	 * 
	 * <pre>
	 * declare error:
	 *     call(* Instruction.copy()) &amp;&amp; within(org.aspectj.weaver)
	 *       &amp;&amp; !withincode(* Utility.copyInstruction(Instruction)):
	 *     &quot;use Utility.copyInstruction to work-around bug in Select.copy()&quot;;
	 * </pre>
	 */
	public static Instruction copyInstruction(Instruction i) {
		if (i instanceof InstructionSelect) {
			InstructionSelect freshSelect = (InstructionSelect) i;

			// Create a new targets array that looks just like the existing one
			InstructionHandle[] targets = new InstructionHandle[freshSelect.getTargets().length];
			for (int ii = 0; ii < targets.length; ii++) {
				targets[ii] = freshSelect.getTargets()[ii];
			}

			// Create a new select statement with the new targets array

			return new SwitchBuilder(freshSelect.getMatchs(), targets, freshSelect.getTarget()).getInstruction();
		} else {
			return i.copy(); // Use clone for shallow copy...
		}
	}

	/** returns -1 if no source line attribute */
	// this naive version overruns the JVM stack size, if only Java understood
	// tail recursion...
	// public static int getSourceLine(InstructionHandle ih) {
	// if (ih == null) return -1;
	//
	// InstructionTargeter[] ts = ih.getTargeters();
	// if (ts != null) {
	// for (int j = ts.length - 1; j >= 0; j--) {
	// InstructionTargeter t = ts[j];
	// if (t instanceof LineNumberTag) {
	// return ((LineNumberTag)t).getLineNumber();
	// }
	// }
	// }
	// return getSourceLine(ih.getNext());
	// }
	public static int getSourceLine(InstructionHandle ih) {// ,boolean
		// goforwards) {
		int lookahead = 0;
		// arbitrary rule that we will never lookahead more than 100
		// instructions for a line #
		while (lookahead++ < 100) {
			if (ih == null) {
				return -1;
			}
			Iterator<InstructionTargeter> tIter = ih.getTargeters().iterator();
			while (tIter.hasNext()) {
				InstructionTargeter t = tIter.next();
				if (t instanceof LineNumberTag) {
					return ((LineNumberTag) t).getLineNumber();
				}
			}
			// if (goforwards) ih=ih.getNext(); else
			ih = ih.getPrev();
		}
		// System.err.println("no line information available for: " + ih);
		return -1;
	}

	// public static int getSourceLine(InstructionHandle ih) {
	// return getSourceLine(ih,false);
	// }

	// assumes that there is no already extant source line tag. Otherwise we'll
	// have to be better.
	public static void setSourceLine(InstructionHandle ih, int lineNumber) {
		// OPTIMIZE LineNumberTag instances for the same line could be shared
		// throughout a method...
		ih.addTargeter(new LineNumberTag(lineNumber));
	}

	public static int makePublic(int i) {
		return i & ~(Modifier.PROTECTED | Modifier.PRIVATE) | Modifier.PUBLIC;
	}

	public static BcelVar[] pushAndReturnArrayOfVars(ResolvedType[] proceedParamTypes, InstructionList il, InstructionFactory fact,
			LazyMethodGen enclosingMethod) {
		int len = proceedParamTypes.length;
		BcelVar[] ret = new BcelVar[len];

		for (int i = len - 1; i >= 0; i--) {
			ResolvedType typeX = proceedParamTypes[i];
			Type type = BcelWorld.makeBcelType(typeX);
			int local = enclosingMethod.allocateLocal(type);

			il.append(InstructionFactory.createStore(type, local));
			ret[i] = new BcelVar(typeX, local);
		}
		return ret;
	}

	public static boolean isConstantPushInstruction(Instruction i) {
		long ii = Constants.instFlags[i.opcode];
		return ((ii & Constants.PUSH_INST) != 0 && (ii & Constants.CONSTANT_INST) != 0);
	}

	/**
	 * Checks for suppression specified on the member or on the declaring type of that member
	 */
	public static boolean isSuppressing(Member member, String lintkey) {
		boolean isSuppressing = Utils.isSuppressing(member.getAnnotations(), lintkey);
		if (isSuppressing) {
			return true;
		}
		UnresolvedType type = member.getDeclaringType();
		if (type instanceof ResolvedType) {
			return Utils.isSuppressing(((ResolvedType) type).getAnnotations(), lintkey);
		}
		return false;
	}

	public static List<Lint.Kind> getSuppressedWarnings(AnnotationAJ[] anns, Lint lint) {
		if (anns == null) {
			return Collections.emptyList();
		}
		// Go through the annotation types
		List<Lint.Kind> suppressedWarnings = new ArrayList<Lint.Kind>();
		boolean found = false;
		for (int i = 0; !found && i < anns.length; i++) {
			// Check for the SuppressAjWarnings annotation
			if (UnresolvedType.SUPPRESS_AJ_WARNINGS.getSignature().equals(
					((BcelAnnotation) anns[i]).getBcelAnnotation().getTypeSignature())) {
				found = true;
				// Two possibilities:
				// 1. there are no values specified (i.e. @SuppressAjWarnings)
				// 2. there are values specified (i.e. @SuppressAjWarnings("A")
				// or @SuppressAjWarnings({"A","B"})
				List<NameValuePair> vals = ((BcelAnnotation) anns[i]).getBcelAnnotation().getValues();
				if (vals == null || vals.isEmpty()) { // (1)
					suppressedWarnings.addAll(lint.allKinds());
				} else { // (2)
					// We know the value is an array value
					ArrayElementValue array = (ArrayElementValue) (vals.get(0)).getValue();
					ElementValue[] values = array.getElementValuesArray();
					for (int j = 0; j < values.length; j++) {
						// We know values in the array are strings
						SimpleElementValue value = (SimpleElementValue) values[j];
						Lint.Kind lintKind = lint.getLintKind(value.getValueString());
						if (lintKind != null) {
							suppressedWarnings.add(lintKind);
						}
					}
				}
			}
		}
		return suppressedWarnings;
	}

	// not yet used...
	// public static boolean isSimple(Method method) {
	// if (method.getCode()==null) return true;
	// if (method.getCode().getCode().length>10) return false;
	// InstructionList instrucs = new
	// InstructionList(method.getCode().getCode()); // expensive!
	// InstructionHandle InstrHandle = instrucs.getStart();
	// while (InstrHandle != null) {
	// Instruction Instr = InstrHandle.getInstruction();
	// int opCode = Instr.opcode;
	// // if current instruction is a branch instruction, see if it's a backward
	// branch.
	// // if it is return immediately (can't be trivial)
	// if (Instr instanceof InstructionBranch) {
	// // InstructionBranch BI = (InstructionBranch) Instr;
	// if (Instr.getIndex() < 0) return false;
	// } else if (Instr instanceof InvokeInstruction) {
	// // if current instruction is an invocation, indicate that it can't be
	// trivial
	// return false;
	// }
	// InstrHandle = InstrHandle.getNext();
	// }
	// return true;
	// }

	public static Attribute bcelAttribute(AjAttribute a, ConstantPool pool) {
		int nameIndex = pool.addUtf8(a.getNameString());
		byte[] bytes = a.getBytes(new BcelConstantPoolWriter(pool));
		int length = bytes.length;

		return new Unknown(nameIndex, length, bytes, pool);
	}
}