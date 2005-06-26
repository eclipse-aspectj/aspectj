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
 * 
 * Extended by Adrian Colyer, June 2005 to support unpacking of Signature
 * attribute
 */


import  org.aspectj.apache.bcel.Constants;

import sun.reflect.generics.tree.SimpleClassTypeSignature;

import  java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is derived from <em>Attribute</em> and represents a reference
 * to a <href="http://wwwipd.ira.uka.de/~pizza/gj/">GJ</a> attribute.
 *
 * @version $Id: Signature.java,v 1.3 2005/06/26 20:29:23 acolyer Exp $
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see     Attribute
 */
public final class Signature extends Attribute {
  private int signature_index;
  
  /**
   * Initialize from another object. Note that both objects use the same
   * references (shallow copy). Use clone() for a physical copy.
   */
  public Signature(Signature c) {
    this(c.getNameIndex(), c.getLength(), c.getSignatureIndex(), c.getConstantPool());
  }

  /**
   * Construct object from file stream.
   * @param name_index Index in constant pool to CONSTANT_Utf8
   * @param length Content length in bytes
   * @param file Input stream
   * @param constant_pool Array of constants
   * @throws IOException
   */
  Signature(int name_index, int length, DataInputStream file,
	   ConstantPool constant_pool) throws IOException
  {
    this(name_index, length, file.readUnsignedShort(), constant_pool);
  }

  /**
   * @param name_index Index in constant pool to CONSTANT_Utf8
   * @param length Content length in bytes
   * @param constant_pool Array of constants
   * @param Signature_index Index in constant pool to CONSTANT_Utf8
   */
  public Signature(int name_index, int length, int signature_index,
		  ConstantPool constant_pool)
  {
    super(Constants.ATTR_SIGNATURE, name_index, length, constant_pool);
    this.signature_index = signature_index;
  }

  /**
   * Called by objects that are traversing the nodes of the tree implicitely
   * defined by the contents of a Java class. I.e., the hierarchy of methods,
   * fields, attributes, etc. spawns a tree of objects.
   *
   * @param v Visitor object
   */
   public void accept(Visitor v) {
     System.err.println("Visiting non-standard Signature object");
     v.visitSignature(this);
   }
   
  /**
   * Dump source file attribute to file stream in binary format.
   *
   * @param file Output file stream
   * @throws IOException
   */ 
  public final void dump(DataOutputStream file) throws IOException
  {
    super.dump(file);
    file.writeShort(signature_index);
  }    

  /**
   * @return Index in constant pool of source file name.
   */  
  public final int getSignatureIndex() { return signature_index; }    

  /**
   * @param Signature_index.
   */
  public final void setSignatureIndex(int signature_index) {
    this.signature_index = signature_index;
  }    

  /**
   * @return GJ signature.
   */ 
  public final String getSignature() {
    ConstantUtf8 c = (ConstantUtf8)constant_pool.getConstant(signature_index, 
							     Constants.CONSTANT_Utf8);
    return c.getBytes();
  }

  /**
   * Extends ByteArrayInputStream to make 'unreading' chars possible.
   */
  private static final class MyByteArrayInputStream extends ByteArrayInputStream {
    MyByteArrayInputStream(String data) { super(data.getBytes()); }
    final int  mark()                   { return pos; }
    final String getData()              { return new String(buf); }
    final void reset(int p)             { pos = p; }
    final void unread()                 { if(pos > 0) pos--; }
  }

  private static boolean identStart(int ch) {
    return ch == 'T' || ch == 'L';
  }

  private static boolean identPart(int ch) {
    return ch == '/' || ch == ';';
  }

