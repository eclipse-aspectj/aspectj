/* *******************************************************************
 * Copyright (c) 2005 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement (IBM)     initial implementation 
 * ******************************************************************/
package org.aspectj.apache.bcel.classfile.tests;

import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Attribute;
import org.aspectj.apache.bcel.classfile.ClassFormatException;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.Utility;

/**
 * Generics introduces more complex signature possibilities, they are no longer just
 * made up of primitives or big 'L' types. The addition of 'anglies' due to
 * parameterization and the ability to specify wildcards (possibly bounded)
 * when talking about parameterized types means we need to be much more sophisticated.
 *
 *
 * Notes:
 * Signatures are used to encode Java programming language type informaiton
 * that is not part of the JVM type system, such as generic type and method
 * declarations and parameterized types.  This kind of information is
 * needed to support reflection and debugging, and by the Java compiler.
 * 
 * ============================================= 
 * 
 * ClassTypeSignature =      LPackageSpecifier* SimpleClassTypeSignature ClassTypeSignatureSuffix*;
 * 
 * PackageSpecifier =        Identifier/PackageSpecifier*
 * SimpleClassTypeSignature= Identifier TypeArguments(opt)
 * ClassTypeSignatureSuffix= .SimpleClassTypeSignature
 * TypeVariableSignature =   TIdentifier;
 * TypeArguments =           <TypeArgument+>
 * TypeArgument =            WildcardIndiciator(opt) FieldTypeSignature
 *                           *
 * WildcardIndicator =       +
 *                           -
 * ArrayTypeSignature =      [TypeSignature
 * TypeSignature =           [FieldTypeSignature
 *                           [BaseType
 *                           
 *                           <not sure those [ should be prefixing fts and bt>
 * Examples:
 * 	Ljava/util/List;                      ==   java.util.List
 *  Ljava/util/List<Ljava/lang/String;>;  ==   java.util.List<java.lang.String>
 *  Ljava/util/List<Ljava/lang/Double;>;  ==   java.util.List<java.lang.Double>
 *  Ljava/util/List<+Ljava/lang/Number;>; ==   java.util.List<? extends java.lang.Number>
 *  Ljava/util/List<-Ljava/lang/Number;>; ==   java.util.List<? super java.lang.Number>
 *  Ljava/util/List<*>;                   ==   java.util.List<?>
 *  Ljava/util/Map<*-Ljava/lang/Number;>; ==   java.util.Map<?,? super java.lang.Number>
 *                           
 * ============================================= 
 * 
 * ClassSignature =          FormalTypeParameters(opt) SuperclassSignature SuperinterfaceSignatures*
 *   
 *   optional formal type parameters then a superclass signature then a superinterface signature
 * 
 * FormalTypeParameters =    <FormalTypeParameter+>
 * FormalTypeParameter  =    Identifier ClassBound InterfaceBound*  
 * ClassBound =              :FieldTypeSignature(opt)
 * InterfaceBound =          :FieldTypeSignature
 *    
 *   If it exists, a set of formal type parameters are contained in anglies and consist of an identifier a classbound (assumed to be
 *   object if not specified) and then an optional list of InterfaceBounds
 *   
 * SuperclassSignature =     ClassTypeSignature
 * SuperinterfaceSignature = ClassTypeSignature
 * FieldTypeSignature =      ClassTypeSignature
 *                           ArrayTypeSignature
 *                           TypeVariableSignature
 *                           
 *                           
 * MethodTypeSignature =     FormalTypeParameters(opt) ( TypeSignature* ) ReturnType ThrowsSignature*
 * ReturnType =              TypeSignature
 *                           VoidDescriptor
 * ThrowsSignature =         ^ClassTypeSignature
 *                           ^TypeVariableSignature
 *                           
 *  Examples: 
 * 
 * <T::Ljava/lang/Comparable<-Ljava/lang/Number;>;>
 * 
 * ClassBound not supplied, Object assumed.  Interface bound is Comparable<? super Number>
 * 
 * "T:Ljava/lang/Object;:Ljava/lang/Comparable<-TT;>;","T extends java.lang.Object & java.lang.Comparable<? super T>"
 *
 */
public class GenericSignatureParsingTest extends BcelTestCase {
	
	
	/** 
	 * Throw some generic format signatures at the BCEL signature 
	 * parsing code and see what it does.
	 */
	public void testParsingGenericSignatures_ClassTypeSignature() {
		// trivial
		checkClassTypeSignature("Ljava/util/List;","java.util.List");
		
		// basics
		checkClassTypeSignature("Ljava/util/List<Ljava/lang/String;>;","java.util.List<java.lang.String>");
		checkClassTypeSignature("Ljava/util/List<Ljava/lang/Double;>;","java.util.List<java.lang.Double>");

		// madness
		checkClassTypeSignature("Ljava/util/List<+Ljava/lang/Number;>;","java.util.List<? extends java.lang.Number>");
		checkClassTypeSignature("Ljava/util/List<-Ljava/lang/Number;>;","java.util.List<? super java.lang.Number>");
		checkClassTypeSignature("Ljava/util/List<*>;",                  "java.util.List<?>");
		checkClassTypeSignature("Ljava/util/Map<*-Ljava/lang/Number;>;","java.util.Map<?,? super java.lang.Number>");
		
		// with type params
		checkClassTypeSignature("Ljava/util/Collection<TT;>;","java.util.Collection<T>");
		
		// arrays
		checkClassTypeSignature("Ljava/util/List<[Ljava/lang/String;>;","java.util.List<java.lang.String[]>");
		checkClassTypeSignature("[Ljava/util/List<Ljava/lang/String;>;","java.util.List<java.lang.String>[]");

	}
	
	
	public void testMethodTypeToSignature() {
	  checkMethodTypeToSignature("void",new String[]{"java.lang.String[]","boolean"},"([Ljava/lang/String;Z)V");
	  checkMethodTypeToSignature("void",new String[]{"java.util.List<java/lang/String>"},"(Ljava/util/List<java/lang/String>;)V");
	}
	
