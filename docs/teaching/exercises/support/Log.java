/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Common Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *     PARC     initial implementation
 * ******************************************************************/


package support;

import java.util.List;
import java.util.ArrayList;

public class Log {
    static List data = new ArrayList();

    public static void write(String s) {
        data.add(s);
    }

    public static List getData() {
        return data;
    }

    public static void clear() {
        data.clear();
    }
}
