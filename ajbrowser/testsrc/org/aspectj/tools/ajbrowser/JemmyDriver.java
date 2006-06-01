/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.tools.ajbrowser;
//
//import org.netbeans.jemmy.Test;
//import org.netbeans.jemmy.Scenario;
//import org.netbeans.jemmy.ClassReference;
//import org.netbeans.jemmy.operators.JFrameOperator;
//import org.netbeans.jemmy.operators.JButtonOperator;
//
///**
// * Programmatically drives AJBrowser gui,
// * currently to do a build.
// */
//public class JemmyDriver implements Scenario {
//
//    public static final String CUT 
//        = "org.aspectj.tools.ajbrowser.Main";
//    public static final String DRIVER 
//        = "org.aspectj.tools.ajbrowser.JemmyDriver";
//    
//    // XXX need to fix literal path locations
//    public static final String USERDIR 
//        = "j:/home/wes/dev/tools/aj/examples";
//    public static final String FILENAME_RELATIVE 
//        = USERDIR + "spacewar/debug.lst";
//    public static final String FILENAME 
//        = USERDIR + "/" + FILENAME_RELATIVE;
//
//    public static void main(String[] argv) {
//        // 0 is (this) class name
//        // 1 is the location of the work directory
//        // others are parameters
//        String[] params = {DRIVER, USERDIR, FILENAME};
//        Test.main(params);
//    }
//
//    public int runIt(Object param) {
//        try {
//            String[] args = new String[]{};
//            // grab parameter - pass in .lst file
//            if (null != param) {
//                Class c = param.getClass();
//                if (c.isArray() && 
//                    (String.class == c.getComponentType())) {
//                    args = (String[]) param;
//                    if (0 < args.length) {
//                        if (FILENAME.equals(args[0])) {
//                            System.out.println("got file...");
//                        }
//                    }
//                }
//            }
//            // start application with our .lst file
//            new ClassReference(CUT).startApplication(args);
//            // wait frame
//            JFrameOperator mainFrame = new JFrameOperator("AspectJ Browser");
//            // do a build - hangs if no list file
//            new JButtonOperator(mainFrame, "Build").push();
//
//        } catch(Exception e) {
//            e.printStackTrace();
//            return(1);
//        }
//        return(0);
//    }
//
//}
