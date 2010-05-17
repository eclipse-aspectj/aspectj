/* *******************************************************************
 * Copyright (c) 2010 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
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
