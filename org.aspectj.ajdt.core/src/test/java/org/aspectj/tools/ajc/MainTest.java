/* *******************************************************************
 * Copyright (c) 2004 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

package org.aspectj.tools.ajc;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.aspectj.bridge.AbortException;

/**
 * 
 */
public class MainTest extends AjcTestCase {
	
    public void testMainbare() {
        List<String> list = new ArrayList<>();
// Usage now printed by Eclipse compiler so doesn't appear here in our message list
//        Main.bareMain(new String[] {"-help"}, false, list, null, null, null);
//        assertTrue(1 == list.size());
        Main.bareMain(new String[] {"-X"}, false, list, null, null, null);
        assertTrue(1 == list.size());        Object o = list.get(0);
        assertTrue(o instanceof String);
//        assertTrue(-1 != ((String)o).indexOf("-aspectpath"));
//        assertTrue(-1 != ((String)o).indexOf("-incremental"));
    }
    
    public void testDashX() {
    	String xoptionText = ResourceBundle.getBundle("org.aspectj.ajdt.ajc.messages").getString("xoption.usage");
        xoptionText = "non-standard options:"; //xoptionText.substring("{0}".length());
		CompilationResult result = ajc(null,new String[] {"-X"});
		assertMessages(result,"Expecting xoptions usage message",
				new MessageSpec(null,null,null,newMessageList(new Message(xoptionText)),null));
    }
    
    public void testDashMessageHolder() {
    	try {
    		new Main().runMain(new String[] {"-messageHolder","org.xyz.abc"},false);
    		fail ("Should have thrown abort exception");
    	} catch (AbortException ex) {
    		// good
    	}
    }
    
    public void testDashMessageHolderOk() {
    	Main main = new Main();
    	main.runMain(new String[] {"-messageHolder","org.aspectj.tools.ajc.TestMessageHolder"},false);
    	assertSame("ajc should be using our message handler",TestMessageHolder.class,main.getHolder().getClass());
    }
}
