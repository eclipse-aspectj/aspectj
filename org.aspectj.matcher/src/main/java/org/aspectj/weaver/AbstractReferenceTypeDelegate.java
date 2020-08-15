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
import java.util.Collections;
import java.util.List;

import org.aspectj.bridge.ISourceLocation;
import org.aspectj.util.GenericSignature;
import org.aspectj.util.GenericSignature.ClassSignature;
import org.aspectj.util.GenericSignature.FormalTypeParameter;
import org.aspectj.util.GenericSignatureParser;
import org.aspectj.weaver.AjAttribute.WeaverVersionInfo;

public abstract class AbstractReferenceTypeDelegate implements ReferenceTypeDelegate {

	private String sourcefilename = UNKNOWN_SOURCE_FILE;
	private ISourceContext sourceContext = SourceContextImpl.UNKNOWN_SOURCE_CONTEXT;

	protected boolean exposedToWeaver;
	protected ReferenceType resolvedTypeX;
	protected ClassSignature cachedGenericClassTypeSignature;

	// Happens to match Bcel javaClass default of '<Unknown>'
	public final static String UNKNOWN_SOURCE_FILE = "<Unknown>";

	public AbstractReferenceTypeDelegate(ReferenceType resolvedTypeX, boolean exposedToWeaver) {
		this.resolvedTypeX = resolvedTypeX;
		this.exposedToWeaver = exposedToWeaver;
	}

	@Override
	public final boolean isClass() {
		return !isAspect() && !isInterface();
	}

	@Override
	public boolean isCacheable() {
		return false;
	}

	/**
	 * Designed to be overriden by EclipseType to disable collection of shadow mungers during pre-weave compilation phase
	 */
	@Override
	public boolean doesNotExposeShadowMungers() {
		return false;
	}

	@Override
	public boolean isExposedToWeaver() {
		return exposedToWeaver;
	}

	@Override
	public ReferenceType getResolvedTypeX() {
		return resolvedTypeX;
	}

	@Override
	public final String getSourcefilename() {
		return sourcefilename;
	}

	public final void setSourcefilename(String sourceFileName) {
		sourcefilename = sourceFileName;
		if (sourceFileName != null && sourceFileName.equals(AbstractReferenceTypeDelegate.UNKNOWN_SOURCE_FILE)) {
			sourcefilename = "Type '" + getResolvedTypeX().getName() + "' (no debug info available)";
		} else {
			String pname = getResolvedTypeX().getPackageName();
			if (pname != null) {
				sourcefilename = pname.replace('.', '/') + '/' + sourceFileName;
			}
		}
		if (sourcefilename != null && sourceContext instanceof SourceContextImpl) {
			((SourceContextImpl) sourceContext).setSourceFileName(sourcefilename);
		}
	}

	public ISourceLocation getSourceLocation() {
		return getSourceContext().makeSourceLocation(0, 0);
	}

	@Override
	public ISourceContext getSourceContext() {
		return sourceContext;
	}

	public void setSourceContext(ISourceContext isc) {
		sourceContext = isc;
	}

	public GenericSignature.ClassSignature getGenericClassTypeSignature() {
		if (cachedGenericClassTypeSignature == null) {
			String sig = getDeclaredGenericSignature();
			if (sig != null) {
				GenericSignatureParser parser = new GenericSignatureParser();
				cachedGenericClassTypeSignature = parser.parseAsClassSignature(sig);
			}
		}
		return cachedGenericClassTypeSignature;
	}

	protected GenericSignature.FormalTypeParameter[] getFormalTypeParametersFromOuterClass() {
		List<GenericSignature.FormalTypeParameter> typeParameters = new ArrayList<>();
		ResolvedType outerClassType = getOuterClass();
		if (!(outerClassType instanceof ReferenceType)) {
			if (outerClassType == null) {
				return GenericSignature.FormalTypeParameter.NONE;
			} else {
				if (System.getProperty("aspectj.debug565713","false").toLowerCase().equals("true")) {
					System.out.println("DEBUG 565713: Whilst processing type '" + this.resolvedTypeX.getSignature()+
							"' - cannot cast the outer type to a reference type.  Signature=" + outerClassType.getSignature() +
							" toString()=" + outerClassType.toString()+" class=" + outerClassType.getClassName());
					return GenericSignature.FormalTypeParameter.NONE;
				} else {
					throw new BCException("Whilst processing type '" + this.resolvedTypeX.getSignature()
					+ "' - cannot cast the outer type to a reference type.  Signature=" + outerClassType.getSignature()
					+ " toString()=" + outerClassType.toString()+" class=" + outerClassType.getClassName());
				}
			}
		}
		ReferenceType outer = (ReferenceType) outerClassType;
		ReferenceTypeDelegate outerDelegate = outer.getDelegate();
		AbstractReferenceTypeDelegate outerObjectType = (AbstractReferenceTypeDelegate) outerDelegate;
		if (outerObjectType.isNested()) {
			GenericSignature.FormalTypeParameter[] parentParams = outerObjectType.getFormalTypeParametersFromOuterClass();
			Collections.addAll(typeParameters, parentParams);
		}
		GenericSignature.ClassSignature outerSig = outerObjectType.getGenericClassTypeSignature();
		if (outerSig != null) {
			Collections.addAll(typeParameters, outerSig.formalTypeParameters);
		}

		GenericSignature.FormalTypeParameter[] ret = new GenericSignature.FormalTypeParameter[typeParameters.size()];
		typeParameters.toArray(ret);
		return ret;
	}

	@Override
	public boolean copySourceContext() {
		return true;
	}

	@Override
	public int getCompilerVersion() {
		return WeaverVersionInfo.getCurrentWeaverMajorVersion();
	}

	@Override
	public void ensureConsistent() {

	}

	@Override
	public boolean isWeavable() {
		return false;
	}

	@Override
	public boolean hasBeenWoven() {
		return false;
	}
}