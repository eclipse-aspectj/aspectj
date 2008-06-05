/* *******************************************************************
 * Copyright (c) 2002 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 *     Andy Clement - June 2005 - separated out from ResolvedType
 * ******************************************************************/
package org.aspectj.weaver;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.classfile.GenericSignatureParser;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.Signature.ClassSignature;
import org.aspectj.bridge.ISourceLocation;

public abstract class AbstractReferenceTypeDelegate implements ReferenceTypeDelegate {
	
	public final static String UNKNOWN_SOURCE_FILE = "<Unknown>"; // Just randomly picked, happens to match BCEL javaclass default
	
	private String sourcefilename = UNKNOWN_SOURCE_FILE; // Sourcefilename is stored only here
	protected boolean exposedToWeaver;
	protected ReferenceType resolvedTypeX;
	private ISourceContext sourceContext = SourceContextImpl.UNKNOWN_SOURCE_CONTEXT;
	protected ClassSignature cachedGenericClassTypeSignature;

    public AbstractReferenceTypeDelegate(ReferenceType resolvedTypeX, boolean exposedToWeaver) {
        this.resolvedTypeX = resolvedTypeX;
        this.exposedToWeaver = exposedToWeaver;
    }
        
	public final boolean isClass() {
    	return !isAspect() && !isInterface();
    }

	/**
	 * Designed to be overriden by EclipseType to disable collection of shadow mungers
	 * during pre-weave compilation phase
	 */
	public boolean doesNotExposeShadowMungers() {
		return false;
	}

	public boolean isExposedToWeaver() {
		return exposedToWeaver;
	}

	public ReferenceType getResolvedTypeX() {
		return resolvedTypeX;
	}
	
  
    
    public final String getSourcefilename() {
    	return sourcefilename;
    }
    
    public final void setSourcefilename(String sourceFileName) {
		this.sourcefilename = sourceFileName;
		if (sourceFileName!=null && sourceFileName.equals(AbstractReferenceTypeDelegate.UNKNOWN_SOURCE_FILE)) {
			this.sourcefilename = "Type '"+ getResolvedTypeX().getName()+"' (no debug info available)";
		} else {
			String pname = getResolvedTypeX().getPackageName();
			if (pname != null) {
				this.sourcefilename = pname.replace('.', '/') + '/' + sourceFileName;
			}
		}
		if (this.sourcefilename!=null && sourceContext instanceof SourceContextImpl) {
			((SourceContextImpl)sourceContext).setSourceFileName(this.sourcefilename);
		}
    }
    
	public ISourceLocation getSourceLocation() {
		return getSourceContext().makeSourceLocation(0, 0); 
	}
	
	public ISourceContext getSourceContext() {
		return sourceContext;
	}
	
	public void setSourceContext(ISourceContext isc) {
		this.sourceContext = isc;
	}

	public Signature.ClassSignature getGenericClassTypeSignature() {
		if (cachedGenericClassTypeSignature==null) {
			String sig = getDeclaredGenericSignature();
			if (sig!=null) {
				GenericSignatureParser parser = new GenericSignatureParser();
				cachedGenericClassTypeSignature = parser.parseAsClassSignature(sig);
			}
		}
		return cachedGenericClassTypeSignature;
	}
	
	protected Signature.FormalTypeParameter[] getFormalTypeParametersFromOuterClass() {
		List typeParameters = new ArrayList();
		ReferenceType outer = (ReferenceType)getOuterClass();
		ReferenceTypeDelegate outerDelegate = outer.getDelegate();
		AbstractReferenceTypeDelegate outerObjectType = (AbstractReferenceTypeDelegate) outerDelegate;
		if (outerObjectType.isNested()) {
			Signature.FormalTypeParameter[] parentParams = outerObjectType.getFormalTypeParametersFromOuterClass();
			for (int i = 0; i < parentParams.length; i++) {
				typeParameters.add(parentParams[i]);
			}
		}
		Signature.ClassSignature outerSig = outerObjectType.getGenericClassTypeSignature();
		if (outerSig != null) {
			for (int i = 0; i < outerSig.formalTypeParameters .length; i++) {
				typeParameters.add(outerSig.formalTypeParameters[i]);
			}
		} 
		
		Signature.FormalTypeParameter[] ret = new Signature.FormalTypeParameter[typeParameters.size()];
		typeParameters.toArray(ret);
		return ret;
	}


}