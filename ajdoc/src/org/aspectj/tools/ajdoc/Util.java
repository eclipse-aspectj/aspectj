/*
 * Created on Jan 12, 2005
  */
package org.aspectj.tools.ajdoc;

/**
 * @author Mik Kersten
 */
public class Util {

    public static boolean isExecutingOnJava5() {
        String version = System.getProperty("java.class.version","44.0");
        return version.equals("49.0");
    }
    
}
