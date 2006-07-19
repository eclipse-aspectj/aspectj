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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.DataOutputStream;
import java.io.FilterReader;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.annotation.Annotation;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisibleAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisibleParameterAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisibleAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisibleParameterAnnotations;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.annotation.AnnotationGen;
import org.aspectj.apache.bcel.util.ByteSequence;

/**
 * Utility functions that do not really belong to any class in particular.
 *
 * @version $Id: Utility.java,v 1.5 2006/07/19 12:06:15 aclement Exp $
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * 
 * modified: Andy Clement  2-mar-05  Removed unnecessary static and optimized
 */
public abstract class Utility {

  /* The `WIDE' instruction is used in the byte code to allow 16-bit wide indices for local 
   * variables. This opcode precedes an 'ILOAD', e.g.. The opcode immediately following takes 
   * an extra byte which is combined with the following byte to form a 16-bit value.
   */
  private static boolean wide = false; 
  
  /**
   * Convert bit field of flags into string such as 'static final'.
   *
   * @param  access_flags Access flags
   * @return String representation of flags
   */
  public static final String accessToString(int access_flags) {
    return accessToString(access_flags, false);
  }

  
  /**
   * Convert bit field of flags into string such as 'static final'.
   *
   * Special case: Classes compiled with new compilers and with the
   * 'ACC_SUPER' flag would be said to be "synchronized". This is
   * because SUN used the same value for the flags 'ACC_SUPER' and
   * 'ACC_SYNCHRONIZED'. 
   *
   * @param  access_flags Access flags
   * @param  for_class access flags are for class qualifiers ?
   * @return String representation of flags
   */
  public static final String accessToString(int access_flags, boolean for_class) {
    StringBuffer buf = new StringBuffer();

    int p = 0;
    for (int i=0; p < Constants.MAX_ACC_FLAG; i++) { // Loop through known flags
      p = pow2(i);
      if ((access_flags & p) != 0) {
	    // Special case: see comment at top of class...
	    if (for_class && ((p == Constants.ACC_SUPER) || (p == Constants.ACC_INTERFACE)))
	       continue;	    
	    buf.append(Constants.ACCESS_NAMES[i]).append(" ");
      }
    }
    return buf.toString().trim();
  }

  
  /**
   * @return "class" or "interface", depending on the ACC_INTERFACE flag
   */
  public static final String classOrInterface(int access_flags) {
    return ((access_flags & Constants.ACC_INTERFACE) != 0)? "interface" : "class";
  }

  
  /**
   * Disassemble a byte array of JVM byte codes starting from code line 
   * 'index' and return the disassembled string representation. Decode only
   * 'num' opcodes (including their operands), use -1 if you want to
   * decompile everything.
   *
   * @param  code byte code array
   * @param  constant_pool Array of constants
   * @param  index offset in `code' array
   * <EM>(number of opcodes, not bytes!)</EM>
   * @param  length number of opcodes to decompile, -1 for all
   * @param  verbose be verbose, e.g. print constant pool index
   * @return String representation of byte codes
   */
  public static final String codeToString(byte[] code, 
					  ConstantPool constant_pool, 
					  int index, int length, boolean verbose)
  {
    StringBuffer buf    = new StringBuffer(code.length * 20); // Should be sufficient
    ByteSequence stream = new ByteSequence(code);

    try {
      for (int i=0; i < index; i++) // Skip `index' lines of code
	    codeToString(stream, constant_pool, verbose);

      for (int i=0; stream.available() > 0; i++) {
	    if ((length < 0) || (i < length)) {
	      String indices = fillup(stream.getIndex() + ":", 6, true, ' ');
	      buf.append(indices + codeToString(stream, constant_pool, verbose) + '\n');
	    }
      }
    } catch(IOException e) {
      System.out.println(buf.toString());
      e.printStackTrace();
      throw new ClassFormatException("Byte code error: " + e);
    }

    return buf.toString();
  }

  
  
  /** 
   * Disassemble a stream of byte codes and return the
   * string representation.
   */
  public static final String codeToString(byte[] code, ConstantPool constant_pool, int index, int length) {
    return codeToString(code, constant_pool, index, length, true);
  }



