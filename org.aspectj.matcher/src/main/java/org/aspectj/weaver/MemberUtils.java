/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 * Andy Clement, SpringSource
 * ******************************************************************/
package org.aspectj.weaver;

/**
 * Common utility methods for members.
 *
 * @author Andy Clement
 */
public class MemberUtils {

	public static boolean isConstructor(ResolvedMember member) {
		return member.getName().equals("<init>");
	}

}
