package org.aspectj.apache.bcel.classfile;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisParamAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeParamAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisParamAnnos;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.util.ByteSequence;

/**
 * Utility functions that do not really belong to any class in particular.
 * 
 * @version $Id: Utility.java,v 1.14 2009/09/28 16:39:46 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * 
 *         modified: Andy Clement 2-mar-05 Removed unnecessary static and optimized
 */
public abstract class Utility {

	/*
	 * The 'WIDE' instruction is used in the byte code to allow 16-bit wide indices for local variables. This opcode precedes an
	 * 'ILOAD', e.g.. The opcode immediately following takes an extra byte which is combined with the following byte to form a
	 * 16-bit value.
	 */
	private static boolean wide = false;

	/**
	 * Convert bit field of flags into string such as 'static final'.
	 * 
	 * @param access_flags Access flags
	 * @return String representation of flags
	 */
	public static final String accessToString(int access_flags) {
		return accessToString(access_flags, false);
	}

	/**
	 * Convert bit field of flags into string such as 'static final'.
	 * 
	 * Special case: Classes compiled with new compilers and with the 'ACC_SUPER' flag would be said to be "synchronized". This is
	 * because SUN used the same value for the flags 'ACC_SUPER' and 'ACC_SYNCHRONIZED'.
	 * 
	 * @param access_flags Access flags
	 * @param for_class access flags are for class qualifiers ?
	 * @return String representation of flags
	 */
	public static final String accessToString(int access_flags, boolean for_class) {
		StringBuffer buf = new StringBuffer();

		int p = 0;
		for (int i = 0; p < Constants.MAX_ACC_FLAG; i++) { // Loop through known flags
			p = pow2(i);
			if ((access_flags & p) != 0) {
				// Special case: see comment at top of class...
				if (for_class && ((p == Constants.ACC_SUPER) || (p == Constants.ACC_INTERFACE))) {
					continue;
				}
				buf.append(Constants.ACCESS_NAMES[i]).append(" ");
			}
		}
		return buf.toString().trim();
	}

	/**
	 * @return "class" or "interface", depending on the ACC_INTERFACE flag
	 */
	public static final String classOrInterface(int access_flags) {
		return ((access_flags & Constants.ACC_INTERFACE) != 0) ? "interface" : "class";
	}

	/**
	 * Disassemble a byte array of JVM byte codes starting from code line 'index' and return the disassembled string representation.
	 * Decode only 'num' opcodes (including their operands), use -1 if you want to decompile everything.
	 * 
	 * @param code byte code array
	 * @param constant_pool Array of constants
	 * @param index offset in `code' array <EM>(number of opcodes, not bytes!)</EM>
	 * @param length number of opcodes to decompile, -1 for all
	 * @param verbose be verbose, e.g. print constant pool index
	 * @return String representation of byte codes
	 */
	public static final String codeToString(byte[] code, ConstantPool constant_pool, int index, int length, boolean verbose) {
		StringBuffer buf = new StringBuffer(code.length * 20); // Should be sufficient
		ByteSequence stream = new ByteSequence(code);

		try {
			for (int i = 0; i < index; i++) {
				// Skip `index' lines of code
				codeToString(stream, constant_pool, verbose);
			}

			for (int i = 0; stream.available() > 0; i++) {
				if ((length < 0) || (i < length)) {
					String indices = fillup(stream.getIndex() + ":", 6, true, ' ');
					buf.append(indices + codeToString(stream, constant_pool, verbose) + '\n');
				}
			}
		} catch (IOException e) {
			System.out.println(buf.toString());
			e.printStackTrace();
			throw new ClassFormatException("Byte code error: " + e);
		}

		return buf.toString();
	}

	/**
	 * Disassemble a stream of byte codes and return the string representation.
	 */
	public static final String codeToString(byte[] code, ConstantPool constant_pool, int index, int length) {
		return codeToString(code, constant_pool, index, length, true);
	}

	public static final String codeToString(ByteSequence bytes, ConstantPool constant_pool) throws IOException {
		return codeToString(bytes, constant_pool, true);
	}

