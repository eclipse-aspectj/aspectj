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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.verifier.utility.Frame;

/**
 * This class represents a stack map entry recording the types of
 * local variables and the the of stack items at a given byte code offset.
 * See CLDC specification §5.3.1.2
 *
 * @version $Id$
 * @author  <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @see     StackMapTable
 * @see     StackMapType
 */
public final class StackMapFrame implements Cloneable, Node {

  // XXX move to bcel constants
  public static final byte SameFrameKind = 0;
  public static final byte SameLocalsOneStackItemFrameKind = 1;
  public static final byte RESERVEDKind = 2;
  public static final byte SameLocalsOneStackItemFrameExtendedKind = 3;
  public static final byte ChopFrameKind = 4;
  public static final byte SameFrameExtendedKind = 5;
  public static final byte AppendFrameKind = 6;
  public static final byte FullFrameKind = 7;
  public static final byte UnknownKind = 8;
  
  private byte kind = UnknownKind;
  
  private static String[] ALL_KINDS = { 
	  "SameFrame","SameLocalsOneStackItemFrame","RESERVED",
	  "SameLocalsOneStackItemFrameExtended","ChopFrame",
	  "SameFrameExtended","AppendFrame","FullFrame","???"
  };
  
  /**
   * 000..063 => SameFrame
   * 064..127 => SameLocalsOneStackItemFrame
   * 128..246 => RESERVED
   * 247      => SameLocalsOneStackItemFrameExtended
   * 248..250 => ChopFrame
   * 251      => SameFrameExtended
   * 252..254 => AppendFrame
   * 255      => FullFrame
   */
  private int           tag;
  
  private int            offsetDelta;
  
  private int            localCount=0;
  private int            stackCount=0;
  private StackMapType[] localTypes;
  private StackMapType[] stackTypes;
  private ConstantPool   cpool;

  
  // temporary
  boolean built = false;
  /**
   * Build a StackMapFrame from the file stream contents
   * @param is Data input stream
   * @throws IOException
   */
  StackMapFrame(DataInputStream is, ConstantPool constantPool) throws IOException {
	cpool = constantPool;
    tag   = is.readUnsignedByte();
    if (tag<64)          { // SameFrame
	    	// same locals as before, stack height is zero
	    	kind = SameFrameKind;
	    	offsetDelta=tag;
	    	if (debug) System.err.println("Loading SameFrame: offset="+offsetDelta);
    } else if (tag<128)  { // SameLocalsOneStackItemFrame
	    	kind = SameLocalsOneStackItemFrameKind;
	    	offsetDelta = tag-64;
	    	if (debug) System.err.println("Loading SameLocalsOneStackItemFrame: offset="+offsetDelta);
	    	stackCount=1;
	    	stackTypes=new StackMapType[1];
	    	stackTypes[0]=new StackMapType(is,constantPool);
    } else if (tag<247)  { // RESERVED	
	    	kind=RESERVEDKind;
	    	if (debug) System.err.println("Loading reserved");
    } else if (tag==247) { // SameLocalsOneStackItemFrameExtended
	    	kind=SameLocalsOneStackItemFrameExtendedKind;
	    	offsetDelta = is.readUnsignedShort();
	    	if (debug) System.err.println("Loading SameLocalsOneStackItemFrameExtended: offset="+offsetDelta);
	    	stackCount=1;
	    	stackTypes=new StackMapType[1];
	    	stackTypes[0]=new StackMapType(is,constantPool);    	
    } else if (tag<251)  { // ChopFrame
	    	kind=ChopFrameKind;
	    	if (debug) System.err.println("Loading ChopFrame");
	    	offsetDelta = is.readUnsignedShort();
	    	localCount=251-tag; // locals removed from previous frame
    } else if (tag==251) { // SameFrameExtended
	    	kind=SameFrameExtendedKind;
	    	// same locals as before, stack height is zero
	    	offsetDelta = is.readUnsignedShort();
	    	if (debug) System.err.println("Loading SameFrameExtended: offset="+offsetDelta);
    } else if (tag<255)  { // AppendFrame
	    	kind=AppendFrameKind;
	    	offsetDelta = is.readUnsignedShort();
	    	localCount     = tag - 251;
	    	stackCount = 0; // implied
	    	if (debug) System.err.println("Loading AppendFrame, number of locals="+localCount+" offset="+offsetDelta);
	    	localTypes = new StackMapType[localCount];
	    	for(int i=0;i<localCount;i++) {
	    		localTypes[i] = new StackMapType(is,constantPool);
	    		if (debug) System.err.println("  Position ["+i+"] = "+localTypes[i]);
	    	}
    } else if (tag==255) { // FullFrame
	    	kind=FullFrameKind;
	    	if (debug) System.err.println("Loading FullFrame...");
	    	offsetDelta = is.readUnsignedShort();
	    	localCount = is.readUnsignedShort();
	    	localTypes = new StackMapType[localCount];
	    	for(int i=0;i<localCount;i++) {
	    		localTypes[i] = new StackMapType(is,constantPool);
	    		if (debug) System.err.println("  Locals ["+i+"] = "+localTypes[i]);
	    	}
	    	stackCount   = is.readUnsignedShort();
	    	stackTypes = new StackMapType[stackCount];
	    	for(int i=0;i<stackCount;i++) {
	    		stackTypes[i] = new StackMapType(is,constantPool);
	    		if (debug) System.err.println("  Stack ["+i+"] = "+stackTypes[i]);
	    	}
	    	if (debug) System.err.println("Loaded FullFrame, offset="+offsetDelta+"  number of locals="+localCount+"  number of stack items="+stackCount);
    }
  }
  
