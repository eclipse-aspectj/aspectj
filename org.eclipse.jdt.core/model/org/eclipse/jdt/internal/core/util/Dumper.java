/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import java.io.*;

/**
 * A dumper knows how to dump objects in a human-readable representation to a PrintWriter.
 * Objects which implement IDumpable know how to dump themselves to a Dumper, otherwise
 * the dumper just uses toString() on the object.
 * Example:
 * <pre>
 *   Dumper dumper = new Dumper(System.out);
 *   dumper.dump(myObject);
 *   dumper.flush();
 * </pre>
 *
 * @see IDumpable
 * @see PrintWriter
 */
public class Dumper {
	int fTabLevel = 0;
	PrintWriter fWriter;
/**
 * Creates a dumper which sends output to System.out.
 */
public Dumper() {
	this(System.out);
}
/**
 * Creates a dumper which sends output to the given OutputStream.
 */
public Dumper(OutputStream out) {
	this(new PrintWriter(out));
}
/**
 * Creates a dumper which sends output to the given PrintWriter.
 */
public Dumper(PrintWriter writer) {
	fWriter = writer;
}
/**
 * Returns the class name to use for the given object.
 */
String classNameFor(Object val) {
	String name = val.getClass().getName();
	int i = name.lastIndexOf('.');
	if (i != -1) {
		name = name.substring(i+1);
	}
	return name;
}
/**
 * Dumps the given array.
 * Prints a maximum of maxPerLine items per line.
 */
public void dump(int[] val, int maxPerLine) {
	int len = val.length;
	boolean oneLine = (len <= maxPerLine);
	if (!oneLine) {
		++fTabLevel;
	}
	fWriter.print("["); //$NON-NLS-1$
	for (int i = 0; i < len; ++i) {
		if (!oneLine) {
			if ((i % maxPerLine) == 0) {
				fWriter.println();
				tabIfNeeded();
				fWriter.print(i);
				fWriter.print(": "); //$NON-NLS-1$
			}
		}
		fWriter.print(val[i]);
		if (i + 1 < len) {
			fWriter.print(", "); //$NON-NLS-1$
		}
	}
	if (oneLine) {
		fWriter.print("]"); //$NON-NLS-1$
		fWriter.println();
	}
	else {
		fWriter.println();
		--fTabLevel;
		tabIfNeeded();
		fWriter.print("]"); //$NON-NLS-1$
		fWriter.println();
	}
}
/**
 * Dumps the given array.
 */
public void dump(Object[] val) {
	int len = val.length;
	fWriter.print("["); //$NON-NLS-1$
	if (len > 0) {
		fWriter.println();
		++fTabLevel;
		for (int i = 0; i < len; ++i) {
			dump(val[i]);
		}
		--fTabLevel;
		tabIfNeeded();
	}
	fWriter.println("]"); //$NON-NLS-1$
}
/**
 * Dumps the given array.
 * Prints a maximum of maxPerLine items per line.
 */
public void dump(String[] val, int maxPerLine) {
	int len = val.length;
	boolean oneLine = (len <= maxPerLine);
	if (!oneLine) {
		++fTabLevel;
	}
	fWriter.print("["); //$NON-NLS-1$
	boolean newLine = !oneLine;
	for (int i = 0; i < len; ++i) {
		if (newLine) {
			fWriter.println();
			tabIfNeeded();
		}
		fWriter.print("\"" + val[i] + "\""); //$NON-NLS-1$ //$NON-NLS-2$
		if (i + 1 < len) {
			fWriter.print(", "); //$NON-NLS-1$
		}
		newLine = (i != 0 && (i % maxPerLine) == 0);
	}
	fWriter.print("]"); //$NON-NLS-1$
	if (oneLine || newLine) {
		fWriter.println();
	}
	if (!oneLine) {
		--fTabLevel;
	}
}
/**
 * Dumps the given value.
 */
public void dump(Object val) {
	tabIfNeeded();
	if (val instanceof IDumpable) {
		IDumpable dumpable = (IDumpable) val;
		fWriter.println(classNameFor(val) + " {"); //$NON-NLS-1$
		int originalLevel = fTabLevel;
		++fTabLevel;
		try {
			dumpable.dump(this);
		}
		catch (Throwable t) {
			fWriter.println("*ERR*"); //$NON-NLS-1$
		}
		fTabLevel = originalLevel;
		tabIfNeeded();
		fWriter.println("}"); //$NON-NLS-1$
	}
	else {
		fWriter.println(val);
	}
}
/**
 * Dumps the given value, with the given name as a prefix.
 */
public void dump(String name, int[] val) {
	dump(name, val, 10);
}
/**
 * Dumps the given array, with the given name as a prefix.
 * Prints a maximum of maxPerLine items per line.
 */
public void dump(String name, int[] val, int maxPerLine) {
	prefix(name);
	dump(val, maxPerLine);
}
/**
 * Dumps the given value, with the given name as a prefix.
 */
public void dump(String name, Object[] val) {
	prefix(name);
	dump(val);
}
/**
 * Dumps the given value, with the given name as a prefix.
 */
public void dump(String name, String[] val) {
	prefix(name);
	dump(val, 5);
}
/**
 * Dumps the given value, with the given name as a prefix.
 */
public void dump(String name, int val) {
	prefix(name);
	fWriter.println(val);
}
/**
 * Dumps the given value, with the given name as a prefix.
 */
public void dump(String name, Object val) {
	prefix(name);
	fWriter.println();
	++fTabLevel;
	dump(val);
	--fTabLevel;
}
/**
 * Dumps the given value, with the given name as a prefix.
 */
public void dump(String name, String val) {
	prefix(name);
	fWriter.println(val == null ? "null" : "\"" + val + "\""); //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-2$
}
/**
 * Dumps the given value, with the given name as a prefix.
 */
public void dump(String name, boolean val) {
	prefix(name);
	fWriter.println(val);
}
/**
 * Dumps the given message, with the given name as a prefix.
 */
public void dumpMessage(String name, String msg) {
	prefix(name);
	fWriter.println(msg);
}
/**
 * Flushes the output writer.
 */
public void flush() {
	fWriter.flush();
}
/**
 * Returns the current tab level.
 */
public int getTabLevel() {
	return fTabLevel;
}
/**
 * Returns the underlying PrintWriter.
 */
public PrintWriter getWriter() {
	return fWriter;
}
/**
 * Increase the current tab level.
 */
public void indent() {
	++fTabLevel;
}
/**
 * Decrease the current tab level.
 */
public void outdent() {
	if (--fTabLevel < 0) 
		fTabLevel = 0;
}
/**
 * Outputs the given prefix, tabbing first if needed.
 */
void prefix(String name) {
	tabIfNeeded();
	fWriter.print(name);
	fWriter.print(": "); //$NON-NLS-1$
}
/**
 * Print an object directly, without a newline.
 */
public void print(Object obj) {
	fWriter.print(obj);
}
/**
 * Print a newline directly.
 */
public void println() {
	fWriter.println();
}
/**
 * Print an object directly, with a newline.
 */
public void println(Object obj) {
	fWriter.println(obj);
}
/**
 * Outputs tabs if needed.
 */
void tabIfNeeded() {
	for (int i = 0; i < fTabLevel; ++i) {
		fWriter.print("  "); //$NON-NLS-1$
	}
}
}
