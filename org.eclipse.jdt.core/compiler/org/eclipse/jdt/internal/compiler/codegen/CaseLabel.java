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
package org.eclipse.jdt.internal.compiler.codegen;

public class CaseLabel extends Label {
	public int instructionPosition = POS_NOT_SET;
	public int backwardsBranch = POS_NOT_SET;
/**
 * CaseLabel constructor comment.
 * @param codeStream org.eclipse.jdt.internal.compiler.codegen.CodeStream
 */
public CaseLabel(CodeStream codeStream) {
	super(codeStream);
}
/*
* Put down  a refernece to the array at the location in the codestream.
*/
void branch() {
	if (position == POS_NOT_SET) {
		addForwardReference(codeStream.position);
		// Leave 4 bytes free to generate the jump offset afterwards
		codeStream.position += 4;
		codeStream.classFileOffset += 4;
	} else { //Position is set. Write it!
		codeStream.writeSignedWord(position - codeStream.position + 1);
	}
}
/*
* Put down  a refernece to the array at the location in the codestream.
*/
void branchWide() {
	if (position == POS_NOT_SET) {
		addForwardReference(codeStream.position);
		// Leave 4 bytes free to generate the jump offset afterwards
		codeStream.position += 4;
	} else { //Position is set. Write it!
		codeStream.writeSignedWord(position - codeStream.position + 1);
	}
}
public boolean isStandardLabel(){
	return false;
}
/*
* Put down  a reference to the array at the location in the codestream.
*/
public void place() {
	position = codeStream.position;
	if (instructionPosition == POS_NOT_SET)
		backwardsBranch = position;
	else {
		int offset = position - instructionPosition;
		for (int i = 0; i < forwardReferenceCount; i++) {
			codeStream.writeSignedWord(forwardReferences[i], offset);
		}
		// add the label int the codeStream labels collection
		codeStream.addLabel(this);
	}
}
/*
* Put down  a refernece to the array at the location in the codestream.
*/
void placeInstruction() {
	if (instructionPosition == POS_NOT_SET) {
		instructionPosition = codeStream.position;
		if (backwardsBranch != POS_NOT_SET) {
			int offset = backwardsBranch - instructionPosition;
			for (int i = 0; i < forwardReferenceCount; i++)
				codeStream.writeSignedWord(forwardReferences[i], offset);
			backwardsBranch = POS_NOT_SET;
		}
	}
}
}
