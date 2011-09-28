/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import java.io.FileOutputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.SimpleElementValue;
import org.aspectj.apache.bcel.generic.FieldGen;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.LocalVariableTag;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.weaver.AnnotationAJ;
import org.aspectj.weaver.GeneratedReferenceTypeDelegate;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.World;
import org.aspectj.weaver.bcel.BcelAnnotation;
import org.aspectj.weaver.bcel.BcelPerClauseAspectAdder;
import org.aspectj.weaver.bcel.BcelWorld;
import org.aspectj.weaver.bcel.LazyClassGen;
import org.aspectj.weaver.bcel.LazyMethodGen;
import org.aspectj.weaver.loadtime.definition.Definition;
import org.aspectj.weaver.loadtime.definition.Definition.AdviceKind;
import org.aspectj.weaver.loadtime.definition.Definition.PointcutAndAdvice;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PerSingleton;

/**
 * Generates bytecode for concrete-aspect.
 * <p>
 * The concrete aspect is @AspectJ code generated. As it is build during aop.xml definitions registration we perform the type
 * munging for perclause, ie. aspectOf() artifact directly, instead of waiting for it to go thru the weaver (that we are in the
 * middle of configuring).
 * 
 * @author Alexandre Vasseur
 * @author Andy Clement
 */
public class ConcreteAspectCodeGen {

	private final static String[] EMPTY_STRINGS = new String[0];
	private final static Type[] EMPTY_TYPES = new Type[0];

	/**
	 * Concrete aspect definition we build for
	 */
	private final Definition.ConcreteAspect concreteAspect;

	/**
	 * World for which we build for
	 */
	private final World world;

	/**
	 * Set to true when all is checks are verified
	 */
	private boolean isValid = false;

	/**
	 * The parent aspect, not concretized
	 */
	private ResolvedType parent;

	/**
	 * Aspect perClause, used for direct munging of aspectOf artifacts
	 */
	private PerClause perclause;

	/**
	 * Bytecode for the generated class
	 */
	private byte[] bytes;

	/**
	 * Create a new compiler for a concrete aspect
	 * 
	 * @param concreteAspect
	 * @param world
	 */
	ConcreteAspectCodeGen(Definition.ConcreteAspect concreteAspect, World world) {
		this.concreteAspect = concreteAspect;
		this.world = world;
	}

