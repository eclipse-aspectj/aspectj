/*
 * Created on Jan 12, 2005
  */
package org.aspectj.tools.ajdoc;

import junit.framework.TestCase;

/**
 * @author Mik Kersten
 */
public class JDKVersionTest extends TestCase {

//    public void testIsUsing1point4() {
//        String v = System.getProperty("java.class.version","44.0");
//        assertTrue(("49.0".compareTo(v) > 0) && ("48.0".compareTo(v) <= 0));
//        assertFalse(Util.isExecutingOnJava5());
//    }

    public void testIsUsing1point5() {
        assertTrue(Util.isExecutingOnJava5());
    }
    
}
