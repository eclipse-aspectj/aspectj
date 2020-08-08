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
 * ******************************************************************/


/*
 * LineReader.java created on May 3, 2002
 *
 */
package org.aspectj.testing.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;

/** LineNumberReader which absorbs our lines and renders as file:line */
public class LineReader extends LineNumberReader {
    /** delimited multi-line output of readToBlankLine */
    public static final String RETURN= "\n\r";
    
    private static final String[] NONE = new String[0];
    private static final String cSCRIPT = "#";
    private static final String cJAVA = "//";
    private static final String[] TESTER_LEAD = new String[] {cSCRIPT, cJAVA};
    
    /** 
     * Convenience factory for tester suite files
     * @return null if IOException or IllegalArgumentException thrown
     */
    public static final LineReader createTester(File file) {
        return create(file, TESTER_LEAD, null);
    }
    
    /** 
     * convenience factory 
     * @return null if IOException or IllegalArgumentException thrown
     */
    public static final LineReader create(File file, 
        String[] leadComments, String[] eolComments) {
        try {
            FileReader reader = new FileReader(file);
            return new LineReader(reader, file, leadComments, eolComments);
        } catch (IllegalArgumentException e) {
        } catch (IOException e) {
        }
        return null;
    }
    
    final private File file;
    final private String[] eolComments;
    final private String[] leadComments;
    
    /**
     * @param file the File used to open the FileReader
     * @param leadComments the String[] to be taken as the start of
     * comments when they are the first non-blank text on a line -
     * pass null to signal none.
     * @param leadComments the String[] to be taken as the start of
     * comment anywhere on a line - pass null to signal none.
     *@throws IllegalArgumentException if any String in
     * leadComments or eolComments is null.
     */
    public LineReader(FileReader reader, File file, 
        String[] leadComments, String[] eolComments) {
        super(reader); 
        this.file = file;
        this.eolComments = normalize(eolComments);
        this.leadComments = normalize(leadComments);
    }
    public LineReader(FileReader reader, File file) {
        this(reader, file, null, null);
    }
    
    /** @return file:line */
    public String toString() {
        return file.getPath() + ":" + getLineNumber();
    }
    
    /** @return underlying file */
    public File getFile() { return file; }

    /**
     * Reader first..last (inclusive) and return in String[].
     * This will return (1+(last-first)) elements only if this
     * reader has not read past the first line and there are last lines
     * and there are no IOExceptions during reads.
     * @param first the first line to read - if negative, use 0
     * @param last the last line to read (inclusive) 
     *         - if less than first, use first
     * @return String[] of first..last (inclusive) lines read or 
     */
    public String[] readLines(int first, int last) {
        if (0 > first) first = 0;
        if (first > last) last = first;
        ArrayList list = new ArrayList();
        try {
            String line = null;
            while (getLineNumber() < first) { 
                line = readLine();
                if (null == line) {
                    break; 
                }
            }
            if (getLineNumber() > first) { 
                // XXX warn? something else read past line
            }
            if ((null != line) && (first == getLineNumber())) {
                list.add(line);
                while (last >= getLineNumber()) {
                    line = readLine();
                    if (null == line) {
                        break;
                    }
                    list.add(line);
                }
            }
        } catch (IOException e) {
            return NONE;
        }
        return (String[]) list.toArray(NONE);
    }
    
    /** Skip to next blank line 
     * @return the String containing all lines skipped (delimited with RETURN)
     */
    public String readToBlankLine() throws IOException {
        StringBuffer sb = new StringBuffer();
        String input;
        while (null != (input = nextLine(false))) { // get next empty line to restart
            sb.append(input);
            sb.append(RETURN);// XXX verify/ignore/correct
        }
        return sb.toString();
    }

    /**
     * Get the next line from the input stream, stripping eol and
     * leading comments.
     * If emptyLinesOk is true, then this reads past lines which are
     * empty after omitting comments and trimming until the next non-empty line.
     * Otherwise, this returns null on reading an empty line.
     * (The input stream is not exhausted until this
     * returns null when emptyLines is true.)
     * @param emptyLinesOk if false, return null if the line is empty
     * @return next non-null, non-empty line in reader,
     * ignoring comments
     */
    public String nextLine(boolean emptyLinesOk) throws IOException {
        int len = 0;
        String result = null;
        do {
            result = readLine();
            if (result == null)
                return null;
            result = result.trim();
			for (String eolComment : eolComments) {
				int loc = result.indexOf(eolComment);
				if (-1 != loc) {
					result = result.substring(0, loc);
					break;
				}
			}
            len = result.length();
            if (0 < len) {
				for (String leadComment : leadComments) {
					if (result.startsWith(leadComment)) {
						result = "";
						break;
					}
				}
                len = result.length();
            }
            len = result.length();
            if (!emptyLinesOk && (0 == len))
                return null;
        } while (0 == len);
        return result;
    }

    private String[] normalize(String[] input) {
        if ((null == input) || (0 == input.length)) return NONE;
        String[] result = new String[input.length];
        System.arraycopy(input, 0, result, 0, result.length);
        for (int i = 0; i < result.length; i++) {
            if ((null == result[i]) || (0 == result[i].length())) {
                throw new IllegalArgumentException("empty input at [" + i + "]");
            }
        }
        return result;
    }

}

