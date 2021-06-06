/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 * Andy Clement (SpringSource)
 * ******************************************************************/
package org.aspectj.weaver;

/**
 * Used during attribute reading to decode constant pool references.
 *
 * @author Andy Clement
 */
public interface ConstantPoolReader {

	String readUtf8(int constantPoolIndex);

}
