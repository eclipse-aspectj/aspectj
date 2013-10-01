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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Synthetic;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.generic.BranchHandle;
import org.aspectj.apache.bcel.generic.ClassGenException;
import org.aspectj.apache.bcel.generic.CodeExceptionGen;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionBranch;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.InstructionSelect;
import org.aspectj.apache.bcel.generic.InstructionTargeter;
import org.aspectj.apache.bcel.generic.LineNumberTag;
import org.aspectj.apache.bcel.generic.LocalVariableTag;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Tag;
import org.aspectj.apache.bcel.generic.TargetLostException;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.MemberImpl;
import org.aspectj.weaver.NameMangler;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.tools.Traceable;

/**
 * A LazyMethodGen should be treated as a MethodGen. It's our way of abstracting over the low-level Method objects. It converts
 * through {@link MethodGen} to create and to serialize, but that's it.
 * 
 * <p>
 * At any rate, there are two ways to create LazyMethodGens. One is from a method, which does work through MethodGen to do the
 * correct thing. The other is the creation of a completely empty LazyMethodGen, and it is used when we're constructing code from
 * scratch.
 * 
 * <p>
 * We stay away from targeters for rangey things like Shadows and Exceptions.
 */
public final class LazyMethodGen implements Traceable {
	private static final int ACC_SYNTHETIC = 0x1000;

	private int modifiers;
	private Type returnType;
	private final String name;
	private Type[] argumentTypes;
	// private final String[] argumentNames;
	private String[] declaredExceptions;
	private InstructionList body;
	private List<Attribute> attributes;
	private List<AnnotationAJ> newAnnotations;
	private List<ResolvedType> annotationsForRemoval;
	private AnnotationAJ[][] newParameterAnnotations;
	private final LazyClassGen enclosingClass;
	private BcelMethod memberView;
	private AjAttribute.EffectiveSignatureAttribute effectiveSignature;
	int highestLineNumber = 0;
	boolean wasPackedOptimally = false;
	private Method savedMethod = null;
	private static final AnnotationAJ[] NO_ANNOTATIONAJ = new AnnotationAJ[] {};

	/*
	 * We use LineNumberTags and not Gens.
	 * 
	 * This option specifies whether we let the BCEL classes create LineNumberGens and LocalVariableGens or if we make it create
	 * LineNumberTags and LocalVariableTags. Up until 1.5.1 we always created Gens - then on return from the MethodGen ctor we took
	 * them apart, reprocessed them all and created Tags. (see unpackLocals/unpackLineNumbers). As we have our own copy of Bcel, why
	 * not create the right thing straightaway? So setting this to true will call the MethodGen ctor() in such a way that it creates
	 * Tags - removing the need for unpackLocals/unpackLineNumbers - HOWEVER see the ensureAllLineNumberSetup() method for some
	 * other relevant info.
	 * 
	 * Whats the difference between a Tag and a Gen? A Tag is more lightweight, it doesn't know which instructions it targets, it
	 * relies on the instructions targettingit - this reduces the amount of targeter manipulation we have to do.
	 */

	/**
	 * This is nonnull if this method is the result of an "inlining". We currently copy methods into other classes for around
	 * advice. We add this field so we can get JSR45 information correct. If/when we do _actual_ inlining, we'll need to subtype
	 * LineNumberTag to have external line numbers.
	 */
	String fromFilename = null;
	private int maxLocals;
	private boolean canInline = true;
	private boolean isSynthetic = false;
	List<BcelShadow> matchedShadows;
	// Used for interface introduction - this is the type of the interface the method is technically on
	public ResolvedType definingType = null;
	
	static class LightweightBcelMethod extends BcelMethod {

		LightweightBcelMethod(BcelObjectType declaringType, Method method) {
			super(declaringType, method);
			// TODO Auto-generated constructor stub
		}
		
	}

	public LazyMethodGen(int modifiers, Type returnType, String name, Type[] paramTypes, String[] declaredExceptions,
			LazyClassGen enclosingClass) {
		// enclosingClass.getName() + ", " + returnType);
		this.memberView = null; // should be okay, since constructed ones aren't woven into
		this.modifiers = modifiers;
		this.returnType = returnType;
		this.name = name;
		this.argumentTypes = paramTypes;
		// this.argumentNames = Utility.makeArgNames(paramTypes.length);
		this.declaredExceptions = declaredExceptions;
		if (!Modifier.isAbstract(modifiers)) {
			body = new InstructionList();
			setMaxLocals(calculateMaxLocals());
		} else {
			body = null;
		}
		this.attributes = new ArrayList<Attribute>();
		this.enclosingClass = enclosingClass;
		assertGoodBody();

		// @AJ advice are not inlined by default since requires further analysis and weaving ordering control
		// TODO AV - improve - note: no room for improvement as long as aspects are reweavable
		// since the inlined version with wrappers and an to be done annotation to keep
		// inline state will be garbaged due to reweavable impl
		if (memberView != null && isAdviceMethod()) {
			if (enclosingClass.getType().isAnnotationStyleAspect()) {
				// TODO we could check for @Around advice as well
				this.canInline = false;
			}
		}
	}

	private int calculateMaxLocals() {
		int ret = Modifier.isStatic(modifiers) ? 0 : 1; // will there be a 'this'?
		for (Type type : argumentTypes) {
			ret += type.getSize();
		}
		return ret;
	}

	// build from an existing method, lazy build saves most work for
	// initialization
	public LazyMethodGen(Method m, LazyClassGen enclosingClass) {
		savedMethod = m;

		this.enclosingClass = enclosingClass;
		if (!(m.isAbstract() || m.isNative()) && m.getCode() == null) {
			throw new RuntimeException("bad non-abstract method with no code: " + m + " on " + enclosingClass);
		}
		if ((m.isAbstract() || m.isNative()) && m.getCode() != null) {
			throw new RuntimeException("bad abstract method with code: " + m + " on " + enclosingClass);
		}
		this.memberView = new BcelMethod(enclosingClass.getBcelObjectType(), m);

		this.modifiers = m.getModifiers();
		this.name = m.getName();

		// @AJ advice are not inlined by default since requires further analysis
		// and weaving ordering control
		// TODO AV - improve - note: no room for improvement as long as aspects
		// are reweavable
		// since the inlined version with wrappers and an to be done annotation
		// to keep
		// inline state will be garbaged due to reweavable impl
		if (memberView != null && isAdviceMethod()) {
			if (enclosingClass.getType().isAnnotationStyleAspect()) {
				// TODO we could check for @Around advice as well
				this.canInline = false;
			}
		}
	}

	private boolean isAbstractOrNative(int modifiers) {
		return Modifier.isAbstract(modifiers) || Modifier.isNative(modifiers);
	}

	public LazyMethodGen(BcelMethod m, LazyClassGen enclosingClass) {
		savedMethod = m.getMethod();
		this.enclosingClass = enclosingClass;
		if (!isAbstractOrNative(m.getModifiers()) && savedMethod.getCode() == null) {
			throw new RuntimeException("bad non-abstract method with no code: " + m + " on " + enclosingClass);
		}
		if (isAbstractOrNative(m.getModifiers()) && savedMethod.getCode() != null) {
			throw new RuntimeException("bad abstract method with code: " + m + " on " + enclosingClass);
		}
		// this.memberView = new BcelMethod(enclosingClass.getBcelObjectType(),
		// m);
		this.memberView = m;
		this.modifiers = savedMethod.getModifiers();
		this.name = m.getName();

		// @AJ advice are not inlined by default since requires further analysis
		// and weaving ordering control
		// TODO AV - improve - note: no room for improvement as long as aspects
		// are reweavable
		// since the inlined version with wrappers and an to be done annotation
		// to keep
		// inline state will be garbaged due to reweavable impl
		if (memberView != null && isAdviceMethod()) {
			if (enclosingClass.getType().isAnnotationStyleAspect()) {
				// TODO we could check for @Around advice as well
				this.canInline = false;
			}
		}

	}

