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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.aspectj.apache.bcel.Constants;

/**
 * Wrapper class that parses a given Java .class file. The method <A
 * href ="#parse">parse</A> returns a <A href ="JavaClass.html">
 * JavaClass</A> object on success. When an I/O error or an
 * inconsistency occurs an appropiate exception is propagated back to
 * the caller.
 *
 * The structure and the names comply, except for a few conveniences,
 * exactly with the <A href="ftp://java.sun.com/docs/specs/vmspec.ps">
 * JVM specification 1.0</a>. See this paper for
 * further details about the structure of a bytecode file.
 *
 * @version $Id: ClassParser.java,v 1.6 2008/05/30 17:29:14 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A> 
 */
public final class ClassParser {
  private DataInputStream file;
  private String          filename;
  private int             classnameIndex;
  private int             superclassnameIndex;
  private int             major, minor; 
  private int             accessflags;
  private int[]           interfaceIndices;
  private ConstantPool    cpool;
  private Field[]         fields;
  private Method[]        methods;
  private Attribute[]     attributes;

  private static final int BUFSIZE = 8192;

  /** Parse class from the given stream */
  public ClassParser(InputStream file, String filename) {
    this.filename = filename;
    if (file instanceof DataInputStream) this.file = (DataInputStream)file;
    else                                 this.file = new DataInputStream(new BufferedInputStream(file,BUFSIZE));
  }

  public ClassParser(ByteArrayInputStream baos, String filename) {
	    this.filename = filename;
	    this.file = new DataInputStream(baos);
  }

  /** Parse class from given .class file */
  public ClassParser(String file_name) throws IOException {    
    this.filename = file_name;
    file = new DataInputStream(new BufferedInputStream(new FileInputStream(file_name),BUFSIZE));
  }

  /**
   * Parse the given Java class file and return an object that represents
   * the contained data, i.e., constants, methods, fields and commands.
   * A <em>ClassFormatException</em> is raised, if the file is not a valid
   * .class file. (This does not include verification of the byte code as it
   * is performed by the java interpreter).
   */    
  public JavaClass parse() throws IOException, ClassFormatException {
    /****************** Read headers ********************************/
    // Check magic tag of class file
    readID();

    // Get compiler version
    readVersion();

    /****************** Read constant pool and related **************/
    // Read constant pool entries
    readConstantPool();

    // Get class information
    readClassInfo();

    // Get interface information, i.e., implemented interfaces
    readInterfaces();

    /****************** Read class fields and methods ***************/ 
    // Read class fields, i.e., the variables of the class
    readFields();

    // Read class methods, i.e., the functions in the class
    readMethods();

    // Read class attributes
    readAttributes();

    // Read everything of interest, so close the file
    file.close();

    // Return the information we have gathered in a new object
    JavaClass jc= new JavaClass(classnameIndex, superclassnameIndex, 
			 filename, major, minor, accessflags,
			 cpool, interfaceIndices, fields,
			 methods, attributes);
    return jc;
  }
  
  /** Read information about the attributes of the class */
  private final void readAttributes() {
	  attributes = AttributeUtils.readAttributes(file,cpool);
  }

  /** Read information about the class and its super class */
  private final void readClassInfo() throws IOException {
    accessflags = file.readUnsignedShort();

    /* Interfaces are implicitely abstract, the flag should be set
     * according to the JVM specification */
    if((accessflags & Constants.ACC_INTERFACE) != 0)
      accessflags |= Constants.ACC_ABSTRACT;

    // don't police it like this... leave higher level verification code to check it.
//    if(((access_flags & Constants.ACC_ABSTRACT) != 0) && 
//       ((access_flags & Constants.ACC_FINAL)    != 0 ))
//      throw new ClassFormatException("Class can't be both final and abstract");

    classnameIndex      = file.readUnsignedShort();
    superclassnameIndex = file.readUnsignedShort();
  }
  
  private final void readConstantPool() throws IOException {
    try {
		cpool = new ConstantPool(file);
	} catch (ClassFormatException cfe) {
		// add some context if we can
		cfe.printStackTrace();
		if (filename!=null) {
			String newmessage = "File: '"+filename+"': "+cfe.getMessage();
			throw new ClassFormatException(newmessage); // this loses the old stack trace but I dont think that matters!
		}
		throw cfe;
	}
  }    

  /** Read information about the fields of the class */
  private final void readFields() throws IOException, ClassFormatException {
    int fieldCount = file.readUnsignedShort();
    if (fieldCount == 0) {
    	fields = Field.NoFields;
    } else {
    	fields = new Field[fieldCount];
    	for(int i=0; i < fieldCount; i++)
    		fields[i] = new Field(file, cpool);
    }
  }    

  /** Check whether the header of the file is ok. Of course, this has 
   *  to be the first action on successive file reads */
  private final void readID() throws IOException {
    int magic = 0xCAFEBABE;
    if (file.readInt() != magic) 
      throw new ClassFormatException(filename + " is not a Java .class file");
  }   
  
  private static final int[] NO_INTERFACES = new int[0];

  /** Read information about the interfaces implemented by this class */
  private final void readInterfaces() throws IOException {
    int interfacesCount = file.readUnsignedShort();
    if (interfacesCount==0) {
    	interfaceIndices = NO_INTERFACES;
    } else {
	    interfaceIndices = new int[interfacesCount];
	    for(int i=0; i < interfacesCount; i++)
	      interfaceIndices[i] = file.readUnsignedShort();
    }
  }     
  
  /** Read information about the methods of the class */
  private final void readMethods() throws IOException {
    int methodsCount = file.readUnsignedShort();
    if (methodsCount==0) {
    	methods = Method.NoMethods;
    } else {
	    methods = new Method[methodsCount];
	    for(int i=0; i < methodsCount; i++)
	      methods[i] = new Method(file, cpool);
    }
  }      
  
  /** Read major and minor version of compiler which created the file */
  private final void readVersion() throws IOException {
    minor = file.readUnsignedShort();
    major = file.readUnsignedShort();
  }    
  
}