  public static final String codeToString(ByteSequence bytes, ConstantPool constant_pool)
    throws IOException {
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
   * Shorten long class name <em>str</em>, i.e., chop off the <em>prefix</em>,
   * if the
   * class name starts with this string and the flag <em>chopit</em> is true.
   * Slashes <em>/</em> are converted to dots <em>.</em>.
   *
   * @param str The long class name
   * @param prefix The prefix the get rid off
   * @param chopit Flag that determines whether chopping is executed or not
   * @return Compacted class name
   */
  public static final String compactClassName(String str, 
					      String prefix,
					      boolean chopit)
  {
    int len = prefix.length();

    str = str.replace('/', '.'); // Is '/' on all systems, even DOS

    if (chopit) {
      // If string starts with 'prefix' and contains no further dots
      if (str.startsWith(prefix) && (str.substring(len).indexOf('.') == -1))
	    str = str.substring(len);
    }
	
    return str;
  }

  
  
  /**
   * Shorten long class names, <em>java/lang/String</em> becomes 
   * <em>java.lang.String</em>,
   * e.g.. If <em>chopit</em> is <em>true</em> the prefix <em>java.lang</em>
   * is also removed.
   *
   * @param str The long class name
   * @param chopit Flag that determines whether chopping is executed or not
   * @return Compacted class name
   */
  public static final String compactClassName(String str, boolean chopit) {
    return compactClassName(str, "java.lang.", chopit);
  }    

  // bit modification

  /**
   * @return 'flag' with bit 'i' set to 1
   */
  public static final int setBit(int flag, int i) {
    return flag | pow2(i); 
  }

  /**
   * @return 'flag' with bit 'i' set to 0
   */
  public static final int clearBit(int flag, int i) {
    int bit = pow2(i); 
    return (flag & bit) == 0? flag : flag ^ bit; 
  }
   
  /**
   * @return true, if bit 'i' in 'flag' is set
   */
  public static final boolean isSet(int flag, int i) {
    return (flag & pow2(i)) != 0;
  }

  
  
  /**
   * Converts string containing the method return and argument types 
   * to a byte code method signature.
   *
   * @param  returnType Return type of method (e.g. "char" or "java.lang.String[]")
   * @param  methodArgs Types of method arguments
   * @return Byte code representation of method signature
   */
  public final static String methodTypeToSignature(String returnType, String[] methodArgs) throws ClassFormatException {
	  
    StringBuffer buf = new StringBuffer("(");

    if (methodArgs != null) {
      for (int i=0; i < methodArgs.length; i++) {
	    String str = getSignature(methodArgs[i]);

	    if (str.equals("V")) // void can't be a method argument
	      throw new ClassFormatException("Invalid type: " + methodArgs[i]);

	    buf.append(str);
      }
    }
    
    buf.append(")" + getSignature(returnType));

    return buf.toString();
  }
  
  

  /**
   * @param  signature    Method signature
   * @return Array of argument types
   * @throws  ClassFormatException  
   */
  public static final String[] methodSignatureArgumentTypes(String signature) throws ClassFormatException {
    return methodSignatureArgumentTypes(signature, true);
  }    

  
  
  
  /**
   * For some method signature (class file format) like '([Ljava/lang/String;Z)V' this returns an array
   * of strings representing the arguments in their 'normal' form, e.g. '{java.lang.String[],boolean}'
   * 
   * @param  signature    Method signature
   * @param     chopit    Shorten class names
   * @return              Array of argument types
   */
  public static final String[] methodSignatureArgumentTypes(String signature,boolean chopit) throws ClassFormatException {
    ArrayList vec = new ArrayList();
    int       index;
    String[]  types;

    try { // Read all declarations between for `(' and `)'
      if (signature.charAt(0) != '(')
	    throw new ClassFormatException("Invalid method signature: " + signature);

      index = 1; // current string position

      while(signature.charAt(index) != ')') {
      	ResultHolder rh = signatureToStringInternal(signature.substring(index),chopit);
	    vec.add(rh.getResult());
	    index += rh.getConsumedChars();
      }
    } catch(StringIndexOutOfBoundsException e) {
      throw new ClassFormatException("Invalid method signature: " + signature);
    }
	
    types = new String[vec.size()];
    vec.toArray(types);
    return types;
  }      
  
  
  /**
   * For some method signature (class file format) like '([Ljava/lang/String;)Z' this returns
   * the string representing the return type its 'normal' form, e.g. 'boolean'
   * 
   * @param  signature    Method signature
   * @return return type of method
   * @throws  ClassFormatException  
   */
  public static final String methodSignatureReturnType(String signature) throws ClassFormatException {
    return methodSignatureReturnType(signature, true);
  }    

  
  /**
   * For some method signature (class file format) like '([Ljava/lang/String;)Z' this returns
   * the string representing the return type its 'normal' form, e.g. 'boolean'
   *
   * @param  signature    Method signature
   * @param  chopit       Shorten class names
   * @return return type of method
   */
  public static final String methodSignatureReturnType(String signature,boolean chopit) throws ClassFormatException {
    int    index;
    String type;
    try {
      // Read return type after `)'
      index = signature.lastIndexOf(')') + 1; 
      type = signatureToString(signature.substring(index), chopit);
    } catch (StringIndexOutOfBoundsException e) {
      throw new ClassFormatException("Invalid method signature: " + signature);
    }
    return type;
  }

  
  
  public static final String methodSignatureToString(String signature,String name,String access) {
    return methodSignatureToString(signature, name, access, true);
  }

  public static final String methodSignatureToString(String signature,String name,
		                                               String access,boolean chopit) {
    return methodSignatureToString(signature, name, access, chopit, null);
  }

  /**
   * This method converts such a string into a Java type declaration like
   * 'void main(String[])' and throws a 'ClassFormatException' when the parsed 
   * type is invalid.
   *
   * @param  signature    Method signature
   * @param  name         Method name
   * @param  access       Method access rights
   * @return Java type declaration
   * @throws  ClassFormatException  
   */
  public static final String methodSignatureToString(String signature, String name,
		  String access, boolean chopit, LocalVariableTable vars) throws ClassFormatException {
//	  
//	  
//	if (signature.charAt(0)!='(')
//	     throw new ClassFormatException("Invalid method signature: " + signature);
//	
//	// Break the signature into two pieces: ([PARAMS])[RETURNTYPE]
//	int lastBracketPos = signature.lastIndexOf(")");
//	String parameters = signature.substring(1,lastBracketPos);
//	String returnType = signature.substring(lastBracketPos+1);
//
//	// e.g. parameters="Ljava/util/List<Ljava/lang/String;>;"
//	//      returnType="V"
//
//	// Break signature into its parts
//	// dont want lots of substringing so lets use an index
//	int posn=0;
//    StringBuffer piece;
//	while (posn<parameters.length()) {
//	  piece = new StringBuffer();
//      posn+=getSignatureFrom(parameters,piece);
//	}
//	
	StringBuffer buf = new StringBuffer("(");
    String       type;
    int          index;
    int          var_index = (access.indexOf("static") >= 0)? 0 : 1;

    try { // Read all declarations between for `(' and `)'
      if (signature.charAt(0) != '(')
	     throw new ClassFormatException("Invalid method signature: " + signature);

      index = 1; // current string position

      while(signature.charAt(index) != ')') {
      	ResultHolder rh = signatureToStringInternal(signature.substring(index), chopit);
	    String param_type = rh.getResult();
	    buf.append(param_type);

	    if(vars != null) {
	      LocalVariable l = vars.getLocalVariable(var_index);

	      if(l != null)
	        buf.append(" " + l.getName());
	    } else
	      buf.append(" arg" + var_index);

	      if("double".equals(param_type) || "long".equals(param_type))
	      var_index += 2;
          else
	      var_index++;

          buf.append(", ");
	      index += rh.getConsumedChars(); 
      }

      index++;

      // Read return type after `)'
      type = signatureToString(signature.substring(index), chopit);

    } catch(StringIndexOutOfBoundsException e) { // Should never occur
      throw new ClassFormatException("Invalid method signature: " + signature);
    }

    if(buf.length() > 1) // Tack off the extra ", "
      buf.setLength(buf.length() - 2);

    buf.append(")");

    return access + ((access.length() > 0)? " " : "") + // May be an empty string
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
    int          index, old_index;
    StringBuffer buf = new StringBuffer();

    try {
	  index = str.indexOf(old);
      if ( index != -1) {
	    old_index = 0;
	  
	    // While we have something to replace
	    while((index = str.indexOf(old, old_index)) != -1) {
	      buf.append(str.substring(old_index, index)); // append prefix
	      buf.append(new_);                            // append replacement
	      old_index = index + old.length(); // Skip 'old'.length chars
	    }

	    buf.append(str.substring(old_index)); // append rest of string
	    str = buf.toString();	
      }
    } catch(StringIndexOutOfBoundsException e) { 
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
  

  public static final String signatureToString(String signature,boolean chopit) {
  	ResultHolder rh = signatureToStringInternal(signature,chopit);
  	return rh.getResult();
  }

  /**
   * This method converts this string into a Java type declaration such as
   * 'String[]' and throws a `ClassFormatException' when the parsed type is 
   * invalid.
   *
   * @param  signature  Class signature
   * @param  chopit     Flag that determines whether chopping is executed or not
   * @return Java type declaration
   */
  public static final ResultHolder signatureToStringInternal(String signature,boolean chopit) {
    int processedChars = 1; // This is the default, read just one char
    try {
      switch(signature.charAt(0)) {
        case 'B' : return ResultHolder.BYTE;
        case 'C' : return ResultHolder.CHAR;
        case 'D' : return ResultHolder.DOUBLE;
        case 'F' : return ResultHolder.FLOAT;
        case 'I' : return ResultHolder.INT;
        case 'J' : return ResultHolder.LONG;

        case 'L' : { // Full class name
      	  int    index = signature.indexOf(';'); // Look for closing `;'
      	  // Jump to the correct ';'
      	  if (index!=-1 && 
      		signature.length()>index+1 && 
		    signature.charAt(index+1)=='>') index = index+2;

	      if (index < 0)
	        throw new ClassFormatException("Invalid signature: " + signature);
	
	      int genericStart = signature.indexOf('<');
	      int genericEnd = signature.indexOf('>');
	      if (genericStart !=-1) {
	      	// FIXME asc going to need a lot more work in here for generics
	      	ResultHolder rh = signatureToStringInternal(signature.substring(genericStart+1,genericEnd),chopit);
		    ResultHolder retval = new ResultHolder(compactClassName(signature.substring(1,genericStart)+"<"+
				    rh.getResult()+">",chopit),genericEnd+1);
				    return retval;
	      } else {
	        processedChars = index + 1; // "Lblabla;" `L' and `;' are removed
	        ResultHolder retval = new ResultHolder(compactClassName(signature.substring(1, index), chopit),processedChars);
	        return retval;
	      }
        }

      case 'S' : return ResultHolder.SHORT;
      case 'Z' : return ResultHolder.BOOLEAN;

      case '[' : { // Array declaration
      	StringBuffer  brackets;
      	int          consumedChars,n;

	    brackets = new StringBuffer(); // Accumulate []'s
	    // Count opening brackets and look for optional size argument
	    for(n=0; signature.charAt(n) == '['; n++)
	      brackets.append("[]");
	      consumedChars = n;
	      // The rest of the string denotes a `<field_type>'
	      ResultHolder restOfIt = signatureToStringInternal(signature.substring(n),chopit);
	      
	      //  type = signatureToString(signature.substring(n), chopit);
	
	      consumedChars+= restOfIt.getConsumedChars();
	      return new ResultHolder(restOfIt.getResult() + brackets.toString(),consumedChars);
        }
            	
      case 'V' : return ResultHolder.VOID;

      default  : throw new ClassFormatException("Invalid signature: `" +
					    signature + "'");
      }
    } catch(StringIndexOutOfBoundsException e) { // Should never occur
      throw new ClassFormatException("Invalid signature: " + e + ":" + signature);
    }
  }
  
  


  /** 
   * Parse Java type such as "char", or "java.lang.String[]" and return the
   * signature in byte code format, e.g. "C" or "[Ljava/lang/String;" respectively.
   *
   * @param  type Java type
   * @return byte code signature
   */
  public static String getSignature(String type) {
    StringBuffer buf        = new StringBuffer();
    char[]       chars      = type.toCharArray();
    boolean      char_found = false, delim = false;
    int          index      = -1;

    loop:
      for (int i=0; i < chars.length; i++) {
        switch (chars[i]) {
          case ' ': case '\t': case '\n': case '\r': case '\f':
	        if (char_found) delim = true;
	        break;

          case '[':
	        if (!char_found) throw new RuntimeException("Illegal type: " + type);
	        index = i;
	        break loop;

          default:
	        char_found = true;
	        if (!delim) buf.append(chars[i]);
        }
      }

    int brackets = 0;

    if(index > 0) brackets = countBrackets(type.substring(index));

    type = buf.toString();
    buf.setLength(0);

    for (int i=0; i < brackets; i++) buf.append('[');

    boolean found = false;

    for(int i=Constants.T_BOOLEAN; (i <= Constants.T_VOID) && !found; i++) {
      if (Constants.TYPE_NAMES[i].equals(type)) {
	    found = true;
	    buf.append(Constants.SHORT_TYPE_NAMES[i]);
      }
    }
    
    // Class name
    if (!found) buf.append('L' + type.replace('.', '/') + ';');

    return buf.toString();
  }

  


  /**
   * Return type of method signature as a byte value as defined in <em>Constants</em>
   *
   * @param  signature in format described above
   * @return type of method signature
   * @see    Constants
   */
  public static final byte typeOfMethodSignature(String signature) throws ClassFormatException {
    int index;

    try {
      if (signature.charAt(0) != '(')
	    throw new ClassFormatException("Invalid method signature: " + signature);
      index = signature.lastIndexOf(')') + 1;
      return typeOfSignature(signature.substring(index));
    } catch(StringIndexOutOfBoundsException e) {
      throw new ClassFormatException("Invalid method signature: " + signature);
    }
  }



  /** Map opcode names to opcode numbers. E.g., return Constants.ALOAD for "aload"
   */
  public static short searchOpcode(String name) {
    name = name.toLowerCase();

    for (short i=0; i < Constants.OPCODE_NAMES.length; i++) {
      if (Constants.OPCODE_NAMES[i].equals(name))
	    return i;
    }
    
    return -1;
  }

  /**
   * Convert (signed) byte to (unsigned) short value, i.e., all negative
   * values become positive.
   */
  private static final short byteToShort(byte b) {
    return (b < 0)? (short)(256 + b) : (short)b;
  }

  /** 
   * Convert bytes into hexidecimal string
   *
   * @return bytes as hexidecimal string, e.g. 00 FA 12 ...
   */
  public static final String toHexString(byte[] bytes) {
    StringBuffer buf = new StringBuffer();

    for(int i=0; i < bytes.length; i++) {
      short  b   = byteToShort(bytes[i]);
      String hex = Integer.toString(b, 0x10);

	  // Just one digit, so prepend 0
      if (b < 0x10) buf.append('0');

      buf.append(hex);

      if (i < bytes.length - 1) buf.append(' ');
    }

    return buf.toString();
  }

  /**
   * Return a string for an integer justified left or right and filled up with
   * 'fill' characters if necessary.
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
    int    len = length - str.length();
    char[] buf = new char[(len < 0)? 0 : len];

    for(int j=0; j < buf.length; j++)
      buf[j] = fill;

    if(left_justify)
      return str + new String(buf);    
    else
      return new String(buf) + str;
  }

  static final boolean equals(byte[] a, byte[] b) {
    int size;

    if((size=a.length) != b.length)
      return false;

    for(int i=0; i < size; i++)
      if(a[i] != b[i])
	return false;

    return true;
  }

  public static final void printArray(PrintStream out, Object[] obj) {
    out.println(printArray(obj, true));
  }

  public static final void printArray(PrintWriter out, Object[] obj) {
    out.println(printArray(obj, true));
  }

  public static final String printArray(Object[] obj) {
    return printArray(obj, true);
  }

  public static final String printArray(Object[] obj, boolean braces) {
    return printArray(obj, braces, false);
  }

  public static final String printArray(Object[] obj, boolean braces,
					boolean quote) {
    if(obj == null)
      return null;

    StringBuffer buf = new StringBuffer();
    if(braces)
      buf.append('{');

    for(int i=0; i < obj.length; i++) {
      if(obj[i] != null) {
	buf.append((quote? "\"" : "") + obj[i].toString() + (quote? "\"" : ""));
      } else {
	buf.append("null");
      }

      if(i < obj.length - 1) {
	buf.append(", ");
      }
    }

    if(braces)
      buf.append('}');

    return buf.toString();
  }

  /** @return true, if character is one of (a, ... z, A, ... Z, 0, ... 9, _)
   */
  public static boolean isJavaIdentifierPart(char ch) {
    return ((ch >= 'a') && (ch <= 'z')) ||
      ((ch >= 'A') && (ch <= 'Z')) ||
      ((ch >= '0') && (ch <= '9')) ||
      (ch == '_');
  }

  /** 
   * Encode byte array it into Java identifier string, i.e., a string
   * that only contains the following characters: (a, ... z, A, ... Z,
   * 0, ... 9, _, $).  The encoding algorithm itself is not too
   * clever: if the current byte's ASCII value already is a valid Java
   * identifier part, leave it as it is. Otherwise it writes the
   * escape character($) followed by <p><ul><li> the ASCII value as a
   * hexadecimal string, if the value is not in the range
   * 200..247</li> <li>a Java identifier char not used in a lowercase
   * hexadecimal string, if the value is in the range
   * 200..247</li><ul></p>
   *
   * <p>This operation inflates the original byte array by roughly 40-50%</p>
   *
   * @param bytes the byte array to convert
   * @param compress use gzip to minimize string
   */
  public static String encode(byte[] bytes, boolean compress) throws IOException {
    if(compress) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      GZIPOutputStream      gos  = new GZIPOutputStream(baos);

      gos.write(bytes, 0, bytes.length);
      gos.close();
      baos.close();

      bytes = baos.toByteArray();
    }

    CharArrayWriter caw = new CharArrayWriter();
    JavaWriter      jw  = new JavaWriter(caw);

    for(int i=0; i < bytes.length; i++) {
      int in = bytes[i] & 0x000000ff; // Normalize to unsigned
      jw.write(in);
    }

    return caw.toString();
  }

  /** Decode a string back to a byte array.
   *
   * @param bytes the byte array to convert
   * @param uncompress use gzip to uncompress the stream of bytes
   */
  public static byte[] decode(String s, boolean uncompress) throws IOException {
    char[] chars = s.toCharArray();

    CharArrayReader car = new CharArrayReader(chars);
    JavaReader      jr  = new JavaReader(car);

    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    int ch;

    while((ch = jr.read()) >= 0) {
      bos.write(ch);
    }

    bos.close();
    car.close();
    jr.close();

    byte[] bytes = bos.toByteArray();

    if(uncompress) {
      GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));

      byte[] tmp   = new byte[bytes.length * 3]; // Rough estimate
      int    count = 0;
      int    b;

      while((b = gis.read()) >= 0)
	tmp[count++] = (byte)b;

      bytes = new byte[count];
      System.arraycopy(tmp, 0, bytes, 0, count);
    }

    return bytes;
  }

