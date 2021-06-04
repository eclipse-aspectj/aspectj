/* *******************************************************************
 * Copyright (c) 2006 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Andy Clement IBM     initial implementation
 * ******************************************************************/
package org.aspectj.weaver;

public class ArrayAnnotationValue extends AnnotationValue {

	private AnnotationValue[] values;

	public ArrayAnnotationValue() {
		super(AnnotationValue.ARRAY);
	}

	public void setValues(AnnotationValue[] values) {
		this.values = values;
	}

	public ArrayAnnotationValue(AnnotationValue[] values) {
		super(AnnotationValue.ARRAY);
		this.values = values;
	}

	public AnnotationValue[] getValues() {
		return values;
	}

	public String stringify() {
		StringBuffer sb = new StringBuffer();
		sb.append("[");
		for (int i = 0; i < values.length; i++) {
			sb.append(values[i].stringify());
			if (i + 1 < values.length)
				sb.append(",");
		}
		sb.append("]");
		return sb.toString();
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for (int i = 0; i < values.length; i++) {
			sb.append(values[i].toString());
			if ((i + 1) < values.length)
				sb.append(",");
		}
		sb.append("}");
		return sb.toString();
	}

}