  private static final void matchIdent(MyByteArrayInputStream in, StringBuffer buf) {
    int ch;

    if((ch = in.read()) == -1)
      throw new RuntimeException("Illegal signature: " + in.getData() +
				 " no ident, reaching EOF");

    //System.out.println("return from ident:" + (char)ch);

    if(!identStart(ch)) {
      StringBuffer buf2 = new StringBuffer();

      int count = 1;
      while(Character.isJavaIdentifierPart((char)ch)) {
	buf2.append((char)ch);
	count++;
	ch = in.read();
      }
      
      if(ch == ':') { // Ok, formal parameter
	in.skip("Ljava/lang/Object".length());
	buf.append(buf2);

        ch = in.read();
	in.unread();
	//System.out.println("so far:" + buf2 + ":next:" +(char)ch);
      } else {
	for(int i=0; i < count; i++)
	  in.unread();
      }

      return;
    }

    StringBuffer buf2 = new StringBuffer();
    ch = in.read();

    do {
      buf2.append((char)ch);
      ch = in.read();
      //System.out.println("within ident:"+ (char)ch);

    } while((ch != -1) && (Character.isJavaIdentifierPart((char)ch) || (ch == '/')));

    buf.append(buf2.toString().replace('/', '.'));

    //System.out.println("regular return ident:"+ (char)ch + ":" + buf2);

    if(ch != -1)
      in.unread();
  }

  private static final void matchGJIdent(MyByteArrayInputStream in,
					 StringBuffer buf)
  {
    int ch;

    matchIdent(in, buf);

    ch = in.read();
    if((ch == '<') || ch == '(') { // Parameterized or method
      //System.out.println("Enter <");
      buf.append((char)ch);
      matchGJIdent(in, buf);
      
      while(((ch = in.read()) != '>') && (ch != ')')) { // List of parameters
	if(ch == -1)
	  throw new RuntimeException("Illegal signature: " + in.getData() +
				     " reaching EOF");

	//System.out.println("Still no >");
	buf.append(", ");
	in.unread();
	matchGJIdent(in, buf); // Recursive call
      }

      //System.out.println("Exit >");

      buf.append((char)ch);
    } else
      in.unread();

    ch = in.read();
    if(identStart(ch)) {
      in.unread();
      matchGJIdent(in, buf);
    } else if(ch == ')') {
      in.unread();
      return;
    } else if(ch != ';')
      throw new RuntimeException("Illegal signature: " + in.getData() + " read " +
				 (char)ch);
  }

  public static String translate(String s) {
    //System.out.println("Sig:" + s);
    StringBuffer buf = new StringBuffer();

    matchGJIdent(new MyByteArrayInputStream(s), buf);

    return buf.toString();
  }

  public static final boolean isFormalParameterList(String s) {
    return s.startsWith("<") && (s.indexOf(':') > 0);
  }

  public static final boolean isActualParameterList(String s) {
    return s.startsWith("L") && s.endsWith(">;");
  }    

  /**
   * @return String representation
   */ 
  public final String toString() {
    String s = getSignature();

    return "Signature(" + s + ")";
  }    

  /**
   * @return deep copy of this attribute
   */
  public Attribute copy(ConstantPool constant_pool) {
    return (Signature)clone();
  }

  // =============================================
  // AMC extensions
  
  /**
   * structure capturing a FormalTypeParameter from the Signature grammar
   */
  static class FormalTypeParameter {
	  String identifier;
	  FieldTypeSignature classBound;
	  FieldTypeSignature[] interfaceBounds;
  }
  
  static abstract class FieldTypeSignature {
	  boolean isClassTypeSignature() { return false; }
	  boolean isTypeVariableSignature() { return false; }
	  boolean isArrayTypeSignature() { return false; }
  }
  
  static class ClassTypeSignature extends FieldTypeSignature {
	  public String classSignature;
	  public SimpleClassTypeSignature outerType;
	  public SimpleClassTypeSignature[] nestedTypes; 
	  public ClassTypeSignature(String sig,String identifier) { 
		  this.classSignature = sig;
		  this.outerType = new SimpleClassTypeSignature(identifier);
		  this.nestedTypes = new SimpleClassTypeSignature[0];
	  }
	  public ClassTypeSignature(String sig, SimpleClassTypeSignature outer, SimpleClassTypeSignature[] inners) {
		  this.classSignature = sig;
		  this.outerType = outer;
		  this.nestedTypes = inners;
	  }
	  boolean isClassTypeSignature() { return true; }
  }

  static class TypeVariableSignature extends FieldTypeSignature {
	  public String typeVariableName;
	  public TypeVariableSignature(String typeVarToken) {
		  this.typeVariableName = typeVarToken.substring(1);
	  }
	  boolean isTypeVariableSignature() { return true; }
  }

