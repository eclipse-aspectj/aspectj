/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

package org.aspectj.tools.ant.taskdefs;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;

import java.io.File;

import junit.framework.TestCase;

/**
 *
 */
public class AjdocTest extends TestCase {

	public AjdocTest(String name) {
		super(name);
	}

    public void testSource14() {
        new File("bin/AjdocTest").mkdirs();

        Ajdoc task = new Ajdoc();
        Project p = new Project();
        task.setProject(p);
        task.setSource("1.4");
        Path ppath = new Path(p,"../taskdefs/testdata");
        task.setSourcepath(ppath);
        task.setIncludes("Ajdoc14Source.java");
        task.setDestdir("bin/AjdocTest");
        task.setClasspath(new Path(p, "../lib/test/aspectjrt.jar"));
        task.execute();
    }

    public void testHelp() {
        Ajdoc task = new Ajdoc();
        Project p = new Project();
        task.setProject(p);
        task.setSourcepath(new Path(p, "../taskdefs/testdata"));
        task.setIncludes("none");
        task.setDestdir("bin/AjdocTest");
        task.execute();
    }
}