	public boolean hasDeclaredLineNumberInfo() {
		return (memberView != null && memberView.hasDeclarationLineNumberInfo());
	}

	public int getDeclarationLineNumber() {
		if (hasDeclaredLineNumberInfo()) {
			return memberView.getDeclarationLineNumber();
		} else {
			return -1;
		}
	}

	public int getDeclarationOffset() {
		if (hasDeclaredLineNumberInfo()) {
			return memberView.getDeclarationOffset();
		} else {
			return 0;
		}
	}

	public void addAnnotation(AnnotationAJ ax) {
		initialize();
		if (memberView == null) {
			// If member view is null, we manage them in newAnnotations
			if (newAnnotations == null) {
				newAnnotations = new ArrayList<AnnotationAJ>();
			}
			newAnnotations.add(ax);
		} else {
			memberView.addAnnotation(ax);
		}
	}

	public void removeAnnotation(ResolvedType annotationType) {
		initialize();
		if (memberView == null) {
			// If member view is null, we manage them in newAnnotations
			if (annotationsForRemoval == null) {
				annotationsForRemoval = new ArrayList<ResolvedType>();
			}
			annotationsForRemoval.add(annotationType);
		} else {
			memberView.removeAnnotation(annotationType);
		}
	}

	public void addParameterAnnotation(int parameterNumber, AnnotationAJ anno) {
		initialize();
		if (memberView == null) {
			if (newParameterAnnotations == null) {
				// time to create it
				int pcount = getArgumentTypes().length;
				newParameterAnnotations = new AnnotationAJ[pcount][];
				for (int i = 0; i < pcount; i++) {
					if (i == parameterNumber) {
						newParameterAnnotations[i] = new AnnotationAJ[1];
						newParameterAnnotations[i][0] = anno;
					} else {
						newParameterAnnotations[i] = NO_ANNOTATIONAJ;
					}
				}
			} else {
				AnnotationAJ[] currentAnnoArray = newParameterAnnotations[parameterNumber];
				AnnotationAJ[] newAnnoArray = new AnnotationAJ[currentAnnoArray.length + 1];
				System.arraycopy(currentAnnoArray, 0, newAnnoArray, 0, currentAnnoArray.length);
				newAnnoArray[currentAnnoArray.length] = anno;
				newParameterAnnotations[parameterNumber] = newAnnoArray;
			}
		} else {
			memberView.addParameterAnnotation(parameterNumber, anno);
		}
	}
	
	public ResolvedType[] getAnnotationTypes() {
		initialize();
		if (memberView == null && newAnnotations!=null && newAnnotations.size()!=0) {
			// TODO Ignoring removed annotations for now
			ResolvedType[] annotationTypes = new ResolvedType[newAnnotations.size()];
			for (int a=0,len=newAnnotations.size();a<len;a++) {
				annotationTypes[a] = newAnnotations.get(a).getType();
			}
			return annotationTypes;
		}
		return null;
	}
	
	public AnnotationAJ[] getAnnotations() {
		initialize();
		if (memberView == null && newAnnotations!=null && newAnnotations.size()!=0) {
			return newAnnotations.toArray(new AnnotationAJ[newAnnotations.size()]);
		}
		return null;
	}

	public boolean hasAnnotation(UnresolvedType annotationType) {
		initialize();
		if (memberView == null) {
			if (annotationsForRemoval != null) {
				for (ResolvedType at : annotationsForRemoval) {
					if (at.equals(annotationType)) {
						return false;
					}
				}
			}
			// Check local annotations first
			if (newAnnotations != null) {
				for (AnnotationAJ annotation : newAnnotations) {
					if (annotation.getTypeSignature().equals(annotationType.getSignature())) {
						return true;
					}
				}
			}
			memberView = new BcelMethod(getEnclosingClass().getBcelObjectType(), getMethod());
			return memberView.hasAnnotation(annotationType);
		}
		return memberView.hasAnnotation(annotationType);
	}

	private void initialize() {
		if (returnType != null) {
			return;
		}
		MethodGen gen = new MethodGen(savedMethod, enclosingClass.getName(), enclosingClass.getConstantPool(), true);

		this.returnType = gen.getReturnType();
		this.argumentTypes = gen.getArgumentTypes();
		this.declaredExceptions = gen.getExceptions();
		this.attributes = gen.getAttributes();
		// this.annotations = gen.getAnnotations();
		this.maxLocals = gen.getMaxLocals();

		// this.returnType = BcelWorld.makeBcelType(memberView.getReturnType());
		// this.argumentTypes =
		// BcelWorld.makeBcelTypes(memberView.getParameterTypes());
		//
		// this.declaredExceptions =
		// UnresolvedType.getNames(memberView.getExceptions());
		// //gen.getExceptions();
		// this.attributes = new Attribute[0]; //gen.getAttributes();
		// this.maxLocals = savedMethod.getCode().getMaxLocals();

		if (gen.isAbstract() || gen.isNative()) {
			body = null;
		} else {
			// body = new InstructionList(savedMethod.getCode().getCode());
			body = gen.getInstructionList();
			unpackHandlers(gen);
			ensureAllLineNumberSetup();
			highestLineNumber = gen.getHighestlinenumber();
		}
		assertGoodBody();
	}

	// XXX we're relying on the javac promise I've just made up that we won't
	// have an early exception
	// in the list mask a later exception: That is, for two exceptions E and F,
	// if E preceeds F, then either E \cup F = {}, or E \nonstrictsubset F. So
	// when we add F,
	// we add it on the _OUTSIDE_ of any handlers that share starts or ends with
	// it.

	// with that in mind, we merrily go adding ranges for exceptions.

	private void unpackHandlers(MethodGen gen) {
		CodeExceptionGen[] exns = gen.getExceptionHandlers();
		if (exns != null) {
			int len = exns.length;
			// if (len > 0) hasExceptionHandlers = true;
			int priority = len - 1;
			for (int i = 0; i < len; i++, priority--) {
				CodeExceptionGen exn = exns[i];

				InstructionHandle start = Range.genStart(body, getOutermostExceptionStart(exn.getStartPC()));
				InstructionHandle end = Range.genEnd(body, getOutermostExceptionEnd(exn.getEndPC()));
				// this doesn't necessarily handle overlapping correctly!!!
				ExceptionRange er = new ExceptionRange(body, exn.getCatchType() == null ? null : BcelWorld.fromBcel(exn
						.getCatchType()), priority);
				er.associateWithTargets(start, end, exn.getHandlerPC());
				exn.setStartPC(null); // also removes from target
				exn.setEndPC(null); // also removes from target
				exn.setHandlerPC(null); // also removes from target
			}
			gen.removeExceptionHandlers();
		}
	}

	private InstructionHandle getOutermostExceptionStart(InstructionHandle ih) {
		while (true) {
			if (ExceptionRange.isExceptionStart(ih.getPrev())) {
				ih = ih.getPrev();
			} else {
				return ih;
			}
		}
	}

	private InstructionHandle getOutermostExceptionEnd(InstructionHandle ih) {
		while (true) {
			if (ExceptionRange.isExceptionEnd(ih.getNext())) {
				ih = ih.getNext();
			} else {
				return ih;
			}
		}
	}

	/**
	 * On entry to this method we have a method whose instruction stream contains a few instructions that have line numbers assigned
	 * to them (LineNumberTags). The aim is to ensure every instruction has the right line number. This is necessary because some of
	 * them may be extracted out into other methods - and it'd be useful for them to maintain the source line number for debugging.
	 */
	public void ensureAllLineNumberSetup() {
		LineNumberTag lastKnownLineNumberTag = null;
		boolean skip = false;
		for (InstructionHandle ih = body.getStart(); ih != null; ih = ih.getNext()) {
			skip = false;
			for (InstructionTargeter targeter : ih.getTargeters()) {
				if (targeter instanceof LineNumberTag) {
					lastKnownLineNumberTag = (LineNumberTag) targeter;
					skip = true;
				}
			}
			if (lastKnownLineNumberTag != null && !skip) {
				ih.addTargeter(lastKnownLineNumberTag);
			}
		}
	}

