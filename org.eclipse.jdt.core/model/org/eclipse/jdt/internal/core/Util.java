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
package org.eclipse.jdt.internal.core;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Provides convenient utility methods to other types in this package.
 */
public class Util {

	private final static char[] DOUBLE_QUOTES = "''".toCharArray(); //$NON-NLS-1$
	private final static char[] SINGLE_QUOTE = "'".toCharArray(); //$NON-NLS-1$
	private static final String ARGUMENTS_DELIMITER = "#"; //$NON-NLS-1$
	private static final String EMPTY_ARGUMENT = "   "; //$NON-NLS-1$

	public interface Comparable {
		/**
		 * Returns 0 if this and c are equal, >0 if this is greater than c,
		 * or <0 if this is less than c.
		 */
		int compareTo(Comparable c);
	}

	public interface Comparer {
		/**
		 * Returns 0 if a and b are equal, >0 if a is greater than b,
		 * or <0 if a is less than b.
		 */
		int compare(Object a, Object b);
	}
	
	public static final String[] fgEmptyStringArray = new String[0];

	/**
	 * Are we running JDK 1.1?
	 */
	private static boolean JDK1_1 = false;

	/* Bundle containing messages */
	protected static ResourceBundle bundle;
	private final static String bundleName = "org.eclipse.jdt.internal.core.messages"; //$NON-NLS-1$

	public final static char[] SUFFIX_class = ".class".toCharArray(); //$NON-NLS-1$
	public final static char[] SUFFIX_CLASS = ".CLASS".toCharArray(); //$NON-NLS-1$
	public final static char[] SUFFIX_java = ".java".toCharArray(); //$NON-NLS-1$
	public final static char[] SUFFIX_JAVA = ".JAVA".toCharArray(); //$NON-NLS-1$

	static {
		String ver = System.getProperty("java.version"); //$NON-NLS-1$
		JDK1_1 = ((ver != null) && ver.startsWith("1.1")); //$NON-NLS-1$
		relocalize();
	}	
	/**
	 * Checks the type signature in String sig, 
	 * starting at start and ending before end (end is not included).
	 * Returns the index of the character immediately after the signature if valid,
	 * or -1 if not valid.
	 */
	private static int checkTypeSignature(String sig, int start, int end, boolean allowVoid) {
		if (start >= end) return -1;
		int i = start;
		char c = sig.charAt(i++);
		int nestingDepth = 0;
		while (c == '[') {
			++nestingDepth;
			if (i >= end) return -1;
			c = sig.charAt(i++);
		}
		switch (c) {
			case 'B':
			case 'C': 
			case 'D':
			case 'F':
			case 'I':
			case 'J':
			case 'S': 
			case 'Z':
				break;
			case 'V':
				if (!allowVoid) return -1;
				// array of void is not allowed
				if (nestingDepth != 0) return -1;
				break;
			case 'L':
				int semicolon = sig.indexOf(';', i);
				// Must have at least one character between L and ;
				if (semicolon <= i || semicolon >= end) return -1;
				i = semicolon + 1;
				break;
			default:
				return -1;
		}
		return i;
	}
/**
 * Combines two hash codes to make a new one.
 */
public static int combineHashCodes(int hashCode1, int hashCode2) {
	return hashCode1 * 17 + hashCode2;
}
/**
 * Compares two byte arrays.  
 * Returns <0 if a byte in a is less than the corresponding byte in b, or if a is shorter, or if a is null.
 * Returns >0 if a byte in a is greater than the corresponding byte in b, or if a is longer, or if b is null.
 * Returns 0 if they are equal or both null.
 */
public static int compare(byte[] a, byte[] b) {
	if (a == b)
		return 0;
	if (a == null)
		return -1;
	if (b == null)
		return 1;
	int len = Math.min(a.length, b.length);
	for (int i = 0; i < len; ++i) {
		int diff = a[i] - b[i];
		if (diff != 0)
			return diff;
	}
	if (a.length > len)
		return 1;
	if (b.length > len)
		return -1;
	return 0;
}
/**
 * Compares two char arrays lexicographically. 
 * The comparison is based on the Unicode value of each character in
 * the char arrays. 
 * @return  the value <code>0</code> if a is equal to
 *          b; a value less than <code>0</code> if a
 *          is lexicographically less than b; and a
 *          value greater than <code>0</code> if a is
 *          lexicographically greater than b.
 */
public static int compare(char[] v1, char[] v2) {
	int len1 = v1.length;
	int len2 = v2.length;
	int n = Math.min(len1, len2);
	int i = 0;
	while (n-- != 0) {
		if (v1[i] != v2[i]) {
			return v1[i] - v2[i];
		}
		++i;
	}
	return len1 - len2;
}
	/**
	 * Concatenate two strings with a char in between.
	 * @see #concat(String, String)
	 */
	public static String concat(String s1, char c, String s2) {
		if (s1 == null) s1 = "null"; //$NON-NLS-1$
		if (s2 == null) s2 = "null"; //$NON-NLS-1$
		int l1 = s1.length();
		int l2 = s2.length();
		char[] buf = new char[l1 + 1 + l2];
		s1.getChars(0, l1, buf, 0);
		buf[l1] = c;
		s2.getChars(0, l2, buf, l1 + 1);
		return new String(buf);
	}
	/**
	 * Concatenate two strings.
	 * Much faster than using +, which:
	 * 		- creates a StringBuffer,
	 * 		- which is synchronized,
	 * 		- of default size, so the resulting char array is
	 *        often larger than needed.
	 * This implementation creates an extra char array, since the
	 * String constructor copies its argument, but there's no way around this.
	 */
	public static String concat(String s1, String s2) {
		if (s1 == null) s1 = "null"; //$NON-NLS-1$
		if (s2 == null) s2 = "null"; //$NON-NLS-1$
		int l1 = s1.length();
		int l2 = s2.length();
		char[] buf = new char[l1 + l2];
		s1.getChars(0, l1, buf, 0);
		s2.getChars(0, l2, buf, l1);
		return new String(buf);
	}

