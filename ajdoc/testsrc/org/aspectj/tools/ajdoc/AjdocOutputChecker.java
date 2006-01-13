/********************************************************************
 * Copyright (c) 2005 Contributors. All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: IBM Corporation - initial API and implementation 
 * 				 Helen Hawkins   - iniital version
 *******************************************************************/
package org.aspectj.tools.ajdoc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to check whether the ajdoc contains the expected
 * information.
 */
public class AjdocOutputChecker {

	/**
	 * Checks whether the given html file contains the required String. 
	 * 
	 * @param htmlFile
	 * @param requiredString
	 * @return true if the file contains the given string or
	 * false otherwise (or if the file is null or not an html file)
	 * @throws Exception
	 */
	public static boolean containsString(File htmlFile,
			String requiredString) throws Exception {
		if ((htmlFile == null) || !htmlFile.getAbsolutePath().endsWith("html")) {
			return false;
		}
		BufferedReader reader = new BufferedReader(new FileReader(htmlFile));
		String line = reader.readLine();
		while (line != null) {
			if (line.indexOf(requiredString) != -1) {
				reader.close();
				return true;
			}
			line = reader.readLine();
		}
		reader.close();
		return false;
	}

	/**
	 * Returns those strings from the given array which aren't in the
	 * html file
	 * 
	 * @param htmlFile
	 * @param an array of requiredStrings
	 * @return a List of those strings not found
	 * @throws Exception
	 */	
	public static List /*String*/ getMissingStringsInFile(File htmlFile,
			String[] requiredStrings) throws Exception {
		List missingStrings = new ArrayList();
		for (int i = 0; i < requiredStrings.length; i++) {
			String string = requiredStrings[i];
			if (!containsString(htmlFile,string)) {
				missingStrings.add(string);
			}
		}
		return missingStrings;
	}
	
	/**
	 * Checks whether the section of the html file contains the
	 * required String
	 * 
	 * @param htmlFile
	 * @param requiredString
	 * @param sectionHeader
	 * @return true if the file contains the given string within the 
	 * required section or false otherwise (or if the file is null or 
	 * not an html file)
	 * @throws Exception
	 */
	public static boolean containsStringWithinSection(File htmlFile,
			String requiredString, String sectionHeader) throws Exception {
		if ((htmlFile == null) || !htmlFile.getAbsolutePath().endsWith("html")) {
			return false;
		}
		BufferedReader reader = new BufferedReader(new FileReader(htmlFile));
		String line = reader.readLine();
		while (line != null) {
			if (line.indexOf(sectionHeader) != -1) {
				String nextLine = reader.readLine();
				while (nextLine != null && 
						(nextLine.indexOf("========") == -1)) {
					if (nextLine.indexOf(requiredString) != -1) {
						reader.close();
						return true;
					}
					nextLine = reader.readLine();
				}
				reader.close();
				return false;
			}
			line = reader.readLine();
		}
		reader.close();
		return false;
	}
	
	/**
	 * Returns those strings from the given array which aren't in the
	 * ajdoc html file	 
	 *  
	 * @param htmlFile
	 * @param an array of requiredStrings
	 * @param sectionHeader
	 * @return List of those requiredStrings not found
	 * @throws Exception
	 */
	public static List /*String*/ getMissingStringsInSection(File htmlFile,
			String[] requiredStrings, String sectionHeader) throws Exception {
		List missingStrings = new ArrayList();
		for (int i = 0; i < requiredStrings.length; i++) {
			String string = requiredStrings[i];
			if (!containsStringWithinSection(htmlFile,string,sectionHeader)) {
				missingStrings.add(string);
			}
		}
		return missingStrings;
	}
	
	/**
	 * Checks whether the given strings appear one after the other in the
	 * ajdoc html file
	 * 
	 * @param htmlFile
	 * @param firstString
	 * @param secondString expected to follow the firstString
	 * @return true if secondString appears after firstString, false otherwise
	 * @throws Exception
	 */
	public static boolean fileContainsConsecutiveStrings(File htmlFile, 
			String firstString, String secondString ) throws Exception {
		if ((htmlFile == null) || !htmlFile.getAbsolutePath().endsWith("html")) {
			return false;
		}
		BufferedReader reader = new BufferedReader(new FileReader(htmlFile));
		String line = reader.readLine();
		while (line != null) {
			if (line.indexOf(firstString) != -1) {
				if ( (line.indexOf(secondString) != -1 
						&& line.indexOf(secondString) > line.indexOf(firstString)) 
						|| reader.readLine().indexOf(secondString) != -1) {
					reader.close();
					return true;
				}
				reader.close();
				return false;
			}
			line = reader.readLine();
		}
		reader.close();
		return false;
	}

	/**
	 * Checks whether the given strings appear one after the other in the
	 * given section of the ajdoc html file
	 * 
	 * @param htmlFile
	 * @param firstString
	 * @param secondString expected to follow the firstString
	 * @param sectionHeader
	 * @return true if secondString appears after firstString, false otherwise
	 * @throws Exception
	 */
	public static boolean sectionContainsConsecutiveStrings(File htmlFile, 
			String firstString, String secondString, String sectionHeader) throws Exception  {
		if (((htmlFile == null) || !htmlFile.getAbsolutePath().endsWith("html"))) {
			return false;
		}
		BufferedReader reader = new BufferedReader(new FileReader(htmlFile));
		String line = reader.readLine();
		while (line != null) {
			if (line.indexOf(sectionHeader) != -1) {
				String nextLine = reader.readLine();
				while (nextLine != null && (nextLine.indexOf("========") == -1)) {
					if (nextLine.indexOf(firstString) != -1) {
						if ( (nextLine.indexOf(secondString) != -1 
								&& nextLine.indexOf(secondString) > nextLine.indexOf(firstString)) 
								|| reader.readLine().indexOf(secondString) != -1) {
							reader.close();
							return true;
						}
					}
					nextLine = reader.readLine();
				}
				reader.close();
				return false;
			}
			line = reader.readLine();
		}
		reader.close();
		return false;
	}
}
