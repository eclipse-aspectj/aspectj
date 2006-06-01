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
 *     Mik Kersten	  port to AspectJ 1.1+ code base
 * ******************************************************************/

package org.aspectj.tools.ajdoc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Mik Kersten
 */
class JavadocRunner {
	
	static boolean has14ToolsAvailable() {
		try {
            Class jdMainClass = com.sun.tools.javadoc.Main.class;
			Class[] paramTypes = new Class[] {String[].class};
			jdMainClass.getMethod("execute", paramTypes);
        } catch (NoClassDefFoundError e) {
            return false;
        } catch (UnsupportedClassVersionError e) {
            return false;
        } catch (NoSuchMethodException e) {
			return false;
		}
		return true;
	}
	
     static void callJavadoc( String[] javadocargs ){
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
        	// for JDK 1.4 and above call the execute method...
        	Class jdMainClass = com.sun.tools.javadoc.Main.class;
        	Method executeMethod = null;
			try {
				Class[] paramTypes = new Class[] {String[].class};
				executeMethod = jdMainClass.getMethod("execute", paramTypes);
			} catch (NoSuchMethodException e) {
				 com.sun.tools.javadoc.Main.main(javadocargs); 
//				throw new UnsupportedOperationException("ajdoc requires a tools library from JDK 1.4 or later.");
			}
			try {
				executeMethod.invoke(null, new Object[] {javadocargs});
			} catch (IllegalArgumentException e1) {
				throw new RuntimeException("Failed to invoke javadoc");
			} catch (IllegalAccessException e1) {
				throw new RuntimeException("Failed to invoke javadoc");
			} catch (InvocationTargetException e1) {
				throw new RuntimeException("Failed to invoke javadoc");
			}
        	// main method is documented as calling System.exit() - which stops us dead in our tracks
            //com.sun.tools.javadoc.Main.main( javadocargs );
        }
        catch ( SecurityException se ) {
            // Do nothing since we expect it to be thrown
            //System.out.println( ">> se: " + se.getMessage() );
        }
        // Set the security manager back
        System.setSecurityManager( defaultSecurityManager );
    }
}
