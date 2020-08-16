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

import java.util.List;
import java.util.Vector;

import javax.tools.DocumentationTool;
import javax.tools.DocumentationTool.DocumentationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * @author Mik Kersten
 */
class JavadocRunner {

	public static void callJavadocViaToolProvider(Iterable<String> options, List<String> files) {
		DocumentationTool doctool = ToolProvider.getSystemDocumentationTool();
		StandardJavaFileManager fm = doctool.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> jfos = fm.getJavaFileObjects(files.toArray(new String[0]));
		DocumentationTask task = doctool.getTask(null/*standard System.err*/, null/*standard file manager*/,
				null/*default diagnostic listener*/, null/*standard doclet*/, options, jfos);
		task.call();
	}
}
