package org.aspectj.apache.bcel.verifier.utility;

import java.util.HashSet;
import java.util.Iterator;

import org.aspectj.apache.bcel.generic.ASTORE;
import org.aspectj.apache.bcel.generic.IndexedInstruction;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.JsrInstruction;
import org.aspectj.apache.bcel.generic.LocalVariableInstruction;
import org.aspectj.apache.bcel.generic.RET;
import org.aspectj.apache.bcel.verifier.exc.AssertionViolatedException;
import org.aspectj.apache.bcel.verifier.exc.StructuralCodeConstraintException;

class SubroutineImpl implements Subroutine {

	private final Subroutines subroutines;

	/**
	 * UNSET, a symbol for an uninitialized localVariable
	 * field. This is used for the "top-level" Subroutine;
	 * i.e. no subroutine.
	 */
	private static final int UNSET = -1;

	/** The instructions that belong to this subroutine. */
	private HashSet instructions = new HashSet(); // Elements: InstructionHandle

	/**
	 * The Local Variable slot where the first
	 * instruction of this subroutine (an ASTORE) stores
	 * the JsrInstruction's ReturnAddress in and
	 * the RET of this subroutine operates on.
	 */
	int localVariable = UNSET;

	/**
	 * The JSR or JSR_W instructions that define this
	 * subroutine by targeting it.
	 */
	HashSet theJSRs = new HashSet();
	
	/**
	 * The RET instruction that leaves this subroutine.
	 */
	InstructionHandle theRET;
	
	/**
	 * The default constructor.
	 * @param subroutines TODO
	 */
	public SubroutineImpl(Subroutines subroutines){
		this.subroutines = subroutines;
	}
	
	
	/**
	 * Adds a new JSR or JSR_W that has this subroutine as its target.
	 */
	public void addEnteringJsrInstruction(InstructionHandle jsrInst){
		if ( (jsrInst == null) || (! (jsrInst.getInstruction() instanceof JsrInstruction))){
			throw new AssertionViolatedException("Expecting JsrInstruction InstructionHandle.");
		}
		if (localVariable == UNSET){
			throw new AssertionViolatedException("Set the localVariable first!");
		}
		else{
			// Something is wrong when an ASTORE is targeted that does not operate on the same local variable than the rest of the
			// JsrInstruction-targets and the RET.
			// (We don't know out leader here so we cannot check if we're really targeted!)
			if (localVariable != ((ASTORE) (((JsrInstruction) jsrInst.getInstruction()).getTarget().getInstruction())).getIndex()){
				throw new AssertionViolatedException("Setting a wrong JsrInstruction.");
			}
		}
		theJSRs.add(jsrInst);
	}
	
	// ---
	/*
	 * Refer to the Subroutine interface for documentation.
	 */
	public boolean contains(InstructionHandle inst){
		return instructions.contains(inst);
	}
			
	/*
	 * Satisfies Subroutine.getAccessedLocalIndices().
	 */
	public int[] getAccessedLocalsIndices(){
		//TODO: Implement caching.
		HashSet acc = new HashSet();
		if (theRET == null && this != this.subroutines.TOPLEVEL){
			throw new AssertionViolatedException("This subroutine object must be built up completely before calculating accessed locals.");
		}
		Iterator i = instructions.iterator();
		while (i.hasNext()){
			InstructionHandle ih = (InstructionHandle) i.next();
			// RET is not a LocalVariableInstruction in the current version of BCEL.
			if (ih.getInstruction() instanceof LocalVariableInstruction || ih.getInstruction() instanceof RET){
				int idx = ((IndexedInstruction) (ih.getInstruction())).getIndex();
				acc.add(new Integer(idx));
				// LONG? DOUBLE?.
				try{
					// LocalVariableInstruction instances are typed without the need to look into
					// the constant pool.
					if (ih.getInstruction() instanceof LocalVariableInstruction){
						int s = ((LocalVariableInstruction) ih.getInstruction()).getType(null).getSize();
						if (s==2) acc.add(new Integer(idx+1));
					}
				}
				catch(RuntimeException re){
					throw new AssertionViolatedException("Oops. BCEL did not like NULL as a ConstantPoolGen object.");
				}
			}
		}
		
		int[] ret = new int[acc.size()];
		i = acc.iterator();
		int j=-1;
		while (i.hasNext()){
			j++;
			ret[j] = ((Integer) i.next()).intValue();
		}
		return ret;
	}

	/*
	 * Refer to the Subroutine interface for documentation.
	 */
	public InstructionHandle[] getEnteringJsrInstructions(){
		if (this == this.subroutines.TOPLEVEL) {
			throw new AssertionViolatedException("getLeavingRET() called on top level pseudo-subroutine.");
		}
		InstructionHandle[] jsrs = new InstructionHandle[theJSRs.size()];
		return (InstructionHandle[]) (theJSRs.toArray(jsrs));
	}

	/*
	 * Refer to the Subroutine interface for documentation.
	 */
	public InstructionHandle[] getInstructions(){
		InstructionHandle[] ret = new InstructionHandle[instructions.size()];
		return (InstructionHandle[]) instructions.toArray(ret);
	}
	