	/**
	 * Shorten long class names, <em>java/lang/String</em> becomes <em>String</em>.
	 * 
	 * @param str The long class name
	 * @return Compacted class name
	 */
	public static final String compactClassName(String str) {
		return compactClassName(str, true);
	}

	/**
	 * Shorten long class name <em>str</em>, i.e., chop off the <em>prefix</em>, if the class name starts with this string and the
	 * flag <em>chopit</em> is true. Slashes <em>/</em> are converted to dots <em>.</em>.
	 * 
	 * @param str The long class name
	 * @param prefix The prefix the get rid off
	 * @param chopit Flag that determines whether chopping is executed or not
	 * @return Compacted class name
	 */
	public static final String compactClassName(String str, String prefix, boolean chopit) {
		str = str.replace('/', '.');
		if (chopit) {
			int len = prefix.length();
			// If string starts with 'prefix' and contains no further dots
			if (str.startsWith(prefix)) {
				String result = str.substring(len);
				if (result.indexOf('.') == -1) {
					str = result;
				}
			}
		}
		return str;
	}

	/**
	 * Shorten long class names, <em>java/lang/String</em> becomes <em>java.lang.String</em>, e.g.. If <em>chopit</em> is
	 * <em>true</em> the prefix <em>java.lang</em> is also removed.
	 * 
	 * @param str The long class name
	 * @param chopit Flag that determines whether chopping is executed or not
	 * @return Compacted class name
	 */
	public static final String compactClassName(String str, boolean chopit) {
		return compactClassName(str, "java.lang.", chopit);
	}

	public static final String methodSignatureToString(String signature, String name, String access) {
		return methodSignatureToString(signature, name, access, true);
	}

	public static final String methodSignatureToString(String signature, String name, String access, boolean chopit) {
		return methodSignatureToString(signature, name, access, chopit, null);
	}

	/**
	 * This method converts such a string into a Java type declaration like 'void main(String[])' and throws a
	 * 'ClassFormatException' when the parsed type is invalid.
	 */
	public static final String methodSignatureToString(String signature, String name, String access, boolean chopit,
			LocalVariableTable vars) throws ClassFormatException {
		StringBuffer buf = new StringBuffer("(");
		String type;
		int index;
		int var_index = (access.contains("static")) ? 0 : 1;

		try { // Read all declarations between for `(' and `)'
			if (signature.charAt(0) != '(') {
				throw new ClassFormatException("Invalid method signature: " + signature);
			}

			index = 1; // current string position

			while (signature.charAt(index) != ')') {
				ResultHolder rh = signatureToStringInternal(signature.substring(index), chopit);
				String param_type = rh.getResult();
				buf.append(param_type);

				if (vars != null) {
					LocalVariable l = vars.getLocalVariable(var_index);

					if (l != null) {
						buf.append(" " + l.getName());
					}
				} else {
					buf.append(" arg" + var_index);
				}

				if ("double".equals(param_type) || "long".equals(param_type)) {
					var_index += 2;
				} else {
					var_index++;
				}

				buf.append(", ");
				index += rh.getConsumedChars();
			}

			index++;

			// Read return type after `)'
			type = signatureToString(signature.substring(index), chopit);

		} catch (StringIndexOutOfBoundsException e) { // Should never occur
			throw new ClassFormatException("Invalid method signature: " + signature);
		}

		if (buf.length() > 1) {
			buf.setLength(buf.length() - 2);
		}

		buf.append(")");

		return access + ((access.length() > 0) ? " " : "") + // May be an empty string
				type + " " + name + buf.toString();
	}

	/**
	 * Replace all occurences of <em>old</em> in <em>str</em> with <em>new</em>.
	 * 
	 * @param str String to permute
	 * @param old String to be replaced
	 * @param new Replacement string
	 * @return new String object
	 */
	public static final String replace(String str, String old, String new_) {
		int index, old_index;
		StringBuffer buf = new StringBuffer();

		try {
			index = str.indexOf(old);
			if (index != -1) {
				old_index = 0;

				// While we have something to replace
				while ((index = str.indexOf(old, old_index)) != -1) {
					buf.append(str.substring(old_index, index)); // append prefix
					buf.append(new_); // append replacement
					old_index = index + old.length(); // Skip 'old'.length chars
				}

				buf.append(str.substring(old_index)); // append rest of string
				str = buf.toString();
			}
		} catch (StringIndexOutOfBoundsException e) {
			System.err.println(e);
		}

		return str;
	}