  static class ArrayTypeSignature extends FieldTypeSignature {
	  public FieldTypeSignature fieldTypeSig;
	  public String baseTypeSig;
	  public boolean isBaseTypeArray;
	  public ArrayTypeSignature(FieldTypeSignature aFieldSig) {
		  this.fieldTypeSig = aFieldSig;
		  isBaseTypeArray = false;
	  }
	  public ArrayTypeSignature(String aBaseType) {
		  this.baseTypeSig = aBaseType;
		  isBaseTypeArray = true;
	  }
	  boolean isArrayTypeSignature() { return true; }
  }

  static class SimpleClassTypeSignature {
	  public String identifier;
	  public TypeArgument[] typeArguments;
	  
	  public SimpleClassTypeSignature(String identifier) {
		  this.identifier = identifier;
		  this.typeArguments = new TypeArgument[0];
	  }
	  
	  public SimpleClassTypeSignature(String identifier, TypeArgument[] args) {
		  this.identifier = identifier;
		  this.typeArguments = args;
	  }
	  
	  public String toString() {
		  StringBuffer sb = new StringBuffer();
		  sb.append(identifier);
		  if (typeArguments.length > 0) {
			  sb.append("<");
			  for (int i = 0; i < typeArguments.length; i++) {
				  sb.append(typeArguments[i].toString());
			  }
			  sb.append(">");
		  }
		  return sb.toString();
	  }
  }
  
  static class TypeArgument {
	  public boolean isWildcard = false;
	  public boolean isPlus = false; 
	  public boolean isMinus = false;
	  public FieldTypeSignature signature;  // null if isWildcard
	  
	  public TypeArgument() {
		  isWildcard = true;
	  }
	  
	  public TypeArgument(boolean plus, boolean minus, FieldTypeSignature aSig) {
		  this.isPlus = plus;
		  this.isMinus = minus;
		  this.signature = aSig;
	  }
	  
  }
  
  /**
   * can't ask questions of signature content until it has been parsed.
   */
  private boolean isParsed = false;
  
  private FormalTypeParameter[] formalTypeParameters;
  private ClassTypeSignature superclassSignature;
  private ClassTypeSignature[] superInterfaceSignatures;
  
  private String[] tokenStream;  // for parse in flight
  private int tokenIndex = 0;
  
  public FormalTypeParameter[] getFormalTypeParameters() {
	  if (!isParsed) throw new IllegalStateException("Must parse signature attribute first");
	  return formalTypeParameters;
  }
  
  public ClassTypeSignature getSuperclassSignature() {
	  if (!isParsed) throw new IllegalStateException("Must parse signature attribute first");
	  return superclassSignature;	  
  }
  
  public ClassTypeSignature[] getSuperInterfaceSignatures() {
	  if (!isParsed) throw new IllegalStateException("Must parse signature attribute first");
	  return superInterfaceSignatures;	  	  
  }
  
  /**
   * AMC.
   * Parse the signature string interpreting it as a ClassSignature according to
   * the grammar defined in Section 4.4.4 of the JVM specification.
   */
  public void parseAsClassSignature() {
	  tokenStream = tokenize(getSignature());
	  tokenIndex = 0;
	  // FormalTypeParameters-opt
	  if (maybeEat("<")) {
		List formalTypeParametersList = new ArrayList();
		do {
			formalTypeParametersList.add(parseFormalTypeParameter());
		} while (!maybeEat(">"));
		formalTypeParameters = new FormalTypeParameter[formalTypeParametersList.size()];
		formalTypeParametersList.toArray(formalTypeParameters);
	  }
	  superclassSignature = parseClassTypeSignature();
	  List superIntSigs = new ArrayList();
	  while (tokenIndex < tokenStream.length) {
		  superIntSigs.add(parseClassTypeSignature());
	  }
	  superInterfaceSignatures = new ClassTypeSignature[superIntSigs.size()];
	  superIntSigs.toArray(superInterfaceSignatures);
	  isParsed = true;
  }
  
  /**
   * AMC.
   * Parse the signature string interpreting it as a MethodTypeSignature according to
   * the grammar defined in Section 4.4.4 of the JVM specification.
   */
  public void parseAsMethodSignature() {
	  isParsed = true;	  
  }
  