	/*
	 * Refer to the Subroutine interface for documentation.
	 */
	public InstructionHandle getLeavingRET(){
		if (this == this.subroutines.TOPLEVEL) {
			throw new AssertionViolatedException("getLeavingRET() called on top level pseudo-subroutine.");
		}
		return theRET;
	}
	
	/* Satisfies Subroutine.getRecursivelyAccessedLocalsIndices(). */
	public int[] getRecursivelyAccessedLocalsIndices(){
		HashSet s = new HashSet();
		int[] lvs = getAccessedLocalsIndices();
		for (int j=0; j<lvs.length; j++){
			s.add(new Integer(lvs[j]));
		}
		_getRecursivelyAccessedLocalsIndicesHelper(s, this.subSubs());
		int[] ret = new int[s.size()];
		Iterator i = s.iterator();
		int j=-1;
		while (i.hasNext()){
			j++;
			ret[j] = ((Integer) i.next()).intValue();
		}
		return ret;
	}

	/*
	 * Satisfies Subroutine.subSubs().
	 */
	public Subroutine[] subSubs(){
		HashSet h = new HashSet();

		Iterator i = instructions.iterator();
		while (i.hasNext()){
			Instruction inst = ((InstructionHandle) i.next()).getInstruction();
			if (inst instanceof JsrInstruction){
				InstructionHandle targ = ((JsrInstruction) inst).getTarget();
				h.add(this.subroutines.getSubroutine(targ));
			}
		}
		Subroutine[] ret = new Subroutine[h.size()];
		return (Subroutine[]) h.toArray(ret);
	}

	/**
	 * Returns a String representation of this object, merely
	 * for debugging purposes.
	 * (Internal) Warning: Verbosity on a problematic subroutine may cause
	 * stack overflow errors due to recursive subSubs() calls.
	 * Don't use this, then.
	 */
	public String toString(){
		String ret = "Subroutine (size:"+instructions.size()+"instructions): Local variable is '"+localVariable+"', JSRs are '"+theJSRs+"', RET is '"+theRET+"', Instructions: '"+instructions.toString()+"'.";
		
		ret += " Accessed local variable slots: '";
		int[] alv = getAccessedLocalsIndices();
		for (int i=0; i<alv.length; i++){
			ret += alv[i]+" ";
		}
		ret+="'.";

		ret += " Recursively (via subsub...routines) accessed local variable slots: '";
		alv = getRecursivelyAccessedLocalsIndices();
		for (int i=0; i<alv.length; i++){
			ret += alv[i]+" ";
		}
		ret+="'.";

		return ret;
	}

	/**
	 * A recursive helper method for getRecursivelyAccessedLocalsIndices().
	 * @see #getRecursivelyAccessedLocalsIndices()
	 */
	private void _getRecursivelyAccessedLocalsIndicesHelper(HashSet s, Subroutine[] subs){
		for (int i=0; i<subs.length; i++){
			int[] lvs = subs[i].getAccessedLocalsIndices();
			for (int j=0; j<lvs.length; j++){
				s.add(new Integer(lvs[j]));
			}
			if(subs[i].subSubs().length != 0){
				_getRecursivelyAccessedLocalsIndicesHelper(s, subs[i].subSubs());
			}
		}
	}

	/*
	 * Adds an instruction to this subroutine.
	 * All instructions must have been added before invoking setLeavingRET().
	 * @see #setLeavingRET
	 */
	void addInstruction(InstructionHandle ih){
		if (theRET != null){
			throw new AssertionViolatedException("All instructions must have been added before invoking setLeavingRET().");
		}
		instructions.add(ih);
	}
	
	/**
	 * Sets the leaving RET instruction. Must be invoked after all instructions are added.
	 * Must not be invoked for top-level 'subroutine'.
	 */
	void setLeavingRET(){
		if (localVariable == UNSET){
			throw new AssertionViolatedException("setLeavingRET() called for top-level 'subroutine' or forgot to set local variable first.");
		}
		Iterator iter = instructions.iterator();
		InstructionHandle ret = null;
		while(iter.hasNext()){
			InstructionHandle actual = (InstructionHandle) iter.next();
			if (actual.getInstruction() instanceof RET){
				if (ret != null){
					throw new StructuralCodeConstraintException("Subroutine with more then one RET detected: '"+ret+"' and '"+actual+"'.");
				}
				else{
					ret = actual;
				}
			}
		}
		if (ret == null){
			throw new StructuralCodeConstraintException("Subroutine without a RET detected.");
		}
		if (((RET) ret.getInstruction()).getIndex() != localVariable){
			throw new StructuralCodeConstraintException("Subroutine uses '"+ret+"' which does not match the correct local variable '"+localVariable+"'.");
		}
		theRET = ret;
	}
	
	/*
	 * Sets the local variable slot the ASTORE that is targeted
	 * by the JsrInstructions of this subroutine operates on.
	 * This subroutine's RET operates on that same local variable
	 * slot, of course.
	 */
	void setLocalVariable(int i){
		if (localVariable != UNSET){
			throw new AssertionViolatedException("localVariable set twice.");
		}
		else{
			localVariable = i;
		}
	}

}