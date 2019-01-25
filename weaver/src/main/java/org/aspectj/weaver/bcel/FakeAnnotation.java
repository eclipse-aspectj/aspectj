/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * initial implementation              Andy Clement
 *******************************************************************************/
package org.aspectj.weaver.bcel;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.NameValuePair;

/**
 * For implementing declare @type interacting with declare @parents during compilation - we need to be able to add an annotation to
 * 'binary type binding' (this is how types are seen during incremental compilation). Unlike a SourceTypeBinding - a
 * BinaryTypeBinding does not allow easy interaction with its annotations - so what we do is take the eclipse annotation, suck out
 * the name/signature and visibility and put that information in a 'FakeAnnotation'. The FakeAnnotation is attached to the BCEL
 * delegate for the binary type binding - this will allow type resolution to succeed correctly. The FakeAnnotation never makes it to
 * disk, since the weaver does the job properly, attaching a real annotation.
 */
public class FakeAnnotation extends AnnotationGen {

	private String name;
	private String sig;
	private boolean isRuntimeVisible;

	public FakeAnnotation(String name, String sig, boolean isRuntimeVisible) {
		super(null, null, true, null);
		this.name = name;
		this.sig = sig;
		this.isRuntimeVisible = isRuntimeVisible;
	}

	public String getTypeName() {
		return name;
	}

	public String getTypeSignature() {
		return sig;
	}

	public void addElementNameValuePair(NameValuePair evp) {
		// doesnt need to know about name/value pairs
	}

	public void dump(DataOutputStream dos) throws IOException {
		// should be serialized
	}

	public int getTypeIndex() {
		return 0;
	}

	public List getValues() {
		return null;
	}

	public boolean isRuntimeVisible() {
		return isRuntimeVisible;
	}

	protected void setIsRuntimeVisible(boolean b) {
	}

	public String toShortString() {
		return "@" + this.name;
	}

	public String toString() {
		return this.name;
	}
}
