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

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import org.apache.bcel.classfile.ExceptionTable;
import org.apache.bcel.classfile.LocalVariable;
import org.apache.bcel.classfile.LocalVariableTable;
import org.apache.bcel.classfile.Method;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ShadowMunger;
import org.aspectj.weaver.TypeX;
import org.aspectj.weaver.World;

final class BcelMethod extends ResolvedMember {

	private Method method;
	private boolean isAjSynthetic;
	private ShadowMunger associatedShadowMunger;
	private AjAttribute.EffectiveSignatureAttribute effectiveSignature;

	BcelMethod(BcelObjectType declaringType, Method method) {
		super(
			method.getName().equals("<init>") ? CONSTRUCTOR : 
				(method.getName().equals("<clinit>") ? STATIC_INITIALIZATION : METHOD), 
			declaringType.getResolvedTypeX(),
			declaringType.isInterface() 
				? method.getAccessFlags() | Modifier.INTERFACE
				: method.getAccessFlags(),
			method.getName(), 
			method.getSignature());
		this.method = method;
		unpackAjAttributes(declaringType.getResolvedTypeX().getWorld());
		unpackJavaAttributes();
	}

	// ----

	private void unpackJavaAttributes() {
		ExceptionTable exnTable = method.getExceptionTable();
		checkedExceptions = (exnTable == null) 
			? TypeX.NONE
			: TypeX.forNames(exnTable.getExceptionNames());
			
		LocalVariableTable varTable = method.getLocalVariableTable();
		int len = getArity();
		if (varTable == null) {
			this.parameterNames = Utility.makeArgNames(len);
		} else {
			TypeX[] paramTypes = getParameterTypes();
			String[] paramNames = new String[len];
			int index = isStatic() ? 0 : 1;
			for (int i = 0; i < len; i++) {
				LocalVariable lv = varTable.getLocalVariable(index);
				if (lv == null) {
					paramNames[i] = "arg" + i;
				} else {
					paramNames[i] = lv.getName();
				}
				index += paramTypes[i].getSize();
			}
			this.parameterNames = paramNames;
		}
	}

	private void unpackAjAttributes(World world) {
		List as = BcelAttributes.readAjAttributes(method.getAttributes(), getSourceContext(world));
		//System.out.println("unpack: " + this + ", " + as);
		for (Iterator iter = as.iterator(); iter.hasNext();) {
			AjAttribute a = (AjAttribute) iter.next();
			if (a instanceof AjAttribute.AdviceAttribute) {
				associatedShadowMunger = ((AjAttribute.AdviceAttribute)a).reify(this, world);
				return;
			} else if (a instanceof AjAttribute.AjSynthetic) {
				isAjSynthetic = true;
			} else if (a instanceof AjAttribute.EffectiveSignatureAttribute) {
				//System.out.println("found effective: " + this);
				effectiveSignature = (AjAttribute.EffectiveSignatureAttribute)a;
			} else {
				throw new BCException("weird method attribute " + a);
			}
		}
		associatedShadowMunger = null;
	}

	public boolean isAjSynthetic() {
		return isAjSynthetic; // || getName().startsWith(NameMangler.PREFIX);
	}
	
	public ShadowMunger getAssociatedShadowMunger() {
		return associatedShadowMunger;
	}
	
	public AjAttribute.EffectiveSignatureAttribute getEffectiveSignature() {
		return effectiveSignature;
	}
	
	public Kind getKind() {
		if (associatedShadowMunger != null) {
			return ADVICE;
		} else {
			return super.getKind();
		}
	}
}
