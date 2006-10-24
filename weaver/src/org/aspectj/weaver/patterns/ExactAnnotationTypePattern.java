/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ReferenceType;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.TypeVariableReference;
import org.aspectj.weaver.UnresolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;

/**
 * Matches an annotation of a given type
 */
public class ExactAnnotationTypePattern extends AnnotationTypePattern {

	protected UnresolvedType annotationType;
	protected String formalName;
	protected boolean resolved = false;
	private boolean bindingPattern = false;
	
	/**
	 * 
	 */
	public ExactAnnotationTypePattern(UnresolvedType annotationType) {
		this.annotationType = annotationType;
		this.resolved = (annotationType instanceof ResolvedType);
	}

	protected ExactAnnotationTypePattern(String formalName) {
		this.formalName = formalName;
		this.resolved = false;
		this.bindingPattern = true;
		// will be turned into BindingAnnotationTypePattern during resolution
	}

	public ResolvedType getResolvedAnnotationType() {
		if (!resolved) throw new IllegalStateException("I need to be resolved first!");
		return (ResolvedType) annotationType;
	}
	
    public UnresolvedType getAnnotationType() {
        return annotationType;
    }

	public FuzzyBoolean fastMatches(AnnotatedElement annotated) {
		if (annotated.hasAnnotation(annotationType)) {
			return FuzzyBoolean.YES;
		} else {
			// could be inherited, but we don't know that until we are 
			// resolved, and we're not yet...
			return FuzzyBoolean.MAYBE;
		}
	}
	
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		boolean checkSupers = false;
		if (getResolvedAnnotationType().hasAnnotation(UnresolvedType.AT_INHERITED)) {
			if (annotated instanceof ResolvedType) {
				checkSupers = true;
			}
		}
		
