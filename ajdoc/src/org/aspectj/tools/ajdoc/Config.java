/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Mik Kersten	  port to AspectJ 1.1+ code base
 * ******************************************************************/

package org.aspectj.tools.ajdoc;

interface Config {
	
    static final String DECL_ID_STRING     = "__AJDECLID:";
    static final String DECL_ID_TERMINATOR = ":__";
    static final String WORKING_DIR        = "ajdocworkingdir";
    static final String DIR_SEP_CHAR       = "/";
    static final String USAGE =
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
                               "  -argfile <file>           the file is a line-delimted list of arguments" +
                               "  -verbose                  Output messages about what Javadoc is doing\n" +
                               "  -v                        Print out the version of ajdoc" +
                               "\n"+
                               "If an argument is of the form @<filename>, the file will be interpreted as\n"+
                               "a line delimited set of arguments to insert into the argument list.";

}
