/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;

public class NLSLine {
	private List elements;

	public NLSLine() {
		this.elements = new ArrayList();
	}
	
	/**
	 * Adds a NLS element to this line.
	 */
	public void add(StringLiteral element) {
		this.elements.add(element);
	}
	
	/**
	 * returns an Iterator over NLSElements
	 */
	public Iterator iterator() {
		return this.elements.iterator();
	}
	
	public StringLiteral get(int index) {
		return (StringLiteral) this.elements.get(index);
	}
	
	public void set(int index, StringLiteral literal) {
		this.elements.set(index, literal);
	}
	
	public boolean exists(int index) {
		return index >= 0 && index < this.elements.size();
	}
	
	public int size(){
		return this.elements.size();
	}
	
	public String toString() {
		StringBuffer result= new StringBuffer();
		for (Iterator iter= iterator(); iter.hasNext(); ) {
			result.append("\t"); //$NON-NLS-1$
			result.append(iter.next().toString());
			result.append("\n"); //$NON-NLS-1$
		}
		return result.toString();
	}
}