  // A-Z, g-z, _, $
  private static final int   FREE_CHARS  = 48;
  private static       int[] CHAR_MAP    = new int[FREE_CHARS];
  private static       int[] MAP_CHAR    = new int[256]; // Reverse map
  private static final char  ESCAPE_CHAR = '$';

  static {
    int j = 0, k = 0;
    for(int i='A'; i <= 'Z'; i++) {
      CHAR_MAP[j] = i;
      MAP_CHAR[i] = j;
      j++;
    }

    for(int i='g'; i <= 'z'; i++) {
      CHAR_MAP[j] = i;
      MAP_CHAR[i] = j;
      j++;
    }

    CHAR_MAP[j]   = '$';
    MAP_CHAR['$'] = j;
    j++;

    CHAR_MAP[j]   = '_';
    MAP_CHAR['_'] = j;
  }

  /** Decode characters into bytes.
   * Used by <a href="Utility.html#decode(java.lang.String, boolean)">decode()</a>
   */
  private static class JavaReader extends FilterReader {
    public JavaReader(Reader in) {
      super(in);
    }

    public int read() throws IOException {
      int b = in.read();

      if(b != ESCAPE_CHAR) {
	return b;
      } else {
	int i = in.read();

	if(i < 0)
	  return -1;

	if(((i >= '0') && (i <= '9')) || ((i >= 'a') && (i <= 'f'))) { // Normal escape
	  int j = in.read();

	  if(j < 0)
	    return -1;

	  char[] tmp = { (char)i, (char)j };
	  int    s   = Integer.parseInt(new String(tmp), 16);

	  return s;
	} else { // Special escape
	  return MAP_CHAR[i];
	}
      }
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
      for(int i=0; i < len; i++)
	cbuf[off + i] = (char)read();

      return len;
    }
  }

