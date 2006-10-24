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
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.VersionedDataInputStream;
import org.aspectj.weaver.WeaverMessages;
import org.aspectj.weaver.World;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WildAnnotationTypePattern extends AnnotationTypePattern {

	private TypePattern typePattern;
	private boolean resolved = false;
	
	/**
	 * 
	 */
	public WildAnnotationTypePattern(TypePattern typePattern) {
		super();
		this.typePattern = typePattern;
		this.setLocation(typePattern.getSourceContext(), typePattern.start, typePattern.end);
	}

    public TypePattern getTypePattern() {
        return typePattern;
    }

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#matches(org.aspectj.weaver.AnnotatedElement)
	 */
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		if (!resolved) {
			throw new IllegalStateException("Can't match on an unresolved annotation type pattern");
		}
		// matches if the type of any of the annotations on the AnnotatedElement is
		// matched by the typePattern.
		ResolvedType[] annTypes = annotated.getAnnotationTypes();

		if (annTypes!=null && annTypes.length!=0) {
			for (int i = 0; i < annTypes.length; i++) {
				if (typePattern.matches(annTypes[i],TypePattern.STATIC).alwaysTrue()) {
					return FuzzyBoolean.YES;
				}
			}
		}
		return FuzzyBoolean.NO;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#resolve(org.aspectj.weaver.World)
	 */
	public void resolve(World world) {
		// nothing to do...
	    resolved = true;
	}
	
	/**
	 * This can modify in place, or return a new TypePattern if the type changes.
	 */
    public AnnotationTypePattern resolveBindings(IScope scope, Bindings bindings, 
    								             boolean allowBinding)
    { 
	if (!scope.getWorld().isInJava5Mode()) {
		scope.message(MessageUtil.error(WeaverMessages.format(WeaverMessages.ANNOTATIONS_NEED_JAVA5),
				getSourceLocation()));
		return this;
	}
	if (resolved) return this;
    	this.typePattern = typePattern.resolveBindings(scope,bindings,false,false);
    	resolved = true;
    	if (typePattern instanceof ExactTypePattern) {
    		ExactTypePattern et = (ExactTypePattern)typePattern;
			if (!et.getExactType().resolve(scope.getWorld()).isAnnotation()) {
				IMessage m = MessageUtil.error(
						WeaverMessages.format(WeaverMessages.REFERENCE_TO_NON_ANNOTATION_TYPE,et.getExactType().getName()),
						getSourceLocation());
				scope.getWorld().getMessageHandler().handleMessage(m);
				resolved = false;
			}
    		ExactAnnotationTypePattern eatp =  new ExactAnnotationTypePattern(et.getExactType().resolve(scope.getWorld()));
    		eatp.copyLocationFrom(this);
    		return eatp;
    	} else {
    		return this;
    	}
    }
    
    public AnnotationTypePattern parameterizeWith(Map typeVariableMap,World w) {
    	WildAnnotationTypePattern ret = new WildAnnotationTypePattern(typePattern.parameterizeWith(typeVariableMap,w));
    	ret.copyLocationFrom(this);
    	ret.resolved = resolved;
    	return ret;
    }

	private static final byte VERSION = 1; // rev if ser. form changes
	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.PatternNode#write(java.io.DataOutputStream)
	 */
	public void write(DataOutputStream s) throws IOException {
		s.writeByte(AnnotationTypePattern.WILD);
		s.writeByte(VERSION);
		typePattern.write(s);
		writeLocation(s);
	}

	public static AnnotationTypePattern read(VersionedDataInputStream s,ISourceContext context) throws IOException {
		AnnotationTypePattern ret;
		byte version = s.readByte();
		if (version > VERSION) {
			throw new BCException("ExactAnnotationTypePattern was written by a newer version of AspectJ");
		}
		TypePattern t = TypePattern.read(s,context);
		ret = new WildAnnotationTypePattern(t);
		ret.readLocation(context,s);
		return ret;		
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (!(obj instanceof WildAnnotationTypePattern)) return false;
		WildAnnotationTypePattern other = (WildAnnotationTypePattern) obj;
		return other.typePattern.equals(typePattern);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return 17 + 37*typePattern.hashCode();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "@(" + typePattern.toString() + ")";
	}

    public Object accept(PatternNodeVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }
}
