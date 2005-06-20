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
package ataspectj;

import junit.framework.TestCase;

import java.io.File;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DumpTest extends TestCase {

    public static void main(String[] args) {
        TestHelper.runAndThrowOnFailure(suite());
    }

    public static junit.framework.Test suite() {
        return new junit.framework.TestSuite(DumpTest.class);
    }

    public void testDump() {
        File f = new File("_ajdump/ataspectj/DumpTest.class");
        assertFalse(f.exists());

        DumpTestTheDump forceLoad = new DumpTestTheDump();
        f = new File("_ajdump/ataspectj/DumpTestTheDump.class");
        assertTrue(f.exists());
    }

}