  /** Encode bytes into valid java identifier characters.
   * Used by <a href="Utility.html#encode(byte[], boolean)">encode()</a>
   */
  private static class JavaWriter extends FilterWriter {
    public JavaWriter(Writer out) {
      super(out);
    }

    public void write(int b) throws IOException {
      if(isJavaIdentifierPart((char)b) && (b != ESCAPE_CHAR)) {
	out.write(b);
      } else {
	out.write(ESCAPE_CHAR); // Escape character

	// Special escape
	if(b >= 0 && b < FREE_CHARS) {
	  out.write(CHAR_MAP[b]);
	} else { // Normal escape
	  char[] tmp = Integer.toHexString(b).toCharArray();

	  if(tmp.length == 1) {
	    out.write('0');
	    out.write(tmp[0]);
	  } else {
	    out.write(tmp[0]);
	    out.write(tmp[1]);
	  }
	}
      }
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
      for(int i=0; i < len; i++)
	write(cbuf[off + i]);
    }

    public void write(String str, int off, int len) throws IOException {
      write(str.toCharArray(), off, len);
    }
  }

  /**
   * Escape all occurences of newline chars '\n', quotes \", etc.
   */
  public static final String convertString(String label) {
    char[]       ch  = label.toCharArray();
    StringBuffer buf = new StringBuffer();

    for(int i=0; i < ch.length; i++) {
      switch(ch[i]) {
      case '\n':
	buf.append("\\n"); break;
      case '\r':
	buf.append("\\r"); break;
      case '\"':
	buf.append("\\\""); break;
      case '\'':
	buf.append("\\'"); break;
      case '\\':
	buf.append("\\\\"); break;
      default:
	buf.append(ch[i]); break;
      }
    }

    return buf.toString();
  }