	/**
	 * Checks that concrete aspect is valid
	 * 
	 * @return true if ok, false otherwise
	 */
	public boolean validate() {
		if (!(world instanceof BcelWorld)) {
			reportError("Internal error: world must be of type BcelWorld");
			return false;
		}

		// name must be undefined so far
		// TODO only convert the name to signature once, probably earlier than this
		ResolvedType current = world.lookupBySignature(UnresolvedType.forName(concreteAspect.name).getSignature());

		if (current != null && !current.isMissing()) {
			reportError("Attempt to concretize but chosen aspect name already defined: " + stringify());
			return false;
		}

		if (concreteAspect.pointcutsAndAdvice.size() != 0) {
			isValid = true;
			return true;
		}

		// it can happen that extends is null, for precedence only declaration
		if (concreteAspect.extend == null && concreteAspect.precedence != null) {
			if (concreteAspect.pointcuts.isEmpty()) {
				isValid = true;
				// m_perClause = new PerSingleton();
				parent = null;
				return true;// no need to checks more in that special case
			} else {
				reportError("Attempt to use nested pointcuts without extends clause: " + stringify());
				return false;
			}
		}

		String parentAspectName = concreteAspect.extend;

		if (parentAspectName.indexOf("<") != -1) {
			// yikes, generic parent
			parent = world.resolve(UnresolvedType.forName(parentAspectName), true);
			if (parent.isMissing()) {
				reportError("Unable to resolve type reference: " + stringify());
				return false;
			}
			if (parent.isParameterizedType()) {
				UnresolvedType[] typeParameters = parent.getTypeParameters();
				for (int i = 0; i < typeParameters.length; i++) {
					UnresolvedType typeParameter = typeParameters[i];
					if (typeParameter instanceof ResolvedType && ((ResolvedType) typeParameter).isMissing()) {
						reportError("Unablet to resolve type parameter '" + typeParameter.getName() + "' from " + stringify());
						return false;
					}
				}
			}
		} else {
			parent = world.resolve(concreteAspect.extend, true);
		}
		// handle inner classes
		if (parent.isMissing()) {
			// fallback on inner class lookup mechanism
			String fixedName = concreteAspect.extend;
			int hasDot = fixedName.lastIndexOf('.');
			while (hasDot > 0) {
				char[] fixedNameChars = fixedName.toCharArray();
				fixedNameChars[hasDot] = '$';
				fixedName = new String(fixedNameChars);
				hasDot = fixedName.lastIndexOf('.');
				parent = world.resolve(UnresolvedType.forName(fixedName), true);
				if (!parent.isMissing()) {
					break;
				}
			}
		}

		if (parent.isMissing()) {
			reportError("Cannot find parent aspect for: " + stringify());
			return false;
		}

		// extends must be abstract (allow for special case of Object where just using aspect for deows)
		if (!(parent.isAbstract() || parent.equals(ResolvedType.OBJECT))) {
			reportError("Attempt to concretize a non-abstract aspect: " + stringify());
			return false;
		}

		// m_parent must be aspect (allow for special case of Object where just using aspect for deows)
		if (!(parent.isAspect() || parent.equals(ResolvedType.OBJECT))) {
			reportError("Attempt to concretize a non aspect: " + stringify());
			return false;
		}

		// must have all abstractions defined
		List<String> elligibleAbstractions = new ArrayList<String>();

		Collection<ResolvedMember> abstractMethods = getOutstandingAbstractMethods(parent);
		// for (Iterator iter = abstractMethods.iterator(); iter.hasNext();) {
		// ResolvedMember method = (ResolvedMember) iter.next();
		for (ResolvedMember method : abstractMethods) {
			if ("()V".equals(method.getSignature())) {
				String n = method.getName();
				// Allow for the abstract pointcut being from a code style
				// aspect compiled with -1.5 (see test for 128744)
				if (n.startsWith("ajc$pointcut")) {
					n = n.substring(14);
					n = n.substring(0, n.indexOf("$"));
					elligibleAbstractions.add(n);
				} else if (hasPointcutAnnotation(method)) {
					elligibleAbstractions.add(method.getName());
				} else {
					// error, an outstanding abstract method that can't be
					// concretized in XML
					reportError("Abstract method '" + method.toString() + "' cannot be concretized in XML: " + stringify());
					return false;
				}
			} else {
				if (method.getName().startsWith("ajc$pointcut") || hasPointcutAnnotation(method)) {
					// it may be a pointcut but it doesn't meet the requirements
					// for XML concretization
					reportError("Abstract method '"
							+ method.toString()
							+ "' cannot be concretized as a pointcut (illegal signature, must have no arguments, must return void): "
							+ stringify());
					return false;
				} else {
					// error, an outstanding abstract method that can't be
					// concretized in XML
					reportError("Abstract method '" + method.toString() + "' cannot be concretized in XML: " + stringify());
					return false;
				}
			}
		}
		List<String> pointcutNames = new ArrayList<String>();
		for (Definition.Pointcut abstractPc : concreteAspect.pointcuts) {
			pointcutNames.add(abstractPc.name);
		}
		for (String elligiblePc : elligibleAbstractions) {
			if (!pointcutNames.contains(elligiblePc)) {
				reportError("Abstract pointcut '" + elligiblePc + "' not configured: " + stringify());
				return false;
			}
		}

		if (concreteAspect.perclause != null) {
			String perclauseString = concreteAspect.perclause;
			if (perclauseString.startsWith("persingleton")) {
			} else if (perclauseString.startsWith("percflow")) {
			} else if (perclauseString.startsWith("pertypewithin")) {
			} else if (perclauseString.startsWith("perthis")) {
			} else if (perclauseString.startsWith("pertarget")) {
			} else if (perclauseString.startsWith("percflowbelow")) {
			} else {
				reportError("Unrecognized per clause specified " + stringify());
				return false;
			}
		}
		isValid = true;
		return isValid;
	}

	private Collection<ResolvedMember> getOutstandingAbstractMethods(ResolvedType type) {
		Map<String, ResolvedMember> collector = new HashMap<String, ResolvedMember>();
		// let's get to the top of the hierarchy and then walk down ...
		// recording abstract methods then removing
		// them if they get defined further down the hierarchy
		getOutstandingAbstractMethodsHelper(type, collector);
		return collector.values();
	}

