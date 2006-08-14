/*******************************************************************************
 * Copyright (c) 2006 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ron Bodkin
 */
package child;

import util.A;
import util.T;

aspect Advisor {
	declare parents: A* implements T;

	before() : call(* T+.*(..)) {
		System.out.println("T call");
	}
}