	/**
	 * Converts signature to string with all class names compacted.
	 * 
	 * @param signature to convert
	 * @return Human readable signature
	 */
	public static final String signatureToString(String signature) {
		return signatureToString(signature, true);
	}

	public static final String signatureToString(String signature, boolean chopit) {
		ResultHolder rh = signatureToStringInternal(signature, chopit);
		return rh.getResult();
	}

	/**
	 * This method converts this string into a Java type declaration such as 'String[]' and throws a `ClassFormatException' when the
	 * parsed type is invalid.
	 */
	public static final ResultHolder signatureToStringInternal(String signature, boolean chopit) {
		int processedChars = 1; // This is the default, read just one char
		try {
			switch (signature.charAt(0)) {
			case 'B':
				return ResultHolder.BYTE;
			case 'C':
				return ResultHolder.CHAR;
			case 'D':
				return ResultHolder.DOUBLE;
			case 'F':
				return ResultHolder.FLOAT;
			case 'I':
				return ResultHolder.INT;
			case 'J':
				return ResultHolder.LONG;
			case 'L': { // Full class name
				int index = signature.indexOf(';'); // Look for closing ';'

				if (index < 0) {
					throw new ClassFormatException("Invalid signature: " + signature);
				}

				if (signature.length() > index + 1 && signature.charAt(index + 1) == '>') {
					index = index + 2;
				}

				int genericStart = signature.indexOf('<');
				if (genericStart != -1) {
					int genericEnd = signature.indexOf('>');
					// FIXME asc going to need a lot more work in here for generics
					ResultHolder rh = signatureToStringInternal(signature.substring(genericStart + 1, genericEnd), chopit);
					StringBuffer sb = new StringBuffer();
					sb.append(signature.substring(1, genericStart));
					sb.append("<").append(rh.getResult()).append(">");
					ResultHolder retval = new ResultHolder(compactClassName(sb.toString(), chopit), genericEnd + 1);
					return retval;
				} else {
					processedChars = index + 1; // "Lblabla;" `L' and `;' are removed
					ResultHolder retval = new ResultHolder(compactClassName(signature.substring(1, index), chopit), processedChars);
					return retval;
				}
			}

			case 'S':
				return ResultHolder.SHORT;
			case 'Z':
				return ResultHolder.BOOLEAN;

			case '[': { // Array declaration
				StringBuffer brackets;
				int consumedChars, n;

				brackets = new StringBuffer(); // Accumulate []'s
				// Count opening brackets and look for optional size argument
				for (n = 0; signature.charAt(n) == '['; n++) {
					brackets.append("[]");
				}
				consumedChars = n;
				ResultHolder restOfIt = signatureToStringInternal(signature.substring(n), chopit);
				consumedChars += restOfIt.getConsumedChars();
				brackets.insert(0, restOfIt.getResult());
				return new ResultHolder(brackets.toString(), consumedChars);
			}
			case 'V':
				return ResultHolder.VOID;

			default:
				throw new ClassFormatException("Invalid signature: `" + signature + "'");
			}
		} catch (StringIndexOutOfBoundsException e) { // Should never occur
			throw new ClassFormatException("Invalid signature: " + e + ":" + signature);
		}
	}

	/**
	 * Return type of method signature as a byte value as defined in <em>Constants</em>
	 * 
	 * @param signature in format described above
	 * @return type of method signature
	 * @see Constants
	 */
	public static final byte typeOfMethodSignature(String signature) throws ClassFormatException {
		int index;
		try {
			if (signature.charAt(0) != '(') {
				throw new ClassFormatException("Invalid method signature: " + signature);
			}
			index = signature.lastIndexOf(')') + 1;
			return typeOfSignature(signature.substring(index));
		} catch (StringIndexOutOfBoundsException e) {
			throw new ClassFormatException("Invalid method signature: " + signature);
		}
	}