	// We are trying to determine abstract methods left over at the bottom of a
	// hierarchy that have not been concretized.
	private void getOutstandingAbstractMethodsHelper(ResolvedType type, Map<String, ResolvedMember> collector) {
		if (type == null) {
			return;
		}
		// Get to the top
		if (!type.equals(ResolvedType.OBJECT)) {
			if (type.getSuperclass() != null) {
				getOutstandingAbstractMethodsHelper(type.getSuperclass(), collector);
			}
		}
		ResolvedMember[] rms = type.getDeclaredMethods();
		if (rms != null) {
			for (int i = 0; i < rms.length; i++) {
				ResolvedMember member = rms[i];
				String key = member.getName() + member.getSignature();
				if (member.isAbstract()) {
					collector.put(key, member);
				} else {
					collector.remove(key);
				}
			}
		}
	}

	/**
	 * Rebuild the XML snip that defines this concrete aspect, for log error purpose
	 * 
	 * @return string repr.
	 */
	private String stringify() {
		StringBuffer sb = new StringBuffer("<concrete-aspect name='");
		sb.append(concreteAspect.name);
		sb.append("' extends='");
		sb.append(concreteAspect.extend);
		sb.append("' perclause='");
		sb.append(concreteAspect.perclause);
		sb.append("'/> in aop.xml");
		return sb.toString();
	}

	private boolean hasPointcutAnnotation(ResolvedMember member) {
		AnnotationAJ[] as = member.getAnnotations();
		if (as == null || as.length == 0) {
			return false;
		}
		for (int i = 0; i < as.length; i++) {
			if (as[i].getTypeSignature().equals("Lorg/aspectj/lang/annotation/Pointcut;")) {
				return true;
			}
		}
		return false;
	}

	public String getClassName() {
		return concreteAspect.name;
	}

