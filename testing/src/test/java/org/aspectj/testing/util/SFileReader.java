/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v 2.0
 * which accompanies this distribution and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/EPL-2.0.txt
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 * ******************************************************************/

package org.aspectj.testing.util;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.util.LangUtil;


/**
 * This reads a structured (config) file, which may have
 * lines with @ signalling a recursive read
 * and EOL comments # or //.
 * This duplicates ConfigFileUtil in some sense.
  */
public class SFileReader<T> {
    // XXX move into LineReader, but that forces util to depend on AbortException?
    // Formerly in SuiteReader

    /**
     * Read args as config files and echo to stderr.
     * @param args String[] of fully-qualified paths to config files
     */
    public static void main(String[] args) throws IOException {
        List<String> result = new ArrayList<>();
        ObjectChecker<String> collector = new StandardObjectChecker<>(String.class, result);
        SFileReader<String> me = new SFileReader<String>(null);
		for (String arg : args) {
			Node<String> node = me.readNodes(new File(arg), null, true, System.err);
			if (!Node.visit(node, collector, null)) {
				System.err.println("halted during copy of " + arg);
			} else {
				String s = org.aspectj.testing.util.LangUtil.debugStr(null, "\n  ", null,
						null, result.toArray(), "\n  ", "");
				System.err.println(arg + ": " + s);
			}
		}
    }

    /*
     * readSuite(..) reads .txt file, and for each test case specification
     * creates a spec using readTestSpecifications
     * and (if the specifications match the constraints)
     * creates a test case using creatTestCase.
     */

    /** pass this to protected methods requiring String[] if you have none */
    protected static final String[] NONE = new String[0];

    final Maker<T> maker;

    /** @param maker the Maker factory to use - if null, use Maker.ECHO */
    public SFileReader(Maker<T> maker) {
        this.maker = (null == maker ? Maker.ECHO : maker);
    }

    /**
     * Creates a (potentially recursive) tree of node
     * by reading from the file and constructing using the maker.
     * Clients may read results in Node tree form when complete
     * or snoop the selector for a list of objects made.
     * The selector can prevent collection in the node by
     * returning false.
     * Results are guaranteed by the Maker to be of the Maker's type.
     * @param file an absolute path to a structured file
     * @param selector determines whether not to keep an object made.
     *         (if null, then all are kept)
     * @return Node with objects available from getItems()
     *          and sub-suite Node available from getNodes()
     * @throws Error on any read error if abortOnReadError (default)
     */
    public Node<T> readNodes(
        final File file,
        final ObjectChecker<? super T> selector,
        final boolean abortOnReadError,
        final PrintStream err)
        throws IOException {
        final Node<T> result = new Node<>(file.getPath());
        if (null == file) {
            throw new IllegalArgumentException("null file");
        } else if (!file.isAbsolute()) {
            throw new IllegalArgumentException("file not absolute");
        }
        UtilLineReader reader = null;
        try {
            reader = UtilLineReader.createTester(file);
            if (null == reader) {
                throw new IOException("no reader for " + file);
            }
            final String baseDir = file.getParent();

            String line;
            boolean skipEmpties = true;
            while (null != (line = reader.nextLine(skipEmpties))) {
                if (line.charAt(0) == '@') {
                    if (line.length() > 1) {
                        String newFilePath = line.substring(1).trim();
                        File newFile = new File(newFilePath);
                        if (!newFile.isAbsolute()) {
                            newFile = new File(baseDir, newFilePath);
                        }
                        Node<T> node = readNodes(newFile, selector, abortOnReadError, err);
                        if (!result.addNode(node)) {
                            // XXX signal error?
                            System.err.println("warning: unable to add node: " + node);
                            break;
                        }
                    }
                } else {
                    try {
                        T made = maker.make(reader);
                        if ((null == selector) || (selector.isValid(made))) {
                            if (!result.add(made)) {
                               break;  // XXX signal error?
                            }
                        }
                    } catch (AbortException e) {
                        if (abortOnReadError) { // XXX todo - verify message has context?
                           throw e;
                        }
                        if (null != err) {
                            String m;
                            IMessage mssg = e.getIMessage();
                            if (null != mssg) {
                                m = "Message: " + mssg;
                            } else {
                                m = LangUtil.unqualifiedClassName(e) + "@" + e.getMessage();
                            }
                            err.println(m);
                        }
                        reader.readToBlankLine();
                    }
                }
            }
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
            } // ignore
        }

        return result;
    }

    /** factory produces objects by reading LineReader */
    public interface Maker<T> {
        /**
         * Make the result using the input from the LineReader,
         * starting with lastLine().
         */
        T make(UtilLineReader reader) throws AbortException, IOException;

        /** @return type of the Object made */
        Class<T> getType();

        /** This echoes each line, prefixed by the reader.
         * @return file:line: {line}
         */
		Maker ECHO = new Maker() {
            public Object make(UtilLineReader reader) {
                return reader + ": " + reader.lastLine();
            }
            public Class getType() { return String.class; }
        };
    }
}