	// ===============

	public int allocateLocal(Type type) {
		return allocateLocal(type.getSize());
	}

	public int allocateLocal(int slots) {
		int max = getMaxLocals();
		setMaxLocals(max + slots);
		return max;
	}

	public Method getMethod() {
		if (savedMethod != null) {
			return savedMethod; // ??? this relies on gentle treatment of
			// constant pool
		}

		try {
			MethodGen gen = pack();
			savedMethod = gen.getMethod();
			return savedMethod;
		} catch (ClassGenException e) {
			enclosingClass
					.getBcelObjectType()
					.getResolvedTypeX()
					.getWorld()
					.showMessage(
							IMessage.ERROR,
							WeaverMessages.format(WeaverMessages.PROBLEM_GENERATING_METHOD, this.getClassName(), this.getName(),
									e.getMessage()),
							this.getMemberView() == null ? null : this.getMemberView().getSourceLocation(), null);
			// throw e; PR 70201.... let the normal problem reporting
			// infrastructure deal with this rather than crashing.
			body = null;
			MethodGen gen = pack();
			return gen.getMethod();
		} catch (RuntimeException re) {
			if (re.getCause() instanceof ClassGenException) {
				enclosingClass
						.getBcelObjectType()
						.getResolvedTypeX()
						.getWorld()
						.showMessage(
								IMessage.ERROR,
								WeaverMessages.format(WeaverMessages.PROBLEM_GENERATING_METHOD, this.getClassName(),
										this.getName(), re.getCause().getMessage()),
								this.getMemberView() == null ? null : this.getMemberView().getSourceLocation(), null);
				// throw e; PR 70201.... let the normal problem reporting
				// infrastructure deal with this rather than crashing.
				body = null;
				MethodGen gen = pack();
				return gen.getMethod();
			}
			throw re;
		}
	}

	public void markAsChanged() {
		if (wasPackedOptimally) {
			throw new RuntimeException("Already packed method is being re-modified: " + getClassName() + " " + toShortString());
		}
		initialize();
		savedMethod = null;
	}

	// =============================

	@Override
	public String toString() {
		BcelObjectType bot = enclosingClass.getBcelObjectType();
		WeaverVersionInfo weaverVersion = (bot == null ? WeaverVersionInfo.CURRENT : bot.getWeaverVersionAttribute());
		return toLongString(weaverVersion);
	}

	public String toShortString() {
		String access = org.aspectj.apache.bcel.classfile.Utility.accessToString(getAccessFlags());

		StringBuffer buf = new StringBuffer();

		if (!access.equals("")) {
			buf.append(access);
			buf.append(" ");
		}
		buf.append(org.aspectj.apache.bcel.classfile.Utility.signatureToString(getReturnType().getSignature(), true));
		buf.append(" ");
		buf.append(getName());
		buf.append("(");
		{
			int len = argumentTypes.length;
			if (len > 0) {
				buf.append(org.aspectj.apache.bcel.classfile.Utility.signatureToString(argumentTypes[0].getSignature(), true));
				for (int i = 1; i < argumentTypes.length; i++) {
					buf.append(", ");
					buf.append(org.aspectj.apache.bcel.classfile.Utility.signatureToString(argumentTypes[i].getSignature(), true));
				}
			}
		}
		buf.append(")");

		{
			int len = declaredExceptions != null ? declaredExceptions.length : 0;
			if (len > 0) {
				buf.append(" throws ");
				buf.append(declaredExceptions[0]);
				for (int i = 1; i < declaredExceptions.length; i++) {
					buf.append(", ");
					buf.append(declaredExceptions[i]);
				}
			}
		}
		return buf.toString();
	}

	public String toLongString(WeaverVersionInfo weaverVersion) {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		print(new PrintStream(s), weaverVersion);
		return new String(s.toByteArray());
	}

	public void print(WeaverVersionInfo weaverVersion) {
		print(System.out, weaverVersion);
	}

	public void print(PrintStream out, WeaverVersionInfo weaverVersion) {
		out.print("  " + toShortString());
		printAspectAttributes(out, weaverVersion);

		InstructionList body = getBody();
		if (body == null) {
			out.println(";");
			return;
		}
		out.println(":");
		new BodyPrinter(out).run();
		out.println("  end " + toShortString());
	}

	private void printAspectAttributes(PrintStream out, WeaverVersionInfo weaverVersion) {
		ISourceContext context = null;
		if (enclosingClass != null && enclosingClass.getType() != null) {
			context = enclosingClass.getType().getSourceContext();
		}
		List<AjAttribute> as = Utility.readAjAttributes(getClassName(), attributes.toArray(new Attribute[] {}), context, null, weaverVersion,
				new BcelConstantPoolReader(this.enclosingClass.getConstantPool()));
		if (!as.isEmpty()) {
			out.println("    " + as.get(0)); // XXX assuming exactly one
			// attribute, munger...
		}
	}

	private class BodyPrinter {
		Map<InstructionHandle, String> labelMap = new HashMap<InstructionHandle, String>();

		InstructionList body;
		PrintStream out;
		ConstantPool pool;

		BodyPrinter(PrintStream out) {
			this.pool = enclosingClass.getConstantPool();
			this.body = getBodyForPrint();
			this.out = out;
		}

		BodyPrinter(PrintStream out, InstructionList il) {
			this.pool = enclosingClass.getConstantPool();
			this.body = il;
			this.out = out;
		}

		void run() {
			// killNops();
			assignLabels();
			print();
		}

		// label assignment
		void assignLabels() {
			LinkedList<ExceptionRange> exnTable = new LinkedList<ExceptionRange>();
			String pendingLabel = null;
			// boolean hasPendingTargeters = false;
			int lcounter = 0;
			for (InstructionHandle ih = body.getStart(); ih != null; ih = ih.getNext()) {
				Iterator<InstructionTargeter> tIter = ih.getTargeters().iterator();
				while (tIter.hasNext()) {
					InstructionTargeter t = tIter.next();// targeters
					// [
					// i
					// ]
					// ;
					if (t instanceof ExceptionRange) {
						// assert isRangeHandle(h);
						ExceptionRange r = (ExceptionRange) t;
						if (r.getStart() == ih) {
							insertHandler(r, exnTable);
						}
					} else if (t instanceof InstructionBranch) {
						if (pendingLabel == null) {
							pendingLabel = "L" + lcounter++;
						}
					} else {
						// assert isRangeHandle(h)
					}
				}
				if (pendingLabel != null) {
					labelMap.put(ih, pendingLabel);
					if (!Range.isRangeHandle(ih)) {
						pendingLabel = null;
					}
				}
			}
			int ecounter = 0;
			for (Iterator i = exnTable.iterator(); i.hasNext();) {
				ExceptionRange er = (ExceptionRange) i.next();
				String exceptionLabel = "E" + ecounter++;
				labelMap.put(Range.getRealStart(er.getHandler()), exceptionLabel);
				labelMap.put(er.getHandler(), exceptionLabel);
			}
		}

		// printing

