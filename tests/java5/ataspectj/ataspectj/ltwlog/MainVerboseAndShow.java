/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
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
public class MainVerboseAndShow {

    void target() {};

    public static void main(String args[]) throws Throwable {
        new MainVerboseAndShow().target();
        
        if (!MessageHolder.startsAs(Arrays.asList(new String[]{
                "info weaving 'ataspectj/ltwlog/MainVerboseAndShow'",
                "info weaver operating in reweavable mode.  Need to verify any required types exist.",
                "weaveinfo Join point 'method-execution(void ataspectj.ltwlog.MainVerboseAndShow.target())' in Type 'ataspectj.ltwlog.MainVerboseAndShow' (MainVerboseAndShow.java:22) advised by before advice from 'ataspectj.ltwlog.Aspect1' (Aspect1.java)",
                "info weaving 'ataspectj/ltwlog/Aspect1'",
                "info weaver operating in reweavable mode.  Need to verify any required types exist."}))) {
            MessageHolder.dump();
            throw new RuntimeException("failed");
        }
    }


}
