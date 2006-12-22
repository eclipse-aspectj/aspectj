/* *******************************************************************
 * Copyright (c) 2005 IBM Corporation
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Adrian Colyer, 
 * ******************************************************************/
package org.aspectj.testing;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.aspectj.tools.ajc.AjcTestCase;

public class OutputSpec {
	
	private List expectedOutputLines = new ArrayList();

	public void addLine(OutputLine line) {
		expectedOutputLines.add(line.getText());
	}
	
	public void matchAgainst(String output) {
		matchAgainst(output, "yes");
	}
	
	public void matchAgainst(String output, String ordered) {
		if (ordered != null && ordered.equals("no")) {
			unorderedMatchAgainst(output);
			return;
		}
		boolean matches = false;
		int lineNo = 0;
		StringTokenizer strTok = new StringTokenizer(output,"\n");
		if (strTok.countTokens() == expectedOutputLines.size()) {
			matches = true;
			for (Iterator iter = expectedOutputLines.iterator(); iter.hasNext();) {
				String line = (String) iter.next();
				lineNo++;
				String outputLine = strTok.nextToken().trim();
				/* Avoid trying to match on ajSandbox source names that appear in messages */
				if (outputLine.indexOf(line) == -1) {
					matches = false;
					break;
				}
			}
		} else { lineNo = -1; }
		if (!matches) {
			createFailureMessage(output, lineNo, strTok.countTokens());
		}
	}
	
	public void unorderedMatchAgainst(String output) {
		List outputFound = getOutputFound(output);
		if(outputFound.size() != expectedOutputLines.size()) {
			createFailureMessage(output, -1, outputFound.size());
			return;
		} 
		List expected = new ArrayList();
		expected.addAll(expectedOutputLines);
		List found = new ArrayList();
		found.addAll(outputFound);
		for (Iterator iterator = outputFound.iterator(); iterator.hasNext();) {
			String lineFound = (String) iterator.next();
			for (Iterator iterator2 = expectedOutputLines.iterator(); iterator2.hasNext();) {
				String lineExpected = (String) iterator2.next();
				if (lineFound.indexOf(lineExpected)!= -1) {
					found.remove(lineFound);
					expected.remove(lineExpected);
					continue;
				}
			}
		}
		if (!found.isEmpty() || !expected.isEmpty()) {
			createFailureMessage(output,-2,outputFound.size());
		}
	}
	
	private void createFailureMessage(String output, int lineNo, int sizeFound) {
		StringBuffer failMessage = new StringBuffer();
		failMessage.append("\n  expecting output:\n");
		int l = 0;
		for (Iterator iter = expectedOutputLines.iterator(); iter.hasNext();) {
			String line = (String) iter.next();
			failMessage.append(line);
			failMessage.append("\n");
		}
		failMessage.append("  but found output:\n");
		failMessage.append(output);
		failMessage.append("\n");
		if (lineNo==-1) {
			failMessage.append("Expected "+expectedOutputLines.size()+" lines of output but there are "+sizeFound);
		} else if (lineNo >= 0) {
			failMessage.append("First difference is on line " + lineNo);
		}
		failMessage.append("\n");
		AjcTestCase.fail(failMessage.toString());		
	}
	
	private List getOutputFound(String output) {
		List found = new ArrayList();
		StringTokenizer strTok = new StringTokenizer(output,"\n");
		while(strTok.hasMoreTokens()) {
			String outputLine = strTok.nextToken().trim();
			found.add(outputLine);
		}
		return found;
	}
}