	/**
	 * Convert (signed) byte to (unsigned) short value, i.e., all negative values become positive.
	 */
	private static final short byteToShort(byte b) {
		return (b < 0) ? (short) (256 + b) : (short) b;
	}

	/**
	 * Convert bytes into hexidecimal string
	 * 
	 * @return bytes as hexidecimal string, e.g. 00 FA 12 ...
	 */
	public static final String toHexString(byte[] bytes) {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < bytes.length; i++) {
			short b = byteToShort(bytes[i]);
			String hex = Integer.toString(b, 0x10);

			// Just one digit, so prepend 0
			if (b < 0x10) {
				buf.append('0');
			}

			buf.append(hex);

			if (i < bytes.length - 1) {
				buf.append(' ');
			}
		}

		return buf.toString();
	}

	/**
	 * Return a string for an integer justified left or right and filled up with 'fill' characters if necessary.
	 * 
	 * @param i integer to format
	 * @param length length of desired string
	 * @param left_justify format left or right
	 * @param fill fill character
	 * @return formatted int
	 */
	public static final String format(int i, int length, boolean left_justify, char fill) {
		return fillup(Integer.toString(i), length, left_justify, fill);
	}

	/**
	 * Fillup char with up to length characters with char `fill' and justify it left or right.
	 * 
	 * @param str string to format
	 * @param length length of desired string
	 * @param left_justify format left or right
	 * @param fill fill character
	 * @return formatted string
	 */
	public static final String fillup(String str, int length, boolean left_justify, char fill) {
		int len = length - str.length();
		char[] buf = new char[(len < 0) ? 0 : len];

		for (int j = 0; j < buf.length; j++) {
			buf[j] = fill;
		}

		if (left_justify) {
			return str + new String(buf);
		} else {
			return new String(buf) + str;
		}
	}

	/**
	 * Escape all occurences of newline chars '\n', quotes \", etc.
	 */
	public static final String convertString(String label) {
		char[] ch = label.toCharArray();
		StringBuffer buf = new StringBuffer();

		for (char c : ch) {
			switch (c) {
				case '\n':
					buf.append("\\n");
					break;
				case '\r':
					buf.append("\\r");
					break;
				case '\"':
					buf.append("\\\"");
					break;
				case '\'':
					buf.append("\\'");
					break;
				case '\\':
					buf.append("\\\\");
					break;
				default:
					buf.append(c);
					break;
			}
		}

		return buf.toString();
	}

	/**
	 * Converts a list of AnnotationGen objects into a set of attributes that can be attached to the class file.
	 * 
	 * @param cp The constant pool gen where we can create the necessary name refs
	 * @param annotations A list of AnnotationGen objects
	 */
	public static Collection<RuntimeAnnos> getAnnotationAttributes(ConstantPool cp, List<AnnotationGen> annotations) {

		if (annotations.size() == 0) {
			return null;
		}

		try {
			int countVisible = 0;
			int countInvisible = 0;

			// put the annotations in the right output stream
			for (AnnotationGen a : annotations) {
				if (a.isRuntimeVisible()) {
					countVisible++;
				} else {
					countInvisible++;
				}
			}

			ByteArrayOutputStream rvaBytes = new ByteArrayOutputStream();
			ByteArrayOutputStream riaBytes = new ByteArrayOutputStream();
			DataOutputStream rvaDos = new DataOutputStream(rvaBytes);
			DataOutputStream riaDos = new DataOutputStream(riaBytes);

			rvaDos.writeShort(countVisible);
			riaDos.writeShort(countInvisible);

			// put the annotations in the right output stream
			for (AnnotationGen a : annotations) {
				if (a.isRuntimeVisible()) {
					a.dump(rvaDos);
				} else {
					a.dump(riaDos);
				}
			}

			rvaDos.close();
			riaDos.close();

			byte[] rvaData = rvaBytes.toByteArray();
			byte[] riaData = riaBytes.toByteArray();

			int rvaIndex = -1;
			int riaIndex = -1;

			if (rvaData.length > 2) {
				rvaIndex = cp.addUtf8("RuntimeVisibleAnnotations");
			}
			if (riaData.length > 2) {
				riaIndex = cp.addUtf8("RuntimeInvisibleAnnotations");
			}

			List<RuntimeAnnos> newAttributes = new ArrayList<>();
			if (rvaData.length > 2) {
				newAttributes.add(new RuntimeVisAnnos(rvaIndex, rvaData.length, rvaData, cp));
			}
			if (riaData.length > 2) {
				newAttributes.add(new RuntimeInvisAnnos(riaIndex, riaData.length, riaData, cp));
			}

			return newAttributes;
		} catch (IOException e) {
			System.err.println("IOException whilst processing annotations");
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Annotations against a class are stored in one of four attribute kinds: - RuntimeVisibleParameterAnnotations -
	 * RuntimeInvisibleParameterAnnotations
	 */
	// OPTIMIZE looks heavyweight?
	public static Attribute[] getParameterAnnotationAttributes(ConstantPool cp, List<AnnotationGen>[] vec) {

		int visCount[] = new int[vec.length];
		int totalVisCount = 0;
		int invisCount[] = new int[vec.length];
		int totalInvisCount = 0;
		try {

			for (int i = 0; i < vec.length; i++) {
				List<AnnotationGen> l = vec[i];
				if (l != null) {
					for (AnnotationGen element : l) {
						if (element.isRuntimeVisible()) {
							visCount[i]++;
							totalVisCount++;
						} else {
							invisCount[i]++;
							totalInvisCount++;
						}
					}
				}
			}

			// Lets do the visible ones
			ByteArrayOutputStream rvaBytes = new ByteArrayOutputStream();
			DataOutputStream rvaDos = new DataOutputStream(rvaBytes);
			rvaDos.writeByte(vec.length); // First goes number of parameters

			for (int i = 0; i < vec.length; i++) {
				rvaDos.writeShort(visCount[i]);
				if (visCount[i] > 0) {
					List<AnnotationGen> l = vec[i];
					for (AnnotationGen element : l) {
						if (element.isRuntimeVisible()) {
							element.dump(rvaDos);
						}
					}
				}
			}
			rvaDos.close();

			// Lets do the invisible ones
			ByteArrayOutputStream riaBytes = new ByteArrayOutputStream();
			DataOutputStream riaDos = new DataOutputStream(riaBytes);
			riaDos.writeByte(vec.length); // First goes number of parameters

			for (int i = 0; i < vec.length; i++) {
				riaDos.writeShort(invisCount[i]);
				if (invisCount[i] > 0) {
					List<AnnotationGen> l = vec[i];
					for (AnnotationGen element : l) {
						if (!element.isRuntimeVisible()) {
							element.dump(riaDos);
						}
					}
				}
			}
			riaDos.close();

			byte[] rvaData = rvaBytes.toByteArray();
			byte[] riaData = riaBytes.toByteArray();

			int rvaIndex = -1;
			int riaIndex = -1;

			if (totalVisCount > 0) {
				rvaIndex = cp.addUtf8("RuntimeVisibleParameterAnnotations");
			}
			if (totalInvisCount > 0) {
				riaIndex = cp.addUtf8("RuntimeInvisibleParameterAnnotations");
			}

			List<RuntimeParamAnnos> newAttributes = new ArrayList<>();

			if (totalVisCount > 0) {
				newAttributes.add(new RuntimeVisParamAnnos(rvaIndex, rvaData.length, rvaData, cp));
			}

			if (totalInvisCount > 0) {
				newAttributes.add(new RuntimeInvisParamAnnos(riaIndex, riaData.length, riaData, cp));
			}

			return newAttributes.toArray(new Attribute[] {});
		} catch (IOException e) {
			System.err.println("IOException whilst processing parameter annotations");
			e.printStackTrace();
		}
		return null;
	}

	public static class ResultHolder {
		private String result;
		private int consumed;

		public static final ResultHolder BYTE = new ResultHolder("byte", 1);
		public static final ResultHolder CHAR = new ResultHolder("char", 1);
		public static final ResultHolder DOUBLE = new ResultHolder("double", 1);
		public static final ResultHolder FLOAT = new ResultHolder("float", 1);
		public static final ResultHolder INT = new ResultHolder("int", 1);
		public static final ResultHolder LONG = new ResultHolder("long", 1);
		public static final ResultHolder SHORT = new ResultHolder("short", 1);
		public static final ResultHolder BOOLEAN = new ResultHolder("boolean", 1);
		public static final ResultHolder VOID = new ResultHolder("void", 1);

		public ResultHolder(String s, int c) {
			result = s;
			consumed = c;
		}

		public String getResult() {
			return result;
		}

		public int getConsumedChars() {
			return consumed;
		}
	}

	/**
	 * Return type of signature as a byte value as defined in <em>Constants</em>
	 * 
	 * @param signature in format described above
	 * @return type of signature
	 * @see Constants
	 */
	public static final byte typeOfSignature(String signature) throws ClassFormatException {
		try {
			switch (signature.charAt(0)) {
			case 'B':
				return Constants.T_BYTE;
			case 'C':
				return Constants.T_CHAR;
			case 'D':
				return Constants.T_DOUBLE;
			case 'F':
				return Constants.T_FLOAT;
			case 'I':
				return Constants.T_INT;
			case 'J':
				return Constants.T_LONG;
			case 'L':
				return Constants.T_REFERENCE;
			case '[':
				return Constants.T_ARRAY;
			case 'V':
				return Constants.T_VOID;
			case 'Z':
				return Constants.T_BOOLEAN;
			case 'S':
				return Constants.T_SHORT;
			default:
				throw new ClassFormatException("Invalid method signature: " + signature);
			}
		} catch (StringIndexOutOfBoundsException e) {
			throw new ClassFormatException("Invalid method signature: " + signature);
		}
	}

	public static final byte typeOfSignature(char c) throws ClassFormatException {
		switch (c) {
		case 'B':
			return Constants.T_BYTE;
		case 'C':
			return Constants.T_CHAR;
		case 'D':
			return Constants.T_DOUBLE;
		case 'F':
			return Constants.T_FLOAT;
		case 'I':
			return Constants.T_INT;
		case 'J':
			return Constants.T_LONG;
		case 'L':
			return Constants.T_REFERENCE;
		case '[':
			return Constants.T_ARRAY;
		case 'V':
			return Constants.T_VOID;
		case 'Z':
			return Constants.T_BOOLEAN;
		case 'S':
			return Constants.T_SHORT;
		default:
			throw new ClassFormatException("Invalid type of signature: " + c);
		}
	}

	/**
	 * Disassemble a stream of byte codes and return the string representation.
	 * 
	 * @param bytes stream of bytes
	 * @param constant_pool Array of constants
	 * @param verbose be verbose, e.g. print constant pool index
	 * @return String representation of byte code
	 */
	public static final String codeToString(ByteSequence bytes, ConstantPool constant_pool, boolean verbose) throws IOException {
		short opcode = (short) bytes.readUnsignedByte();
		int default_offset = 0, low, high, npairs;
		int index, vindex, constant;
		int[] match, jump_table;
		int no_pad_bytes = 0, offset;
		StringBuffer buf = new StringBuffer(Constants.OPCODE_NAMES[opcode]);

		/*
		 * Special case: Skip (0-3) padding bytes, i.e., the following bytes are 4-byte-aligned
		 */
		if ((opcode == Constants.TABLESWITCH) || (opcode == Constants.LOOKUPSWITCH)) {
			int remainder = bytes.getIndex() % 4;
			no_pad_bytes = (remainder == 0) ? 0 : 4 - remainder;

			for (int i = 0; i < no_pad_bytes; i++) {
				byte b = bytes.readByte();
				if (b != 0) {
					System.err.println("Warning: Padding byte != 0 in " + Constants.OPCODE_NAMES[opcode] + ":" + b);
				}
			}

			// Both cases have a field default_offset in common
			default_offset = bytes.readInt();
		}

		switch (opcode) {
		/*
		 * Table switch has variable length arguments.
		 */
		case Constants.TABLESWITCH:
			low = bytes.readInt();
			high = bytes.readInt();

			offset = bytes.getIndex() - 12 - no_pad_bytes - 1;
			default_offset += offset;

			buf.append("\tdefault = " + default_offset + ", low = " + low + ", high = " + high + "(");

			jump_table = new int[high - low + 1];
			for (int i = 0; i < jump_table.length; i++) {
				jump_table[i] = offset + bytes.readInt();
				buf.append(jump_table[i]);
				if (i < jump_table.length - 1) {
					buf.append(", ");
				}
			}
			buf.append(")");
			break;

		/*
		 * Lookup switch has variable length arguments.
		 */
		case Constants.LOOKUPSWITCH: {

			npairs = bytes.readInt();
			offset = bytes.getIndex() - 8 - no_pad_bytes - 1;

			match = new int[npairs];
			jump_table = new int[npairs];
			default_offset += offset;

			buf.append("\tdefault = " + default_offset + ", npairs = " + npairs + " (");

			for (int i = 0; i < npairs; i++) {
				match[i] = bytes.readInt();
				jump_table[i] = offset + bytes.readInt();
				buf.append("(" + match[i] + ", " + jump_table[i] + ")");
				if (i < npairs - 1) {
					buf.append(", ");
				}
			}
			buf.append(")");
		}
			break;

		// Two address bytes + offset from start of byte stream form the jump target
		case Constants.GOTO:
		case Constants.IFEQ:
		case Constants.IFGE:
		case Constants.IFGT:
		case Constants.IFLE:
		case Constants.IFLT:
		case Constants.JSR:
		case Constants.IFNE:
		case Constants.IFNONNULL:
		case Constants.IFNULL:
		case Constants.IF_ACMPEQ:
		case Constants.IF_ACMPNE:
		case Constants.IF_ICMPEQ:
		case Constants.IF_ICMPGE:
		case Constants.IF_ICMPGT:
		case Constants.IF_ICMPLE:
		case Constants.IF_ICMPLT:
		case Constants.IF_ICMPNE:
			buf.append("\t\t#" + ((bytes.getIndex() - 1) + bytes.readShort()));
			break;

		// 32-bit wide jumps
		case Constants.GOTO_W:
		case Constants.JSR_W:
			buf.append("\t\t#" + ((bytes.getIndex() - 1) + bytes.readInt()));
			break;

		// Index byte references local variable (register)
		case Constants.ALOAD:
		case Constants.ASTORE:
		case Constants.DLOAD:
		case Constants.DSTORE:
		case Constants.FLOAD:
		case Constants.FSTORE:
		case Constants.ILOAD:
		case Constants.ISTORE:
		case Constants.LLOAD:
		case Constants.LSTORE:
		case Constants.RET:
			if (wide) {
				vindex = bytes.readUnsignedShort();
				wide = false; // Clear flag
			} else {
				vindex = bytes.readUnsignedByte();
			}
			buf.append("\t\t%" + vindex);
			break;

		/*
		 * Remember wide byte which is used to form a 16-bit address in the following instruction. Relies on that the method is
		 * called again with the following opcode.
		 */
		case Constants.WIDE:
			wide = true;
			buf.append("\t(wide)");
			break;

		// Array of basic type
		case Constants.NEWARRAY:
			buf.append("\t\t<" + Constants.TYPE_NAMES[bytes.readByte()] + ">");
			break;

		// Access object/class fields
		case Constants.GETFIELD:
		case Constants.GETSTATIC:
		case Constants.PUTFIELD:
		case Constants.PUTSTATIC:
			index = bytes.readUnsignedShort();
			buf.append("\t\t" + constant_pool.constantToString(index, Constants.CONSTANT_Fieldref)
					+ (verbose ? " (" + index + ")" : ""));
			break;

		// Operands are references to classes in constant pool
		case Constants.NEW:
		case Constants.CHECKCAST:
			buf.append("\t");
		case Constants.INSTANCEOF:
			index = bytes.readUnsignedShort();
			buf.append("\t<" + constant_pool.constantToString(index) + ">" + (verbose ? " (" + index + ")" : ""));
			break;

		// Operands are references to methods in constant pool
		case Constants.INVOKESPECIAL:
		case Constants.INVOKESTATIC:
		case Constants.INVOKEVIRTUAL:
			index = bytes.readUnsignedShort();
			buf.append("\t" + constant_pool.constantToString(index) + (verbose ? " (" + index + ")" : ""));
			break;

		case Constants.INVOKEINTERFACE:
			index = bytes.readUnsignedShort();
			int nargs = bytes.readUnsignedByte(); // historical, redundant
			buf.append("\t" + constant_pool.constantToString(index) + (verbose ? " (" + index + ")\t" : "") + nargs + "\t"
					+ bytes.readUnsignedByte()); // Last byte is a reserved
			// space
			break;
			
		case Constants.INVOKEDYNAMIC://http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokedynamic
			index = bytes.readUnsignedShort();
			bytes.readUnsignedShort(); // zeroes
			buf.append("\t" + constant_pool.constantToString(index) + (verbose ? " (" + index + ")" : ""));
			break;
			
		// Operands are references to items in constant pool
		case Constants.LDC_W:
		case Constants.LDC2_W:
			index = bytes.readUnsignedShort();
			buf.append("\t\t" + constant_pool.constantToString(index) + (verbose ? " (" + index + ")" : ""));
			break;

		case Constants.LDC:
			index = bytes.readUnsignedByte();
			buf.append("\t\t" + constant_pool.constantToString(index) + (verbose ? " (" + index + ")" : ""));
			break;

		// Array of references
		case Constants.ANEWARRAY:
			index = bytes.readUnsignedShort();
			buf.append("\t\t<" + compactClassName(constant_pool.getConstantString(index, Constants.CONSTANT_Class), false) + ">"
					+ (verbose ? " (" + index + ")" : ""));
			break;

		// Multidimensional array of references
		case Constants.MULTIANEWARRAY: {
			index = bytes.readUnsignedShort();
			int dimensions = bytes.readUnsignedByte();

			buf.append("\t<" + compactClassName(constant_pool.getConstantString(index, Constants.CONSTANT_Class), false) + ">\t"
					+ dimensions + (verbose ? " (" + index + ")" : ""));
		}
			break;

		// Increment local variable
		case Constants.IINC:
			if (wide) {
				vindex = bytes.readUnsignedShort();
				constant = bytes.readShort();
				wide = false;
			} else {
				vindex = bytes.readUnsignedByte();
				constant = bytes.readByte();
			}
			buf.append("\t\t%" + vindex + "\t" + constant);
			break;

		default:
			if ((Constants.iLen[opcode] - 1) > 0) {
				for (int i = 0; i < Constants.TYPE_OF_OPERANDS[opcode].length; i++) {
					buf.append("\t\t");
					switch (Constants.TYPE_OF_OPERANDS[opcode][i]) {
					case Constants.T_BYTE:
						buf.append(bytes.readByte());
						break;
					case Constants.T_SHORT:
						buf.append(bytes.readShort());
						break;
					case Constants.T_INT:
						buf.append(bytes.readInt());
						break;

					default: // Never reached
						System.err.println("Unreachable default case reached!");
						System.exit(-1);
					}
				}
			}
		}
		return buf.toString();
	}

	// private helpers
	private static final int pow2(int n) {
		return 1 << n;
	}

	/**
	 * Convert type to Java method signature, e.g. int[] f(java.lang.String x) becomes (Ljava/lang/String;)[I
	 * 
	 * @param returnType what the method returns
	 * @param argTypes what are the argument types
	 * @return method signature for given type(s).
	 */
	public static String toMethodSignature(Type returnType, Type[] argTypes) {
		StringBuffer buf = new StringBuffer("(");
		int length = (argTypes == null) ? 0 : argTypes.length;
		for (int i = 0; i < length; i++) {
			buf.append(argTypes[i].getSignature());
		}
		buf.append(')');
		buf.append(returnType.getSignature());
		return buf.toString();
	}
}
