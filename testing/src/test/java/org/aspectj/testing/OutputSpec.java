/* *******************************************************************
 * Copyright (c) 2005 IBM Corporation
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Adrian Colyer,
 * ******************************************************************/
package org.aspectj.testing;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
		String[] actualOutputLines = getTrimmedLines(output);

		if (actualOutputLines.length == expectedOutputLines.size()) {
			matches = true;
			for (String lineExpected : expectedOutputLines) {
				String lineFound = actualOutputLines[lineNo++];
				/* Avoid trying to match on ajSandbox source names that appear in messages */
				if (!lineFound.contains(lineExpected.trim())) {
					matches = false;
					break;
				}
			}
		} else { lineNo = -1; }
		if (!matches) {
			createFailureMessage(output, lineNo, actualOutputLines.length);
		}
	}

	private void unorderedMatchAgainst(String output) {
		List<String> actualOutputLines = Arrays.asList(getTrimmedLines(output));
		int numberOfOutputLines = actualOutputLines.size();
		if(numberOfOutputLines != expectedOutputLines.size()) {
			createFailureMessage(output, -1, numberOfOutputLines);
			return;
		}
		List<String> expected = new ArrayList<>(expectedOutputLines);
		List<String> found = new ArrayList<>(actualOutputLines);
		for (String lineFound : actualOutputLines) {
			for (String lineExpected : expectedOutputLines) {
				if (lineFound.contains(lineExpected.trim())) {
					found.remove(lineFound);
					expected.remove(lineExpected);
				}
			}
		}
		if (!found.isEmpty() || !expected.isEmpty()) {
			createFailureMessage(output, -2, numberOfOutputLines);
		}
	}

	private void createFailureMessage(String output, int lineNo, int sizeFound) {
		StringBuilder failMessage = new StringBuilder();
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

	private String[] getTrimmedLines(String text) {
		// Remove leading/trailing empty lines and leading/trailing whitespace from each line
		String[] trimmedLines = text.trim().split("\\s*\n\\s*");
		return trimmedLines.length == 1 && trimmedLines[0].equals("") ? new String[0] : trimmedLines;
	}
}
