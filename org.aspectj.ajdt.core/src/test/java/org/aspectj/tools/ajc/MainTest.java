/* *******************************************************************
 * Copyright (c) 2004 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Wes Isberg     initial implementation
 * ******************************************************************/
package org.aspectj.tools.ajc;

import org.aspectj.bridge.AbortException;

import java.util.ArrayList;
import java.util.List;

public class MainTest extends AjcTestCase {

  public void testBareMainUsage() {
    List<String> fails = new ArrayList<>();
    List<String> errors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    List<String> infos = new ArrayList<>();
    List<String> usages = new ArrayList<>();
    Main.bareMain(new String[] { "-?" }, false, fails, errors, warnings, infos, usages);
    assertNotNull(
      "usage text not found in compiler output",
      usages.stream()
        .filter(message -> message.contains("AspectJ-specific options:"))
        .findFirst()
        .orElse(null)
    );
  }

  public void testBareMainUsageX() {
    List<String> fails = new ArrayList<>();
    List<String> errors = new ArrayList<>();
    List<String> warnings = new ArrayList<>();
    List<String> infos = new ArrayList<>();
    List<String> usages = new ArrayList<>();
    Main.bareMain(new String[] { "-X" }, false, fails, errors, warnings, infos, usages);
    assertNotNull(
      "usage text not found in compiler output",
      usages.stream()
        .filter(message -> message.contains("AspectJ-specific non-standard options:"))
        .findFirst()
        .orElse(null)
    );
    }

  public void testAjcUsageX() {
    CompilationResult compilationResult = ajc(null, new String[] { "-X" });
    MessageSpec messageSpec = new MessageSpec(
      null, null, null, null, null,
      newMessageList(new Message("AspectJ-specific non-standard options:"))
    );
    assertMessages(compilationResult, "Expecting xoptions usage message", messageSpec);
    }

  public void testMainMessageHolderFail() {
    	try {
    		new Main().runMain(new String[] {"-messageHolder","org.xyz.abc"},false);
      fail("ajc should have thrown abort exception");
    }
    catch (AbortException ex) {
    		// good
    	}
    }

  public void testMainMessageHolderOk() {
    	Main main = new Main();
    	main.runMain(new String[] {"-messageHolder","org.aspectj.tools.ajc.TestMessageHolder"},false);
    	assertSame("ajc should be using our message handler",TestMessageHolder.class,main.getHolder().getClass());
    }

}
