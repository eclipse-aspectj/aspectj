/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.internal.lang.reflect;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.InterTypeDeclaration;

/**
 * @author colyer
 *
 */
public class InterTypeDeclarationImpl implements InterTypeDeclaration {

	private AjType<?> declaringType;
	protected String targetTypeName;
	private AjType<?> targetType;
	private int modifiers;

	public InterTypeDeclarationImpl(AjType<?> decType, String target, int mods) {
		this.declaringType = decType;
		this.targetTypeName = target;
		this.modifiers = mods;
		try {
			this.targetType = (AjType<?>) StringToType.stringToType(target, decType.getJavaClass());
		} catch (ClassNotFoundException cnf) {
			// we'll only report this later if the user asks for the target type.
		}
	}

	public InterTypeDeclarationImpl(AjType<?> decType, AjType<?> targetType, int mods) {
		this.declaringType = decType;
		this.targetType = targetType;
		this.targetTypeName = targetType.getName();
		this.modifiers = mods;
	}

	public AjType<?> getDeclaringType() {
		return this.declaringType;
	}

	public AjType<?> getTargetType() throws ClassNotFoundException {
		if (this.targetType == null) throw new ClassNotFoundException(this.targetTypeName);
		return this.targetType;
	}

	public int getModifiers() {
		return this.modifiers;
	}

}