  public static List getListOfAnnotationNames(Annotation a) {
  	List l = a.getValues();
    List names = new ArrayList();
    for (Iterator i = l.iterator(); i.hasNext();) {
		ElementNameValuePair element = (ElementNameValuePair) i.next();
		names.add(element.getNameString());
	}
    return names;
  }

  
  /**
   * Converts a list of AnnotationGen objects into a set of attributes 
   * that can be attached to the class file.
   *
   * @param cp The constant pool gen where we can create the necessary name refs
   * @param vec A list of AnnotationGen objects
   */
  public static Attribute[] getAnnotationAttributes(ConstantPoolGen cp,List vec) {
  	
  	if (vec.size()==0) return null;
  	
  	try {
  		int countVisible   = 0;
  		int countInvisible = 0;
  	
  		//  put the annotations in the right output stream
  		for (int i=0; i<vec.size(); i++) {
  			AnnotationGen a = (AnnotationGen)vec.get(i);
  			if (a.isRuntimeVisible()) countVisible++;
  			else			   countInvisible++;
  		}
  	
  		ByteArrayOutputStream rvaBytes = new ByteArrayOutputStream();
  		ByteArrayOutputStream riaBytes = new ByteArrayOutputStream();
  		DataOutputStream rvaDos = new DataOutputStream(rvaBytes);
  		DataOutputStream riaDos = new DataOutputStream(riaBytes);
  	
  		rvaDos.writeShort(countVisible);
  		riaDos.writeShort(countInvisible);

  		// put the annotations in the right output stream
  		for (int i=0; i<vec.size(); i++) {
  			AnnotationGen a = (AnnotationGen)vec.get(i);
  			if (a.isRuntimeVisible()) a.dump(rvaDos);
  			else			   a.dump(riaDos);
  		}

    rvaDos.close();
    riaDos.close();
    
    byte[] rvaData = rvaBytes.toByteArray();
    byte[] riaData = riaBytes.toByteArray();
    
    int rvaIndex = -1;
    int riaIndex = -1;
    
    if (rvaData.length>2) rvaIndex = cp.addUtf8("RuntimeVisibleAnnotations");
    if (riaData.length>2) riaIndex = cp.addUtf8("RuntimeInvisibleAnnotations");

  	List newAttributes = new ArrayList();
  	if (rvaData.length>2) {
  		newAttributes.add(
  		  new RuntimeVisibleAnnotations(rvaIndex,rvaData.length,rvaData,cp.getConstantPool()));
  	}
  	if (riaData.length>2) {
  		newAttributes.add(
  		  new RuntimeInvisibleAnnotations(riaIndex,riaData.length,riaData,cp.getConstantPool()));
  	}

  	return (Attribute[])newAttributes.toArray(new Attribute[]{});
  	} catch (IOException e) {
  		System.err.println("IOException whilst processing annotations");
		e.printStackTrace();
	}
  	return null;
  }

