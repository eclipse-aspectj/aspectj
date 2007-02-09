package org.aspectj.apache.bcel.verifier.utility;


import java.util.ArrayList;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ConstantClass;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.classfile.StackMapFrame;
import org.aspectj.apache.bcel.classfile.StackMapType;
import org.aspectj.apache.bcel.generic.ArrayType;
import org.aspectj.apache.bcel.generic.BasicType;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.apache.bcel.generic.Type.TypeHolder;

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


/**
 * This class represents a JVM execution frame; that means, a local variable array and an operand stack.
 *
 * @version $Id$
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
 
public class Frame {

	/**
	 * For instance initialization methods, it is important to remember
	 * which instance it is that is not initialized yet. It will be
	 * initialized invoking another constructor later.
	 * NULL means the instance already *is* initialized.
	 */
	protected static UninitializedObjectType _this;

	private LocalVariables locals;
	private OperandStack stack;
	
	public int position; // used for stackmapframe creation 
	
	// ---
	public Frame(int maxLocals, int maxStack){
		locals = new LocalVariables(maxLocals);
		stack  = new OperandStack(maxStack);
	}
	
	public Frame(LocalVariables locals, OperandStack stack){
		this.locals = locals;
		this.stack = stack;
	}
	
	// ===
	
	protected Object clone() {
		Frame f = new Frame(locals.getClone(), stack.getClone());
		return f;
	}

	public Frame getClone() {
		return (Frame) clone();
	}

	public LocalVariables getLocals() { return locals; }
	public OperandStack getStack()    { return stack;  }
	
	public boolean equals(Object o){
		if (!(o instanceof Frame)) return false; // implies "null" is non-equal.
		Frame f = (Frame) o;
		return this.stack.equals(f.stack) && this.locals.equals(f.locals);
	}

	/**
	 * Returns a String representation of the Frame instance.
	 */
	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("FRAMELOCS"+locals.toCompactString()).append("\n");
		sb.append("FRAMESTAK"+stack.toCompactString()).append("\n");
		return sb.toString();
	}

	public String toString(String prefix){
		StringBuffer sb = new StringBuffer();
		sb.append(prefix).append("FRAMELOCS"+locals.toCompactString()).append("\n");
		sb.append(prefix).append("FRAMESTAK"+stack.toCompactString());
		return sb.toString();
	}
	
	private int addLocal(int pos,StackMapType smType) {		
	   byte itemkey = smType.getType();
	   ConstantPool cp = smType.getConstantPool();
	   if (debug) System.out.println(">>"+Constants.ITEM_NAMES[itemkey]);
	   switch (itemkey) { 
	   
	   	   // single slot items
       	   case Constants.ITEM_Integer:
	    	       locals.set(pos++, BasicType.INT);
	    	       return 1;
	       case Constants.ITEM_Object:
		    	   ConstantClass c = (ConstantClass)cp.getConstant(smType.getIndex(), Constants.CONSTANT_Class);
		    	   String sig = c.getBytes(cp);
		    	   if (sig.charAt(0)!='[') sig = "L"+sig+";";
		    	   TypeHolder th = Type.getTypeInternal(sig);
		    	   locals.set(pos++,th.getType());//new ObjectType(c.getBytes(cp)));
		    	   return 1;
	    	   
		   case Constants.ITEM_Top:
			   locals.set(pos++,Type.TOP/*Top - why is it not Top? */);return 1;
			   
			   
		   case Constants.ITEM_Float:
		   case Constants.ITEM_Null:
		   case Constants.ITEM_UninitializedThis:
		   case Constants.ITEM_Uninitialized:
			   
		   // double slot items
		   case Constants.ITEM_Double:
		   case Constants.ITEM_Long:
			   throw new RuntimeException("Frame.addLocal() - need to implement support for "+Constants.ITEM_NAMES[itemkey]);
	   }
	   return 0;
	}

	private int addStack(StackMapType smType) {		
	   byte itemkey = smType.getType();
	   ConstantPool cp = smType.getConstantPool();
	   if (debug) System.out.println(">>"+Constants.ITEM_NAMES[itemkey]);
	   switch (itemkey) { 
	   
	   	   // single slot items
       	   case Constants.ITEM_Integer:
    	       stack.push(BasicType.INT);
    	       return 1;
	       case Constants.ITEM_Object:
	    	   ConstantClass c = (ConstantClass)cp.getConstant(smType.getIndex(), Constants.CONSTANT_Class);
	    	   stack.push(new ObjectType(c.getBytes(cp)));
	    	   return 1;
		   case Constants.ITEM_Top:
		   case Constants.ITEM_Float:
		   case Constants.ITEM_Null:
		   case Constants.ITEM_UninitializedThis:
		   case Constants.ITEM_Uninitialized:
			   
		   // double slot items
		   case Constants.ITEM_Double:
		   case Constants.ITEM_Long:			  
			   throw new RuntimeException("Frame.addStack() - need to implement support for "+Constants.ITEM_NAMES[itemkey]);

	   }
	   return 0;
	}


	private boolean debug=true;
	/** Applies a StackMapFrame to this Frame */
	public boolean apply(StackMapFrame frame) {
	  if (debug) System.out.println(">> Applying a stackmapframe: "+frame);
	  int newLocalEntryCount,newStackEntryCount;
	  StackMapType[] localEntries,stackEntries;
	  switch (frame.getKind()) {
	    
	    // Same locals, empty stack
	    case StackMapFrame.SameFrameKind: stack.clear();return true;
	    
	    // Same locals, empty stack
	    case StackMapFrame.SameFrameExtendedKind: stack.clear();return true;
	    
	    // Empty stack, additional locals specified
	    case StackMapFrame.AppendFrameKind:
		   newLocalEntryCount = frame.getNumberOfLocals();
		   localEntries = frame.getTypesOfLocals();
		   if (debug) System.out.println("AppendFrame - "+newLocalEntryCount+" new locals");
		   int pos    = locals.getNextUnset();
		   locals.haveSet(newLocalEntryCount+pos);
		   for (int i = 0; i < newLocalEntryCount; i++) pos+=addLocal(pos,localEntries[i]);
		   stack.clear();
		   return true;
		   
		   
	    case StackMapFrame.FullFrameKind:
	       newLocalEntryCount = frame.getNumberOfLocals();
	       newStackEntryCount = frame.getNumberOfStackItems();
		   localEntries = frame.getTypesOfLocals();
		   locals.flush();
		   stackEntries = frame.getTypesOfStackItems();
		   if (debug) System.out.println("FullFrame - "+newLocalEntryCount+" new locals");
		   if (debug) System.out.println("Fixing locals");
		   pos=0;
		   for (int i = 0; i < newLocalEntryCount; i++) pos+=addLocal(pos,localEntries[i]);
		   stack.clear();
		   locals.haveSet(newLocalEntryCount);
		   if (debug) System.out.println("Fixing stack");
		   for (int i = 0; i < newStackEntryCount; i++) pos+=addStack(stackEntries[i]);
		   return true;
		   
	    case StackMapFrame.SameLocalsOneStackItemFrameKind:
	    	  stack.clear();
	    	  stackEntries = frame.getTypesOfStackItems();
	    	  if (stackEntries.length!=1) throw new RuntimeException("Assertion failed: should be length 1 but is length "+stackEntries.length);
	    	  // assert(stackEntries.length==1)
	    	  addStack(stackEntries[0]);
	    	  return true;
	    	  
	    case StackMapFrame.ChopFrameKind:
	    	  stack.clear();
	    	  // XXX is this the last X number of known ones or the last X of the maxLocals() number?
	    	  // From methodTypesToSignature in Utility - there are two chop frames, so it it must be chopping 'the last X we currently know about'
	    	  pos = locals.getNextUnset();
	    	  if (pos==-1) pos=locals.maxLocals();
	    	  locals.haveSet(locals.getNextUnset()-frame.getNumberOfLocals());
	    	  for (int j=0;j<frame.getNumberOfLocals();j++) {
	    		  locals.set(pos-j-1, Type.UNKNOWN);
	    	  }
	    	  return true;
		   
		   default:
			   throw new RuntimeException("Frame.apply() - you need to implement support for "+frame.getKindString());
//			   System.err.println("DUNNO > "+frame.getKindString());
	  }
	}
	
	private StackMapType getTheType(Type type,ConstantPoolGen cpg) {
//		System.out.println("> Frame.getTheType(): type = "+type);
		byte tag = type.getType();
		StackMapType smt = null;
		int pos = -1;
		switch (tag) {
			case Constants.T_INT:    
				smt = new StackMapType(Constants.ITEM_Integer,-1,cpg.getConstantPool());
				break;
			case Constants.T_OBJECT: 
				if (type==Type.NULL) {
					smt = new StackMapType(Constants.ITEM_Null,-1,cpg.getConstantPool());
				} else {
					pos = cpg.addClass((ObjectType)type);
					smt = new StackMapType(Constants.ITEM_Object,pos,cpg.getConstantPool());
				}
				break;
			case Constants.T_UNKNOWN:
				UninitializedObjectType uot = (UninitializedObjectType)type;
				if (uot.isThis()) {
					smt = new StackMapType(Constants.ITEM_UninitializedThis,-1,cpg.getConstantPool());
				} else {
//					pos = cpg.addClass(uot.getInitialized());
					smt = new StackMapType(Constants.ITEM_Uninitialized,uot.getLoc(),cpg.getConstantPool());
				}
				break;
			case Constants.T_ARRAY:
				ArrayType aType = (ArrayType)type;
				pos = cpg.addClass(type.getSignature());
				smt = new StackMapType(Constants.ITEM_Object,pos,cpg.getConstantPool());
				break;
				
			case Constants.T_TOP:
				smt = new StackMapType(Constants.ITEM_Top,-1,cpg.getConstantPool());
				break;
				
			case Constants.T_FLOAT:
				smt = new StackMapType(Constants.ITEM_Float,-1,cpg.getConstantPool());
				break;
				
			case Constants.T_LONG:
				smt = new StackMapType(Constants.ITEM_Long,-1,cpg.getConstantPool());
				break;

			case Constants.T_DOUBLE:
				smt = new StackMapType(Constants.ITEM_Double,-1,cpg.getConstantPool());
				break;
				
			default:
				throw new RuntimeException("Frame.getTheType(): Dont know about tag "+tag);
		}
		return smt;
	}
	public StackMapType[] getLocalsAsStackMapTypes(ConstantPoolGen cpg) {
//		StackMapType[] smts = new StackMapType[locals.maxLocals()];
		List l = new ArrayList();
		for (int i = 0; i < locals.maxLocals(); i++) {
			Type type = (Type)locals.get(i);
			l.add(getTheType(type, cpg));
			if (type.getSize()==2) {
				// can skip the TOP that follows ... yes?
				i++;
				if (locals.get(i)!=Type.TOP) {
					throw new RuntimeException("Bang! should be TOP but is "+locals.get(i));
				}
			}
		}
		return (StackMapType[])l.toArray(new StackMapType[]{});
		
//		return smts;
	}

	public StackMapType[] getStackAsStackMapTypes(ConstantPoolGen cpg) {
		List l = new ArrayList();
		for (int i = 0; i < stack.slotsUsed(); i++) {
			Type type = (Type)stack.get(i);
			l.add(getTheType(type, cpg));
			if (type.getSize()==2) {
				// can skip the TOP that follows ... yes?
				i++; 
				try {
				if (stack.get(i)!=Type.TOP) {
					throw new RuntimeException("Bang! should be TOP but is "+locals.get(i));
				}
				} catch (IndexOutOfBoundsException iiobe) {
					int stop = 1;
				}
			}
		}
		return (StackMapType[])l.toArray(new StackMapType[]{});
		
//		StackMapType[] smts = new StackMapType[stack.slotsUsed()];
//		for (int i = 0; i < stack.size(); i++) {
//			smts[i] = getTheType((Type)stack.get(i),cp);
//		}
//		return smts;
	}

}
