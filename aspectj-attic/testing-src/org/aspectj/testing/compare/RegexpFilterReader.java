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
 * ******************************************************************/


package org.aspectj.testing.compare;


import org.aspectj.testing.util.StringVisitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Vector;

/**
 * Adapt Writer to StringVisitor, with pre- and post-write
 * methods for subclasses to override.
 */
class StringDumper implements StringVisitor {
    /** subclasses should write errors here and be silent if null */
    protected PrintStream errSink;
    private final boolean appendNewlines;
    private final BufferedWriter writer;
    /** true only if writer non-null and close() was invoked */
    private boolean closed;

    /**
     * @param writer the Writer to write to - ignored if null
     * @param appendNewLines if true, append newlines after writing
     */
    public StringDumper(BufferedWriter writer, boolean appendNewlines) {
        this.appendNewlines = appendNewlines;
        this.writer = writer;
        // closed = false;
        errSink = System.err;
    }

    /**
     * Set error sink - may be null to ignore IOExceptions.
     * @param err the PrintStream to use for errors (silent if null)
     */
    public void setErrorSink(PrintStream err) {
        errSink = err;
    }

    /**
     * Invoked before the String is written.
     * This implementation does nothing.
     * @param string the String to be written
     * @return false if writing should abort
     */
    protected boolean preWrite(String string) 
        throws IOException {
        return true;
    }

    /**
     * Implement StringVisitor.accept(String)
     * by doing preWrite(String), process(String),
     * writer.write(String...), and postWrite(String),
     * any one of which may result in a false return value.
     * @throws Error if invoked after <code>close()</code>
     */
    public final boolean accept(String string) {
        if (closed) {
            String m = "did close() before accept(\"" + string + "\")";
            throw new Error(m);
        }
        if (null == writer) return false;
        try {
            if (!preWrite(string)) return false;
            string = process(string);
            if (null == string) return false;
            if (null != writer) writer.write(string, 0, string.length());
            if (!postWrite(string)) return false;
        } catch (IOException e) {
            PrintStream sink = errSink;
            if (null != sink) e.printStackTrace(sink);
            return false;
        }
        return true;
    }

    /**
     * Transform the input before writing.
     * This implementation returns the input.
     * @param string the String to transform
     * @return the String as changed - if null,
     *         then halt and return false from the accept method.
     */
    protected String process(String string) {
        return string;
    }

    /**
     * Invoked after the String is written.
     * This implementation handles writing of the newline.
     * @param string the String that was written
     * @return false if writing should abort
     */
    protected boolean postWrite(String string) 
        throws IOException {
        if (appendNewlines && null != writer) {
            writer.newLine();
        }
        return true;
    }

    /** convenience method to close adopted writer */
    public void close() throws IOException {
        if (null != writer) {
            writer.close();
            closed = true;
        }
    }
}
class FilteredDumper extends StringDumper {
    protected final RegexpFilter filter;
    public FilteredDumper(BufferedWriter writer, 
                          boolean appendNewlines,
                          RegexpFilter filter) {
        super(writer, appendNewlines);
        this.filter = filter;
    }
    public String process(String arg) {
        return filter.process(arg);
    }
}

class FilteredAccumulator extends FilteredDumper {
    protected final Vector results;
    public FilteredAccumulator(RegexpFilter filter) {
        super(null, false, filter);
        results = new Vector();
    }
    public String process(String arg) {
        arg = super.process(arg);
        synchronized (results) {
            results.add(arg);
        }
        return arg;
    }
    public Vector getResults() {
        synchronized (results) {
            return (Vector) results.clone();
        }
    }
}

/**  

/**  
 * Input file, using a RegexpFilter to preprocess each line.
 * <li>read line using superclass LineNumberReader.readLine()</li>
 * <li>Preprocess with case and white space operations</li>
 * <li>run all the replace operations on the input, in order</li>
 * <li>return the line.</li>
 * Using anything but the <code>readLine()</code> method will circumvent
 * the regular expression replacement processing.
 */
public class RegexpFilterReader extends BufferedReader {

    // ---------------------------------------------- static methods

    /**
     * Pass lines from BufferedReader to visitor.
     * Stop reading lines if visitor returns false.
     * @param input the BufferedReader with the input 
     *              - if null, use System.in
     * @param visitor the StringVisitor to pass each line 
     *                if null, just read in all lines and ignore
     */
    public static void visitLines(BufferedReader input, 
                                  StringVisitor visitor) 
        throws IOException {
        final boolean openInput = (null == input);
        if (openInput) input = new BufferedReader(new InputStreamReader(System.in));
        try {
            String line = null;
            if (null == visitor) { 
                while (null != (line = input.readLine())) { 
                    // read and ignore
                }
            } else {
                while (null != (line = input.readLine())) { 
                    if (!visitor.accept(line)) {
                        break;
                    }
                }
            }
        } finally {
            if (openInput && (null != input)) {
                try { input.close(); }  // todo: ok to close since System.in underlies?
                catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    } // visitLines

    // todo move write(in,out) to a utility class
    /**
     * Convenience method to write one file to another by line.
     * Neither input nor output are closed.
     * @param input the BufferedReader with the input - if null, use System.in
     * @param output the BufferedWriter for the output - if null, use System.out
     */
    public static void write(BufferedReader input, 
                             BufferedWriter output)  
        throws IOException {
        final boolean openOutput = (null == output);
        if (openOutput) output = new BufferedWriter(new OutputStreamWriter(System.out));
        StringDumper visitor = new StringDumper(output, true);
        try {
            RegexpFilterReader.visitLines(input, visitor);
        } finally {
            if (openOutput && (null != visitor)) {
                try { visitor.close(); }  // todo: ok to close since System.out underlies?
                catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        }
    } // write

    /**
     * Process file (or System.in) like sed.
     * @param args  the String[] containing RegexpFilter arguments
     */
    public static void main(String[] args) throws IOException {
        RegexpFilter filter = RegexpFilter.init(args, null);
        if (null != filter) {
            File file = filter.getFile();
            Reader reader = null;
            if (file != null) {
                reader = new FileReader(file);
            } else {
                reader = new InputStreamReader(System.in);
            }
            RegexpFilterReader me = new RegexpFilterReader(reader);
            me.setFilter(filter);
            RegexpFilterReader.write(me, null);
        }
    }

    // ---------------------------------------------- constructors
    public RegexpFilterReader(Reader reader) {
        super(reader);
    }
    public RegexpFilterReader(Reader reader, int size) {
        super(reader, size);
    }

    // ---------------------------------------------- instance fields
    protected RegexpFilter filter;
    // ---------------------------------------------- instance methods
    public void setFilter(RegexpFilter filter) {
        this.filter = filter;
    }
   
    /**
     * Process each line as it is read in by the superclass.
     */
    public String readLine() throws IOException {
        RegexpFilter filter = this.filter;
        String line =  super.readLine();
        if (null != filter) {
            line = filter.process(line);
        }
        return line;
    }

} // class RegexpFilterReader