		void print() {
			int depth = 0;
			int currLine = -1;
			bodyPrint: for (InstructionHandle ih = body.getStart(); ih != null; ih = ih.getNext()) {
				if (Range.isRangeHandle(ih)) {
					Range r = Range.getRange(ih);
					// don't print empty ranges, that is, ranges who contain no
					// actual instructions
					for (InstructionHandle xx = r.getStart(); Range.isRangeHandle(xx); xx = xx.getNext()) {
						if (xx == r.getEnd()) {
							continue bodyPrint;
						}
					}

					// doesn't handle nested: if (r.getStart().getNext() ==
					// r.getEnd()) continue;
					if (r.getStart() == ih) {
						printRangeString(r, depth++);
					} else {
						if (r.getEnd() != ih) {
							throw new RuntimeException("bad");
						}
						printRangeString(r, --depth);
					}
				} else {
					printInstruction(ih, depth);
					int line = getLineNumber(ih, currLine);
					if (line != currLine) {
						currLine = line;
						out.println("   (line " + line + ")");
					} else {
						out.println();
					}
				}
			}
		}

		void printRangeString(Range r, int depth) {
			printDepth(depth);
			out.println(getRangeString(r, labelMap));
		}

		String getRangeString(Range r, Map<InstructionHandle, String> labelMap) {
			if (r instanceof ExceptionRange) {
				ExceptionRange er = (ExceptionRange) r;
				return er.toString() + " -> " + labelMap.get(er.getHandler());
				//
				// + " PRI " + er.getPriority();
			} else {
				return r.toString();
			}
		}

		void printDepth(int depth) {
			pad(BODY_INDENT);
			while (depth > 0) {
				out.print("| ");
				depth--;
			}
		}

		void printLabel(String s, int depth) {
			int space = Math.max(CODE_INDENT - depth * 2, 0);
			if (s == null) {
				pad(space);
			} else {
				space = Math.max(space - (s.length() + 2), 0);
				pad(space);
				out.print(s);
				out.print(": ");
			}
		}

		void printInstruction(InstructionHandle h, int depth) {
			printDepth(depth);
			printLabel(labelMap.get(h), depth);

			Instruction inst = h.getInstruction();
			if (inst.isConstantPoolInstruction()) {
				out.print(Constants.OPCODE_NAMES[inst.opcode].toUpperCase());
				out.print(" ");
				out.print(pool.constantToString(pool.getConstant(inst.getIndex())));
			} else if (inst instanceof InstructionSelect) {
				InstructionSelect sinst = (InstructionSelect) inst;
				out.println(Constants.OPCODE_NAMES[sinst.opcode].toUpperCase());
				int[] matches = sinst.getMatchs();
				InstructionHandle[] targets = sinst.getTargets();
				InstructionHandle defaultTarget = sinst.getTarget();
				for (int i = 0, len = matches.length; i < len; i++) {
					printDepth(depth);
					printLabel(null, depth);
					out.print("  ");
					out.print(matches[i]);
					out.print(": \t");
					out.println(labelMap.get(targets[i]));
				}
				printDepth(depth);
				printLabel(null, depth);
				out.print("  ");
				out.print("default: \t");
				out.print(labelMap.get(defaultTarget));
			} else if (inst instanceof InstructionBranch) {
				InstructionBranch brinst = (InstructionBranch) inst;
				out.print(Constants.OPCODE_NAMES[brinst.getOpcode()].toUpperCase());
				out.print(" ");
				out.print(labelMap.get(brinst.getTarget()));
			} else if (inst.isLocalVariableInstruction()) {
				// LocalVariableInstruction lvinst = (LocalVariableInstruction)
				// inst;
				out.print(inst.toString(false).toUpperCase());
				int index = inst.getIndex();
				LocalVariableTag tag = getLocalVariableTag(h, index);
				if (tag != null) {
					out.print("     // ");
					out.print(tag.getType());
					out.print(" ");
					out.print(tag.getName());
				}
			} else {
				out.print(inst.toString(false).toUpperCase());
			}
		}

		static final int BODY_INDENT = 4;
		static final int CODE_INDENT = 16;

		void pad(int size) {
			for (int i = 0; i < size; i++) {
				out.print(" ");
			}
		}
	}

	static LocalVariableTag getLocalVariableTag(InstructionHandle ih, int index) {
		for (InstructionTargeter t : ih.getTargeters()) {
			if (t instanceof LocalVariableTag) {
				LocalVariableTag lvt = (LocalVariableTag) t;
				if (lvt.getSlot() == index) {
					return lvt;
				}
			}
		}
		return null;
	}

	static int getLineNumber(InstructionHandle ih, int prevLine) {
		for (InstructionTargeter t : ih.getTargeters()) {
			if (t instanceof LineNumberTag) {
				return ((LineNumberTag) t).getLineNumber();
			}
		}
		return prevLine;
	}

	public boolean isStatic() {
		return Modifier.isStatic(getAccessFlags());
	}

	public boolean isAbstract() {
		return Modifier.isAbstract(getAccessFlags());
	}

	public boolean isBridgeMethod() {
		return (getAccessFlags() & Constants.ACC_BRIDGE) != 0;
	}

	public void addExceptionHandler(InstructionHandle start, InstructionHandle end, InstructionHandle handlerStart,
			ObjectType catchType, boolean highPriority) {

		InstructionHandle start1 = Range.genStart(body, start);
		InstructionHandle end1 = Range.genEnd(body, end);

		ExceptionRange er = new ExceptionRange(body, (catchType == null ? null : BcelWorld.fromBcel(catchType)), highPriority);
		er.associateWithTargets(start1, end1, handlerStart);
	}

	public int getAccessFlags() {
		return modifiers;
	}

	public int getAccessFlagsWithoutSynchronized() {
		if (isSynchronized()) {
			return modifiers - Modifier.SYNCHRONIZED;
		}
		return modifiers;
	}

	public boolean isSynchronized() {
		return (modifiers & Modifier.SYNCHRONIZED) != 0;
	}

	public void setAccessFlags(int newFlags) {
		this.modifiers = newFlags;
	}

	public Type[] getArgumentTypes() {
		initialize();
		return argumentTypes;
	}

	public LazyClassGen getEnclosingClass() {
		return enclosingClass;
	}

	public int getMaxLocals() {
		return maxLocals;
	}

	public String getName() {
		return name;
	}

	public String getGenericReturnTypeSignature() {
		if (memberView == null) {
			return getReturnType().getSignature();
		} else {
			return memberView.getGenericReturnType().getSignature();
		}
	}

	public Type getReturnType() {
		initialize();
		return returnType;
	}

	public void setMaxLocals(int maxLocals) {
		this.maxLocals = maxLocals;
	}

	public InstructionList getBody() {
		markAsChanged();
		return body;
	}

	public InstructionList getBodyForPrint() {
		return body;
	}

