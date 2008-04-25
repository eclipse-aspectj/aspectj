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

import  org.aspectj.apache.bcel.Constants;

/**
 * Super class for all objects that have modifiers like private, final, ...
 * I.e. classes, fields, and methods.
 *
 * @version $Id: AccessFlags.java,v 1.2.10.2 2008/04/25 17:55:37 aclement Exp $
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 */
public abstract class AccessFlags implements java.io.Serializable {
  protected int accessflags;
  
  public AccessFlags() {}
  
  public AccessFlags(int a) {
    accessflags = a;
  }

  public final int getAccessFlags() { return accessflags; }
  public final int getModifiers()  { return accessflags; }

  public final void setAccessFlags(int access_flags) {
    this.accessflags = access_flags;
  }

  public final void setModifiers(int access_flags) {
    setAccessFlags(access_flags);
  }

  private final void setFlag(int flag, boolean set) {
    if((accessflags & flag) != 0) { // Flag is set already
      if(!set) // Delete flag ?
    	  accessflags ^= flag;
    } else {   // Flag not set
      if(set)  // Set flag ?
    	  accessflags |= flag;
    }
  }

  public final void isPublic(boolean flag) { setFlag(Constants.ACC_PUBLIC, flag); }
  public final boolean isPublic() {
    return (accessflags & Constants.ACC_PUBLIC) != 0;
  }

  public final void isPrivate(boolean flag) { setFlag(Constants.ACC_PRIVATE, flag); }
  public final boolean isPrivate() {
    return (accessflags & Constants.ACC_PRIVATE) != 0;
  }

  public final void isProtected(boolean flag) { setFlag(Constants.ACC_PROTECTED, flag); }
  public final boolean isProtected() {
    return (accessflags & Constants.ACC_PROTECTED) != 0;
  }

  public final void isStatic(boolean flag) { setFlag(Constants.ACC_STATIC, flag); }
  public final boolean isStatic() {
    return (accessflags & Constants.ACC_STATIC) != 0;
  }

  public final void isFinal(boolean flag) { setFlag(Constants.ACC_FINAL, flag); }
  public final boolean isFinal() {
    return (accessflags & Constants.ACC_FINAL) != 0;
  }

  public final void isSynchronized(boolean flag) { setFlag(Constants.ACC_SYNCHRONIZED, flag); }
  public final boolean isSynchronized() {
    return (accessflags & Constants.ACC_SYNCHRONIZED) != 0;
  }

  public final void isVolatile(boolean flag) { setFlag(Constants.ACC_VOLATILE, flag); }
  public final boolean isVolatile() {
    return (accessflags & Constants.ACC_VOLATILE) != 0;
  }

  public final void isTransient(boolean flag) { setFlag(Constants.ACC_TRANSIENT, flag); }
  public final boolean isTransient() {
    return (accessflags & Constants.ACC_TRANSIENT) != 0;
  }

  public final void isNative(boolean flag) { setFlag(Constants.ACC_NATIVE, flag); }
  public final boolean isNative() {
    return (accessflags & Constants.ACC_NATIVE) != 0;
  }

  public final void isInterface(boolean flag) { setFlag(Constants.ACC_INTERFACE, flag); }
  public final boolean isInterface() {
    return (accessflags & Constants.ACC_INTERFACE) != 0;
  }

  public final void isAbstract(boolean flag) { setFlag(Constants.ACC_ABSTRACT, flag); }
  public final boolean isAbstract() {
    return (accessflags & Constants.ACC_ABSTRACT) != 0;
  }

  public final void isStrictfp(boolean flag) { setFlag(Constants.ACC_STRICT, flag); }
  public final boolean isStrictfp() {
    return (accessflags & Constants.ACC_STRICT) != 0;
  }
  
  public final void isVarargs(boolean flag) { setFlag(Constants.ACC_VARARGS, flag); }
  public final boolean isVarargs() {
    return (accessflags & Constants.ACC_VARARGS) != 0;
  }

  public final void isBridge(boolean flag) { setFlag(Constants.ACC_BRIDGE, flag); }
  public final boolean isBridge() {
    return (accessflags & Constants.ACC_BRIDGE) != 0;
  }
}
