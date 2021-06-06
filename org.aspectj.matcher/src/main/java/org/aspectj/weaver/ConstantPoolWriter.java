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
 * Used during attribute writing to encode common strings/etc as constant pool references.
 *
 * @author Andy Clement
 */
public interface ConstantPoolWriter {

	int writeUtf8(String string);

}
