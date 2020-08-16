/* *******************************************************************
 * Copyright (c) 2004, 2013 IBM, VMware
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement -     initial implementation {date}
 * ******************************************************************/
package org.aspectj.apache.bcel.classfile.annotation;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ConstantPool;

public abstract class RuntimeAnnos extends Attribute {

	private List<AnnotationGen> annotations;
	private boolean visible;

	// Keep just a byte stream of the data until someone actually asks for it
	private boolean inflated = false;
	private byte[] annotation_data;

	public RuntimeAnnos(byte attrid, boolean visible, int nameIdx, int len, ConstantPool cpool) {
		super(attrid, nameIdx, len, cpool);
		this.visible = visible;
		annotations = new ArrayList<>();
	}

	public RuntimeAnnos(byte attrid, boolean visible, int nameIdx, int len, byte[] data, ConstantPool cpool) {
		super(attrid, nameIdx, len, cpool);
		this.visible = visible;
		annotations = new ArrayList<>();
		annotation_data = data;
	}

	public List<AnnotationGen> getAnnotations() {
		if (!inflated)
			inflate();
		return annotations;
	}

	public boolean areVisible() {
		return visible;
	}

	protected void readAnnotations(DataInputStream dis, ConstantPool cpool) throws IOException {
		annotation_data = new byte[length];
		dis.readFully(annotation_data, 0, length);
	}

	protected void writeAnnotations(DataOutputStream dos) throws IOException {
		if (!inflated) {
			dos.write(annotation_data, 0, length);
		} else {
			dos.writeShort(annotations.size());
			for (AnnotationGen ann : annotations) {
				ann.dump(dos);
			}
		}
	}

	
	private void inflate() {
		try {
			DataInputStream dis = new DataInputStream(new ByteArrayInputStream(annotation_data));
			int numberOfAnnotations = dis.readUnsignedShort();
			if (numberOfAnnotations > 0) {
				List<AnnotationGen> inflatedAnnotations = new ArrayList<>();
				for (int i = 0; i < numberOfAnnotations; i++) {
					inflatedAnnotations.add(AnnotationGen.read(dis, getConstantPool(), visible));
				}
				annotations = inflatedAnnotations;
			}
			dis.close();
			inflated = true;
		} catch (IOException ioe) {
			throw new RuntimeException("Unabled to inflate annotation data, badly formed? ");
		}
	}

	/** FOR TESTING ONLY: Tells you if the annotations have been inflated to an object graph */
	public boolean isInflated() {
		return inflated;
	}

}
