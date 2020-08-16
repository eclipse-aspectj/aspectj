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

interface Config {

    String DECL_ID_STRING     = "__AJDECLID:";
    String DECL_ID_TERMINATOR = ":__";
    String WORKING_DIR        = "ajdocworkingdir";
    String DIR_SEP_CHAR       = "/";
    String USAGE =
                               "Usage: ajdoc <options> <source files>\n" +
                               "\n" +
                               "where <options> includes:\n"+
                               "  -public                   Show only public classes and members\n"+
                               "  -protected                Show protected/public classes and members\n"+
                               "  -package                  Show package/protected/public classes and members\n" +  // default
                               "  -private                  Show all classes and members\n" +
                               "  -help                     Display command line options\n" +
                               "  -sourcepath <pathlist>    Specify where to find source files\n" +
                               "  -classpath <pathlist>     Specify where to find user class files\n" +
                               "  -bootclasspath <pathlist> Override location of class files loaded\n" +
                               "  -d <directory>            Destination directory for output files\n" +
                               "  -windowtitle <text>       Browser window title for the documenation" +
                               "  -bottom <html-code>       Include bottom text for each page" +
                               "  -link <url>               Create links to javadoc output at <url>" +
                               "  -argfile <file>           Build config file (wildcards not supported)\n" +
                               "  -verbose                  Output messages about what Javadoc is doing\n" +
                               "  -v                        Print out the version of ajdoc\n" +
							   "  -source <version>         set source level (1.3, 1.4 or 1.5)\n" +
							   "\n" +
                               "as well as the AspectJ Compiler options.\n" +
                               "\n"+
                               "If an argument is of the form @<filename>, the file will be interpreted as\n"+
                               "a line delimited set of arguments to insert into the argument list.\n";

}