	/**
	 * Build the bytecode for the concrete aspect
	 * 
	 * @return concrete aspect bytecode
	 */
	public byte[] getBytes() {
		if (!isValid) {
			throw new RuntimeException("Must validate first");
		}
		if (bytes != null) {
			return bytes;
		}
		PerClause parentPerClause = (parent != null ? parent.getPerClause() : null);
		if (parentPerClause == null) {
			parentPerClause = new PerSingleton();
		}
		PerClause.Kind perclauseKind = PerClause.SINGLETON;
		String perclauseString = null;

		if (concreteAspect.perclause != null) {
			perclauseString = concreteAspect.perclause;
			if (perclauseString.startsWith("persingleton")) {
				perclauseKind = PerClause.SINGLETON;
			} else if (perclauseString.startsWith("percflow")) {
				perclauseKind = PerClause.PERCFLOW;
			} else if (perclauseString.startsWith("pertypewithin")) {
				perclauseKind = PerClause.PERTYPEWITHIN;
			} else if (perclauseString.startsWith("perthis")) {
				perclauseKind = PerClause.PEROBJECT;
			} else if (perclauseString.startsWith("pertarget")) {
				perclauseKind = PerClause.PEROBJECT;
			} else if (perclauseString.startsWith("percflowbelow")) {
				perclauseKind = PerClause.PERCFLOW;
			}
		}

		// TODO AV - abstract away from BCEL...
		// @Aspect //inherit clause from m_parent
		// @DeclarePrecedence("....") // if any
		// public class xxxName [extends xxxExtends] {
		// [@Pointcut(xxxExpression-n)
		// public void xxxName-n() {}]
		// }
		String parentName = "java/lang/Object";
		if (parent != null) {
			if (parent.isParameterizedType()) {
				parentName = parent.getGenericType().getName().replace('.', '/');
			} else {
				parentName = parent.getName().replace('.', '/');
			}
		}
		// @Aspect public class ...
		// TODO AV - we could point to the aop.xml that defines it and use
		// JSR-45
		LazyClassGen cg = new LazyClassGen(concreteAspect.name.replace('.', '/'), parentName, null, Modifier.PUBLIC
				+ Constants.ACC_SUPER, EMPTY_STRINGS, world);
		if (parent != null && parent.isParameterizedType()) {
			cg.setSuperClass(parent);
		}
		if (perclauseString == null) {
			AnnotationGen ag = new AnnotationGen(new ObjectType("org/aspectj/lang/annotation/Aspect"),
					Collections.<NameValuePair> emptyList(), true, cg.getConstantPool());
			cg.addAnnotation(ag);
		} else {
			// List elems = new ArrayList();
			List<NameValuePair> elems = new ArrayList<NameValuePair>();
			elems.add(new NameValuePair("value",
					new SimpleElementValue(ElementValue.STRING, cg.getConstantPool(), perclauseString), cg.getConstantPool()));
			AnnotationGen ag = new AnnotationGen(new ObjectType("org/aspectj/lang/annotation/Aspect"), elems, true,
					cg.getConstantPool());
			cg.addAnnotation(ag);
		}
		if (concreteAspect.precedence != null) {
			SimpleElementValue svg = new SimpleElementValue(ElementValue.STRING, cg.getConstantPool(), concreteAspect.precedence);
			List<NameValuePair> elems = new ArrayList<NameValuePair>();
			elems.add(new NameValuePair("value", svg, cg.getConstantPool()));
			AnnotationGen agprec = new AnnotationGen(new ObjectType("org/aspectj/lang/annotation/DeclarePrecedence"), elems, true,
					cg.getConstantPool());
			cg.addAnnotation(agprec);
		}

		// default constructor
		LazyMethodGen init = new LazyMethodGen(Modifier.PUBLIC, Type.VOID, "<init>", EMPTY_TYPES, EMPTY_STRINGS, cg);
		InstructionList cbody = init.getBody();
		cbody.append(InstructionConstants.ALOAD_0);

		cbody.append(cg.getFactory().createInvoke(parentName, "<init>", Type.VOID, EMPTY_TYPES, Constants.INVOKESPECIAL));
		cbody.append(InstructionConstants.RETURN);
		cg.addMethodGen(init);

		for (Iterator<Definition.Pointcut> it = concreteAspect.pointcuts.iterator(); it.hasNext();) {
			Definition.Pointcut abstractPc = (Definition.Pointcut) it.next();
			// TODO AV - respect visibility instead of opening up as public?
			LazyMethodGen mg = new LazyMethodGen(Modifier.PUBLIC, Type.VOID, abstractPc.name, EMPTY_TYPES, EMPTY_STRINGS, cg);
			SimpleElementValue svg = new SimpleElementValue(ElementValue.STRING, cg.getConstantPool(), abstractPc.expression);
			List<NameValuePair> elems = new ArrayList<NameValuePair>();
			elems.add(new NameValuePair("value", svg, cg.getConstantPool()));
			AnnotationGen mag = new AnnotationGen(new ObjectType("org/aspectj/lang/annotation/Pointcut"), elems, true,
					cg.getConstantPool());
			AnnotationAJ max = new BcelAnnotation(mag, world);
			mg.addAnnotation(max);

			InstructionList body = mg.getBody();
			body.append(InstructionConstants.RETURN);
			cg.addMethodGen(mg);
		}

		if (concreteAspect.deows.size() > 0) {

			int counter = 1;
			for (Definition.DeclareErrorOrWarning deow : concreteAspect.deows) {

				// Building this:

				// @DeclareWarning("call(* javax.sql..*(..)) && !within(org.xyz.daos..*)")
				// static final String aMessage = "Only DAOs should be calling JDBC.";

				FieldGen field = new FieldGen(Modifier.FINAL, ObjectType.STRING, "rule" + (counter++), cg.getConstantPool());
				SimpleElementValue svg = new SimpleElementValue(ElementValue.STRING, cg.getConstantPool(), deow.pointcut);
				List<NameValuePair> elems = new ArrayList<NameValuePair>();
				elems.add(new NameValuePair("value", svg, cg.getConstantPool()));
				AnnotationGen mag = new AnnotationGen(new ObjectType("org/aspectj/lang/annotation/Declare"
						+ (deow.isError ? "Error" : "Warning")), elems, true, cg.getConstantPool());
				field.addAnnotation(mag);

				field.setValue(deow.message);
				cg.addField(field, null);

			}
		}

		if (concreteAspect.pointcutsAndAdvice.size() > 0) {
			int adviceCounter = 1;
			for (PointcutAndAdvice paa : concreteAspect.pointcutsAndAdvice) {
				generateAdviceMethod(paa, adviceCounter, cg);
				adviceCounter++;
			}
		}

		// handle the perClause
		ReferenceType rt = new ReferenceType(ResolvedType.forName(concreteAspect.name).getSignature(), world);
		GeneratedReferenceTypeDelegate grtd = new GeneratedReferenceTypeDelegate(rt);
		grtd.setSuperclass(parent);
		rt.setDelegate(grtd);

		BcelPerClauseAspectAdder perClauseMunger = new BcelPerClauseAspectAdder(rt, perclauseKind);
		perClauseMunger.forceMunge(cg, false);

		// TODO AV - unsafe cast
		// register the fresh new class into the world repository as it does not
		// exist on the classpath anywhere
		JavaClass jc = cg.getJavaClass((BcelWorld) world);
		((BcelWorld) world).addSourceObjectType(jc, true);

		bytes = jc.getBytes();
		try {
			FileOutputStream fos = new FileOutputStream("/Users/aclement/foo.class");
			fos.write(bytes);
			fos.flush();
			fos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}

	/**
	 * The PointcutAndAdvice object encapsulates an advice kind, a pointcut and names a Java method in a particular class. Generate
	 * an annotation style advice that has that pointcut whose implementation delegates to the Java method.
	 */
	private void generateAdviceMethod(PointcutAndAdvice paa, int adviceCounter, LazyClassGen cg) {

		// Check: Verify the class to be called does exist:
		ResolvedType delegateClass = world.resolve(UnresolvedType.forName(paa.adviceClass));
		if (delegateClass.isMissing()) {
			reportError("Class to invoke cannot be found: '" + paa.adviceClass + "'");
			return;
		}

		// Generate a name for this advice, includes advice kind plus a counter
		String adviceName = new StringBuilder("generated$").append(paa.adviceKind.toString().toLowerCase()).append("$advice$")
				.append(adviceCounter).toString();

		// Build the annotation that encapsulates the pointcut
		AnnotationAJ aaj = buildAdviceAnnotation(cg, paa);

		// Chop up the supplied advice method string into its pieces.
		// Example: foo(JoinPoint jp, java.lang.String string)
		// JoinPoint and friends are recognized (so dont need fq package)
		String method = paa.adviceMethod;

		int paren = method.indexOf("(");
		String methodName = method.substring(0, paren);
		String signature = method.substring(paren);

		// Check: signature looks ok
		if (signature.charAt(0) != '(' || !signature.endsWith(")")) {
			reportError("Badly formatted parameter signature: '" + method + "'");
			return;
		}

		// Extract parameter types and names
		List<Type> paramTypes = new ArrayList<Type>();
		List<String> paramNames = new ArrayList<String>();
		if (signature.charAt(1) != ')') {
			// there are parameters to convert into a signature
			StringBuilder convertedSignature = new StringBuilder("(");
			boolean paramsBroken = false;
			int pos = 1;
			while (pos < signature.length() && signature.charAt(pos) != ')' && !paramsBroken) {
				int nextChunkEndPos = signature.indexOf(',', pos);
				if (nextChunkEndPos == -1) {
					nextChunkEndPos = signature.indexOf(')', pos);
				}
				// chunk will be a type plus a space plus a name
				String nextChunk = signature.substring(pos, nextChunkEndPos).trim();
				int space = nextChunk.indexOf(" ");
				ResolvedType resolvedParamType = null;
				if (space == -1) {
					// There is no parameter name, hopefully not needed!
					if (nextChunk.equals("JoinPoint")) {
						nextChunk = "org.aspectj.lang.JoinPoint";
					} else if (nextChunk.equals("JoinPoint.StaticPart")) {
						nextChunk = "org.aspectj.lang.JoinPoint$StaticPart";
					} else if (nextChunk.equals("ProceedingJoinPoint")) {
						nextChunk = "org.aspectj.lang.ProceedingJoinPoint";
					}
					UnresolvedType unresolvedParamType = UnresolvedType.forName(nextChunk);
					resolvedParamType = world.resolve(unresolvedParamType);
				} else {
					String typename = nextChunk.substring(0, space);
					if (typename.equals("JoinPoint")) {
						typename = "org.aspectj.lang.JoinPoint";
					} else if (typename.equals("JoinPoint.StaticPart")) {
						typename = "org.aspectj.lang.JoinPoint$StaticPart";
					} else if (typename.equals("ProceedingJoinPoint")) {
						typename = "org.aspectj.lang.ProceedingJoinPoint";
					}
					UnresolvedType unresolvedParamType = UnresolvedType.forName(typename);
					resolvedParamType = world.resolve(unresolvedParamType);
					String paramname = nextChunk.substring(space).trim();
					paramNames.add(paramname);
				}
				if (resolvedParamType.isMissing()) {
					reportError("Cannot find type specified as parameter: '" + nextChunk + "' from signature '" + signature + "'");
					paramsBroken = true;
				}
				paramTypes.add(Type.getType(resolvedParamType.getSignature()));
				convertedSignature.append(resolvedParamType.getSignature());
				pos = nextChunkEndPos + 1;
			}
			convertedSignature.append(")");
			signature = convertedSignature.toString();
			if (paramsBroken) {
				return;
			}
		}

		Type returnType = Type.VOID;

		// If around advice we must find the actual delegate method and use its return type
		if (paa.adviceKind == AdviceKind.Around) {
			ResolvedMember[] methods = delegateClass.getDeclaredMethods();
			ResolvedMember found = null;
			for (ResolvedMember candidate : methods) {
				if (candidate.getName().equals(methodName)) {
					UnresolvedType[] cparms = candidate.getParameterTypes();
					if (cparms.length == paramTypes.size()) {
						boolean paramsMatch = true;
						for (int i = 0; i < cparms.length; i++) {
							if (!cparms[i].getSignature().equals(paramTypes.get(i).getSignature())) {
								paramsMatch = false;
								break;
							}
						}
						if (paramsMatch) {
							found = candidate;
							break;
						}
					}
				}
			}
			if (found != null) {
				returnType = Type.getType(found.getReturnType().getSignature());
			} else {
				reportError("Unable to find method to invoke.  In class: " + delegateClass.getName() + " cant find "
						+ paa.adviceMethod);
				return;
			}
		}

		// Time to construct the method itself:
		LazyMethodGen advice = new LazyMethodGen(Modifier.PUBLIC, returnType, adviceName, paramTypes.toArray(new Type[paramTypes
				.size()]), EMPTY_STRINGS, cg);

		InstructionList adviceBody = advice.getBody();

		// Generate code to load the parameters
		int pos = 1; // first slot after 'this'
		for (int i = 0; i < paramTypes.size(); i++) {
			adviceBody.append(InstructionFactory.createLoad(paramTypes.get(i), pos));
			pos += paramTypes.get(i).getSize();
		}

		// Generate the delegate call
		adviceBody.append(cg.getFactory().createInvoke(paa.adviceClass, methodName, signature + returnType.getSignature(),
				Constants.INVOKESTATIC));

		// Generate the right return
		if (returnType == Type.VOID) {
			adviceBody.append(InstructionConstants.RETURN);
		} else {
			if (returnType.getSignature().length() < 2) {
				String sig = returnType.getSignature();
				if (sig.equals("F")) {
					adviceBody.append(InstructionConstants.FRETURN);
				} else if (sig.equals("D")) {
					adviceBody.append(InstructionConstants.DRETURN);
				} else if (sig.equals("J")) {
					adviceBody.append(InstructionConstants.LRETURN);
				} else {
					adviceBody.append(InstructionConstants.IRETURN);
				}
			} else {
				adviceBody.append(InstructionConstants.ARETURN);
			}
		}
		// Add the annotation
		advice.addAnnotation(aaj);
		InstructionHandle start = adviceBody.getStart();

		// Setup the local variable targeters so that the binding will work
		String sig = concreteAspect.name.replace('.', '/');
		start.addTargeter(new LocalVariableTag("L" + sig + ";", "this", 0, start.getPosition()));
		if (paramNames.size() > 0) {
			for (int i = 0; i < paramNames.size(); i++) {
				start.addTargeter(new LocalVariableTag(paramTypes.get(i).getSignature(), paramNames.get(i), i + 1, start
						.getPosition()));
			}
		}

		// Record the new method in the class
		cg.addMethodGen(advice);
	}

	/**
	 * For the given PointcutAndAdvice build the correct advice annotation.
	 */
	private AnnotationAJ buildAdviceAnnotation(LazyClassGen cg, PointcutAndAdvice paa) {
		SimpleElementValue svg = new SimpleElementValue(ElementValue.STRING, cg.getConstantPool(), paa.pointcut);
		List<NameValuePair> elems = new ArrayList<NameValuePair>();
		elems.add(new NameValuePair("value", svg, cg.getConstantPool()));
		AnnotationGen mag = new AnnotationGen(new ObjectType("org/aspectj/lang/annotation/" + paa.adviceKind.toString()), elems,
				true, cg.getConstantPool());
		AnnotationAJ aaj = new BcelAnnotation(mag, world);
		return aaj;
	}

	/**
	 * Error reporting
	 * 
	 * @param message
	 */
	private void reportError(String message) {
		world.getMessageHandler().handleMessage(new Message(message, IMessage.ERROR, null, null));
	}
}