		if (annotated.hasAnnotation(annotationType)) {
			if (annotationType instanceof ReferenceType) {
				ReferenceType rt = (ReferenceType)annotationType;
				if (rt.getRetentionPolicy()!=null && rt.getRetentionPolicy().equals("SOURCE")) {
					rt.getWorld().getMessageHandler().handleMessage(
					  MessageUtil.warn(WeaverMessages.format(WeaverMessages.NO_MATCH_BECAUSE_SOURCE_RETENTION,annotationType,annotated),getSourceLocation()));
					return FuzzyBoolean.NO;
				}
			}
			return FuzzyBoolean.YES;
		} else if (checkSupers) {
			ResolvedType toMatchAgainst = ((ResolvedType) annotated).getSuperclass();
			while (toMatchAgainst != null) {
				if (toMatchAgainst.hasAnnotation(annotationType)) return FuzzyBoolean.YES;
				toMatchAgainst = toMatchAgainst.getSuperclass();
			}
		} 
		return FuzzyBoolean.NO;
	}
	
	// this version should be called for @this, @target, @args
	public FuzzyBoolean matchesRuntimeType(AnnotatedElement annotated) {
		if (getResolvedAnnotationType().hasAnnotation(UnresolvedType.AT_INHERITED)) {
			// a static match is good enough
			if (matches(annotated).alwaysTrue()) {
				return FuzzyBoolean.YES;
			} 
		}
		// a subtype could match at runtime
		return FuzzyBoolean.MAYBE;
	}

	
	public void resolve(World world) {
		if (!resolved) annotationType = annotationType.resolve(world);
		resolved = true;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolveBindings(org.aspectj.weaver.patterns.IScope, org.aspectj.weaver.patterns.Bindings, boolean)
	 */
	public AnnotationTypePattern resolveBindings(IScope scope,
			Bindings bindings, boolean allowBinding) {
		if (resolved) return this;
		resolved = true;
		String simpleName = maybeGetSimpleName();
		if (simpleName != null) {
			FormalBinding formalBinding = scope.lookupFormal(simpleName);
			if (formalBinding != null) {
				if (bindings == null) {
					scope.message(IMessage.ERROR, this, "negation doesn't allow binding");
					return this;
				}
				if (!allowBinding) {
					scope.message(IMessage.ERROR, this, 
						"name binding only allowed in @pcds, args, this, and target");
					return this;
				}
				formalName = simpleName;
				bindingPattern = true;
				verifyIsAnnotationType(formalBinding.getType().resolve(scope.getWorld()),scope);
				BindingAnnotationTypePattern binding = new BindingAnnotationTypePattern(formalBinding);
				binding.copyLocationFrom(this);
				bindings.register(binding, scope);
				binding.resolveBinding(scope.getWorld());
				
				return binding;
			} 
		}

		// Non binding case
		String cleanname = annotationType.getName();
		annotationType = scope.getWorld().resolve(annotationType,true);
		
		// We may not have found it if it is in a package, lets look it up...
		if (ResolvedType.isMissing(annotationType)) {
			UnresolvedType type = null;
			while (ResolvedType.isMissing(type = scope.lookupType(cleanname,this))) {
				int lastDot = cleanname.lastIndexOf('.');
				if (lastDot == -1) break;
				cleanname = cleanname.substring(0,lastDot)+"$"+cleanname.substring(lastDot+1);
			}
			annotationType = scope.getWorld().resolve(type,true);
		}
		
		verifyIsAnnotationType((ResolvedType)annotationType,scope);
		return this;
	}
	
	public AnnotationTypePattern parameterizeWith(Map typeVariableMap,World w) {
		UnresolvedType newAnnotationType = annotationType;
		if (annotationType.isTypeVariableReference()) {
			TypeVariableReference t = (TypeVariableReference) annotationType;
			String key = t.getTypeVariable().getName();
			if (typeVariableMap.containsKey(key)) {
				newAnnotationType = (UnresolvedType) typeVariableMap.get(key);
			}
		} else if (annotationType.isParameterizedType()) {
			newAnnotationType = annotationType.parameterize(typeVariableMap);
		}
		ExactAnnotationTypePattern ret = new ExactAnnotationTypePattern(newAnnotationType);
		ret.formalName = formalName;
		ret.bindingPattern = bindingPattern;
		ret.copyLocationFrom(this);
		return ret;
	}
	
	private String maybeGetSimpleName() {
		if (formalName != null) return formalName;
		String ret = annotationType.getName();
		return (ret.indexOf('.') == -1) ? ret : null;
	}
	
	/**
	 * @param scope
	 */
	private void verifyIsAnnotationType(ResolvedType type,IScope scope) {
		if (!type.isAnnotation()) {
			IMessage m = MessageUtil.error(
					WeaverMessages.format(WeaverMessages.REFERENCE_TO_NON_ANNOTATION_TYPE,type.getName()),
					getSourceLocation());
			scope.getWorld().getMessageHandler().handleMessage(m);
			resolved = false;
		}
	}

	private static byte VERSION = 1; // rev if serialisation form changes
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.EXACT);
		s.writeByte(VERSION);
		s.writeBoolean(bindingPattern);
		if (bindingPattern) {
			s.writeUTF(formalName);
		} else {
			annotationType.write(s);
		}
		writeLocation(s);
	}

	public static AnnotationTypePattern read(VersionedDataInputStream s,ISourceContext context) throws IOException {
		AnnotationTypePattern ret;
		byte version = s.readByte();
		if (version > VERSION) {
			throw new BCException("ExactAnnotationTypePattern was written by a newer version of AspectJ");
		}
		boolean isBindingPattern = s.readBoolean();
		if (isBindingPattern) {
			ret = new ExactAnnotationTypePattern(s.readUTF());
		} else {
			ret = new ExactAnnotationTypePattern(UnresolvedType.read(s));			
		}
		ret.readLocation(context,s);
		return ret;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof ExactAnnotationTypePattern)) return false;
		ExactAnnotationTypePattern other = (ExactAnnotationTypePattern) obj;
		return (other.annotationType.equals(annotationType));
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return annotationType.hashCode();
	}
	
	public String toString() {
	    if (!resolved && formalName != null) return formalName;
		String ret = "@" + annotationType.toString();
		if (formalName != null) ret = ret + " " + formalName;
		return ret;
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
