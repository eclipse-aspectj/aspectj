/********************************************************************
 * Copyright (c) 2008 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: Andy Clement
 *******************************************************************/
package org.aspectj.ajdt.internal.core.builder;

/**
 * Bit flags that can indicate what has changed in a configuration, see ICompilerConfiguration
 */
public interface CompilerConfigurationChangeFlags {

	int NO_CHANGES = 0x0000;
	int PROJECTSOURCEFILES_CHANGED = 0x0001;
	int JAVAOPTIONS_CHANGED = 0x0002;
	int ASPECTPATH_CHANGED = 0x0004;
	int CLASSPATH_CHANGED = 0x0008;
	int INPATH_CHANGED = 0x0010;
	int NONSTANDARDOPTIONS_CHANGED = 0x0020;
	int OUTJAR_CHANGED = 0x0040;
	int PROJECTSOURCERESOURCES_CHANGED = 0x0080;
	int OUTPUTDESTINATIONS_CHANGED = 0x0100;
	int INJARS_CHANGED = 0x0200; // deprecated, not in use any more
	int XMLCONFIG_CHANGED = 0x0400;
	int PROCESSOR_CHANGED = 0x0800;
	int EVERYTHING = 0xffff;

}
