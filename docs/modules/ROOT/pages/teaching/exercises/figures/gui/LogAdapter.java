/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/


package figures.gui;

import support.Log;

aspect LogAdapter {

    before(String s): call(void Log.log(String)) && args(s) {
        if (Main.panel != null) {
            Main.panel.cp.println(s);
        }
    }
}
