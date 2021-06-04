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
 *     Mik Kersten	  port to AspectJ 1.1+ code base
 * ******************************************************************/

package org.aspectj.tools.ajdoc;

import javax.tools.DocumentationTool;
import javax.tools.DocumentationTool.DocumentationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * @author Mik Kersten
 */
class JavadocRunner {

	static void callJavadoc(String[] javadocArgs) {
		try {
			Class.forName("com.sun.tools.javadoc.Main")
				.getMethod("execute", String[].class)
				.invoke(null, new Object[] { javadocArgs });
		}
		catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Failed to invoke javadoc", e);
		}
	}

	public static void callJavadocViaToolProvider(Iterable<String> options, List<String> files) {
		DocumentationTool docTool = ToolProvider.getSystemDocumentationTool();
		StandardJavaFileManager fileManager = docTool.getStandardFileManager(null, null, null);
		Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjects(files.toArray(new String[0]));
		DocumentationTask task = docTool.getTask(
			null, // default output writer (System.err)
			null, // default file manager
			null, // default diagnostic listener
			null, // default doclet class
			options,
			fileObjects
		);
		task.call();
	}
}
