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
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.verifier.utility.Frame;

import  java.io.*;

/**
 * This class represents a stack map attribute used for
 * preverification of Java classes for the <a
 * href="http://java.sun.com/j2me/"> Java 2 Micro Edition</a>
 * (J2ME). This attribute is used by the <a
 * href="http://java.sun.com/products/cldc/">KVM</a> and contained
 * within the Code attribute of a method. See CLDC specification
 * §5.3.1.2
 *
 * @version $Id$
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see     Code
 * @see     StackMapFrame
 * @see     StackMapType
 * 
 * overhauled - Andy Clement 4th April 2006
 */
public final class StackMapTable extends Attribute implements Node {
  private StackMapFrame[] entries; // Table of stack map entries
  
  private int             numberOfEntries;
  // if 'isInPackedState' then the data array needs unpacking into the entries array before use
  private boolean isInPackedState = false;
  private byte[] data;
  
  // temporary
  String dataString;
  
  /**
   * @param nameIdx Index of name
   * @param len Content length in bytes
   * @param map Table of stack map entries
   * @param constantPool Array of constants
   */
  public StackMapTable(int nameIdx, int len,  StackMapFrame[] map, ConstantPool constantPool) {
    super(Constants.ATTR_STACK_MAP_TABLE, nameIdx, len, constantPool);
    setStackMap(map);
    isInPackedState=false;
  }
  
   
  /**
   * Construct object from file stream.
   * @param nameIdx Index of name
   * @param len Content length in bytes
   * @param file Input stream
   * @throws IOException
   * @param constant_pool Array of constants
   */
  StackMapTable(int nameIdx, int len, DataInputStream file, ConstantPool constantPool) throws IOException {
    this(nameIdx, len, (StackMapFrame[])null, constantPool);
    numberOfEntries = file.readUnsignedShort();
//    entries         = new StackMapFrame[numberOfEntries];

    data = new byte[length-2];
    int byteReads = file.read(data);
    isInPackedState = true;
    // assert(bytesRead==length)
    
//    for (int i=0; i < numberOfEntries; i++) {
//      entries[i] = new StackMapFrame(file, constantPool);
//    }
  }
  
  private void ensureUnpacked() {
	  if (!isInPackedState) return;
	  try {
		  
		// temporary
		// construct an int representation for debugging
		StringBuffer sb = new StringBuffer();
		sb.append("StackMapTable  [");
		String zeroes = "000";
		for (int i = 0; i < data.length; i++) {
			int b = (int) data[i];
			if (b<0) b+=256;
			StringBuffer sb2 = new StringBuffer().append(zeroes).append(Integer.toString(b));
			sb.append(sb2.substring(sb2.length()-3)).append(" ");
			if ((sb.length()%60)>55) sb.append("\n");
		}
		dataString = sb.append("]").toString();
		  
  	    ByteArrayInputStream bs = new ByteArrayInputStream(data);
		DataInputStream dis = new DataInputStream(bs);
//		numberOfEntries = (dis.readUnsignedShort());
		entries = new StackMapFrame[numberOfEntries];
		if (debug) System.err.println("Unpacking "+numberOfEntries+" StackFrames");
		int bcoffset=0;
		for (int i=0; i < numberOfEntries; i++) {
			entries[i] = new StackMapFrame(dis, constant_pool);
			bcoffset+=(entries[i].getByteCodeOffset()+(i==0?0:1));
			if (debug) System.err.println("That was at bytecode offset "+bcoffset);
		}
	    dis.close();
	    data = null; // throw it away now
      } catch (IOException e) {
		throw new RuntimeException("Unpacking of StackMapTable attribute failed");
	  }
	  isInPackedState=false;
  }
  
  private static boolean debug = false;

  /**
   * Dump line number table attribute to file stream in binary format.
   *
   * @param file Output file stream
   * @throws IOException
   */ 
  public final void dump(DataOutputStream file) throws IOException
  {
    super.dump(file);
    file.writeShort(numberOfEntries);
    if (isInPackedState) {
        file.write(data);
    } else {
      for(int i=0; i < numberOfEntries; i++)
        entries[i].dump(file);
    }
  }    
   
  /**
   * @return Array of stack map entries
   */  
  public final StackMapFrame[] getStackMap() { ensureUnpacked();return entries; }    

  /**
   * @param map Array of stack map entries
   */
  public final void setStackMap(StackMapFrame[] map) {
    this.data = null;
	this.isInPackedState=false;
    entries = map;
    numberOfEntries = (map == null)? 0 : map.length;
  }

  /**
   * @return String representation.
   */ 
  public final String toString() {
	  
	ensureUnpacked();
	return dataString;
//    StringBuffer buf = new StringBuffer("StackMapTable(");
//
//    for(int i=0; i < numberOfEntries; i++) {
//      buf.append(entries[i].toString());
//
//      if(i < numberOfEntries - 1)
//	buf.append(", ");
//    }
//
//    buf.append(')');
//	
//    return buf.toString();    
  }

  /**
   * @return deep copy of this attribute
   */
  public Attribute copy(ConstantPool constant_pool) {
	ensureUnpacked();
    StackMapTable c = (StackMapTable)clone();

    c.entries = new StackMapFrame[numberOfEntries];
    for(int i=0; i < numberOfEntries; i++)
      c.entries[i] = entries[i].copy();

    c.constant_pool = constant_pool;
    return c;
  }

  /**
   * Called by objects that are traversing the nodes of the tree implicitely
   * defined by the contents of a Java class. I.e., the hierarchy of methods,
   * fields, attributes, etc. spawns a tree of objects.
   *
   * @param v Visitor object
   */
   public void accept(Visitor v) {
	 ensureUnpacked();
     v.visitStackMap(this);
   }

  public final int getMapLength() { return numberOfEntries; }

  /** 
   * Takes a sequence of execution frames and builds a StackMapTable attribute for them
   */
  public static StackMapTable forFrames(Frame[] frames,/* int[] offsets,*/ ConstantPoolGen cpg) {
	 if (debug) System.out.println("> StackMapTable.forFrames(): Building table from a set of frames");
	 if (debug) System.out.print("Offsets:");
	  for (int i = 0; i < frames.length; i++) {
		  if (debug) System.out.print(frames[i].position+" ");
	  }
	  if (debug) System.out.println();
	try {
		StackMapFrame[] smf = new StackMapFrame[frames.length]; // don't need the first frame, calculated from descriptor
		int offset = 0;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		for (int i = 0; i < frames.length; i++) {
			if (i==0) offset= frames[0].position; else offset = frames[i].position-frames[i-1].position-1;
			if (offset<0) {
				throw new RuntimeException();
			} 
			smf[i] = StackMapFrame.forFrame(offset,frames[i],cpg);
			smf[i].dump(dos);
		}
		dos.flush();dos.close();
		int len = baos.toByteArray().length+2;
		int nameIndex = cpg.addUtf8(Constants.ATTRIBUTE_NAMES[Constants.ATTR_STACK_MAP_TABLE]);
		StackMapTable table = new StackMapTable(nameIndex,len,smf,cpg.getConstantPool());
		return table;
	} catch (IOException e) {
		e.printStackTrace();
		throw new RuntimeException();
	}
  }
}
