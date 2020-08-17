/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
