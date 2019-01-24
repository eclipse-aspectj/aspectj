/********************************************************************
 * Copyright (c) 2005 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *       Helen Hawkins - Initial implementation
 *******************************************************************/
package org.aspectj.weaver;

import java.io.DataInputStream;
import java.io.IOException;

import org.aspectj.util.TypeSafeEnum;

/**
 * A TypeSafeEnum similar to the Java5 ElementType Enum
 */
public class AnnotationTargetKind extends TypeSafeEnum {

	public AnnotationTargetKind(String name, int key) {
		super(name, key);
	}

	public static AnnotationTargetKind read(DataInputStream s) throws IOException {
		int key = s.readByte();
		switch (key) {
		case 1:
			return ANNOTATION_TYPE;
		case 2:
			return CONSTRUCTOR;
		case 3:
			return FIELD;
		case 4:
			return LOCAL_VARIABLE;
		case 5:
			return METHOD;
		case 6:
			return PACKAGE;
		case 7:
			return PARAMETER;
		case 8:
			return TYPE;
		}
		throw new BCException("weird annotation target kind " + key);
	}

	public static final AnnotationTargetKind ANNOTATION_TYPE = new AnnotationTargetKind("ANNOTATION_TYPE", 1);
	public static final AnnotationTargetKind CONSTRUCTOR = new AnnotationTargetKind("CONSTRUCTOR", 2);
	public static final AnnotationTargetKind FIELD = new AnnotationTargetKind("FIELD", 3);
	public static final AnnotationTargetKind LOCAL_VARIABLE = new AnnotationTargetKind("LOCAL_VARIABLE", 4);
	public static final AnnotationTargetKind METHOD = new AnnotationTargetKind("METHOD", 5);
	public static final AnnotationTargetKind PACKAGE = new AnnotationTargetKind("PACKAGE", 6);
	public static final AnnotationTargetKind PARAMETER = new AnnotationTargetKind("PARAMETER", 7);
	public static final AnnotationTargetKind TYPE = new AnnotationTargetKind("TYPE", 8);

}
