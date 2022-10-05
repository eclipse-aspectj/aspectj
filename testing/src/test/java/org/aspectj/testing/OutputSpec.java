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

import static java.lang.Double.parseDouble;

public class OutputSpec {

	private List<String> expectedOutputLines = new ArrayList<>();

	public void addLine(OutputLine line) {
		if (line.getVm() == null || matchesThisVm(line.getVm())) {
			expectedOutputLines.add(line.getText());
		}
	}

	/**
	 * For a test output line that has specified list of JVM version ranges, determine whether the JVM we are running on
	 * matches at least one of those ranges.
	 *
	 * @param vmVersionRanges might be a single version like "9", a list of versions like "1.2,1.3,1.4,1.5", an equivalent
	 *          range of "1.2-1.5", an open range like "-1.8", "9-" (equivalent to "9+") or a more complex list of ranges
	 *          like "-1.6,9-11,13-14,17-" or "8,11,16+". Empty ranges like in "", " ", "8,,14", ",5", "6-," will be
	 *          ignored. I.e., they will not yield a positive match. Bogus ranges like "9-11-14" will be ignored, too.
	 *
	 * @return true if the current vmVersionRanges version matches the spec
	 */
	private boolean matchesThisVm(String vmVersionRanges) {
		double currentVmVersion = LangUtil.getVmVersion();
		return Arrays.stream(vmVersionRanges.split(","))
			.map(String::trim)
			.filter(range -> !range.isEmpty())
			.map(range -> range
				.replaceFirst("^([0-9.]+)$", "$1-$1")   // single version 'n' to range 'n-n'
				.replaceFirst("^-", "0-")               // left open range '-n' to '0-n'
				.replaceFirst("[+-]$", "-99999")        // right open range 'n-' or 'n+' to 'n-99999'
				.split("-")                             // range 'n-m' to array ['n', 'm']
			)
			.filter(range -> range.length == 2)
			.map(range -> new double[] { parseDouble(range[0]), parseDouble(range[1]) })
			//.filter(range -> { System.out.println(range[0] + " - " +range[1]); return true; })
			.anyMatch(range -> range[0] <= currentVmVersion && range[1] >= currentVmVersion);
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