  /**
   * Annotations against a class are stored in one of four attribute kinds:
   * - RuntimeVisibleParameterAnnotations
   * - RuntimeInvisibleParameterAnnotations
   */
  public static Attribute[] getParameterAnnotationAttributes(ConstantPoolGen cp,List[] /*Array of lists, array size depends on #params */ vec) {
  	
  	int visCount[]   = new int[vec.length]; 
  	int totalVisCount = 0;
  	int invisCount[] = new int[vec.length];
  	int totalInvisCount = 0;
  	try {
  	
  		for (int i=0; i<vec.length; i++) {
  	  		List l = vec[i];
  	  		if (l!=null) {
  	  		  for (Iterator iter = l.iterator(); iter.hasNext();) {
				AnnotationGen element = (AnnotationGen) iter.next();
				if (element.isRuntimeVisible()) {visCount[i]++;totalVisCount++;}
				else                            {invisCount[i]++;totalInvisCount++;}
			  }
  	  		}
  	  	}  		
  		
  		// Lets do the visible ones
  		ByteArrayOutputStream rvaBytes = new ByteArrayOutputStream();
  		DataOutputStream rvaDos = new DataOutputStream(rvaBytes);
  		rvaDos.writeByte(vec.length); // First goes number of parameters
  		
  		for (int i=0; i<vec.length; i++) {
  			rvaDos.writeShort(visCount[i]);
  			if (visCount[i]>0) {
  				List l = vec[i];
  				for (Iterator iter = l.iterator(); iter.hasNext();) {
					AnnotationGen element = (AnnotationGen) iter.next();
					if (element.isRuntimeVisible()) element.dump(rvaDos);
				}
  			}
  		}
  	    rvaDos.close();
  		
  		// Lets do the invisible ones
  		ByteArrayOutputStream riaBytes = new ByteArrayOutputStream();
  		DataOutputStream riaDos = new DataOutputStream(riaBytes);
  		riaDos.writeByte(vec.length); // First goes number of parameters
  		
 		for (int i=0; i<vec.length; i++) {
  			riaDos.writeShort(invisCount[i]);
  			if (invisCount[i]>0) {
  				List l = vec[i];
  				for (Iterator iter = l.iterator(); iter.hasNext();) {
					AnnotationGen element = (AnnotationGen) iter.next();
					if (!element.isRuntimeVisible()) element.dump(riaDos);
				}
  			}
  		}
  	    riaDos.close();
  		
  	    byte[] rvaData = rvaBytes.toByteArray();
  	    byte[] riaData = riaBytes.toByteArray();
    
  	    int rvaIndex = -1;
  	    int riaIndex = -1;
    
  	    if (totalVisCount>0)   rvaIndex = cp.addUtf8("RuntimeVisibleParameterAnnotations");
  	    if (totalInvisCount>0) riaIndex = cp.addUtf8("RuntimeInvisibleParameterAnnotations");

  	    List newAttributes = new ArrayList();

  	    if (totalVisCount>0) {
  		    newAttributes.add(
  		        new RuntimeVisibleParameterAnnotations(rvaIndex,rvaData.length,rvaData,cp.getConstantPool()));
  	    }
  	

  	    if (totalInvisCount>0) {
  	    	newAttributes.add(
  	    	    new RuntimeInvisibleParameterAnnotations(riaIndex,riaData.length,riaData,cp.getConstantPool()));
  	    }

  	    return (Attribute[])newAttributes.toArray(new Attribute[]{});
  	} catch (IOException e) {
  		System.err.println("IOException whilst processing parameter annotations");
		e.printStackTrace();
	}
  	return null;
  }
  

  private static final boolean is_digit(char ch) {
    return (ch >= '0') && (ch <= '9');
  }    
  
  private static final boolean is_space(char ch) {
    return (ch == ' ') || (ch == '\t') || (ch == '\r') || (ch == '\n');
  }    
  
  private static class ResultHolder {
  	private String result;
  	private int consumed;
  	
  	public static final ResultHolder BYTE     = new ResultHolder("byte",1);
  	public static final ResultHolder CHAR     = new ResultHolder("char",1);
  	public static final ResultHolder DOUBLE   = new ResultHolder("double",1);
  	public static final ResultHolder FLOAT    = new ResultHolder("float",1);
  	public static final ResultHolder INT      = new ResultHolder("int",1);
  	public static final ResultHolder LONG     = new ResultHolder("long",1);
  	public static final ResultHolder SHORT    = new ResultHolder("short",1);
  	public static final ResultHolder BOOLEAN  = new ResultHolder("boolean",1);
  	public static final ResultHolder VOID     = new ResultHolder("void",1);
  	
  	public ResultHolder(String s,int c) {
  		result = s;
  		consumed = c;
  	}
  	
  	public String getResult() { return result;}
  	public int getConsumedChars() { return consumed; }
  }

  // code below here I am happy with   ....
  
  /**
   * Return type of signature as a byte value as defined in <em>Constants</em>
   *
   * @param  signature in format described above
   * @return type of signature
   * @see    Constants
   */
  public static final byte typeOfSignature(String signature) throws ClassFormatException {
    try {
      switch(signature.charAt(0)) {
          case 'B' : return Constants.T_BYTE;
          case 'C' : return Constants.T_CHAR;
          case 'D' : return Constants.T_DOUBLE;
	      case 'F' : return Constants.T_FLOAT;
	      case 'I' : return Constants.T_INT;
	      case 'J' : return Constants.T_LONG;
	      case 'L' : return Constants.T_REFERENCE;
	      case '[' : return Constants.T_ARRAY;
	      case 'V' : return Constants.T_VOID;
	      case 'Z' : return Constants.T_BOOLEAN;
	      case 'S' : return Constants.T_SHORT;
          default:  
	        throw new ClassFormatException("Invalid method signature: " + signature);
      }
    } catch(StringIndexOutOfBoundsException e) {
      throw new ClassFormatException("Invalid method signature: " + signature);
    }
  }
  
  public static final byte typeOfSignature(char c) throws ClassFormatException {
      switch(c) {
          case 'B' : return Constants.T_BYTE;
          case 'C' : return Constants.T_CHAR;
          case 'D' : return Constants.T_DOUBLE;
	      case 'F' : return Constants.T_FLOAT;
	      case 'I' : return Constants.T_INT;
	      case 'J' : return Constants.T_LONG;
	      case 'L' : return Constants.T_REFERENCE;
	      case '[' : return Constants.T_ARRAY;
	      case 'V' : return Constants.T_VOID;
	      case 'Z' : return Constants.T_BOOLEAN;
	      case 'S' : return Constants.T_SHORT;
          default:  
	        throw new ClassFormatException("Invalid type of signature: " + c);
      }
  }
  
  public static final String readClassTypeSignatureFrom(String signature) {
    StringBuffer sb = new StringBuffer();
	readClassTypeSignatureFrom(signature,0,sb,false);
	return sb.toString();
  }
  
