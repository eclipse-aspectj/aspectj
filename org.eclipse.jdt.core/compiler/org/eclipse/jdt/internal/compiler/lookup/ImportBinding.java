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
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class ImportBinding extends Binding {
	public char[][] compoundName;
	public boolean onDemand;
	public ImportReference reference;

	Binding resolvedImport; // must ensure the import is resolved
	
public ImportBinding(char[][] compoundName, boolean isOnDemand, Binding binding, ImportReference reference) {
	this.compoundName = compoundName;
	this.onDemand = isOnDemand;
	this.resolvedImport = binding;
	this.reference = reference;
}
/* API
* Answer the receiver's binding type from Binding.BindingID.
*/

public final int bindingType() {
	return IMPORT;
}
public char[] readableName() {
	if (onDemand)
		return CharOperation.concat(CharOperation.concatWith(compoundName, '.'), ".*".toCharArray()); //$NON-NLS-1$
	else
		return CharOperation.concatWith(compoundName, '.');
}
public String toString() {
	return "import : " + new String(readableName()); //$NON-NLS-1$
}
}
