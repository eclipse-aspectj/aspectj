/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.IOException;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.CompressingDataOutputStream;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;

/**
 * Represents an attempt to bind the field of an annotation within a pointcut. For example:<br>
 * <code><pre>
 * before(Level lev): execution(* *(..)) &amp;&amp; @annotation(TraceAnnotation(lev))
 * </pre></code><br>
 * This binding annotation type pattern will be for 'lev'.
 */
public class BindingAnnotationFieldTypePattern extends ExactAnnotationTypePattern implements BindingPattern {

	protected int formalIndex;
	UnresolvedType formalType; // In this construct the formal type differs from the annotation type

	public BindingAnnotationFieldTypePattern(UnresolvedType formalType, int formalIndex, UnresolvedType theAnnotationType) {
		super(theAnnotationType, null);
		this.formalIndex = formalIndex;
		this.formalType = formalType;
	}

	public void resolveBinding(World world) {
		if (resolved) {
			return;
		}
		resolved = true;
		formalType = world.resolve(formalType);
		annotationType = world.resolve(annotationType);
		ResolvedType annoType = (ResolvedType) annotationType;
		if (!annoType.isAnnotation()) {
			IMessage m = MessageUtil
					.error(WeaverMessages.format(WeaverMessages.REFERENCE_TO_NON_ANNOTATION_TYPE, annoType.getName()),
							getSourceLocation());
			world.getMessageHandler().handleMessage(m);
			resolved = false;
		}
	}

	public AnnotationTypePattern parameterizeWith(Map typeVariableMap, World w) {
		throw new BCException("Parameterization not implemented for annotation field binding construct (compiler limitation)");
		// UnresolvedType newAnnotationType = annotationType;
		// if (annotationType.isTypeVariableReference()) {
		// TypeVariableReference t = (TypeVariableReference) annotationType;
		// String key = t.getTypeVariable().getName();
		// if (typeVariableMap.containsKey(key)) {
		// newAnnotationType = (UnresolvedType) typeVariableMap.get(key);
		// }
		// } else if (annotationType.isParameterizedType()) {
		// newAnnotationType = annotationType.parameterize(typeVariableMap);
		// }
		// BindingAnnotationTypePattern ret = new BindingAnnotationTypePattern(newAnnotationType, this.formalIndex);
		// if (newAnnotationType instanceof ResolvedType) {
		// ResolvedType rat = (ResolvedType) newAnnotationType;
		// verifyRuntimeRetention(rat.getWorld(), rat);
		// }
		// ret.copyLocationFrom(this);
		// return ret;
	}

	public int getFormalIndex() {
		return formalIndex;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof BindingAnnotationFieldTypePattern)) {
			return false;
		}
		BindingAnnotationFieldTypePattern btp = (BindingAnnotationFieldTypePattern) obj;
		return (btp.formalIndex == formalIndex) && (annotationType.equals(btp.annotationType))
				&& (formalType.equals(btp.formalType));
	}

	public int hashCode() {
		return (annotationType.hashCode() * 37 + formalIndex * 37) + formalType.hashCode();
	}

	public AnnotationTypePattern remapAdviceFormals(IntMap bindings) {
		if (!bindings.hasKey(formalIndex)) {
			throw new BCException("Annotation field binding reference must be bound (compiler limitation)");
			// must be something like returning the unbound form: return new ExactAnnotationTypePattern(annotationType,
			// null);
		} else {
			int newFormalIndex = bindings.get(formalIndex);
			BindingAnnotationFieldTypePattern baftp = new BindingAnnotationFieldTypePattern(formalType, newFormalIndex,
					annotationType);
			baftp.formalName = formalName;
			return baftp;
		}
	}

	public void write(CompressingDataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.BINDINGFIELD2);
		formalType.write(s); // the type of the field within the annotation
		s.writeShort((short) formalIndex);
		annotationType.write(s); // the annotation type
		s.writeUTF(formalName);
		writeLocation(s);
	}

	public static AnnotationTypePattern read(VersionedDataInputStream s, ISourceContext context) throws IOException {
		AnnotationTypePattern ret = new BindingAnnotationFieldTypePattern(UnresolvedType.read(s), s.readShort(),
				UnresolvedType.read(s));
		ret.readLocation(context, s);
		return ret;
	}

	public static AnnotationTypePattern read2(VersionedDataInputStream s, ISourceContext context) throws IOException {
		BindingAnnotationFieldTypePattern ret = new BindingAnnotationFieldTypePattern(UnresolvedType.read(s), s.readShort(),
				UnresolvedType.read(s));
		ret.formalName = s.readUTF();
		ret.readLocation(context, s);
		return ret;
	}

	public FuzzyBoolean matches(AnnotatedElement annotated, ResolvedType[] parameterAnnotations) {
		// Inheritance irrelevant because @annotation(Anno(x)) only supported at method execution join points (compiler limitation)
		// boolean checkSupers = false;
		// if (getResolvedAnnotationType().hasAnnotation(UnresolvedType.AT_INHERITED)) {
		// if (annotated instanceof ResolvedType) {
		// checkSupers = true;
		// }
		// }
		//
		if (annotated.hasAnnotation(annotationType)) {
			if (annotationType instanceof ReferenceType) {
				ReferenceType rt = (ReferenceType) annotationType;
				if (rt.getRetentionPolicy() != null && rt.getRetentionPolicy().equals("SOURCE")) {
					rt.getWorld()
							.getMessageHandler()
							.handleMessage(
									MessageUtil.warn(WeaverMessages.format(WeaverMessages.NO_MATCH_BECAUSE_SOURCE_RETENTION,
											annotationType, annotated), getSourceLocation()));
					return FuzzyBoolean.NO;
				}
				ResolvedMember[] methods = rt.getDeclaredMethods();
				boolean found = false;
				for (int i = 0; i < methods.length && !found; i++) {
					if (methods[i].getReturnType().equals(formalType)) {
						found = true;
					}
				}
				return (found ? FuzzyBoolean.YES : FuzzyBoolean.NO);
			}
		}
		// else if (checkSupers) {
		// ResolvedType toMatchAgainst = ((ResolvedType) annotated).getSuperclass();
		// while (toMatchAgainst != null) {
		// if (toMatchAgainst.hasAnnotation(annotationType)) {
		// return FuzzyBoolean.YES;
		// }
		// toMatchAgainst = toMatchAgainst.getSuperclass();
		// }
		// }
		//
		return FuzzyBoolean.NO;
	}

	public UnresolvedType getFormalType() {
		return formalType;
	}

}