  private boolean debug = false;

  public StackMapFrame(int byte_code_offset, int number_of_locals,
		       StackMapType[] types_of_locals,
		       int number_of_stack_items,
		       StackMapType[] types_of_stack_items,
		       ConstantPool constant_pool) {
    this.offsetDelta = byte_code_offset;
    this.localCount = number_of_locals;
    this.localTypes = types_of_locals;
    this.stackCount = number_of_stack_items;
    this.stackTypes = types_of_stack_items;
    this.cpool = constant_pool;
  }

  /**
   * Dump stack map entry
   *
   * @param file Output file stream
   * @throws IOException
   */ 
  public final void dump(DataOutputStream file) throws IOException
  {
	  // all frames are full ones!
	  file.writeByte(255);
    file.writeShort(offsetDelta);

    file.writeShort(localTypes.length);//localCount);
    for(int i=0; i < localTypes.length; i++) {
      localTypes[i].dump(file);
    }

    file.writeShort(stackTypes.length);
    for(int i=0; i < stackTypes.length; i++)
      stackTypes[i].dump(file);
  }

  /**
   * @return String representation.
   */ 
  public final String toString() {
//	  if (!built) return "Unfinished StackMapFrame, tag="+Integer.toString(tag);
	  StringBuffer buf = new StringBuffer(getKindString()+" (offset=" + offsetDelta);
	if (localCount!=0 && localTypes==null && kind!=ChopFrameKind) {
    	  System.err.println("BADLY FORMED STACKMAPFRAME: "+getKindString()+"  locals size is "+localCount+" but localtypes is null");
    }
	  
	// For a 'ChopFrame' - the localCount is the number to remove, so the localTypes field stays null.
    if (kind==ChopFrameKind) {
    	  buf.append(" chopping "+localCount+" local").append(localCount>1?"s":"");
    } else {
	    
	    if(localCount > 0) {
	      buf.append(", LOCALS["+localCount+"]={");
	      for(int i=0; i < localCount; i++) {
			buf.append(localTypes[i]);
			if(i < localCount - 1)
			  buf.append(", ");
	      }
	      buf.append("}");
	    }
    }

    if(stackCount > 0) {
      buf.append(", STACK["+stackCount+"]={");
      for(int i=0; i < stackCount; i++) {
		buf.append(stackTypes[i]);
		if(i < stackCount - 1)
		  buf.append(", ");
      }

      buf.append("}");
    }

    buf.append(")");

    return buf.toString();    
  }


  public void           setByteCodeOffset(int b)               { offsetDelta = b; }
  public int            getByteCodeOffset()                    { return offsetDelta; }
  public void           setNumberOfLocals(int n)               { localCount = n; }
  public int            getNumberOfLocals()                    { return localCount; }
  public void           setTypesOfLocals(StackMapType[] t)     { localTypes = t; }
  public StackMapType[] getTypesOfLocals()                     { return localTypes; }
  public void           setNumberOfStackItems(int n)           { stackCount = n; }
  public int            getNumberOfStackItems()                { return stackCount; }
  public void           setTypesOfStackItems(StackMapType[] t) { stackTypes = t; }
  public StackMapType[] getTypesOfStackItems()                 { return stackTypes; }

  /**
   * @return deep copy of this object
   */
  public StackMapFrame copy() {
    try {
      return (StackMapFrame)clone();
    } catch(CloneNotSupportedException e) {}

    return null;
  }

  /**
   * Called by objects that are traversing the nodes of the tree implicitely
   * defined by the contents of a Java class. I.e., the hierarchy of methods,
   * fields, attributes, etc. spawns a tree of objects.
   *
   * @param v Visitor object
   */
  public void accept(Visitor v) {
    v.visitStackMapEntry(this);
  }
  
  public String getKindString() {
	  return ALL_KINDS[kind];
  }

  /**
   * @return Constant pool used by this object.
   */   
  public final ConstantPool getConstantPool() { return cpool; }

  /**
   * @param constant_pool Constant pool to be used for this object.
   */   
  public final void setConstantPool(ConstantPool constant_pool) {
    this.cpool = constant_pool;
  }

	public int getKind() {
		return kind;
	}

	public static StackMapFrame forFrame(int offset, Frame frame, ConstantPoolGen cpg) {
		//System.out.println("? StackMapFrame.forFrame(): new frame at offset "+offset+": locals="+frame.getLocals().toCompactString());
		StackMapFrame smf = 
			new StackMapFrame(offset,
					frame.getLocals().maxLocals(),
					frame.getLocalsAsStackMapTypes(cpg), 
					frame.getStack().maxStack(),
					frame.getStackAsStackMapTypes(cpg),
					cpg.getConstantPool());
		return smf;
	}
}
