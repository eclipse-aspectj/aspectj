/*
Copyright (c) 2001-2002 Palo Alto Research Center Incorporated. All Rights Reserved.
 */

package figures.gui;

import support.Log;

aspect LogAdapter {

    before(String s): call(void Log.log(String)) && args(s) {
        if (Main.panel != null) {
            Main.panel.cp.println(s);
        }
    }
}
