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
	private String memberTypeName;
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

	public static ResolvedTypeMunger read(VersionedDataInputStream stream, ISourceContext context) throws IOException {
		/* int version = */stream.readInt();
		String memberTypeName = stream.readUTF();
		UnresolvedType targetType = UnresolvedType.read(stream);
		ISourceLocation sourceLocation = readSourceLocation(stream);
		List<String> typeVarAliases = readInTypeAliases(stream);

		NewMemberClassTypeMunger newInstance = new NewMemberClassTypeMunger(targetType, memberTypeName);
		newInstance.setTypeVariableAliases(typeVarAliases);
		newInstance.setSourceLocation(sourceLocation);
		return newInstance;
	}

	public UnresolvedType getDeclaringType() {
		return targetType;
	}

	public String getMemberTypeName() {
		return memberTypeName;
	}

}