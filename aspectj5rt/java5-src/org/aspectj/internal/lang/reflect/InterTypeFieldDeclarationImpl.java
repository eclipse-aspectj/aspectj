/* *******************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *   Adrian Colyer			Initial implementation
 * ******************************************************************/
package org.aspectj.internal.lang.reflect;

import java.lang.reflect.Field;
import java.lang.reflect.Type;

import org.aspectj.lang.reflect.AjType;
import org.aspectj.lang.reflect.AjTypeSystem;
import org.aspectj.lang.reflect.InterTypeFieldDeclaration;

/**
 * @author colyer
 *
 */
public class InterTypeFieldDeclarationImpl extends InterTypeDeclarationImpl
		implements InterTypeFieldDeclaration {

	private String name;
	private AjType<?> type;
	private Type genericType;
	
	/**
	 * @param decType
	 * @param target
	 * @param mods
	 */
	public InterTypeFieldDeclarationImpl(AjType<?> decType, String target,
			int mods, String name, AjType<?> type, Type genericType) {
		super(decType, target, mods);
		this.name = name;
		this.type = type;
		this.genericType = genericType;
	}
	
	public InterTypeFieldDeclarationImpl(AjType<?> decType, AjType<?> targetType, Field base) {
		super(decType,targetType,base.getModifiers());
		this.name = base.getName();
		this.type = AjTypeSystem.getAjType(base.getType());
		Type gt = base.getGenericType();
		if (gt instanceof Class) {
			this.genericType = AjTypeSystem.getAjType((Class<?>)gt);
		} else {
			this.genericType = gt;
		}
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.InterTypeFieldDeclaration#getName()
	 */
	public String getName() {
		return this.name;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.InterTypeFieldDeclaration#getType()
	 */
	public AjType<?> getType() {
		return this.type;
	}

	/* (non-Javadoc)
	 * @see org.aspectj.lang.reflect.InterTypeFieldDeclaration#getGenericType()
	 */
	public Type getGenericType() {
		return this.genericType;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(java.lang.reflect.Modifier.toString(getModifiers()));
		sb.append(" ");
		sb.append(getType().toString());
		sb.append(" ");
		sb.append(this.targetTypeName);
		sb.append(".");
		sb.append(getName());
		return sb.toString();
	}

}