  /**
   * Takes a string and consumes a single complete signature from it, returning
   * how many chars it consumed.  The chopit flag indicates whether to shorten
   * type references ( java/lang/String => String )
   * 
   * FIXME asc this should also create some kind of object you can query for information about whether its parameterized, what the bounds are, etc...
   */
  public static final int readClassTypeSignatureFrom(String signature, int posn, StringBuffer result, boolean chopit) {
	    int idx = posn;
	    try {
	      switch (signature.charAt(idx)) {
	        case 'B' : result.append("byte");   return 1;
	        case 'C' : result.append("char");   return 1;
	        case 'D' : result.append("double"); return 1;
	        case 'F' : result.append("float");  return 1;
	        case 'I' : result.append("int");    return 1;
	        case 'J' : result.append("long");   return 1;
		    case 'S' : result.append("short");  return 1;
		    case 'Z' : result.append("boolean");return 1;
	        case 'V' : result.append("void");   return 1;
			
			
			//FIXME ASC Need a state machine to check we are parsing the right stuff here !
	        case 'T' : 
				idx++;
				int nextSemiIdx = signature.indexOf(';',idx);
				result.append(signature.substring(idx,nextSemiIdx));
				return nextSemiIdx+1-posn;
			
	        case '+' : 
				result.append("? extends ");
				return readClassTypeSignatureFrom(signature,idx+1,result,chopit)+1;
				
			case '-' : 
				result.append("? super ");
				return readClassTypeSignatureFrom(signature,idx+1,result,chopit)+1;

			case '*' : 
				result.append("?");
				return 1;
			
			case 'L' : // Full class name
		      boolean parameterized = false;
	      	  int idxSemicolon = signature.indexOf(';',idx); // Look for closing ';' or '<'
			  int idxAngly     = signature.indexOf('<',idx);
			  int endOfSig = idxSemicolon;
			  if ((idxAngly!=-1) && idxAngly<endOfSig) { endOfSig = idxAngly; parameterized = true; }
			  
			  String p = signature.substring(idx+1,endOfSig);
			  String t = compactClassName(p,chopit);
			  
			  result.append(t);
			  idx=endOfSig;
			  // we might have finished now, depending on whether this is a parameterized type...
			  if (parameterized) {
				  idx++;
				  result.append("<");
				  while (signature.charAt(idx)!='>') {
					  idx+=readClassTypeSignatureFrom(signature,idx,result,chopit);
					  if (signature.charAt(idx)!='>') result.append(",");
				  }
				  result.append(">");idx++;
			  }
			  if (signature.charAt(idx)!=';') throw new RuntimeException("Did not find ';' at end of signature, found "+signature.charAt(idx));
			  idx++;
			  return idx-posn;


	      case '[' :  // Array declaration
			  int dim = 0;
			  while (signature.charAt(idx)=='[') {dim++;idx++;}
			  idx+=readClassTypeSignatureFrom(signature,idx,result,chopit);
			  while (dim>0) {result.append("[]");dim--;}
			  return idx-posn;

	      default  : throw new ClassFormatException("Invalid signature: `" +
						    signature + "'");
	      }
	    } catch(StringIndexOutOfBoundsException e) { // Should never occur
	      throw new ClassFormatException("Invalid signature: " + e + ":" + signature);
	    }
	  }
  
  private static int countBrackets(String brackets) {
    char[]  chars = brackets.toCharArray();
    int     count = 0;
    boolean open  = false;

    for(int i=0; i<chars.length; i++) {
      switch(chars[i]) {
        case '[':
	      if (open) throw new RuntimeException("Illegally nested brackets:" + brackets);
	      open = true;
	      break;

        case ']':
	      if (!open) throw new RuntimeException("Illegally nested brackets:" + brackets);
	      open = false;
	      count++;
	      break;

        default:
      }
    }

    if (open) throw new RuntimeException("Illegally nested brackets:" + brackets);

    return count;
  }
  
