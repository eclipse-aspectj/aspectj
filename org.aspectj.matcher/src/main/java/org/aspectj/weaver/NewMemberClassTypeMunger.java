/* *******************************************************************
 * Copyright (c) 2010 SpringSource, Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.IOException;
import java.util.List;

import org.aspectj.bridge.ISourceLocation;

/**
 * Weaver representation of an intertype declared member class. The munger captures the name of the type being declared and the
 * target.
 * 
 * @author Andy Clement
 * @since 1.6.9
 */
public class NewMemberClassTypeMunger extends ResolvedTypeMunger {

	private UnresolvedType targetType;
	private String memberTypeName; // short (last part of) name
	private int version = 1; // 1.6.9m2

	public NewMemberClassTypeMunger(UnresolvedType targetType, String memberTypeName) {
		super(ResolvedTypeMunger.InnerClass, null);
		this.targetType = targetType;
		this.memberTypeName = memberTypeName;
	}

	@Override
	public void write(CompressingDataOutputStream stream) throws IOException {
		kind.write(stream);
		stream.writeInt(version);
		targetType.write(stream);
		stream.writeUTF(memberTypeName);
		writeSourceLocation(stream);
		writeOutTypeAliases(stream);
	}

	public static ResolvedTypeMunger readInnerClass(VersionedDataInputStream stream, ISourceContext context) throws IOException {
		/* int version = */stream.readInt();
		UnresolvedType targetType = UnresolvedType.read(stream);
		String memberTypeName = stream.readUTF();
		ISourceLocation sourceLocation = readSourceLocation(stream);
		List<String> typeVarAliases = readInTypeAliases(stream);

		NewMemberClassTypeMunger newInstance = new NewMemberClassTypeMunger(targetType, memberTypeName);
		newInstance.setTypeVariableAliases(typeVarAliases);
		newInstance.setSourceLocation(sourceLocation);
		return newInstance;
	}

	public UnresolvedType getTargetType() {
		return targetType;
	}

	public UnresolvedType getDeclaringType() {
		return targetType;
	}

	public String getMemberTypeName() {
		return memberTypeName;
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + kind.hashCode();
		result = 37 * result + memberTypeName.hashCode();
		result = 37 * result + targetType.hashCode();
		result = 37 * result + ((typeVariableAliases == null) ? 0 : typeVariableAliases.hashCode());
		return result;
	}

	public boolean equals(Object other) {
		if (!(other instanceof NewMemberClassTypeMunger)) {
			return false;
		}
		NewMemberClassTypeMunger o = (NewMemberClassTypeMunger) other;
		return ((kind == null) ? (o.kind == null) : kind.equals(o.kind))
				&& memberTypeName.equals(o.memberTypeName)
				&& targetType.equals(o.targetType)
				&& ((typeVariableAliases == null) ? (o.typeVariableAliases == null) : typeVariableAliases
						.equals(o.typeVariableAliases));
	}
}