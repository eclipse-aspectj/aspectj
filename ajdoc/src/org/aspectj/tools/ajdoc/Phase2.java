/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Mik Kersten	  port to AspectJ 1.1+ code base
 * ******************************************************************/

package org.aspectj.tools.ajdoc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;

import java.util.*;

class Phase2 {
     static void callJavadoc( String[] javadocargs ) {
        final SecurityManager defaultSecurityManager = System.getSecurityManager();

        System.setSecurityManager( new SecurityManager() {
            public void checkExit(int status) {
                if (status == 0) {
                   throw new SecurityException();
                }
                else {
                     System.setSecurityManager(defaultSecurityManager);
                //System.out.println("Error: javadoc exited unexpectedly");
                     System.exit(0);
                     throw new SecurityException();
                }
            }
            public void checkPermission( java.security.Permission permission ) {
               if ( defaultSecurityManager  != null )
                defaultSecurityManager.checkPermission( permission );
            }
             public void checkPermission( java.security.Permission permission,
					  Object context ) {
               if ( defaultSecurityManager  != null )
                 defaultSecurityManager.checkPermission( permission, context );
            }
            } );

        try {
            com.sun.tools.javadoc.Main.main( javadocargs );
        }
        catch ( SecurityException se ) {
            // Do nothing since we expect it to be thrown
            //System.out.println( ">> se: " + se.getMessage() );
        }
        // Set the security manager back
        System.setSecurityManager( defaultSecurityManager );
    }
}