  /**
   * Disassemble a stream of byte codes and return the string representation.
   *
   * @param  bytes stream of bytes
   * @param  constant_pool Array of constants
   * @param  verbose be verbose, e.g. print constant pool index
   * @return String representation of byte code
   */
  public static final String codeToString(ByteSequence bytes,
					  ConstantPool constant_pool, boolean verbose) throws IOException {
    short        opcode = (short)bytes.readUnsignedByte();
    int          default_offset=0, low, high, npairs;
    int          index, vindex, constant;
    int[]        match, jump_table;
    int          no_pad_bytes=0, offset;
    StringBuffer buf = new StringBuffer(Constants.OPCODE_NAMES[opcode]);

    /* Special case: Skip (0-3) padding bytes, i.e., the
     * following bytes are 4-byte-aligned
     */
    if ((opcode == Constants.TABLESWITCH) || (opcode == Constants.LOOKUPSWITCH)) {
      int remainder = bytes.getIndex() % 4;
      no_pad_bytes  = (remainder == 0)? 0 : 4 - remainder;

      for (int i=0; i < no_pad_bytes; i++) {
	    byte b = bytes.readByte();
	    if (b != 0)
	       System.err.println("Warning: Padding byte != 0 in " + Constants.OPCODE_NAMES[opcode] + ":" + b);
      }

      // Both cases have a field default_offset in common
      default_offset = bytes.readInt();
    }

    switch(opcode) {
      /* Table switch has variable length arguments.
       */
      case Constants.TABLESWITCH:
        low  = bytes.readInt();
        high = bytes.readInt();

        offset = bytes.getIndex() - 12 - no_pad_bytes - 1;
        default_offset += offset;

        buf.append("\tdefault = " + default_offset + ", low = " + low + 
		   ", high = " + high + "(");

        jump_table = new int[high - low + 1];
        for (int i=0; i < jump_table.length; i++) {
	      jump_table[i] = offset + bytes.readInt();
	      buf.append(jump_table[i]);
	      if (i < jump_table.length - 1) buf.append(", ");
        }
        buf.append(")");
        break;

      /* Lookup switch has variable length arguments.
       */
      case Constants.LOOKUPSWITCH: {

        npairs = bytes.readInt();
        offset = bytes.getIndex() - 8 - no_pad_bytes - 1;
	  
        match      = new int[npairs];
        jump_table = new int[npairs];
        default_offset += offset;

        buf.append("\tdefault = " + default_offset + ", npairs = " + npairs + " (");

        for (int i=0; i < npairs; i++) {
	      match[i]      = bytes.readInt();
	      jump_table[i] = offset + bytes.readInt();
	      buf.append("(" + match[i] + ", " + jump_table[i] + ")");
	      if(i < npairs - 1) buf.append(", ");
        }
        buf.append(")");
      }
      break;

      // Two address bytes + offset from start of byte stream form the jump target
      case Constants.GOTO:      case Constants.IFEQ:      case Constants.IFGE:      case Constants.IFGT:
      case Constants.IFLE:      case Constants.IFLT:      case Constants.JSR: case Constants.IFNE:
      case Constants.IFNONNULL: case Constants.IFNULL:    case Constants.IF_ACMPEQ:
      case Constants.IF_ACMPNE: case Constants.IF_ICMPEQ: case Constants.IF_ICMPGE: case Constants.IF_ICMPGT:
      case Constants.IF_ICMPLE: case Constants.IF_ICMPLT: case Constants.IF_ICMPNE:
        buf.append("\t\t#" + ((bytes.getIndex() - 1) + bytes.readShort()));
        break;
	  
      // 32-bit wide jumps
      case Constants.GOTO_W: case Constants.JSR_W:
        buf.append("\t\t#" + ((bytes.getIndex() - 1) + bytes.readInt()));
        break;

      // Index byte references local variable (register)
      case Constants.ALOAD:  case Constants.ASTORE: case Constants.DLOAD:  case Constants.DSTORE: case Constants.FLOAD:
      case Constants.FSTORE: case Constants.ILOAD:  case Constants.ISTORE: case Constants.LLOAD:  case Constants.LSTORE:
      case Constants.RET: 
        if (wide) {
	      vindex = bytes.readUnsignedShort();
	      wide=false; // Clear flag
        } else
	      vindex = bytes.readUnsignedByte();
        buf.append("\t\t%" + vindex);
        break;

      /*
       * Remember wide byte which is used to form a 16-bit address in the
       * following instruction. Relies on that the method is called again with
       * the following opcode.
       */
      case Constants.WIDE: wide=true; buf.append("\t(wide)"); break;

      // Array of basic type
      case Constants.NEWARRAY: buf.append("\t\t<" + Constants.TYPE_NAMES[bytes.readByte()] + ">"); break;

      // Access object/class fields
      case Constants.GETFIELD: case Constants.GETSTATIC: case Constants.PUTFIELD: case Constants.PUTSTATIC:
        index = bytes.readUnsignedShort();
        buf.append("\t\t" +
		  constant_pool.constantToString(index, Constants.CONSTANT_Fieldref) +
		  (verbose? " (" + index + ")" : ""));
        break;
	  
      // Operands are references to classes in constant pool
      case Constants.NEW:
      case Constants.CHECKCAST:
        buf.append("\t");
      case Constants.INSTANCEOF:
        index = bytes.readUnsignedShort();
        buf.append("\t<" + constant_pool.constantToString(index,Constants.CONSTANT_Class) +
		  ">" + (verbose? " (" + index + ")" : ""));
        break;

      // Operands are references to methods in constant pool
      case Constants.INVOKESPECIAL: case Constants.INVOKESTATIC: case Constants.INVOKEVIRTUAL:
        index = bytes.readUnsignedShort();
        buf.append("\t" + constant_pool.constantToString(index,Constants.CONSTANT_Methodref) +
		  (verbose? " (" + index + ")" : ""));
        break;

      case Constants.INVOKEINTERFACE:
        index = bytes.readUnsignedShort();
        int nargs = bytes.readUnsignedByte(); // historical, redundant
        buf.append("\t" + 
		  constant_pool.constantToString(index,Constants.CONSTANT_InterfaceMethodref) +
		  (verbose? " (" + index + ")\t" : "") + nargs + "\t" + 
		  bytes.readUnsignedByte()); // Last byte is a reserved space
        break;
	
      // Operands are references to items in constant pool
      case Constants.LDC_W: case Constants.LDC2_W:
        index = bytes.readUnsignedShort();
        buf.append("\t\t" + constant_pool.constantToString
		  (index, constant_pool.getConstant(index).getTag()) +
		  (verbose? " (" + index + ")" : ""));
        break;

      case Constants.LDC:
        index = bytes.readUnsignedByte();
        buf.append("\t\t" + 
		  constant_pool.constantToString
		  (index, constant_pool.getConstant(index).getTag()) +
		  (verbose? " (" + index + ")" : ""));
        break;
	
      // Array of references
      case Constants.ANEWARRAY:
        index = bytes.readUnsignedShort();
        buf.append("\t\t<" + compactClassName(constant_pool.getConstantString
	      (index, Constants.CONSTANT_Class), false) +
		  ">" + (verbose? " (" + index + ")": ""));
        break;
	
      // Multidimensional array of references
      case Constants.MULTIANEWARRAY: {
        index          = bytes.readUnsignedShort();
        int dimensions = bytes.readUnsignedByte();

        buf.append("\t<" + compactClassName(constant_pool.getConstantString
		  (index, Constants.CONSTANT_Class), false) +
		  ">\t" + dimensions + (verbose? " (" + index + ")" : ""));
        }
        break;

      // Increment local variable
      case Constants.IINC:
        if (wide) {
	      vindex   = bytes.readUnsignedShort();
	      constant = bytes.readShort();
	      wide     = false;
        } else {
	      vindex   = bytes.readUnsignedByte();
	      constant = bytes.readByte();
        }
        buf.append("\t\t%" + vindex + "\t" + constant);
        break;

      default:
        if (Constants.NO_OF_OPERANDS[opcode] > 0) {
	    for (int i=0; i < Constants.TYPE_OF_OPERANDS[opcode].length; i++) {
	      buf.append("\t\t");
	      switch(Constants.TYPE_OF_OPERANDS[opcode][i]) {
	        case Constants.T_BYTE:  buf.append(bytes.readByte()); break;
	        case Constants.T_SHORT: buf.append(bytes.readShort());       break;
	        case Constants.T_INT:   buf.append(bytes.readInt());         break;
					      
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
}
