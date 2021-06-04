/* *******************************************************************
 * Copyright (c) 2004 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Matthew Webster, Adrian Colyer,
 *     Martin Lippert     initial implementation
 * ******************************************************************/

package org.aspectj.weaver.tools;

import java.net.URL;

/**
 * An interface for weaving class loaders to provide callbacks for a
 * WeavingAdaptor.
 */
public interface WeavingClassLoader extends GeneratedClassHandler {

	/**
	 * Returns the aspects to be used by a WeavingAdaptor to weave classes
	 * defined by the class loader.
	 * @return the aspects used for weaving classes.
	 */
	URL[] getAspectURLs();

}
