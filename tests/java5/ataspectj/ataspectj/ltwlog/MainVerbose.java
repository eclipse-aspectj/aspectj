/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package ataspectj.ltwlog;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class MainVerbose {

    void target() {};

    public static void main(String args[]) throws Throwable {
        new MainVerbose().target();
        if (!MessageHolder.startsAs(Arrays.asList(new String[]{
                "info weaving 'ataspectj/ltwlog/MainVerbose'",
                "info weaving 'ataspectj/ltwlog/Aspect1'"}))) {
            MessageHolder.dump();
            throw new RuntimeException("failed");
        }
    }


}
