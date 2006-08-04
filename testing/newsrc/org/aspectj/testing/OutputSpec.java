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
			if (lineNo==-1) 
				failMessage.append("Expected "+expectedOutputLines.size()+" lines of output but there are "+strTok.countTokens());
			else
				failMessage.append("First difference is on line " + lineNo);
			failMessage.append("\n");
			AjcTestCase.fail(failMessage.toString());
		}
	}
}
