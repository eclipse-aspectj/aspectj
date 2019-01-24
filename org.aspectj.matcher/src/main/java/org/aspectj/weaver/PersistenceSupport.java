/* *******************************************************************
 * Copyright (c) 2009 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * ******************************************************************/
package org.aspectj.weaver;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @author Andy Clement
 */
public class PersistenceSupport {

	public static void write(CompressingDataOutputStream stream, ISourceContext sourceContext) throws IOException {
		throw new IllegalStateException();
	}

	public static void write(CompressingDataOutputStream stream, Serializable serializableObject) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(stream);
		oos.writeObject(serializableObject);
		oos.flush();
	}

}
