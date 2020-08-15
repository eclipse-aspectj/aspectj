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
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.tools.ajc.AjcTestCase;
import org.aspectj.util.LangUtil;

public class OutputSpec {

	private List<String> expectedOutputLines = new ArrayList<>();

	public void addLine(OutputLine line) {
		if (line.getVm() == null || matchesThisVm(line.getVm())) {
			expectedOutputLines.add(line.getText());
		}
	}

	/**
	 * For a test output line that has specified a vm version, check if it matches the vm we are running on.
	 * vm might be "1.2,1.3,1.4,1.5" or simply "9" or it may be a version with a '+' suffix indicating that
	 * level or later, e.g. "9+" should be ok on Java 10
	 * @return true if the current vm version matches the spec
	 */
	private boolean matchesThisVm(String vm) {
		// vm might be 1.2, 1.3, 1.4, 1.5 or 1.9 possibly with a '+' in there
		// For now assume + is attached to there only being one version, like "9+"
		//		System.out.println("Checking "+vm+" for "+LangUtil.getVmVersionString());
		String v = LangUtil.getVmVersionString();
		if (v.endsWith(".0")) {
			v = v.substring(0,v.length()-2);
		}
		if (vm.contains(v)) {
			return true;
		}
		if (vm.endsWith("+")) {
			double vmVersion = LangUtil.getVmVersion();
			double vmSpecified = Double.parseDouble(vm.substring(0,vm.length()-1));
			return vmVersion >= vmSpecified;
		}
		return false;
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
			for (String line: expectedOutputLines) {
				lineNo++;
				String outputLine = strTok.nextToken().trim();
				/* Avoid trying to match on ajSandbox source names that appear in messages */
				if (!outputLine.contains(line)) {
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
		List<String> outputFound = getOutputFound(output);
		if(outputFound.size() != expectedOutputLines.size()) {
			createFailureMessage(output, -1, outputFound.size());
			return;
		}
		List<String> expected = new ArrayList<>(expectedOutputLines);
		List<String> found = new ArrayList<>(outputFound);
		for (String lineFound : outputFound) {
			for (String lineExpected : expectedOutputLines) {
				if (lineFound.contains(lineExpected)) {
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
		for (String line: expectedOutputLines) {
			failMessage.append(line+"\n");
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

	private List<String> getOutputFound(String output) {
		List<String> found = new ArrayList<>();
		StringTokenizer strTok = new StringTokenizer(output,"\n");
		while(strTok.hasMoreTokens()) {
			String outputLine = strTok.nextToken().trim();
			found.add(outputLine);
		}
		return found;
	}
}