	public boolean hasBody() {
		if (savedMethod != null) {
			return savedMethod.getCode() != null;
		}
		return body != null;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public String[] getDeclaredExceptions() {
		return declaredExceptions;
	}

	public String getClassName() {
		return enclosingClass.getName();
	}

	// ---- packing!

	public MethodGen pack() {
		forceSyntheticForAjcMagicMembers();

		// killNops();
		int flags = getAccessFlags();
		if (enclosingClass.getWorld().isJoinpointSynchronizationEnabled()
				&& enclosingClass.getWorld().areSynchronizationPointcutsInUse()) {
			flags = getAccessFlagsWithoutSynchronized();
		}
		MethodGen gen = new MethodGen(flags, getReturnType(), getArgumentTypes(), null, // getArgumentNames(),
				getName(), getEnclosingClass().getName(), new InstructionList(), getEnclosingClass().getConstantPool());
		for (int i = 0, len = declaredExceptions.length; i < len; i++) {
			gen.addException(declaredExceptions[i]);
		}

		for (Attribute attr : attributes) {
			gen.addAttribute(attr);
		}

		if (newAnnotations != null) {
			for (AnnotationAJ element : newAnnotations) {
				gen.addAnnotation(new AnnotationGen(((BcelAnnotation) element).getBcelAnnotation(), gen.getConstantPool(), true));
			}
		}

		if (newParameterAnnotations != null) {
			for (int i = 0; i < newParameterAnnotations.length; i++) {
				AnnotationAJ[] annos = newParameterAnnotations[i];
				for (int j = 0; j < annos.length; j++) {
					gen.addParameterAnnotation(i,
							new AnnotationGen(((BcelAnnotation) annos[j]).getBcelAnnotation(), gen.getConstantPool(), true));
				}
			}
		}

		if (memberView != null && memberView.getAnnotations() != null && memberView.getAnnotations().length != 0) {
			AnnotationAJ[] ans = memberView.getAnnotations();
			for (int i = 0, len = ans.length; i < len; i++) {
				AnnotationGen a = ((BcelAnnotation) ans[i]).getBcelAnnotation();
				gen.addAnnotation(new AnnotationGen(a, gen.getConstantPool(), true));
			}
		}

		if (isSynthetic) {
			if (enclosingClass.getWorld().isInJava5Mode()) {
				gen.setModifiers(gen.getModifiers() | ACC_SYNTHETIC);
			}
			if (!hasAttribute("Synthetic")) {
				// belt and braces, do the attribute even on Java 5 in addition to the modifier flag
				ConstantPool cpg = gen.getConstantPool();
				int index = cpg.addUtf8("Synthetic");
				gen.addAttribute(new Synthetic(index, 0, new byte[0], cpg));
			}
		}

		if (hasBody()) {
			if (this.enclosingClass.getWorld().shouldFastPackMethods()) {
				if (isAdviceMethod() || getName().equals("<clinit>")) {
					packBody(gen);
				} else {
					optimizedPackBody(gen);
				}
			} else {
				packBody(gen);
			}
			gen.setMaxLocals();
			gen.setMaxStack();
		} else {
			gen.setInstructionList(null);
		}
		return gen;
	}
	
	private boolean hasAttribute(String attributeName) {
		for (Attribute attr: attributes) {
			if (attr.getName().equals(attributeName)) {
				return true;
			}
		}
		return false;
	}

	private void forceSyntheticForAjcMagicMembers() {
		if (NameMangler.isSyntheticMethod(getName(), inAspect())) {
			makeSynthetic();
		}
	}

	private boolean inAspect() {
		BcelObjectType objectType = enclosingClass.getBcelObjectType();
		return (objectType == null ? false : objectType.isAspect());
	}

	public void makeSynthetic() {
		isSynthetic = true;
	}

	private static class LVPosition {
		InstructionHandle start = null;
		InstructionHandle end = null;
	}

	/**
	 * fill the newly created method gen with our body, inspired by InstructionList.copy()
	 */
	public void packBody(MethodGen gen) {
		InstructionList fresh = gen.getInstructionList();
		Map<InstructionHandle, InstructionHandle> map = copyAllInstructionsExceptRangeInstructionsInto(fresh);

		// at this point, no rangeHandles are in fresh. Let's use that...

		/*
		 * Update branch targets and insert various attributes. Insert our exceptionHandlers into a sorted list, so they can be
		 * added in order later.
		 */
		InstructionHandle oldInstructionHandle = getBody().getStart();
		InstructionHandle newInstructionHandle = fresh.getStart();
		LinkedList<ExceptionRange> exceptionList = new LinkedList<ExceptionRange>();

		Map<LocalVariableTag, LVPosition> localVariables = new HashMap<LocalVariableTag, LVPosition>();

		int currLine = -1;
		int lineNumberOffset = (fromFilename == null) ? 0 : getEnclosingClass().getSourceDebugExtensionOffset(fromFilename);

		while (oldInstructionHandle != null) {
			if (map.get(oldInstructionHandle) == null) {
				// must be a range instruction since they're the only things we
				// didn't copy across
				handleRangeInstruction(oldInstructionHandle, exceptionList);
				// just increment ih.
				oldInstructionHandle = oldInstructionHandle.getNext();
			} else {
				// assert map.get(ih) == jh
				Instruction oldInstruction = oldInstructionHandle.getInstruction();
				Instruction newInstruction = newInstructionHandle.getInstruction();

				if (oldInstruction instanceof InstructionBranch) {
					handleBranchInstruction(map, oldInstruction, newInstruction);
				}

				// now deal with line numbers
				// and store up info for local variables
				for (InstructionTargeter targeter : oldInstructionHandle.getTargeters()) {
					if (targeter instanceof LineNumberTag) {
						int line = ((LineNumberTag) targeter).getLineNumber();
						if (line != currLine) {
							gen.addLineNumber(newInstructionHandle, line + lineNumberOffset);
							currLine = line;
						}
					} else if (targeter instanceof LocalVariableTag) {
						LocalVariableTag lvt = (LocalVariableTag) targeter;
						LVPosition p = localVariables.get(lvt);
						// If we don't know about it, create a new position and
						// store
						// If we do know about it - update its end position
						if (p == null) {
							LVPosition newp = new LVPosition();
							newp.start = newp.end = newInstructionHandle;
							localVariables.put(lvt, newp);
						} else {
							p.end = newInstructionHandle;
						}
					}
				}

				// now continue
				oldInstructionHandle = oldInstructionHandle.getNext();
				newInstructionHandle = newInstructionHandle.getNext();
			}
		}

		addExceptionHandlers(gen, map, exceptionList);
		if (localVariables.size() == 0) {
			// Might be a case of 173978 where around advice on an execution join point
			// has caused everything to be extracted from the method and thus we
			// are left with no local variables, not even the ones for 'this' and
			// parameters passed to the method
			createNewLocalVariables(gen);
		} else {
			addLocalVariables(gen, localVariables);
		}

		// JAVAC adds line number tables (with just one entry) to generated
		// accessor methods - this
		// keeps some tools that rely on finding at least some form of
		// linenumbertable happy.
		// Let's check if we have one - if we don't then let's add one.
		// TODO Could be made conditional on whether line debug info is being
		// produced
		if (gen.getLineNumbers().length == 0) {
			gen.addLineNumber(gen.getInstructionList().getStart(), 1);
		}
	}

	private void createNewLocalVariables(MethodGen gen) {
		gen.removeLocalVariables();
		// ignore <clinit> or <init> for now
		if (!getName().startsWith("<")) {
			int slot = 0;
			InstructionHandle start = gen.getInstructionList().getStart();
			InstructionHandle end = gen.getInstructionList().getEnd();
			// Add a 'this' if non-static
			if (!isStatic()) {
				String cname = this.enclosingClass.getClassName();
				if (cname == null) {
					return; // give up for now
				}
				Type enclosingType = BcelWorld.makeBcelType(UnresolvedType.forName(cname));
				gen.addLocalVariable("this", enclosingType, slot++, start, end);
			}
			// Add entries for the method arguments
			String[] paramNames = (memberView == null ? null : memberView.getParameterNames());
			if (paramNames != null) {
				for (int i = 0; i < argumentTypes.length; i++) {
					String pname = paramNames[i];
					if (pname == null) {
						pname = "arg" + i;
					}
					gen.addLocalVariable(pname, argumentTypes[i], slot, start, end);
					slot += argumentTypes[i].getSize();
				}
			}
		}
	}

	/*
	 * Optimized packing that does a 'local packing' of the code rather than building a brand new method and packing into it. Only
	 * usable when the packing is going to be done just once.
	 */
	public void optimizedPackBody(MethodGen gen) {
		InstructionList theBody = getBody();
		InstructionHandle iHandle = theBody.getStart();

		int currLine = -1;
		int lineNumberOffset = (fromFilename == null) ? 0 : getEnclosingClass().getSourceDebugExtensionOffset(fromFilename);
		Map<LocalVariableTag, LVPosition> localVariables = new HashMap<LocalVariableTag, LVPosition>();
		LinkedList<ExceptionRange> exceptionList = new LinkedList<ExceptionRange>();
		Set<InstructionHandle> forDeletion = new HashSet<InstructionHandle>();
		Set<BranchHandle> branchInstructions = new HashSet<BranchHandle>();
		// OPTIMIZE sort out in here: getRange()/insertHandler() and type of
		// exceptionList
		while (iHandle != null) {
			Instruction inst = iHandle.getInstruction();
			// InstructionHandle nextInst = iHandle.getNext();
			// OPTIMIZE remove this instructionhandle as it now points to
			// nowhere?
			if (inst == Range.RANGEINSTRUCTION) {
				Range r = Range.getRange(iHandle);
				if (r instanceof ExceptionRange) {
					ExceptionRange er = (ExceptionRange) r;
					if (er.getStart() == iHandle) {
						if (!er.isEmpty()) {
							// order is important, insert handlers in order of start
							insertHandler(er, exceptionList);
						}
					}
				}
				forDeletion.add(iHandle);
			} else {
				if (inst instanceof InstructionBranch) {
					branchInstructions.add((BranchHandle) iHandle);
				}

				for (InstructionTargeter targeter : iHandle.getTargetersCopy()) {
					if (targeter instanceof LineNumberTag) {
						int line = ((LineNumberTag) targeter).getLineNumber();
						if (line != currLine) {
							gen.addLineNumber(iHandle, line + lineNumberOffset);
							currLine = line;
						}
					} else if (targeter instanceof LocalVariableTag) {
						LocalVariableTag lvt = (LocalVariableTag) targeter;
						LVPosition p = localVariables.get(lvt);
						// If we don't know about it, create a new position
						// and store
						// If we do know about it - update its end position
						if (p == null) {
							LVPosition newp = new LVPosition();
							newp.start = newp.end = iHandle;
							localVariables.put(lvt, newp);
						} else {
							p.end = iHandle;
						}
					}
				}
			}
			iHandle = iHandle.getNext();
		}
		for (BranchHandle branchHandle : branchInstructions) {
			handleBranchInstruction(branchHandle, forDeletion);
		}
		// now add exception handlers
		for (ExceptionRange r : exceptionList) {
			if (r.isEmpty()) {
				continue;
			}
			gen.addExceptionHandler(jumpForward(r.getRealStart(), forDeletion), jumpForward(r.getRealEnd(), forDeletion),
					jumpForward(r.getHandler(), forDeletion),
					(r.getCatchType() == null) ? null : (ObjectType) BcelWorld.makeBcelType(r.getCatchType()));
		}

		for (InstructionHandle handle : forDeletion) {
			try {
				theBody.delete(handle);
			} catch (TargetLostException e) {
				e.printStackTrace();
			}
		}
		gen.setInstructionList(theBody);
		if (localVariables.size() == 0) {
			// Might be a case of 173978 where around advice on an execution join point
			// has caused everything to be extracted from the method and thus we
			// are left with no local variables, not even the ones for 'this' and
			// parameters passed to the method
			createNewLocalVariables(gen);
		} else {
			addLocalVariables(gen, localVariables);
		}

		// JAVAC adds line number tables (with just one entry) to generated
		// accessor methods - this
		// keeps some tools that rely on finding at least some form of
		// linenumbertable happy.
		// Let's check if we have one - if we don't then let's add one.
		// TODO Could be made conditional on whether line debug info is being
		// produced
		if (gen.getLineNumbers().length == 0) {
			gen.addLineNumber(gen.getInstructionList().getStart(), 1);
		}
		wasPackedOptimally = true;
	}

	private void addLocalVariables(MethodGen gen, Map<LocalVariableTag, LVPosition> localVariables) {
		// now add local variables
		gen.removeLocalVariables();

		// this next iteration _might_ be overkill, but we had problems with
		// bcel before with duplicate local variables. Now that we're patching
		// bcel we should be able to do without it if we're paranoid enough
		// through the rest of the compiler.
		InstructionHandle methodStart = gen.getInstructionList().getStart();
		InstructionHandle methodEnd = gen.getInstructionList().getEnd();

		// Determine how many 'slots' are used by parameters to the method.
		// Then below we can determine if a local variable is a parameter variable, if it is
		// we force its range to from the method start (as it may have been shuffled down
		// due to insertion of advice like cflow entry)
		int paramSlots = gen.isStatic() ? 0 : 1;
		Type[] argTypes = gen.getArgumentTypes();
		if (argTypes != null) {
			for (int i = 0; i < argTypes.length; i++) {
				if (argTypes[i].getSize() == 2) {
					paramSlots += 2;
				} else {
					paramSlots += 1;
				}
			}
		}

		Map<InstructionHandle, Set<Integer>> duplicatedLocalMap = new HashMap<InstructionHandle, Set<Integer>>();
		for (LocalVariableTag tag : localVariables.keySet()) {
			// have we already added one with the same slot number and start
			// location?
			// if so, just continue.
			LVPosition lvpos = localVariables.get(tag);
			InstructionHandle start = (tag.getSlot() < paramSlots ? methodStart : lvpos.start);
			InstructionHandle end = (tag.getSlot() < paramSlots ? methodEnd : lvpos.end);
			Set<Integer> slots = duplicatedLocalMap.get(start);
			if (slots == null) {
				slots = new HashSet<Integer>();
				duplicatedLocalMap.put(start, slots);
			} else if (slots.contains(new Integer(tag.getSlot()))) {
				// we already have a var starting at this tag with this slot
				continue;
			}
			slots.add(Integer.valueOf(tag.getSlot()));
			Type t = tag.getRealType();
			if (t == null) {
				t = BcelWorld.makeBcelType(UnresolvedType.forSignature(tag.getType()));
			}
			gen.addLocalVariable(tag.getName(), t, tag.getSlot(), start, end);
		}
	}

	private void addExceptionHandlers(MethodGen gen, Map<InstructionHandle, InstructionHandle> map,
			LinkedList<ExceptionRange> exnList) {
		// now add exception handlers
		for (ExceptionRange r : exnList) {
			if (r.isEmpty()) {
				continue;
			}
			InstructionHandle rMappedStart = remap(r.getRealStart(), map);
			InstructionHandle rMappedEnd = remap(r.getRealEnd(), map);
			InstructionHandle rMappedHandler = remap(r.getHandler(), map);
			gen.addExceptionHandler(rMappedStart, rMappedEnd, rMappedHandler, (r.getCatchType() == null) ? null
					: (ObjectType) BcelWorld.makeBcelType(r.getCatchType()));
		}
	}

	private void handleBranchInstruction(Map<InstructionHandle, InstructionHandle> map, Instruction oldInstruction,
			Instruction newInstruction) {
		InstructionBranch oldBranchInstruction = (InstructionBranch) oldInstruction;
		InstructionBranch newBranchInstruction = (InstructionBranch) newInstruction;
		InstructionHandle oldTarget = oldBranchInstruction.getTarget(); // old
		// target

		// New target is in hash map
		newBranchInstruction.setTarget(remap(oldTarget, map));

		if (oldBranchInstruction instanceof InstructionSelect) {
			// Either LOOKUPSWITCH or TABLESWITCH
			InstructionHandle[] oldTargets = ((InstructionSelect) oldBranchInstruction).getTargets();
			InstructionHandle[] newTargets = ((InstructionSelect) newBranchInstruction).getTargets();

			for (int k = oldTargets.length - 1; k >= 0; k--) {
				// Update all targets
				newTargets[k] = remap(oldTargets[k], map);
				newTargets[k].addTargeter(newBranchInstruction);
			}
		}
	}

	private InstructionHandle jumpForward(InstructionHandle t, Set<InstructionHandle> handlesForDeletion) {
		InstructionHandle target = t;
		if (handlesForDeletion.contains(target)) {
			do {
				target = target.getNext();
			} while (handlesForDeletion.contains(target));
		}
		return target;
	}

	/**
	 * Process a branch instruction with respect to instructions that are about to be deleted. If the target for the branch is a
	 * candidate for deletion, move it to the next valid instruction after the deleted target.
	 */
	private void handleBranchInstruction(BranchHandle branchHandle, Set<InstructionHandle> handlesForDeletion) {
		InstructionBranch branchInstruction = (InstructionBranch) branchHandle.getInstruction();
		InstructionHandle target = branchInstruction.getTarget(); // old target

		if (handlesForDeletion.contains(target)) {
			do {
				target = target.getNext();
			} while (handlesForDeletion.contains(target));
			branchInstruction.setTarget(target);
		}

		if (branchInstruction instanceof InstructionSelect) {
			// Either LOOKUPSWITCH or TABLESWITCH
			InstructionSelect iSelect = (InstructionSelect) branchInstruction;
			InstructionHandle[] targets = iSelect.getTargets();
			for (int k = targets.length - 1; k >= 0; k--) {
				InstructionHandle oneTarget = targets[k];
				if (handlesForDeletion.contains(oneTarget)) {
					do {
						oneTarget = oneTarget.getNext();
					} while (handlesForDeletion.contains(oneTarget));
					iSelect.setTarget(k, oneTarget);
					oneTarget.addTargeter(branchInstruction);
				}
			}
		}
	}

	private void handleRangeInstruction(InstructionHandle ih, LinkedList<ExceptionRange> exnList) {
		// we're a range instruction
		Range r = Range.getRange(ih);
		if (r instanceof ExceptionRange) {
			ExceptionRange er = (ExceptionRange) r;
			if (er.getStart() == ih) {
				// System.err.println("er " + er);
				if (!er.isEmpty()) {
					// order is important, insert handlers in order of start
					insertHandler(er, exnList);
				}
			}
		} else {
			// we must be a shadow range or something equally useless,
			// so forget about doing anything
		}
	}

	/*
	 * Make copies of all instructions, append them to the new list and associate old instruction references with the new ones,
	 * i.e., a 1:1 mapping.
	 */
	private Map<InstructionHandle, InstructionHandle> copyAllInstructionsExceptRangeInstructionsInto(InstructionList intoList) {
		Map<InstructionHandle, InstructionHandle> map = new HashMap<InstructionHandle, InstructionHandle>();
		for (InstructionHandle ih = getBody().getStart(); ih != null; ih = ih.getNext()) {
			if (Range.isRangeHandle(ih)) {
				continue;
			}
			Instruction inst = ih.getInstruction();
			Instruction copy = Utility.copyInstruction(inst);

			if (copy instanceof InstructionBranch) {
				map.put(ih, intoList.append((InstructionBranch) copy));
			} else {
				map.put(ih, intoList.append(copy));
			}
		}
		return map;
	}

	/**
	 * This procedure should not currently be used.
	 */
	// public void killNops() {
	// InstructionHandle curr = body.getStart();
	// while (true) {
	// if (curr == null) break;
	// InstructionHandle next = curr.getNext();
	// if (curr.getInstruction() instanceof NOP) {
	// InstructionTargeter[] targeters = curr.getTargeters();
	// if (targeters != null) {
	// for (int i = 0, len = targeters.length; i < len; i++) {
	// InstructionTargeter targeter = targeters[i];
	// targeter.updateTarget(curr, next);
	// }
	// }
	// try {
	// body.delete(curr);
	// } catch (TargetLostException e) {
	// }
	// }
	// curr = next;
	// }
	// }
	// private static InstructionHandle fNext(InstructionHandle ih) {
	// while (true) {
	// if (ih.getInstruction()==Range.RANGEINSTRUCTION) ih = ih.getNext();
	// else return ih;
	// }
	// }
	private static InstructionHandle remap(InstructionHandle handle, Map<InstructionHandle, InstructionHandle> map) {
		while (true) {
			InstructionHandle ret = map.get(handle);
			if (ret == null) {
				handle = handle.getNext();
			} else {
				return ret;
			}
		}
	}

	// Update to all these comments, ASC 11-01-2005
	// The right thing to do may be to do more with priorities as
	// we create new exception handlers, but that is a relatively
	// complex task. In the meantime, just taking account of the
	// priority here enables a couple of bugs to be fixed to do
	// with using return or break in code that contains a finally
	// block (pr78021,pr79554).

	// exception ordering.
	// What we should be doing is dealing with priority inversions way earlier
	// than we are
	// and counting on the tree structure. In which case, the below code is in
	// fact right.

	// XXX THIS COMMENT BELOW IS CURRENTLY WRONG.
	// An exception A preceeds an exception B in the exception table iff:

	// * A and B were in the original method, and A preceeded B in the original
	// exception table
	// * If A has a higher priority than B, than it preceeds B.
	// * If A and B have the same priority, then the one whose START happens
	// EARLIEST has LEAST priority.
	// in short, the outermost exception has least priority.
	// we implement this with a LinkedList. We could possibly implement this
	// with a java.util.SortedSet,
	// but I don't trust the only implementation, TreeSet, to do the right
	// thing.

	/* private */static void insertHandler(ExceptionRange fresh, LinkedList<ExceptionRange> l) {
		// Old implementation, simply: l.add(0,fresh);
		for (ListIterator<ExceptionRange> iter = l.listIterator(); iter.hasNext();) {
			ExceptionRange r = iter.next();
			// int freal = fresh.getRealStart().getPosition();
			// int rreal = r.getRealStart().getPosition();
			if (fresh.getPriority() >= r.getPriority()) {
				iter.previous();
				iter.add(fresh);
				return;
			}
		}

		// we have reached the end
		l.add(fresh);
	}

	public boolean isPrivate() {
		return Modifier.isPrivate(getAccessFlags());
	}

	public boolean isProtected() {
		return Modifier.isProtected(getAccessFlags());
	}

	public boolean isDefault() {
		return !(isProtected() || isPrivate() || isPublic());
	}

	public boolean isPublic() {
		return Modifier.isPublic(getAccessFlags());
	}

	// ----

	/**
	 * A good body is a body with the following properties:
	 * 
	 * <ul>
	 * <li>For each branch instruction S in body, target T of S is in body.
	 * <li>For each branch instruction S in body, target T of S has S as a targeter.
	 * <li>For each instruction T in body, for each branch instruction S that is a targeter of T, S is in body.
	 * <li>For each non-range-handle instruction T in body, for each instruction S that is a targeter of T, S is either a branch
	 * instruction, an exception range or a tag
	 * <li>For each range-handle instruction T in body, there is exactly one targeter S that is a range.
	 * <li>For each range-handle instruction T in body, the range R targeting T is in body.
	 * <li>For each instruction T in body, for each exception range R targeting T, R is in body.
	 * <li>For each exception range R in body, let T := R.handler. T is in body, and R is one of T's targeters
	 * <li>All ranges are properly nested: For all ranges Q and R, if Q.start preceeds R.start, then R.end preceeds Q.end.
	 * </ul>
	 * 
	 * Where the shorthand "R is in body" means "R.start is in body, R.end is in body, and any InstructionHandle stored in a field
	 * of R (such as an exception handle) is in body".
	 */

	public void assertGoodBody() {
		if (true) {
			return; // only enable for debugging
		}
		assertGoodBody(getBody(), toString());
	}

	public static void assertGoodBody(InstructionList il, String from) {
		if (true) {
			return; // only to be enabled for debugging
		}
//		if (il == null) {
//			return;
//		}
//		Set body = new HashSet();
//		Stack<Range> ranges = new Stack<Range>();
//		for (InstructionHandle ih = il.getStart(); ih != null; ih = ih.getNext()) {
//			body.add(ih);
//			if (ih.getInstruction() instanceof InstructionBranch) {
//				body.add(ih.getInstruction());
//			}
//		}
//
//		for (InstructionHandle ih = il.getStart(); ih != null; ih = ih.getNext()) {
//			assertGoodHandle(ih, body, ranges, from);
//			Iterator<InstructionTargeter> tIter = ih.getTargeters().iterator();
//			while (tIter.hasNext()) {
//				assertGoodTargeter(tIter.next(), ih, body, from);
//			}
//		}
	}

//	private static void assertGoodHandle(InstructionHandle ih, Set body, Stack<Range> ranges, String from) {
//		Instruction inst = ih.getInstruction();
//		if ((inst instanceof InstructionBranch) ^ (ih instanceof BranchHandle)) {
//			throw new BCException("bad instruction/handle pair in " + from);
//		}
//		if (Range.isRangeHandle(ih)) {
//			assertGoodRangeHandle(ih, body, ranges, from);
//		} else if (inst instanceof InstructionBranch) {
//			assertGoodBranchInstruction((BranchHandle) ih, (InstructionBranch) inst, body, ranges, from);
//		}
//	}

//	private static void assertGoodBranchInstruction(BranchHandle ih, InstructionBranch inst, Set body, Stack<Range> ranges,
//			String from) {
//		if (ih.getTarget() != inst.getTarget()) {
//			throw new BCException("bad branch instruction/handle pair in " + from);
//		}
//		InstructionHandle target = ih.getTarget();
//		assertInBody(target, body, from);
//		assertTargetedBy(target, inst, from);
//		if (inst instanceof InstructionSelect) {
//			InstructionSelect sel = (InstructionSelect) inst;
//			InstructionHandle[] itargets = sel.getTargets();
//			for (int k = itargets.length - 1; k >= 0; k--) {
//				assertInBody(itargets[k], body, from);
//				assertTargetedBy(itargets[k], inst, from);
//			}
//		}
//	}

	/** ih is an InstructionHandle or a BranchInstruction */
//	private static void assertInBody(Object ih, Set body, String from) {
//		if (!body.contains(ih)) {
//			throw new BCException("thing not in body in " + from);
//		}
//	}

//	private static void assertGoodRangeHandle(InstructionHandle ih, Set body, Stack ranges, String from) {
//		Range r = getRangeAndAssertExactlyOne(ih, from);
//		assertGoodRange(r, body, from);
//		if (r.getStart() == ih) {
//			ranges.push(r);
//		} else if (r.getEnd() == ih) {
//			if (ranges.peek() != r) {
//				throw new BCException("bad range inclusion in " + from);
//			}
//			ranges.pop();
//		}
//	}

//	private static void assertGoodRange(Range r, Set body, String from) {
//		assertInBody(r.getStart(), body, from);
//		assertRangeHandle(r.getStart(), from);
//		assertTargetedBy(r.getStart(), r, from);
//
//		assertInBody(r.getEnd(), body, from);
//		assertRangeHandle(r.getEnd(), from);
//		assertTargetedBy(r.getEnd(), r, from);
//
//		if (r instanceof ExceptionRange) {
//			ExceptionRange er = (ExceptionRange) r;
//			assertInBody(er.getHandler(), body, from);
//			assertTargetedBy(er.getHandler(), r, from);
//		}
//	}

//	private static void assertRangeHandle(InstructionHandle ih, String from) {
//		if (!Range.isRangeHandle(ih)) {
//			throw new BCException("bad range handle " + ih + " in " + from);
//		}
//	}

	private static void assertTargetedBy(InstructionHandle target, InstructionTargeter targeter, String from) {
		Iterator tIter = target.getTargeters().iterator();
		while (tIter.hasNext()) {
			if (((InstructionTargeter) tIter.next()) == targeter) {
				return;
			}
		}
		throw new RuntimeException("bad targeting relationship in " + from);
	}

	private static void assertTargets(InstructionTargeter targeter, InstructionHandle target, String from) {
		if (targeter instanceof Range) {
			Range r = (Range) targeter;
			if (r.getStart() == target || r.getEnd() == target) {
				return;
			}
			if (r instanceof ExceptionRange) {
				if (((ExceptionRange) r).getHandler() == target) {
					return;
				}
			}
		} else if (targeter instanceof InstructionBranch) {
			InstructionBranch bi = (InstructionBranch) targeter;
			if (bi.getTarget() == target) {
				return;
			}
			if (targeter instanceof InstructionSelect) {
				InstructionSelect sel = (InstructionSelect) targeter;
				InstructionHandle[] itargets = sel.getTargets();
				for (int k = itargets.length - 1; k >= 0; k--) {
					if (itargets[k] == target) {
						return;
					}
				}
			}
		} else if (targeter instanceof Tag) {
			return;
		}
		throw new BCException(targeter + " doesn't target " + target + " in " + from);
	}

	private static Range getRangeAndAssertExactlyOne(InstructionHandle ih, String from) {
		Range ret = null;
		Iterator<InstructionTargeter> tIter = ih.getTargeters().iterator();
		if (!tIter.hasNext()) {
			throw new BCException("range handle with no range in " + from);
		}
		while (tIter.hasNext()) {
			InstructionTargeter ts = tIter.next();
			if (ts instanceof Range) {
				if (ret != null) {
					throw new BCException("range handle with multiple ranges in " + from);
				}
				ret = (Range) ts;
			}
		}
		if (ret == null) {
			throw new BCException("range handle with no range in " + from);
		}
		return ret;
	}

//	private static void assertGoodTargeter(InstructionTargeter t, InstructionHandle ih, Set body, String from) {
//		assertTargets(t, ih, from);
//		if (t instanceof Range) {
//			assertGoodRange((Range) t, body, from);
//		} else if (t instanceof InstructionBranch) {
//			assertInBody(t, body, from);
//		}
//	}

	// ----

	boolean isAdviceMethod() {
		if (memberView == null) {
			return false;
		}
		return memberView.getAssociatedShadowMunger() != null;
	}

	boolean isAjSynthetic() {
		if (memberView == null) {
			return true;
		}
		return memberView.isAjSynthetic();
	}

	boolean isSynthetic() {
		if (memberView == null) {
			return false;
		}
		return memberView.isSynthetic();
	}

	public ISourceLocation getSourceLocation() {
		if (memberView != null) {
			return memberView.getSourceLocation();
		}
		return null;
	}

	public AjAttribute.EffectiveSignatureAttribute getEffectiveSignature() {
		// if (memberView == null) return null;
		if (effectiveSignature != null) {
			return effectiveSignature;
		}
		return memberView.getEffectiveSignature();
	}

	public void setEffectiveSignature(ResolvedMember member, Shadow.Kind kind, boolean shouldWeave) {
		this.effectiveSignature = new AjAttribute.EffectiveSignatureAttribute(member, kind, shouldWeave);
	}

	public String getSignature() {
		if (memberView != null) {
			return memberView.getSignature();
		}
		return MemberImpl.typesToSignature(BcelWorld.fromBcel(getReturnType()), BcelWorld.fromBcel(getArgumentTypes()), false);
	}

	public String getParameterSignature() {
		if (memberView != null) {
			return memberView.getParameterSignature();
		}
		return MemberImpl.typesToSignature(BcelWorld.fromBcel(getArgumentTypes()));
	}

	public BcelMethod getMemberView() {
		return memberView;
	}

	public void forcePublic() {
		markAsChanged();
		modifiers = Utility.makePublic(modifiers);
	}

	public boolean getCanInline() {
		return canInline;
	}

	public void setCanInline(boolean canInline) {
		this.canInline = canInline;
	}

	public void addAttribute(Attribute attribute) {
		attributes.add(attribute);
	}

	public String toTraceString() {
		return toShortString();
	}

	public ConstantPool getConstantPool() {
		return enclosingClass.getConstantPool();
	}

	public static boolean isConstructor(LazyMethodGen aMethod) {
		return aMethod.getName().equals("<init>");
	}

}