	public void testMethodSignatureToArgumentTypes() {
	  checkMethodSignatureArgumentTypes("([Ljava/lang/String;Z)V",new String[]{"java.lang.String[]","boolean"});
//	  checkMethodSignatureArgumentTypes("(Ljava/util/List<java/lang/String>;)V",new String[]{"java.util.List<java/lang/String>"});
	}
	
	public void testMethodSignatureReturnType() {
	  checkMethodSignatureReturnType("([Ljava/lang/String;)Z","boolean");
	}
	
	public void testLoadingGenerics() throws ClassNotFoundException {
		JavaClass clazz = getClassFromJar("PossibleGenericsSigs");
		// J5TODO asc fill this bit in...
	}
	
	
	// helper methods below

	// These routines call BCEL to determine if it can correctly translate from one form to the other.
	private void checkClassTypeSignature(String sig, String expected) {
		StringBuffer result = new StringBuffer();
		int p = GenericSignatureParsingTest.readClassTypeSignatureFrom(sig,0,result,false);
		assertTrue("Only swallowed "+p+" chars of this sig "+sig+" (len="+sig.length()+")",p==sig.length());
		assertTrue("Expected '"+expected+"' but got '"+result.toString()+"'",result.toString().equals(expected));
	}
	
	private void checkMethodTypeToSignature(String ret,String[] args,String expected) {
		String res = GenericSignatureParsingTest.methodTypeToSignature(ret,args);
		if (!res.equals(expected)) {
			fail("Should match.  Got: "+res+"  Expected:"+expected);
		}
	}
	
	private void checkMethodSignatureReturnType(String sig,String expected) {
		String result = GenericSignatureParsingTest.methodSignatureReturnType(sig,false);
		if (!result.equals(expected)) {
			fail("Should match.  Got: "+result+"  Expected:"+expected);
		}
	}
	
	private void checkMethodSignatureArgumentTypes(String in,String[] expected) {
		String[] result = GenericSignatureParsingTest.methodSignatureArgumentTypes(in,false);
		if (result.length!=expected.length) {
			fail("Expected "+expected.length+" entries to be returned but only got "+result.length);
		}
		for (int i = 0; i < expected.length; i++) {
			String string = result[i];
			if (!string.equals(expected[i]))
				fail("Argument: "+i+" should have been "+expected[i]+" but was "+string);
		}
	}
	
	public Signature getSignatureAttribute(JavaClass clazz,String name) {
		Method m = getMethod(clazz,name);
		Attribute[] as = m.getAttributes();
		for (Attribute attribute : as) {
			if (attribute.getName().equals("Signature")) {
				return (Signature) attribute;
			}
		}
		return null;
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
				  String t = Utility.compactClassName(p,chopit);
				  
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


	public static final String readClassTypeSignatureFrom(String signature) {
	    StringBuffer sb = new StringBuffer();
		GenericSignatureParsingTest.readClassTypeSignatureFrom(signature,0,sb,false);
		return sb.toString();
	  }


	public static int countBrackets(String brackets) {
	    char[]  chars = brackets.toCharArray();
	    int     count = 0;
	    boolean open  = false;

		for (char aChar : chars) {
			switch (aChar) {
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
	
	    if(index > 0) brackets = GenericSignatureParsingTest.countBrackets(type.substring(index));
	
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
	      type = Utility.signatureToString(signature.substring(index), chopit);
	    } catch (StringIndexOutOfBoundsException e) {
	      throw new ClassFormatException("Invalid method signature: " + signature);
	    }
	    return type;
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
	    return GenericSignatureParsingTest.methodSignatureReturnType(signature, true);
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
	    List<String> vec = new ArrayList<>();
	    int       index;
	    String[]  types;
	
	    try { // Read all declarations between for `(' and `)'
	      if (signature.charAt(0) != '(')
		    throw new ClassFormatException("Invalid method signature: " + signature);
	
	      index = 1; // current string position
	
	      while(signature.charAt(index) != ')') {
	      	Utility.ResultHolder rh = Utility.signatureToStringInternal(signature.substring(index),chopit);
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
			for (String methodArg : methodArgs) {
				String str = GenericSignatureParsingTest.getSignature(methodArg);

				if (str.equals("V")) // void can't be a method argument
					throw new ClassFormatException("Invalid type: " + methodArg);

				buf.append(str);
			}
	    }
	    
	    buf.append(")" + GenericSignatureParsingTest.getSignature(returnType));
	
	    return buf.toString();
	  }
	
}
