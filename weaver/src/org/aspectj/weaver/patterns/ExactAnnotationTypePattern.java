/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver.patterns;

import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;

/**
 * Matches an annotation of a given type
 */
public class ExactAnnotationTypePattern extends AnnotationTypePattern {

	protected TypeX annotationType;
	protected String formalName;
	protected boolean resolved = false;
	private boolean bindingPattern = false;
	
	/**
	 * 
	 */
	public ExactAnnotationTypePattern(TypeX annotationType) {
		this.annotationType = annotationType;
		this.resolved = (annotationType instanceof ResolvedTypeX);
	}

	public ExactAnnotationTypePattern(String formalName) {
		this.formalName = formalName;
		this.resolved = false;
		this.bindingPattern = true;
		// will be turned into BindingAnnotationTypePattern during resolution
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
		if (annotationType.hasAnnotation(TypeX.AT_INHERITED)) {
			if (annotated instanceof ResolvedTypeX) {
				checkSupers = true;
			}
		}
		
		if (annotated.hasAnnotation(annotationType)) {
			return FuzzyBoolean.YES;
		} else if (checkSupers) {
			ResolvedTypeX toMatchAgainst = ((ResolvedTypeX) annotated).getSuperclass();
			while (toMatchAgainst != null) {
				if (toMatchAgainst.hasAnnotation(annotationType)) return FuzzyBoolean.YES;
				toMatchAgainst = toMatchAgainst.getSuperclass();
			}
		} 
		return FuzzyBoolean.NO;
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
		if (formalName != null) {
			FormalBinding formalBinding = scope.lookupFormal(formalName);
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
				verifyIsAnnotationType(formalBinding.getType(),scope);
				BindingAnnotationTypePattern binding = new BindingAnnotationTypePattern(formalBinding);
				binding.copyLocationFrom(this);
				bindings.register(binding, scope);
				binding.resolveBinding(scope.getWorld());
				
				return binding;
			} else {
				scope.message(IMessage.ERROR,this,"unbound formal " + formalName);
				return this;
			}
		} else {
			// Non binding case

			String cleanname = annotationType.getClassName();
			annotationType = scope.getWorld().resolve(annotationType,true);
			
			// We may not have found it if it is in a package, lets look it up...
			if (annotationType == ResolvedTypeX.MISSING) {
				TypeX type = null;
				while ((type = scope.lookupType(cleanname,this)) == ResolvedTypeX.MISSING) {
					int lastDot = cleanname.lastIndexOf('.');
					if (lastDot == -1) break;
					cleanname = cleanname.substring(0,lastDot)+"$"+cleanname.substring(lastDot+1);
				}
				annotationType = scope.getWorld().resolve(type,true);
			}
			
			verifyIsAnnotationType(annotationType,scope);
			return this;
		}
	}
	
	/**
	 * @param scope
	 */
	private void verifyIsAnnotationType(TypeX type,IScope scope) {
		if (!type.isAnnotation(scope.getWorld())) {
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
			ret = new ExactAnnotationTypePattern(TypeX.read(s));			
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
}
