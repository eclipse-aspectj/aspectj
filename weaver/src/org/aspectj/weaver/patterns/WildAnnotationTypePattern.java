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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.util.FuzzyBoolean;
import org.aspectj.weaver.AnnotatedElement;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.ResolvedTypeX;
import org.aspectj.weaver.TypeX;

/**
 * @author colyer
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WildAnnotationTypePattern extends AnnotationTypePattern {

	private TypePattern typePattern;
	
	/**
	 * 
	 */
	public WildAnnotationTypePattern(TypePattern typePattern) {
		super();
		this.typePattern = typePattern;
	}


	/* (non-Javadoc)
	 * @see org.aspectj.weaver.patterns.AnnotationTypePattern#matches(org.aspectj.weaver.AnnotatedElement)
	 */
	public FuzzyBoolean matches(AnnotatedElement annotated) {
		// matches if the type of any of the annotations on the AnnotatedElement is
		// matched by the typePattern.
		ResolvedTypeX[] annTypes = annotated.getAnnotationTypes();
		for (int i = 0; i < annTypes.length; i++) {
			if (typePattern.matches(annTypes[i],TypePattern.STATIC).alwaysTrue()) {
				return FuzzyBoolean.YES;
			}
		}
		return FuzzyBoolean.NO;
	}

	/**
	 * This can modify in place, or return a new TypePattern if the type changes.
	 */
    public AnnotationTypePattern resolveBindings(IScope scope, Bindings bindings, 
    								             boolean allowBinding)
    { 
    	this.typePattern = typePattern.resolveBindings(scope,bindings,false,false);
    	if (typePattern instanceof ExactTypePattern) {
    		ExactTypePattern et = (ExactTypePattern)typePattern;
    		return new ExactAnnotationTypePattern(et.getExactType());
    	} else {
    		return this;
    	}
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

	public static AnnotationTypePattern read(DataInputStream s,ISourceContext context) throws IOException {
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
	
}
