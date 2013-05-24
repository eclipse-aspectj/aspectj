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
package org.aspectj.apache.bcel.verifier;

import org.aspectj.apache.bcel.classfile.AnnotationDefault;
import org.aspectj.apache.bcel.classfile.BootstrapMethods;
import org.aspectj.apache.bcel.classfile.Code;
import org.aspectj.apache.bcel.classfile.CodeException;
import org.aspectj.apache.bcel.classfile.ConstantClass;
import org.aspectj.apache.bcel.classfile.ConstantDouble;
import org.aspectj.apache.bcel.classfile.ConstantFieldref;
import org.aspectj.apache.bcel.classfile.ConstantFloat;
import org.aspectj.apache.bcel.classfile.ConstantInteger;
import org.aspectj.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.aspectj.apache.bcel.classfile.ConstantInvokeDynamic;
import org.aspectj.apache.bcel.classfile.ConstantLong;
import org.aspectj.apache.bcel.classfile.ConstantMethodHandle;
import org.aspectj.apache.bcel.classfile.ConstantMethodType;
import org.aspectj.apache.bcel.classfile.ConstantMethodref;
import org.aspectj.apache.bcel.classfile.ConstantNameAndType;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.ConstantString;
import org.aspectj.apache.bcel.classfile.ConstantUtf8;
import org.aspectj.apache.bcel.classfile.ConstantValue;
import org.aspectj.apache.bcel.classfile.Deprecated;
import org.aspectj.apache.bcel.classfile.EnclosingMethod;
import org.aspectj.apache.bcel.classfile.ExceptionTable;
import org.aspectj.apache.bcel.classfile.Field;
import org.aspectj.apache.bcel.classfile.InnerClass;
import org.aspectj.apache.bcel.classfile.InnerClasses;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.LineNumber;
import org.aspectj.apache.bcel.classfile.LineNumberTable;
import org.aspectj.apache.bcel.classfile.LocalVariable;
import org.aspectj.apache.bcel.classfile.LocalVariableTable;
import org.aspectj.apache.bcel.classfile.LocalVariableTypeTable;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.MethodParameters;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.SourceFile;
import org.aspectj.apache.bcel.classfile.StackMap;
import org.aspectj.apache.bcel.classfile.StackMapEntry;
import org.aspectj.apache.bcel.classfile.Synthetic;
import org.aspectj.apache.bcel.classfile.Unknown;
import org.aspectj.apache.bcel.classfile.ClassVisitor;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisParamAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisTypeAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisParamAnnos;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisTypeAnnos;

/**
 * Visitor with empty method bodies, can be extended and used in conjunction with the
 * DescendingVisitor class, e.g.
 *
 * By courtesy of David Spencer.
 *
 * @see DescendingVisitor
 * @version $Id: EmptyClassVisitor.java,v 1.3 2009/09/15 19:40:22 aclement Exp $
 * 
 */
public class EmptyClassVisitor implements ClassVisitor {
  protected EmptyClassVisitor() { }

  public void visitCode(Code obj) {}
  public void visitCodeException(CodeException obj) {}
  public void visitConstantClass(ConstantClass obj) {}
  public void visitConstantDouble(ConstantDouble obj) {}
  public void visitConstantFieldref(ConstantFieldref obj) {}
  public void visitConstantFloat(ConstantFloat obj) {}
  public void visitConstantInteger(ConstantInteger obj) {}
  public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref obj) {}
  public void visitConstantLong(ConstantLong obj) {}
  public void visitConstantMethodref(ConstantMethodref obj) {}
  public void visitConstantMethodHandle(ConstantMethodHandle obj) {}
  public void visitConstantMethodType(ConstantMethodType obj) {}
  public void visitConstantInvokeDynamic(ConstantInvokeDynamic obj) {}
  public void visitConstantNameAndType(ConstantNameAndType obj) {}
  public void visitConstantPool(ConstantPool obj) {}
  public void visitConstantString(ConstantString obj) {}
  public void visitConstantUtf8(ConstantUtf8 obj) {}
  public void visitConstantValue(ConstantValue obj) {}
  public void visitDeprecated(Deprecated obj) {}
  public void visitExceptionTable(ExceptionTable obj) {}
  public void visitField(Field obj) {}
  public void visitInnerClass(InnerClass obj) {}
  public void visitInnerClasses(InnerClasses obj) {}
  public void visitJavaClass(JavaClass obj) {}
  public void visitLineNumber(LineNumber obj) {}
  public void visitBootstrapMethods(BootstrapMethods obj) {}
  public void visitLineNumberTable(LineNumberTable obj) {}
  public void visitLocalVariable(LocalVariable obj) {}
  public void visitLocalVariableTable(LocalVariableTable obj) {}
  public void visitMethod(Method obj) {}
  public void visitSignature(Signature obj) {}
  public void visitSourceFile(SourceFile obj) {}
  public void visitSynthetic(Synthetic obj) {}
  public void visitUnknown(Unknown obj) {}
  public void visitStackMap(StackMap obj) {}
  public void visitStackMapEntry(StackMapEntry obj) {}
  
  // J5SUPPORT:
  public void visitEnclosingMethod(EnclosingMethod obj) {}
  public void visitRuntimeVisibleAnnotations(RuntimeVisAnnos attribute) {}
  public void visitRuntimeInvisibleAnnotations(RuntimeInvisAnnos attribute) {}
  public void visitRuntimeVisibleParameterAnnotations(RuntimeVisParamAnnos attribute) {}
  public void visitRuntimeInvisibleParameterAnnotations(RuntimeInvisParamAnnos attribute) {}
  public void visitAnnotationDefault(AnnotationDefault attribute) {}
  public void visitLocalVariableTypeTable(LocalVariableTypeTable obj) {}
  	 
  // J8SUPPORT:
  public void visitRuntimeVisibleTypeAnnotations(RuntimeVisTypeAnnos attribute) {}
  public void visitRuntimeInvisibleTypeAnnotations(RuntimeInvisTypeAnnos attribute) {}
  public void visitMethodParameters(MethodParameters attribute) {}

}
