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
package org.eclipse.jdt.internal.compiler.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Util {

	public static String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
	public static char[] LINE_SEPARATOR_CHARS = LINE_SEPARATOR.toCharArray();
	public final static char[] SUFFIX_class = ".class".toCharArray(); //$NON-NLS-1$
	public final static char[] SUFFIX_CLASS = ".CLASS".toCharArray(); //$NON-NLS-1$
	public final static char[] SUFFIX_java = ".java".toCharArray(); //$NON-NLS-1$
	public final static char[] SUFFIX_JAVA = ".JAVA".toCharArray(); //$NON-NLS-1$
	public final static char[] SUFFIX_jar = ".jar".toCharArray(); //$NON-NLS-1$
	public final static char[] SUFFIX_JAR = ".JAR".toCharArray(); //$NON-NLS-1$
	public final static char[] SUFFIX_zip = ".zip".toCharArray(); //$NON-NLS-1$
	public final static char[] SUFFIX_ZIP = ".ZIP".toCharArray(); //$NON-NLS-1$
		
	private final static char[] DOUBLE_QUOTES = "''".toCharArray(); //$NON-NLS-1$
	private final static char[] SINGLE_QUOTE = "'".toCharArray(); //$NON-NLS-1$

	/* Bundle containing messages */
	protected static ResourceBundle bundle;
	private final static String bundleName =
		"org.eclipse.jdt.internal.compiler.util.messages"; //$NON-NLS-1$
	static {
		relocalize();
	}
	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given strings.
	 */
	public static String bind(String id, String binding1, String binding2) {
		return bind(id, new String[] { binding1, binding2 });
	}
	/**
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string.
	 */
	public static String bind(String id, String binding) {
		return bind(id, new String[] { binding });
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
						output.append("{missing " + Integer.toString(index) + "}");	//$NON-NLS-2$ //$NON-NLS-1$
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
	 * Lookup the message with the given ID in this catalog 
	 */
	public static String bind(String id) {
		return bind(id, (String[]) null);
	}
	/**
	 * Creates a NLS catalog for the given locale.
	 */
	public static void relocalize() {
		bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
	}
	/**
	 * Returns the given bytes as a char array using a given encoding (null means platform default).
	 */
	public static char[] bytesToChar(byte[] bytes, String encoding) throws IOException {

		return getInputStreamAsCharArray(new ByteArrayInputStream(bytes), bytes.length, encoding);

	}
	/**
	 * Returns the contents of the given file as a byte array.
	 * @throws IOException if a problem occured reading the file.
	 */
	public static byte[] getFileByteContent(File file) throws IOException {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
			return getInputStreamAsByteArray(stream, (int) file.length());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	/**
	 * Returns the contents of the given file as a char array.
	 * When encoding is null, then the platform default one is used
	 * @throws IOException if a problem occured reading the file.
	 */
	public static char[] getFileCharContent(File file, String encoding) throws IOException {
		InputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(file));
			return Util.getInputStreamAsCharArray(stream, (int) file.length(), encoding);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	/**
	 * Returns the given input stream's contents as a byte array.
	 * If a length is specified (ie. if length != -1), only length bytes
	 * are returned. Otherwise all bytes in the stream are returned.
	 * Note this doesn't close the stream.
	 * @throws IOException if a problem occured reading the stream.
	 */
	public static byte[] getInputStreamAsByteArray(InputStream stream, int length)
		throws IOException {
		byte[] contents;
		if (length == -1) {
			contents = new byte[0];
			int contentsLength = 0;
			int bytesRead = -1;
			do {
				int available = stream.available();

				// resize contents if needed
				if (contentsLength + available > contents.length) {
					System.arraycopy(
						contents,
						0,
						contents = new byte[contentsLength + available],
						0,
						contentsLength);
				}

				// read as many bytes as possible
				bytesRead = stream.read(contents, contentsLength, available);

				if (bytesRead > 0) {
					// remember length of contents
					contentsLength += bytesRead;
				}
			} while (bytesRead > 0);

			// resize contents if necessary
			if (contentsLength < contents.length) {
				System.arraycopy(
					contents,
					0,
					contents = new byte[contentsLength],
					0,
					contentsLength);
			}
		} else {
			contents = new byte[length];
			int len = 0;
			int readSize = 0;
			while ((readSize != -1) && (len != length)) {
				// See PR 1FMS89U
				// We record first the read size. In this case len is the actual read size.
				len += readSize;
				readSize = stream.read(contents, len, length - len);
			}
		}

		return contents;
	}
	/**
	 * Returns the given input stream's contents as a character array.
	 * If a length is specified (ie. if length != -1), only length chars
	 * are returned. Otherwise all chars in the stream are returned.
	 * Note this doesn't close the stream.
	 * @throws IOException if a problem occured reading the stream.
	 */
	public static char[] getInputStreamAsCharArray(InputStream stream, int length, String encoding)
		throws IOException {
		InputStreamReader reader = null;
		reader = encoding == null
					? new InputStreamReader(stream)
					: new InputStreamReader(stream, encoding);
		char[] contents;
		if (length == -1) {
			contents = new char[0];
			int contentsLength = 0;
			int charsRead = -1;
			do {
				int available = stream.available();

				// resize contents if needed
				if (contentsLength + available > contents.length) {
					System.arraycopy(
						contents,
						0,
						contents = new char[contentsLength + available],
						0,
						contentsLength);
				}

				// read as many chars as possible
				charsRead = reader.read(contents, contentsLength, available);

				if (charsRead > 0) {
					// remember length of contents
					contentsLength += charsRead;
				}
			} while (charsRead > 0);

			// resize contents if necessary
			if (contentsLength < contents.length) {
				System.arraycopy(
					contents,
					0,
					contents = new char[contentsLength],
					0,
					contentsLength);
			}
		} else {
			contents = new char[length];
			int len = 0;
			int readSize = 0;
			while ((readSize != -1) && (len != length)) {
				// See PR 1FMS89U
				// We record first the read size. In this case len is the actual read size.
				len += readSize;
				readSize = reader.read(contents, len, length - len);
			}
			// See PR 1FMS89U
			// Now we need to resize in case the default encoding used more than one byte for each
			// character
			if (len != length)
				System.arraycopy(contents, 0, (contents = new char[len]), 0, len);
		}

		return contents;
	}
	
	/**
	 * Returns the contents of the given zip entry as a byte array.
	 * @throws IOException if a problem occured reading the zip entry.
	 */
	public static byte[] getZipEntryByteContent(ZipEntry ze, ZipFile zip)
		throws IOException {

		InputStream stream = null;
		try {
			stream = new BufferedInputStream(zip.getInputStream(ze));
			return getInputStreamAsByteArray(stream, (int) ze.getSize());
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}
	/**
	 * Returns true iff str.toLowerCase().endsWith(".jar") || str.toLowerCase().endsWith(".zip")
	 * implementation is not creating extra strings.
	 */
	public final static boolean isArchiveFileName(String name) {
		int nameLength = name == null ? 0 : name.length();
		int suffixLength = SUFFIX_JAR.length;
		if (nameLength < suffixLength) return false;

		// try to match as JAR file
		for (int i = 0; i < suffixLength; i++) {
			char c = name.charAt(nameLength - i - 1);
			int suffixIndex = suffixLength - i - 1;
			if (c != SUFFIX_jar[suffixIndex] && c != SUFFIX_JAR[suffixIndex]) {

				// try to match as ZIP file
				suffixLength = SUFFIX_ZIP.length;
				if (nameLength < suffixLength) return false;
				for (int j = 0; j < suffixLength; j++) {
					c = name.charAt(nameLength - j - 1);
					suffixIndex = suffixLength - j - 1;
					if (c != SUFFIX_zip[suffixIndex] && c != SUFFIX_ZIP[suffixIndex]) return false;
				}
				return true;
			}
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

		for (int i = 0; i < suffixLength; i++) {
			char c = name.charAt(nameLength - i - 1);
			int suffixIndex = suffixLength - i - 1;
			if (c != SUFFIX_class[suffixIndex] && c != SUFFIX_CLASS[suffixIndex]) return false;
		}
		return true;		
	}	
	/**
	 * Returns true iff str.toLowerCase().endsWith(".java")
	 * implementation is not creating extra strings.
	 */
	public final static boolean isJavaFileName(String name) {
		int nameLength = name == null ? 0 : name.length();
		int suffixLength = SUFFIX_JAVA.length;
		if (nameLength < suffixLength) return false;

		for (int i = 0; i < suffixLength; i++) {
			char c = name.charAt(nameLength - i - 1);
			int suffixIndex = suffixLength - i - 1;
			if (c != SUFFIX_java[suffixIndex] && c != SUFFIX_JAVA[suffixIndex]) return false;
		}
		return true;		
	}
}