/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.Field;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;

final class BcelField extends ResolvedMember {

	private Field field;
	private boolean isAjSynthetic;

	BcelField(BcelObjectType declaringType, Field field) {
		super(
			FIELD, 
			declaringType.getResolvedTypeX(),
			field.getAccessFlags(),
			field.getName(), 
			field.getSignature());
		this.field = field;
		unpackAttributes(declaringType.getResolvedTypeX().getWorld());
		checkedExceptions = TypeX.NONE;
	}

	// ----
	
	private void unpackAttributes(World world) {
		List as = BcelAttributes.readAjAttributes(field.getAttributes(), getSourceContext(world));
		for (Iterator iter = as.iterator(); iter.hasNext();) {
			AjAttribute a = (AjAttribute) iter.next();
			if (a instanceof AjAttribute.AjSynthetic) {
				isAjSynthetic = true;
			} else {
				throw new BCException("weird field attribute " + a);
			}
		}
		isAjSynthetic = false;
	}

	public boolean isAjSynthetic() {
		return isAjSynthetic; // || getName().startsWith(NameMangler.PREFIX);
	}
}
