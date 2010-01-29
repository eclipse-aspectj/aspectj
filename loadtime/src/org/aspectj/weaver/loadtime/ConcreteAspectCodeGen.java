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
import org.aspectj.apache.bcel.generic.InstructionList;
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
		// TODO only convert the name to signature once, probably earlier than
		// this
		ResolvedType current = world.lookupBySignature(UnresolvedType.forName(concreteAspect.name).getSignature());

		if (current != null && !current.isMissing()) {
			reportError("Attempt to concretize but chosen aspect name already defined: " + stringify());
			return false;
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
			reportError("Cannot find m_parent aspect for: " + stringify());
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
		List elligibleAbstractions = new ArrayList();

		Collection abstractMethods = getOutstandingAbstractMethods(parent);
		for (Iterator iter = abstractMethods.iterator(); iter.hasNext();) {
			ResolvedMember method = (ResolvedMember) iter.next();
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
		List pointcutNames = new ArrayList();
		for (Iterator it = concreteAspect.pointcuts.iterator(); it.hasNext();) {
			Definition.Pointcut abstractPc = (Definition.Pointcut) it.next();
			pointcutNames.add(abstractPc.name);
		}
		for (Iterator it = elligibleAbstractions.iterator(); it.hasNext();) {
			String elligiblePc = (String) it.next();
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

	private Collection getOutstandingAbstractMethods(ResolvedType type) {
		Map collector = new HashMap();
		// let's get to the top of the hierarchy and then walk down ...
		// recording abstract methods then removing
		// them if they get defined further down the hierarchy
		getOutstandingAbstractMethodsHelper(type, collector);
		return collector.values();
	}

	// We are trying to determine abstract methods left over at the bottom of a
	// hierarchy that have not been
	// concretized.
	private void getOutstandingAbstractMethodsHelper(ResolvedType type, Map collector) {
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
			AnnotationGen ag = new AnnotationGen(new ObjectType("org/aspectj/lang/annotation/Aspect"), Collections.EMPTY_LIST,
					true, cg.getConstantPool());
			cg.addAnnotation(ag);
		} else {
			// List elems = new ArrayList();
			List elems = new ArrayList();
			elems.add(new NameValuePair("value",
					new SimpleElementValue(ElementValue.STRING, cg.getConstantPool(), perclauseString), cg.getConstantPool()));
			AnnotationGen ag = new AnnotationGen(new ObjectType("org/aspectj/lang/annotation/Aspect"), elems, true, cg
					.getConstantPool());
			cg.addAnnotation(ag);
		}
		if (concreteAspect.precedence != null) {
			SimpleElementValue svg = new SimpleElementValue(ElementValue.STRING, cg.getConstantPool(), concreteAspect.precedence);
			List elems = new ArrayList();
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

		for (Iterator it = concreteAspect.pointcuts.iterator(); it.hasNext();) {
			Definition.Pointcut abstractPc = (Definition.Pointcut) it.next();
			// TODO AV - respect visibility instead of opening up as public?
			LazyMethodGen mg = new LazyMethodGen(Modifier.PUBLIC, Type.VOID, abstractPc.name, EMPTY_TYPES, EMPTY_STRINGS, cg);
			SimpleElementValue svg = new SimpleElementValue(ElementValue.STRING, cg.getConstantPool(), abstractPc.expression);
			List elems = new ArrayList();
			elems.add(new NameValuePair("value", svg, cg.getConstantPool()));
			AnnotationGen mag = new AnnotationGen(new ObjectType("org/aspectj/lang/annotation/Pointcut"), elems, true, cg
					.getConstantPool());
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
				List elems = new ArrayList();
				elems.add(new NameValuePair("value", svg, cg.getConstantPool()));
				AnnotationGen mag = new AnnotationGen(new ObjectType("org/aspectj/lang/annotation/Declare"
						+ (deow.isError ? "Error" : "Warning")), elems, true, cg.getConstantPool());
				field.addAnnotation(mag);

				field.setValue(deow.message);
				cg.addField(field, null);


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

		return jc.getBytes();
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
