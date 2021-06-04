/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Mik Kersten     initial implementation
 * ******************************************************************/

package org.aspectj.asm;

import java.util.EventListener;

/**
 * Compiler listeners get notified of structure model update events.
 *
 * @author Mik Kersten
 */
public interface IHierarchyListener extends EventListener {

	void elementsUpdated(IHierarchy rootNode);
}
