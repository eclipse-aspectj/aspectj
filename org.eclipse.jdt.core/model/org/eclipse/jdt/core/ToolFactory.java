/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.core.util.IClassFileDisassembler;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.util.ClassFileReader;
import org.eclipse.jdt.internal.core.util.Disassembler;
import org.eclipse.jdt.internal.formatter.CodeFormatter;

/**
 * Factory for creating various compiler tools, such as scanners, parsers and compilers.
 * <p>
 *  This class provides static methods only; it is not intended to be instantiated or subclassed by clients.
 * </p>
 * 
 * @since 2.0
 */
public class ToolFactory {

	/**
	 * Create an instance of a code formatter. A code formatter implementation can be contributed via the 
	 * extension point "org.eclipse.jdt.core.codeFormatter". If unable to find a registered extension, the factory 
	 * will default to using the default code formatter.
	 * 
	 * @return an instance of a code formatter
	 * @see ICodeFormatter
	 * @see ToolFactory#createDefaultCodeFormatter(Map)
	 */
	public static ICodeFormatter createCodeFormatter(){
		
			Plugin jdtCorePlugin = JavaCore.getPlugin();
			if (jdtCorePlugin == null) return null;
		
			IExtensionPoint extension = jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.FORMATTER_EXTPOINT_ID);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for(int i = 0; i < extensions.length; i++){
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
						IPluginDescriptor plugin = extension.getDeclaringPluginDescriptor();
						if (plugin.isPluginActivated()) {
							for(int j = 0; j < configElements.length; j++){
								try {
									Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
									if (execExt instanceof ICodeFormatter){
										// use first contribution found
										return (ICodeFormatter)execExt;
									}
								} catch(CoreException e){
								}
							}
						}
				}	
			}
		// no proper contribution found, use default formatter			
		return createDefaultCodeFormatter(null);
	}

	/**
	 * Create an instance of the built-in code formatter. A code formatter implementation can be contributed via the 
	 * extension point "org.eclipse.jdt.core.codeFormatter". If unable to find a registered extension, the factory will 
	 * default to using the default code formatter.
	 * 
	 * @param options - the options map to use for formatting with the default code formatter. Recognized options
	 * 	are documented on <code>JavaCore#getDefaultOptions()</code>. If set to <code>null</code>, then use 
	 * 	the current settings from <code>JavaCore#getOptions</code>.
	 * @return an instance of the built-in code formatter
	 * @see ICodeFormatter
	 * @see ToolFactory#createCodeFormatter()
	 * @see JavaCore#getOptions()
	 */
	public static ICodeFormatter createDefaultCodeFormatter(Map options){

		if (options == null) options = JavaCore.getOptions();
		return new CodeFormatter(options);
	}
	
	/**
	 * Create a classfile bytecode disassembler, able to produce a String representation of a given classfile.
	 * 
	 * @return a classfile bytecode disassembler
	 * @see IClassFileDisassembler
	 */
	public static IClassFileDisassembler createDefaultClassFileDisassembler(){
		return new Disassembler();
	}
	
	/**
	 * Create a default classfile reader, able to expose the internal representation of a given classfile
	 * according to the decoding flag used to initialize the reader.
	 * Answer null if the file named fileName doesn't represent a valid .class file.
	 * 
	 * The decoding flags are described in IClassFileReader.
	 * 
	 * @param fileName the name of the file to be read
	 * @param decodingFlag the flag used to decode the class file reader.
	 * @return a default classfile reader
	 * 
	 * @see IClassFileReader
	 */
	public static IClassFileReader createDefaultClassFileReader(String fileName, int decodingFlag){
		try {
			return new ClassFileReader(Util.getFileByteContent(new File(fileName)), decodingFlag);
		} catch(ClassFormatException e) {
			return null;
		} catch(IOException e) {
			return null;
		}
	}	
	
	/**
	 * Create a classfile reader onto a classfile Java element.
	 * Create a default classfile reader, able to expose the internal representation of a given classfile
	 * according to the decoding flag used to initialize the reader.
	 * Answer null if the file named fileName doesn't represent a valid .class file.
	 * 
	 * The decoding flags are described in IClassFileReader.
	 * 
	 * @param classfile the classfile element to introspect
	 * @param decodingFlag the flag used to decode the class file reader.
	 * @return a default classfile reader
	 * 
	 * @see IClassFileReader
	 */
	public static IClassFileReader createDefaultClassFileReader(IClassFile classfile, int decodingFlag){

		IPath filePath = classfile.getPath();
		IPackageFragmentRoot root = (IPackageFragmentRoot) classfile.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		if (root != null){
			try {
				IPath rootPath = root.getPath();
				if (root instanceof JarPackageFragmentRoot) {
						
					String archiveName = ((JarPackageFragmentRoot)root).getJar().getName();
					String entryName = classfile.getParent().getElementName();
					entryName = entryName.replace('.', '/');
					if (entryName.equals("")) { //$NON-NLS-1$
						entryName += classfile.getElementName();
					} else {
						entryName += '/' + classfile.getElementName();
					}
					return createDefaultClassFileReader(archiveName, entryName, decodingFlag);
				} else {
					return createDefaultClassFileReader(classfile.getCorrespondingResource().getLocation().toOSString(), decodingFlag);
				}
			} catch(CoreException e){
			}
		}
		return null;
	}

	/**
	 * Create a default classfile reader, able to expose the internal representation of a given classfile
	 * according to the decoding flag used to initialize the reader.
	 * Answer null if the file named zipFileName doesn't represent a valid zip file or if the zipEntryName
	 * is not a valid entry name for the specified zip file or if the bytes don't represent a valid
	 * .class file according to the JVM specifications.
	 * 
	 * The decoding flags are described in IClassFileReader.
	 * 
	 * @param zipFileName the name of the zip file
	 * @param zipEntryName the name of the entry in the zip file to be read
	 * @param decodingFlag the flag used to decode the class file reader.
	 * @return a default classfile reader
	 * @see IClassFileReader
	 */
	public static IClassFileReader createDefaultClassFileReader(String zipFileName, String zipEntryName, int decodingFlag){
		try {
			ZipFile zipFile = new ZipFile(zipFileName);
			ZipEntry zipEntry = zipFile.getEntry(zipEntryName);
			if (zipEntry == null) {
				return null;
			}
			if (!zipEntryName.toLowerCase().endsWith(".class")) {//$NON-NLS-1$
				return null;
			}
			byte classFileBytes[] = Util.getZipEntryByteContent(zipEntry, zipFile);
			return new ClassFileReader(classFileBytes, decodingFlag);
		} catch(ClassFormatException e) {
			return null;
		} catch(IOException e) {
			return null;
		}
	}	
	
	/**
	 * Create a scanner, indicating the level of detail requested for tokenizing. The scanner can then be
	 * used to tokenize some source in a Java aware way.
	 * Here is a typical scanning loop:
	 * 
	 * <code>
	 * <pre>
	 *   IScanner scanner = ToolFactory.createScanner(false, false, false, false);
	 *   scanner.setSource("int i = 0;".toCharArray());
	 *   while (true) {
	 *     int token = scanner.getNextToken();
	 *     if (token == ITerminalSymbols.TokenNameEOF) break;
	 *     System.out.println(token + " : " + new String(scanner.getCurrentTokenSource()));
	 *   }
	 * </pre>
	 * </code>
	 * 
	 * @param tokenizeComments if set to <code>false</code>, comments will be silently consumed
	 * @param tokenizeWhiteSpace if set to <code>false</code>, white spaces will be silently consumed,
	 * @param assertKeyword if set to <code>false</code>, occurrences of 'assert' will be reported as identifiers
	 * (<code>ITerminalSymbols#TokenNameIdentifier</code>), whereas if set to <code>true</code>, it
	 * would report assert keywords (<code>ITerminalSymbols#TokenNameassert</code>). Java 1.4 has introduced
	 * a new 'assert' keyword.
	 * @param recordLineSeparator if set to <code>true</code>, the scanner will record positions of encountered line 
	 * separator ends. In case of multi-character line separators, the last character position is considered. These positions
	 * can then be extracted using <code>IScanner#getLineEnds</code>
  	 * @return a scanner
	 * 
	 * @see org.eclipse.jdt.core.compiler.IScanner
	 */
	public static IScanner createScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace, boolean assertMode, boolean recordLineSeparator){

		Scanner scanner = new Scanner(tokenizeComments, tokenizeWhiteSpace, false, assertMode);
		scanner.recordLineSeparator = recordLineSeparator;
		return scanner;
	}
}
