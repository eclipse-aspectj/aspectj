/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement - SpringSource
 * ******************************************************************/
package org.aspectj.ajdt.internal.compiler.lookup;

import org.aspectj.asm.internal.CharOperation;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.MemberTypeBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.aspectj.org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;
import org.aspectj.weaver.NewMemberClassTypeMunger;
import org.aspectj.weaver.ResolvedType;

/**
 * A special kind of MemberTypeBinding that is targeting some other type.
 * 
 * @author Andy Clement
 */
public class InterTypeMemberClassBinding extends MemberTypeBinding {

	public InterTypeMemberClassBinding(EclipseFactory world, NewMemberClassTypeMunger munger, ResolvedType aspectType,
			ResolvedType onType, String name, SourceTypeBinding sourceTypeOnType) {
		super(toCompoundName(onType, name), sourceTypeOnType.scope, sourceTypeOnType);
		SourceTypeBinding stb = (SourceTypeBinding) world.makeTypeBinding(aspectType);
		ReferenceBinding found = null;
		for (int i = 0; i < stb.memberTypes.length; i++) {
			ReferenceBinding rb = stb.memberTypes[i];
			char[] sn = rb.sourceName;
			if (CharOperation.equals(name.toCharArray(), sn)) {
				found = rb;
			}
		}

		if (found == null) {
			throw new IllegalStateException();
		}
		FieldBinding[] fbs = found.fields();
		this.fields = new FieldBinding[fbs.length];
		int fCounter = 0;
		for (FieldBinding fb : fbs) {
			this.fields[fCounter++] = new FieldBinding(fb, this);
		}
		// world.makeTypeBinding(onType));
		// helper interface binding is perhaps a good example of a 'new binding'
		// this.fPackage = enclosingType.fPackage;
		// //this.fileName = scope.referenceCompilationUnit().getFileName();
		// this.modifiers = ClassFileConstants.AccPublic | ClassFileConstants.AccInterface | ClassFileConstants.AccAbstract;
		// this.sourceName = enclosingType.scope.referenceContext.name;
		// this.enclosingType = enclosingType;
		// this.typeX = typeX;
		this.typeVariables = Binding.NO_TYPE_VARIABLES;
		this.memberTypes = Binding.NO_MEMBER_TYPES;
		// this.scope =enclosingType.scope;
		// this.superInterfaces = new ReferenceBinding[0];
	}

	private static char[][] toCompoundName(ResolvedType onType, String name) {
		String memberName = onType.getName() + "$" + name;
		return CharOperation.splitOn('.', memberName.toCharArray());
	}
}
