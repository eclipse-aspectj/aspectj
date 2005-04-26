/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.systemtest.ajc150.ataspectj;

import org.aspectj.testing.AutowiredXMLBasedAjcTestCase;
import org.aspectj.testing.XMLBasedAjcTestCase;

import java.io.File;

import junit.framework.Test;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class AtAjMisuseTests extends AutowiredXMLBasedAjcTestCase {

    protected File getSpecFile() {
        return new File("../tests/src/org/aspectj/systemtest/ajc150/ataspectj/misuse.xml");
    }

    public static Test suite() {
        return AutowiredXMLBasedAjcTestCase.loadSuite(AtAjMisuseTests.class);
    }
}