  /**
   * AMC.
   * Parse the signature string interpreting it as a FieldTypeSignature according to
   * the grammar defined in Section 4.4.4 of the JVM specification.
   */
  public void parseAsFieldSignature() {
	  isParsed = true;  
  }

  private FormalTypeParameter parseFormalTypeParameter() {
	  FormalTypeParameter ftp = new FormalTypeParameter();
	  // Identifier
	  ftp.identifier = eatIdentifier();
	  // ClassBound
	  eat(":");
	  ftp.classBound = parseFieldTypeSignature(true);
	  if (ftp.classBound == null) {
		  ftp.classBound = new ClassTypeSignature("Ljava/lang/Object;","Object");
	  }
	  // Optional InterfaceBounds
	  List optionalBounds = new ArrayList();
	  while (maybeEat(":")) {
		  optionalBounds.add(parseFieldTypeSignature(false));
	  }
	  ftp.interfaceBounds = new FieldTypeSignature[optionalBounds.size()];
	  optionalBounds.toArray(ftp.interfaceBounds);
	  return ftp;
  }
  
  private FieldTypeSignature parseFieldTypeSignature(boolean isOptional) {
	  if (isOptional) {
		  // anything other than 'L', 'T' or '[' and we're out of here
		  if (!tokenStream[tokenIndex].startsWith("L") &&
			   !tokenStream[tokenIndex].startsWith("T") &&
			   !tokenStream[tokenIndex].startsWith("[")) {
			  return null;
		  }
	  }
	  if (maybeEat("[")) {
		  return parseArrayTypeSignature();
	  } else if (tokenStream[tokenIndex].startsWith("L")) {
		  return parseClassTypeSignature();
	  } else if (tokenStream[tokenIndex].startsWith("T")) {
		  return parseTypeVariableSignature();
	  } else {
		  throw new IllegalStateException("Expection [,L, or T, but found " +
				  tokenStream[tokenIndex]);
	  }
  }
  
  private ArrayTypeSignature parseArrayTypeSignature() {
	  // opening [ already eaten
	  eat("["); // grammar adds another one!
	  FieldTypeSignature fieldType = parseFieldTypeSignature(true);
	  if (fieldType != null) {
		  return new ArrayTypeSignature(fieldType);
	  } else {
		  // must be BaseType array
		  return new ArrayTypeSignature(eatIdentifier());
	  }
  }

  // L PackageSpecifier* SimpleClassTypeSignature ClassTypeSignature* ;
  private ClassTypeSignature parseClassTypeSignature() {
	  SimpleClassTypeSignature outerType = null;
	  SimpleClassTypeSignature[] nestedTypes = new SimpleClassTypeSignature[0];
	  StringBuffer ret = new StringBuffer();
	  String identifier = eatIdentifier();
	  ret.append(identifier);
	  while (maybeEat("/")) {
		  ret.append(eatIdentifier());
	  }
	  // now we have either a "." indicating the start of a nested type,
	  // or a "<" indication type arguments, or ";" and we are done.
	  while (!maybeEat(";")) {
		  if (maybeEat(".")) {
			  // outer type completed
			  outerType = new SimpleClassTypeSignature(identifier);
			  List nestedTypeList = new ArrayList();
			  do {
				  ret.append(".");
				  SimpleClassTypeSignature sig = parseSimpleClassTypeSignature();
				  ret.append(sig.toString());
				  nestedTypeList.add(ret);
			  } while(maybeEat("."));
			  nestedTypes = new SimpleClassTypeSignature[nestedTypeList.size()];
			  nestedTypeList.toArray(nestedTypes);
		  } else if (tokenStream[tokenIndex].equals("<")) {
			  ret.append("<");
			  TypeArgument[] tArgs = maybeParseTypeArguments();
			  for (int i=0; i < tArgs.length; i++) {
				  ret.append(tArgs[i].toString());
			  }
			  ret.append(">");
			  outerType = new SimpleClassTypeSignature(identifier,tArgs);
			  // now parse possible nesteds...
			  List nestedTypeList = new ArrayList();
			  while (maybeEat(".")) {
				  ret.append(".");
				  SimpleClassTypeSignature sig = parseSimpleClassTypeSignature();
				  ret.append(sig.toString());
				  nestedTypeList.add(ret);				  
			  }
			  nestedTypes = new SimpleClassTypeSignature[nestedTypeList.size()];
			  nestedTypeList.toArray(nestedTypes);
		  } else {
			  throw new IllegalStateException("Expecting .,<, or ;, but found " + tokenStream[tokenIndex]);
		  }
	  }
	  ret.append(";");
	  return new ClassTypeSignature(ret.toString(),outerType,nestedTypes);
  }
  