	/**
	 * Concatenate three strings.
	 * @see #concat(String, String)
	 */
	public static String concat(String s1, String s2, String s3) {
		if (s1 == null) s1 = "null"; //$NON-NLS-1$
		if (s2 == null) s2 = "null"; //$NON-NLS-1$
		if (s3 == null) s3 = "null"; //$NON-NLS-1$
		int l1 = s1.length();
		int l2 = s2.length();
		int l3 = s3.length();
		char[] buf = new char[l1 + l2 + l3];
		s1.getChars(0, l1, buf, 0);
		s2.getChars(0, l2, buf, l1);
		s3.getChars(0, l3, buf, l1 + l2);
		return new String(buf);
	}
/**
 * Converts a type signature from the IBinaryType representation to the DC representation.
 */
public static String convertTypeSignature(char[] sig) {
	return new String(sig).replace('/', '.');
}
/**
 * Compares two arrays using equals() on the elements.
 * Either or both arrays may be null.
 * Returns true if both are null.
 * Returns false if only one is null.
 * If both are arrays, returns true iff they have the same length and
 * all elements are equal.
 */
public static boolean equalArraysOrNull(int[] a, int[] b) {
	if (a == b)
		return true;
	if (a == null || b == null)
		return false;
	int len = a.length;
	if (len != b.length)
		return false;
	for (int i = 0; i < len; ++i) {
		if (a[i] != b[i])
			return false;
	}
	return true;
}
	/**
	 * Compares two arrays using equals() on the elements.
	 * Either or both arrays may be null.
	 * Returns true if both are null.
	 * Returns false if only one is null.
	 * If both are arrays, returns true iff they have the same length and
	 * all elements compare true with equals.
	 */
	public static boolean equalArraysOrNull(Object[] a, Object[] b) {
		if (a == b)	return true;
		if (a == null || b == null) return false;

		int len = a.length;
		if (len != b.length) return false;
		for (int i = 0; i < len; ++i) {
			if (a[i] == null) {
				if (b[i] != null) return false;
			} else {
				if (!a[i].equals(b[i])) return false;
			}
		}
		return true;
	}
	/**
	 * Compares two String arrays using equals() on the elements.
	 * The arrays are first sorted.
	 * Either or both arrays may be null.
	 * Returns true if both are null.
	 * Returns false if only one is null.
	 * If both are arrays, returns true iff they have the same length and
	 * iff, after sorting both arrays, all elements compare true with equals.
	 * The original arrays are left untouched.
	 */
	public static boolean equalArraysOrNullSortFirst(String[] a, String[] b) {
		if (a == b)	return true;
		if (a == null || b == null) return false;
		int len = a.length;
		if (len != b.length) return false;
		if (len >= 2) {  // only need to sort if more than two items
			a = sortCopy(a);
			b = sortCopy(b);
		}
		for (int i = 0; i < len; ++i) {
			if (!a[i].equals(b[i])) return false;
		}
		return true;
	}
	/**
	 * Compares two arrays using equals() on the elements.
	 * The arrays are first sorted.
	 * Either or both arrays may be null.
	 * Returns true if both are null.
	 * Returns false if only one is null.
	 * If both are arrays, returns true iff they have the same length and
	 * iff, after sorting both arrays, all elements compare true with equals.
	 * The original arrays are left untouched.
	 */
	public static boolean equalArraysOrNullSortFirst(Comparable[] a, Comparable[] b) {
		if (a == b)	return true;
		if (a == null || b == null) return false;
		int len = a.length;
		if (len != b.length) return false;
		if (len >= 2) {  // only need to sort if more than two items
			a = sortCopy(a);
			b = sortCopy(b);
		}
		for (int i = 0; i < len; ++i) {
			if (!a[i].equals(b[i])) return false;
		}
		return true;
	}
	/**
	 * Compares two objects using equals().
	 * Either or both array may be null.
	 * Returns true if both are null.
	 * Returns false if only one is null.
	 * Otherwise, return the result of comparing with equals().
	 */
	public static boolean equalOrNull(Object a, Object b) {
		if (a == b) {
			return true;
		}
		if (a == null || b == null) {
			return false;
		}
		return a.equals(b);
	}
	/**
	 * Given a qualified name, extract the last component.
	 * If the input is not qualified, the same string is answered.
	 */
	public static String extractLastName(String qualifiedName) {
		int i = qualifiedName.lastIndexOf('.');
		if (i == -1) return qualifiedName;
		return qualifiedName.substring(i+1);
	}
/**
 * Extracts the parameter types from a method signature.
 */
public static String[] extractParameterTypes(char[] sig) {
	int count = getParameterCount(sig);
	String[] result = new String[count];
	if (count == 0)
		return result;
	int i = CharOperation.indexOf('(', sig) + 1;
	count = 0;
	int len = sig.length;
	int start = i;
	for (;;) {
		if (i == len)
			break;
		char c = sig[i];
		if (c == ')')
			break;
		if (c == '[') {
			++i;
		} else
			if (c == 'L') {
				i = CharOperation.indexOf(';', sig, i + 1) + 1;
				Assert.isTrue(i != 0);
				result[count++] = convertTypeSignature(CharOperation.subarray(sig, start, i));
				start = i;
			} else {
				++i;
				result[count++] = convertTypeSignature(CharOperation.subarray(sig, start, i));
				start = i;
			}
	}
	return result;
}
	/**
	 * Extracts the return type from a method signature.
	 */
	public static String extractReturnType(String sig) {
		int i = sig.lastIndexOf(')');
		Assert.isTrue(i != -1);
		return sig.substring(i+1);	
	}
/**
 * Finds the first line separator used by the given text.
 *
 * @return </code>"\n"</code> or </code>"\r"</code> or  </code>"\r\n"</code>,
 *			or <code>null</code> if none found
 */
private static String findLineSeparator(char[] text) {
	// find the first line separator
	int length = text.length;
	if (length > 0) {
		char nextChar = text[0];
		for (int i = 0; i < length; i++) {
			char currentChar = nextChar;
			nextChar = i < length-1 ? text[i+1] : ' ';
			switch (currentChar) {
				case '\n': return "\n"; //$NON-NLS-1$
				case '\r': return nextChar == '\n' ? "\r\n" : "\r"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}
	// not found
	return null;
}
/**
 * Returns the line separator used by the given buffer.
 * Uses the given text if none found.
 *
 * @return </code>"\n"</code> or </code>"\r"</code> or  </code>"\r\n"</code>
 */
private static String getLineSeparator(char[] text, char[] buffer) {
	// search in this buffer's contents first
	String lineSeparator = findLineSeparator(buffer);
	if (lineSeparator == null) {
		// search in the given text
		lineSeparator = findLineSeparator(text);
		if (lineSeparator == null) {
			// default to system line separator
			return org.eclipse.jdt.internal.compiler.util.Util.LINE_SEPARATOR;
		}
	}
	return lineSeparator;
}


	
/**
 * Returns the number of parameter types in a method signature.
 */
public static int getParameterCount(char[] sig) {
	int i = CharOperation.indexOf('(', sig) + 1;
	Assert.isTrue(i != 0);
	int count = 0;
	int len = sig.length;
	for (;;) {
		if (i == len)
			break;
		char c = sig[i];
		if (c == ')')
			break;
		if (c == '[') {
			++i;
		} else
			if (c == 'L') {
				++count;
				i = CharOperation.indexOf(';', sig, i + 1) + 1;
				Assert.isTrue(i != 0);
			} else {
				++count;
				++i;
			}
	}
	return count;
}
/**
 * Returns the given file's contents as a byte array.
 */
public static byte[] getResourceContentsAsByteArray(IFile file) throws JavaModelException {
	InputStream stream= null;
	try {
		stream = new BufferedInputStream(file.getContents(true));
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
	try {
		return org.eclipse.jdt.internal.compiler.util.Util.getInputStreamAsByteArray(stream, -1);
	} catch (IOException e) {
		throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
	} finally {
		try {
			stream.close();
		} catch (IOException e) {
		}
	}
}
/**
 * Returns the given file's contents as a character array.
 */
public static char[] getResourceContentsAsCharArray(IFile file) throws JavaModelException {
	InputStream stream= null;
	try {
		stream = new BufferedInputStream(file.getContents(true));
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
	try {
		String encoding = JavaCore.getOption(JavaCore.CORE_ENCODING);
		return org.eclipse.jdt.internal.compiler.util.Util.getInputStreamAsCharArray(stream, -1, encoding);
	} catch (IOException e) {
		throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
	} finally {
		try {
			stream.close();
		} catch (IOException e) {
		}
	}
}
	/**
	 * Returns true if the given method signature is valid,
	 * false if it is not.
	 */
	public static boolean isValidMethodSignature(String sig) {
		int len = sig.length();
		if (len == 0) return false;
		int i = 0;
		char c = sig.charAt(i++);
		if (c != '(') return false;
		if (i >= len) return false;
		while (sig.charAt(i) != ')') {
			// Void is not allowed as a parameter type.
			i = checkTypeSignature(sig, i, len, false);
			if (i == -1) return false;
			if (i >= len) return false;
		}
		++i;
		i = checkTypeSignature(sig, i, len, true);
		return i == len;
	}
	/**
	 * Returns true if the given type signature is valid,
	 * false if it is not.
	 */
	public static boolean isValidTypeSignature(String sig, boolean allowVoid) {
		int len = sig.length();
		return checkTypeSignature(sig, 0, len, allowVoid) == len;
	}
	/**
	 * Returns true if the given folder name is valid for a package,
	 * false if it is not.
	 */
	public static boolean isValidFolderNameForPackage(String folderName) {
		return JavaConventions.validateIdentifier(folderName).getSeverity() != IStatus.ERROR;
	}	

	/*
	 * Add a log entry
	 */
	public static void log(Throwable e, String message) {
		IStatus status= new Status(
			IStatus.ERROR, 
			JavaCore.getPlugin().getDescriptor().getUniqueIdentifier(), 
			IStatus.ERROR, 
			message, 
			e); 
		JavaCore.getPlugin().getLog().log(status);
	}	
	
/**
 * Normalizes the cariage returns in the given text.
 * They are all changed  to use the given buffer's line sepatator.
 */
public static char[] normalizeCRs(char[] text, char[] buffer) {
	CharArrayBuffer result = new CharArrayBuffer();
	int lineStart = 0;
	int length = text.length;
	if (length == 0) return text;
	String lineSeparator = getLineSeparator(text, buffer);
	char nextChar = text[0];
	for (int i = 0; i < length; i++) {
		char currentChar = nextChar;
		nextChar = i < length-1 ? text[i+1] : ' ';
		switch (currentChar) {
			case '\n':
				int lineLength = i-lineStart;
				char[] line = new char[lineLength];
				System.arraycopy(text, lineStart, line, 0, lineLength);
				result.append(line);
				result.append(lineSeparator);
				lineStart = i+1;
				break;
			case '\r':
				lineLength = i-lineStart;
				if (lineLength >= 0) {
					line = new char[lineLength];
					System.arraycopy(text, lineStart, line, 0, lineLength);
					result.append(line);
					result.append(lineSeparator);
					if (nextChar == '\n') {
						nextChar = ' ';
						lineStart = i+2;
					} else {
						// when line separator are mixed in the same file
						// \r might not be followed by a \n. If not, we should increment
						// lineStart by one and not by two.
						lineStart = i+1;
					}
				} else {
					// when line separator are mixed in the same file
					// we need to prevent NegativeArraySizeException
					lineStart = i+1;
				}
				break;
		}
	}
	char[] lastLine;
	if (lineStart > 0) {
		int lastLineLength = length-lineStart;
		if (lastLineLength > 0) {
			lastLine = new char[lastLineLength];
			System.arraycopy(text, lineStart, lastLine, 0, lastLineLength);
			result.append(lastLine);
		}
		return result.getContents();
	} else {
		return text;
	}
}

/**
 * Normalizes the cariage returns in the given text.
 * They are all changed  to use given buffer's line sepatator.
 */
public static String normalizeCRs(String text, String buffer) {
	return new String(normalizeCRs(text.toCharArray(), buffer.toCharArray()));
}

	

/**
 * Sort the objects in the given collection using the given sort order.
 */
private static void quickSort(Object[] sortedCollection, int left, int right, int[] sortOrder) {
	int original_left = left;
	int original_right = right;
	int mid = sortOrder[ (left + right) / 2];
	do {
		while (sortOrder[left] < mid) {
			left++;
		}
		while (mid < sortOrder[right]) {
			right--;
		}
		if (left <= right) {
			Object tmp = sortedCollection[left];
			sortedCollection[left] = sortedCollection[right];
			sortedCollection[right] = tmp;
			int tmp2 = sortOrder[left];
			sortOrder[left] = sortOrder[right];
			sortOrder[right] = tmp2;
			left++;
			right--;
		}
	} while (left <= right);
	if (original_left < right) {
		quickSort(sortedCollection, original_left, right, sortOrder);
	}
	if (left < original_right) {
		quickSort(sortedCollection, left, original_right, sortOrder);
	}
}
/**
 * Sort the objects in the given collection using the given comparer.
 */
private static void quickSort(Object[] sortedCollection, int left, int right, Comparer comparer) {
	int original_left = left;
	int original_right = right;
	Object mid = sortedCollection[ (left + right) / 2];
	do {
		while (comparer.compare(sortedCollection[left], mid) < 0) {
			left++;
		}
		while (comparer.compare(mid, sortedCollection[right]) < 0) {
			right--;
		}
		if (left <= right) {
			Object tmp = sortedCollection[left];
			sortedCollection[left] = sortedCollection[right];
			sortedCollection[right] = tmp;
			left++;
			right--;
		}
	} while (left <= right);
	if (original_left < right) {
		quickSort(sortedCollection, original_left, right, comparer);
	}
	if (left < original_right) {
		quickSort(sortedCollection, left, original_right, comparer);
	}
}
/**
 * Sort the strings in the given collection.
 */
private static void quickSort(String[] sortedCollection, int left, int right) {
	int original_left = left;
	int original_right = right;
	String mid = sortedCollection[ (left + right) / 2];
	do {
		while (sortedCollection[left].compareTo(mid) < 0) {
			left++;
		}
		while (mid.compareTo(sortedCollection[right]) < 0) {
			right--;
		}
		if (left <= right) {
			String tmp = sortedCollection[left];
			sortedCollection[left] = sortedCollection[right];
			sortedCollection[right] = tmp;
			left++;
			right--;
		}
	} while (left <= right);
	if (original_left < right) {
		quickSort(sortedCollection, original_left, right);
	}
	if (left < original_right) {
		quickSort(sortedCollection, left, original_right);
	}
}
/**
 * Converts the given relative path into a package name.
 * Returns null if the path is not a valid package name.
 */
public static String packageName(IPath pkgPath) {
	StringBuffer pkgName = new StringBuffer(IPackageFragment.DEFAULT_PACKAGE_NAME);
	for (int j = 0, max = pkgPath.segmentCount(); j < max; j++) {
		String segment = pkgPath.segment(j);
		if (!isValidFolderNameForPackage(segment)) {
			return null;
		}
		pkgName.append(segment);
		if (j < pkgPath.segmentCount() - 1) {
			pkgName.append("." ); //$NON-NLS-1$
		}
	}
	return pkgName.toString();
}
/**
 * Sort the comparable objects in the given collection.
 */
private static void quickSort(Comparable[] sortedCollection, int left, int right) {
	int original_left = left;
	int original_right = right;
	Comparable mid = sortedCollection[ (left + right) / 2];
	do {
		while (sortedCollection[left].compareTo(mid) < 0) {
			left++;
		}
		while (mid.compareTo(sortedCollection[right]) < 0) {
			right--;
		}
		if (left <= right) {
			Comparable tmp = sortedCollection[left];
			sortedCollection[left] = sortedCollection[right];
			sortedCollection[right] = tmp;
			left++;
			right--;
		}
	} while (left <= right);
	if (original_left < right) {
		quickSort(sortedCollection, original_left, right);
	}
	if (left < original_right) {
		quickSort(sortedCollection, left, original_right);
	}
}
/**
 * Sort the strings in the given collection in reverse alphabetical order.
 */
private static void quickSortReverse(String[] sortedCollection, int left, int right) {
	int original_left = left;
	int original_right = right;
	String mid = sortedCollection[ (left + right) / 2];
	do {
		while (sortedCollection[left].compareTo(mid) > 0) {
			left++;
		}
		while (mid.compareTo(sortedCollection[right]) > 0) {
			right--;
		}
		if (left <= right) {
			String tmp = sortedCollection[left];
			sortedCollection[left] = sortedCollection[right];
			sortedCollection[right] = tmp;
			left++;
			right--;
		}
	} while (left <= right);
	if (original_left < right) {
		quickSortReverse(sortedCollection, original_left, right);
	}
	if (left < original_right) {
		quickSortReverse(sortedCollection, left, original_right);
	}
}
/**
 * Sorts an array of objects in place, using the sort order given for each item.
 */
public static void sort(Object[] objects, int[] sortOrder) {
	if (objects.length > 1)
		quickSort(objects, 0, objects.length - 1, sortOrder);
}
/**
 * Sorts an array of objects in place.
 * The given comparer compares pairs of items.
 */
public static void sort(Object[] objects, Comparer comparer) {
	if (objects.length > 1)
		quickSort(objects, 0, objects.length - 1, comparer);
}
/**
 * Sorts an array of strings in place using quicksort.
 */
public static void sort(String[] strings) {
	if (strings.length > 1)
		quickSort(strings, 0, strings.length - 1);
}
/**
 * Sorts an array of Comparable objects in place.
 */
public static void sort(Comparable[] objects) {
	if (objects.length > 1)
		quickSort(objects, 0, objects.length - 1);
}
	/**
	 * Sorts an array of Strings, returning a new array
	 * with the sorted items.  The original array is left untouched.
	 */
	public static Object[] sortCopy(Object[] objects, Comparer comparer) {
		int len = objects.length;
		Object[] copy = new Object[len];
		System.arraycopy(objects, 0, copy, 0, len);
		sort(copy, comparer);
		return copy;
	}
	/**
	 * Sorts an array of Strings, returning a new array
	 * with the sorted items.  The original array is left untouched.
	 */
	public static String[] sortCopy(String[] objects) {
		int len = objects.length;
		String[] copy = new String[len];
		System.arraycopy(objects, 0, copy, 0, len);
		sort(copy);
		return copy;
	}
	/**
	 * Sorts an array of Comparable objects, returning a new array
	 * with the sorted items.  The original array is left untouched.
	 */
	public static Comparable[] sortCopy(Comparable[] objects) {
		int len = objects.length;
		Comparable[] copy = new Comparable[len];
		System.arraycopy(objects, 0, copy, 0, len);
		sort(copy);
		return copy;
	}
/**
 * Sorts an array of strings in place using quicksort
 * in reverse alphabetical order.
 */
public static void sortReverseOrder(String[] strings) {
	if (strings.length > 1)
		quickSortReverse(strings, 0, strings.length - 1);
}
	/**
	 * Converts a String[] to char[][].
	 */
	public static char[][] toCharArrays(String[] a) {
		int len = a.length;
		char[][] result = new char[len][];
		for (int i = 0; i < len; ++i) {
			result[i] = toChars(a[i]);
		}
		return result;
	}
	/**
	 * Converts a String to char[].
	 */
	public static char[] toChars(String s) {
		int len = s.length();
		char[] chars = new char[len];
		s.getChars(0, len, chars, 0);
		return chars;
	}
	/**
	 * Converts a String to char[][], where segments are separate by '.'.
	 */
	public static char[][] toCompoundChars(String s) {
		int len = s.length();
		if (len == 0) {
			return new char[0][];
		}
		int segCount = 1;
		for (int off = s.indexOf('.'); off != -1; off = s.indexOf('.', off + 1)) {
			++segCount;
		}
		char[][] segs = new char[segCount][];
		int start = 0;
		for (int i = 0; i < segCount; ++i) {
			int dot = s.indexOf('.', start);
			int end = (dot == -1 ? s.length() : dot);
			segs[i] = new char[end - start];
			s.getChars(start, end, segs[i], 0);
			start = end + 1;
		}
		return segs;
	}
	/**
	 * Converts a char[][] to String, where segments are separated by '.'.
	 */
	public static String toString(char[][] c) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0, max = c.length; i < max; ++i) {
			if (i != 0) sb.append('.');
			sb.append(c[i]);
		}
		return sb.toString();
	}
	/**
	 * Converts a char[][] and a char[] to String, where segments are separated by '.'.
	 */
	public static String toString(char[][] c, char[] d) {
		if (c == null) return new String(d);
		StringBuffer sb = new StringBuffer();
		for (int i = 0, max = c.length; i < max; ++i) {
			sb.append(c[i]);
			sb.append('.');
		}
		sb.append(d);
		return sb.toString();
	}
	/**
	 * Converts a char[] to String.
	 */
	public static String toString(char[] c) {
		return new String(c);
	}
	/**
	 * Asserts that the given method signature is valid.
	 */
	public static void validateMethodSignature(String sig) {
		Assert.isTrue(isValidMethodSignature(sig));
	}
	/**
	 * Asserts that the given type signature is valid.
	 */
	public static void validateTypeSignature(String sig, boolean allowVoid) {
		Assert.isTrue(isValidTypeSignature(sig, allowVoid));
	}

/**
 * Lookup the message with the given ID in this catalog 
 */
public static String bind(String id) {
	return bind(id, (String[])null);
}

/**
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given string values.
 */
public static String bind(String id, String[] bindings) {
	if (id == null)
		return "No message available"; //$NON-NLS-1$
	String message = null;
	try {
		message = bundle.getString(id);
	} catch (MissingResourceException e) {
		// If we got an exception looking for the message, fail gracefully by just returning
		// the id we were looking for.  In most cases this is semi-informative so is not too bad.
		return "Missing message: " + id + " in: " + bundleName; //$NON-NLS-2$ //$NON-NLS-1$
	}
	// for compatibility with MessageFormat which eliminates double quotes in original message
	char[] messageWithNoDoubleQuotes =
	CharOperation.replace(message.toCharArray(), DOUBLE_QUOTES, SINGLE_QUOTE);
	message = new String(messageWithNoDoubleQuotes);

	if (bindings == null)
		return message;

	int length = message.length();
	int start = -1;
	int end = length;
	StringBuffer output = new StringBuffer(80);
	while (true) {
		if ((end = message.indexOf('{', start)) > -1) {
			output.append(message.substring(start + 1, end));
			if ((start = message.indexOf('}', end)) > -1) {
				int index = -1;
				try {
					index = Integer.parseInt(message.substring(end + 1, start));
					output.append(bindings[index]);
				} catch (NumberFormatException nfe) {
					output.append(message.substring(end + 1, start + 1));
				} catch (ArrayIndexOutOfBoundsException e) {
					output.append("{missing " + Integer.toString(index) + "}"); //$NON-NLS-2$ //$NON-NLS-1$
				}
			} else {
				output.append(message.substring(end, length));
				break;
			}
		} else {
			output.append(message.substring(start + 1, length));
			break;
		}
	}
	return output.toString();
}

/**
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given string.
 */
public static String bind(String id, String binding) {
	return bind(id, new String[] {binding});
}

/**
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given strings.
 */
public static String bind(String id, String binding1, String binding2) {
	return bind(id, new String[] {binding1, binding2});
}

	/**
	 * Returns true iff str.toLowerCase().endsWith(end.toLowerCase())
	 * implementation is not creating extra strings.
	 */
	public final static boolean endsWithIgnoreCase(String str, String end) {
		
		int strLength = str == null ? 0 : str.length();
		int endLength = end == null ? 0 : end.length();
		
		// return false if the string is smaller than the end.
		if(endLength > strLength)
			return false;
			
		// return false if any character of the end are
		// not the same in lower case.
		for(int i = 1 ; i <= endLength; i++){
			if(Character.toLowerCase(end.charAt(endLength - i)) != Character.toLowerCase(str.charAt(strLength - i)))
				return false;
		}
		
		return true;
	}

	/**
	 * Returns true iff str.toLowerCase().endsWith(".class")
	 * implementation is not creating extra strings.
	 */
	public final static boolean isClassFileName(String name) {
		int nameLength = name == null ? 0 : name.length();
		int suffixLength = SUFFIX_CLASS.length;
		if (nameLength < suffixLength) return false;

		for (int i = 0, offset = nameLength - suffixLength; i < suffixLength; i++) {
			char c = name.charAt(offset + i);
			if (c != SUFFIX_class[i] && c != SUFFIX_CLASS[i]) return false;
		}
		return true;		
	}

	/**
	 * Validate the given compilation unit name.
	 * A compilation unit name must obey the following rules:
	 * <ul>
	 * <li> it must not be null
	 * <li> it must include the <code>".java"</code> suffix
	 * <li> its prefix must be a valid identifier
	 * </ul>
	 * </p>
	 * @param name the name of a compilation unit
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a compilation unit name, otherwise a status 
	 *		object indicating what is wrong with the name
	 */
	public static boolean isValidCompilationUnitName(String name) {
		return JavaConventions.validateCompilationUnitName(name).getSeverity() != IStatus.ERROR;
	}

	/**
	 * Validate the given .class file name.
	 * A .class file name must obey the following rules:
	 * <ul>
	 * <li> it must not be null
	 * <li> it must include the <code>".class"</code> suffix
	 * <li> its prefix must be a valid identifier
	 * </ul>
	 * </p>
	 * @param name the name of a .class file
	 * @return a status object with code <code>IStatus.OK</code> if
	 *		the given name is valid as a .class file name, otherwise a status 
	 *		object indicating what is wrong with the name
	 */
	public static boolean isValidClassFileName(String name) {
		return JavaConventions.validateClassFileName(name).getSeverity() != IStatus.ERROR;
	}

	/**
	 * Returns true iff str.toLowerCase().endsWith(".java")
	 * implementation is not creating extra strings.
	 */
	public final static boolean isJavaFileName(String name) {
		int nameLength = name == null ? 0 : name.length();
		int suffixLength = SUFFIX_JAVA.length;
		if (nameLength < suffixLength) return false;

		for (int i = 0, offset = nameLength - suffixLength; i < suffixLength; i++) {
			char c = name.charAt(offset + i);
			if (c != SUFFIX_java[i] && c != SUFFIX_JAVA[i]) return false;
		}
		return true;		
	}

	/**
	 * Creates a NLS catalog for the given locale.
	 */
	public static void relocalize() {
		bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
	}
	
	/**
	 * Put all the arguments in one String.
	 */
	public static String getProblemArgumentsForMarker(String[] arguments){
		StringBuffer args = new StringBuffer(10);
		
		args.append(arguments.length);
		args.append(':');
		
			
		for (int j = 0; j < arguments.length; j++) {
			if(j != 0)
				args.append(ARGUMENTS_DELIMITER);
			
			if(arguments[j].length() == 0) {
				args.append(EMPTY_ARGUMENT);
			} else {			
				args.append(arguments[j]);
			}
		}
		
		return args.toString();
	}
	
	/**
	 * Separate all the arguments of a String made by getProblemArgumentsForMarker
	 */
	public static String[] getProblemArgumentsFromMarker(String argumentsString){
		int index = argumentsString.indexOf(':');
		if(index == -1)
			return null;
		
		int length = argumentsString.length();
		int numberOfArg;
		try{
			numberOfArg = Integer.parseInt(argumentsString.substring(0 , index));
		} catch (NumberFormatException e) {
			return null;
		}
		argumentsString = argumentsString.substring(index + 1, length);
		
		String[] args = new String[length];
		int count = 0;
		
		StringTokenizer tokenizer = new StringTokenizer(argumentsString, ARGUMENTS_DELIMITER);
		while(tokenizer.hasMoreTokens()) {
			String argument = tokenizer.nextToken();
			if(argument.equals(EMPTY_ARGUMENT))
				argument = "";  //$NON-NLS-1$
			args[count++] = argument;
		}
		
		if(count != numberOfArg)
			return null;
		
		System.arraycopy(args, 0, args = new String[count], 0, count);
		return args;
	}
}
