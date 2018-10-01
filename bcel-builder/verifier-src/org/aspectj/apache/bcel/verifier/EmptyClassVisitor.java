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
import org.aspectj.apache.bcel.classfile.ClassVisitor;
import org.aspectj.apache.bcel.classfile.Code;
import org.aspectj.apache.bcel.classfile.CodeException;
import org.aspectj.apache.bcel.classfile.ConstantClass;
import org.aspectj.apache.bcel.classfile.ConstantDouble;
import org.aspectj.apache.bcel.classfile.ConstantDynamic;
import org.aspectj.apache.bcel.classfile.ConstantFieldref;
import org.aspectj.apache.bcel.classfile.ConstantFloat;
import org.aspectj.apache.bcel.classfile.ConstantInteger;
import org.aspectj.apache.bcel.classfile.ConstantInterfaceMethodref;
import org.aspectj.apache.bcel.classfile.ConstantInvokeDynamic;
import org.aspectj.apache.bcel.classfile.ConstantLong;
import org.aspectj.apache.bcel.classfile.ConstantMethodHandle;
import org.aspectj.apache.bcel.classfile.ConstantMethodType;
import org.aspectj.apache.bcel.classfile.ConstantMethodref;
import org.aspectj.apache.bcel.classfile.ConstantModule;
import org.aspectj.apache.bcel.classfile.ConstantNameAndType;
import org.aspectj.apache.bcel.classfile.ConstantPackage;
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
import org.aspectj.apache.bcel.classfile.Module;
import org.aspectj.apache.bcel.classfile.ModuleMainClass;
import org.aspectj.apache.bcel.classfile.ModulePackages;
import org.aspectj.apache.bcel.classfile.NestHost;
import org.aspectj.apache.bcel.classfile.NestMembers;
import org.aspectj.apache.bcel.classfile.Signature;
import org.aspectj.apache.bcel.classfile.SourceFile;
import org.aspectj.apache.bcel.classfile.StackMap;
import org.aspectj.apache.bcel.classfile.StackMapEntry;
import org.aspectj.apache.bcel.classfile.Synthetic;
import org.aspectj.apache.bcel.classfile.Unknown;
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

  @Override
public void visitCode(Code obj) {}
  @Override
public void visitCodeException(CodeException obj) {}
  @Override
public void visitConstantClass(ConstantClass obj) {}
  @Override
public void visitConstantDouble(ConstantDouble obj) {}
  @Override
public void visitConstantFieldref(ConstantFieldref obj) {}
  @Override
public void visitConstantFloat(ConstantFloat obj) {}
  @Override
public void visitConstantInteger(ConstantInteger obj) {}
  @Override
public void visitConstantInterfaceMethodref(ConstantInterfaceMethodref obj) {}
  @Override
public void visitConstantLong(ConstantLong obj) {}
  @Override
public void visitConstantMethodref(ConstantMethodref obj) {}
  @Override
public void visitConstantMethodHandle(ConstantMethodHandle obj) {}
  @Override
public void visitConstantMethodType(ConstantMethodType obj) {}
  @Override
public void visitConstantInvokeDynamic(ConstantInvokeDynamic obj) {}
  @Override
public void visitConstantNameAndType(ConstantNameAndType obj) {}
  @Override
public void visitConstantPool(ConstantPool obj) {}
  @Override
public void visitConstantString(ConstantString obj) {}
  @Override
public void visitConstantModule(ConstantModule obj) {}
  @Override
public void visitConstantPackage(ConstantPackage obj) {}
  @Override
public void visitConstantUtf8(ConstantUtf8 obj) {}
  @Override
public void visitConstantValue(ConstantValue obj) {}
  @Override
public void visitDeprecated(Deprecated obj) {}
  @Override
public void visitExceptionTable(ExceptionTable obj) {}
  @Override
public void visitField(Field obj) {}
  @Override
public void visitInnerClass(InnerClass obj) {}
  @Override
public void visitInnerClasses(InnerClasses obj) {}
  @Override
public void visitJavaClass(JavaClass obj) {}
  @Override
public void visitLineNumber(LineNumber obj) {}
  @Override
public void visitBootstrapMethods(BootstrapMethods obj) {}
  @Override
public void visitLineNumberTable(LineNumberTable obj) {}
  @Override
public void visitLocalVariable(LocalVariable obj) {}
  @Override
public void visitLocalVariableTable(LocalVariableTable obj) {}
  @Override
public void visitMethod(Method obj) {}
  @Override
public void visitSignature(Signature obj) {}
  @Override
public void visitSourceFile(SourceFile obj) {}
  @Override
public void visitSynthetic(Synthetic obj) {}
  @Override
public void visitUnknown(Unknown obj) {}
  @Override
public void visitStackMap(StackMap obj) {}
  @Override
public void visitStackMapEntry(StackMapEntry obj) {}

  // J5:
  @Override
public void visitEnclosingMethod(EnclosingMethod obj) {}
  @Override
public void visitRuntimeVisibleAnnotations(RuntimeVisAnnos attribute) {}
  @Override
public void visitRuntimeInvisibleAnnotations(RuntimeInvisAnnos attribute) {}
  @Override
public void visitRuntimeVisibleParameterAnnotations(RuntimeVisParamAnnos attribute) {}
  @Override
public void visitRuntimeInvisibleParameterAnnotations(RuntimeInvisParamAnnos attribute) {}
  @Override
public void visitAnnotationDefault(AnnotationDefault attribute) {}
  @Override
public void visitLocalVariableTypeTable(LocalVariableTypeTable obj) {}
  	 
  // J8:
  @Override
public void visitRuntimeVisibleTypeAnnotations(RuntimeVisTypeAnnos attribute) {}
  @Override
public void visitRuntimeInvisibleTypeAnnotations(RuntimeInvisTypeAnnos attribute) {}
  @Override
public void visitMethodParameters(MethodParameters attribute) {}

  // J9:
  @Override
public void visitModule(Module attribute) {}
  @Override
public void visitModulePackages(ModulePackages attribute) {}
  @Override
public void visitModuleMainClass(ModuleMainClass attribute) {}

  // J11:
  @Override public void visitConstantDynamic(ConstantDynamic attribute) {}
  @Override public void visitNestHost(NestHost attribute) { }
  @Override public void visitNestMembers(NestMembers attribute) { }
 
}