  private SimpleClassTypeSignature parseSimpleClassTypeSignature() {
	  String identifier = eatIdentifier();
	  TypeArgument[] tArgs = maybeParseTypeArguments();
	  if (tArgs != null) {
		  return new SimpleClassTypeSignature(identifier,tArgs);
	  } else {
		  return new SimpleClassTypeSignature(identifier);
	  }
  }
  
  private TypeArgument parseTypeArgument() {
	  boolean isPlus = false;
	  boolean isMinus = false;
	  if (maybeEat("*")) {
		  return new TypeArgument();
	  } else if (maybeEat("+")) {
		  isPlus = true;
	  } else if (maybeEat("-")) {
		  isMinus = true;
	  }
	  FieldTypeSignature sig = parseFieldTypeSignature(false);
	  return new TypeArgument(isPlus,isMinus,sig);
  }
  
  private TypeArgument[] maybeParseTypeArguments() {
	  if (maybeEat("<")) {
		  List typeArgs = new ArrayList();
		  do {
			  TypeArgument arg = parseTypeArgument();
			  typeArgs.add(arg);
		  } while(!maybeEat(">"));
		  TypeArgument[] tArgs = new TypeArgument[typeArgs.size()];
		  typeArgs.toArray(tArgs);
		  return tArgs;
	  } else {
		  return null;
	  }
  }
  
  private TypeVariableSignature parseTypeVariableSignature() {
	  TypeVariableSignature tv = new TypeVariableSignature(eatIdentifier());
	  eat(";");
	  return tv;
  }
  
  private boolean maybeEat(String token) {
	  if (tokenStream[tokenIndex].equals(token)) {
		  tokenIndex++;
		  return true;
	  }
	  return false;
  }
  
  private void eat(String token) {
	  if (!tokenStream[tokenIndex].equals(token)) {
		  throw new IllegalStateException("Expecting " + token + " but found " + tokenStream[tokenIndex]);
	  }
	  tokenIndex++;
  }
  
  private String eatIdentifier() {
	  return tokenStream[tokenIndex++];
  }

  private String[] tokenize(String signatureString) {
	  char[] chars = signatureString.toCharArray();
	  int index = 0;
	  List tokens = new ArrayList();
	  StringBuffer identifier = new StringBuffer();
	  do {
		switch (chars[index]) {
			case '<' :
				tokens.add("<");
				break;
			case '>' :
				tokens.add(">");
				break;
			case ':' :
				if (identifier.length() > 0) tokens.add(identifier.toString());
				identifier = new StringBuffer();
				tokens.add(":");
				break;
			case '/' :
				if (identifier.length() > 0) tokens.add(identifier.toString());
				identifier = new StringBuffer();
				tokens.add("/");
				break;
			case ';' :
				if (identifier.length() > 0) tokens.add(identifier.toString());
				identifier = new StringBuffer();
				tokens.add(";");
				break;
			case '^':
				tokens.add("^");
				break;
			case '+':
				tokens.add("+");
				break;
			case '-':
				tokens.add("-");
				break;
			case '*':
				tokens.add("*");
			case '.' :
				if (identifier.length() > 0) tokens.add(identifier.toString());
				identifier = new StringBuffer();
				tokens.add(".");
				break;
			case '(' :
				tokens.add("(");
				break;
			case ')' :
				tokens.add(")");
			case '[' :
				break;
			default : 
				identifier.append(chars[index]);
		}
	  } while(index < chars.length);
	  if (identifier.length() > 0) tokens.add(identifier.toString());
	  String [] tokenArray = new String[tokens.size()];
	  tokens.toArray(tokenArray);
	  return tokenArray;
  }
  
}
