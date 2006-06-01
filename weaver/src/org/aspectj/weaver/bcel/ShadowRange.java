/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import org.aspectj.apache.bcel.generic.BranchInstruction;
import org.aspectj.apache.bcel.generic.IndexedInstruction;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.InstructionTargeter;
import org.aspectj.apache.bcel.generic.LocalVariableInstruction;
import org.aspectj.apache.bcel.generic.LocalVariableTag;
import org.aspectj.apache.bcel.generic.RET;
import org.aspectj.apache.bcel.generic.Select;
import org.aspectj.apache.bcel.generic.TargetLostException;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.IntMap;
import org.aspectj.weaver.Shadow;

final class ShadowRange extends Range {

    private BcelShadow shadow;

	// ---- initialization
	
	/**
	 * After this constructor is called, this range is not well situated unless both
	 * {@link #associateWithTargets} and {@link #associateWithShadow} are called.
	 */
	public ShadowRange(InstructionList body) {
		super(body);
	}
	protected void associateWithTargets(InstructionHandle start, InstructionHandle end) {
		// assert body.contains(start) && body.contains(end);
		this.start = start;
		this.end = end;
		start.addTargeter(this);
		end.addTargeter(this);
	}	
	public void associateWithShadow(BcelShadow shadow) {
		this.shadow = shadow;
		shadow.setRange(this);
	}
	
	// ----

    public Shadow.Kind getKind() {
        return shadow.getKind();
    }
    
    public String toString() {
        return shadow.toString();
    }

    void extractInstructionsInto(LazyMethodGen freshMethod, IntMap remap, boolean addReturn) {
    	LazyMethodGen.assertGoodBody(getBody(), toString());
    	freshMethod.assertGoodBody();
        InstructionList freshBody = freshMethod.getBody();

        for (InstructionHandle oldIh = start.getNext(); oldIh != end; oldIh = oldIh.getNext()) {
		    // first we copy the instruction itself.  
            Instruction oldI = oldIh.getInstruction();
            Instruction freshI = (oldI == RANGEINSTRUCTION) ? oldI : Utility.copyInstruction(oldI);

			// Now we add it to the new instruction list.
            InstructionHandle freshIh;
            if (freshI instanceof BranchInstruction) {
				//If it's a targeting instruction,
	    		// update the target(s) to point to the new copy instead of the old copy.
            	BranchInstruction oldBranch = (BranchInstruction) oldI;
            	BranchInstruction freshBranch = (BranchInstruction) freshI;
				InstructionHandle oldTarget = oldBranch.getTarget();
				oldTarget.removeTargeter(oldBranch);
				oldTarget.addTargeter(freshBranch);
				if (freshBranch instanceof Select) {
					Select oldSelect = (Select) oldI;
					Select freshSelect = (Select) freshI;
					InstructionHandle[] oldTargets = freshSelect.getTargets();
					for (int k = oldTargets.length - 1; k >= 0; k--) {
						oldTargets[k].removeTargeter(oldSelect);
						oldTargets[k].addTargeter(freshSelect);
					}
				}
            	freshIh = freshBody.append(freshBranch);
            } else {
                freshIh = freshBody.append(freshI); 
            }

				// if source comes before target:
				// source <--> target
				//		-->  [process: target.removeTargeter(source); target.addTargeter(sourcecopy)]
				// source ---------\
				//			        v
				// sourcecopy <--> target
				//      --> [ process: sourcecopy.updateTarget(target, targetcopy) ]
				// source ----> target
				// sourcecopy <--> targetcopy
				
				// if target comes before source
				
				// target <--> source
				//  --> [process: source.updateTarget(target, targetcopy) ]
				// target 
				// targetcopy <--> source
				//  --> [process: targetcopy.removeTargeter(source); targetcopy.addTargeter(sourcecopy)]
				// target source
				//	        v
				// targetcopy <--> sourcecopy
			
			// now deal with the old instruction's targeters.  Update them all to point to us 
			// instead of the old instruction.  We use updateTarget to do this.  One goal is
			// to make sure we remove all targeters from the old guy, so we can successfully
			// delete it.
            InstructionTargeter[] sources = oldIh.getTargeters();
            if (sources != null) {
                for (int j = sources.length - 1; j >= 0; j--) {
                    InstructionTargeter source = sources[j];
                    if (source instanceof LocalVariableTag) {
                    	Shadow.Kind kind = getKind();
                    	if (kind == Shadow.AdviceExecution ||
                    		kind == Shadow.ConstructorExecution ||
                    		kind == Shadow.MethodExecution ||
                    		kind == Shadow.PreInitialization ||
                    		kind == Shadow.Initialization ||
                    		kind == Shadow.StaticInitialization) {
                    		// if we're extracting a whole block we can do this...
                    		source.updateTarget(oldIh, freshIh);
                    	} else {
                            // XXX destroying local variable info
                    		// but only for a call or get join point, so no big deal
                    		source.updateTarget(oldIh, null);
                    	}
                    } else if (source instanceof Range) {
                        // exceptions and shadows are just moved
                        ((Range)source).updateTarget(oldIh, freshIh, freshBody);
                    } else {
                        // line numbers can be shared,
                        // branches will be copied along with us.
                        source.updateTarget(oldIh, freshIh);
                    }
                }
            }
			// we're now done with the old instruction entirely, and will ignore them through
			// the rest of this loop.  The only time we'll see them again is a second pass to
			// delete them.
			
			// now deal with local variable instructions.  If this points to a remapped 
			// frame location, update the instruction's index.  If this doesn't, 
			// do compaction/expansion: allocate a new local variable, and modify the remap
			// to handle it.  XXX We're doing the safe thing and allocating ALL these local variables
			// as double-wides, in case the location is found to hold a double-wide later.
            if (freshI instanceof LocalVariableInstruction || freshI instanceof RET) {
            	IndexedInstruction indexedI = (IndexedInstruction) freshI;
                int oldIndex = indexedI.getIndex();
            	int freshIndex;
                if (! remap.hasKey(oldIndex)) {
                    freshIndex = freshMethod.allocateLocal(2);
                    remap.put(oldIndex, freshIndex);
                } else {
                    freshIndex = remap.get(oldIndex);
                }
                indexedI.setIndex(freshIndex);
            }
//            System.err.println("JUST COPIED: " + oldIh.getInstruction().toString(freshMethod.getEnclosingClass().getConstantPoolGen().getConstantPool()) 
//            	+ " INTO " + freshIh.getInstruction().toString(freshMethod.getEnclosingClass().getConstantPoolGen().getConstantPool()));
        }
        
        // now go through again and update variable slots that have been altered as a result
        // of remapping...
        for (InstructionHandle newIh = freshBody.getStart(); newIh != freshBody.getEnd(); newIh = newIh.getNext()) {
            InstructionTargeter[] sources = newIh.getTargeters();
            if (sources != null) {
                for (int i = sources.length - 1; i >= 0; i--) {
                    if (sources[i] instanceof LocalVariableTag) {
                    	LocalVariableTag lvt = (LocalVariableTag) sources[i];
                    	if (!lvt.isRemapped() && remap.hasKey(lvt.getSlot())) {
                    		lvt.updateSlot(remap.get(lvt.getSlot()));                    		
                    	}
                    }
                }
            }
        }
       
        
        

		// we've now copied out all the instructions.
        // now delete the instructions... we've already taken care of the damn
        // targets, but since TargetLostException is checked, we have to do this stuff.
        try {
            for (InstructionHandle oldIh = start.getNext(); oldIh != end; ) {
                InstructionHandle next = oldIh.getNext();
                body.delete(oldIh);
                oldIh = next;
            }
        } catch (TargetLostException e) {
            throw new BCException("shouldn't have gotten a target lost");
        }
        
        // now add the return, if one is warranted.  
        InstructionHandle ret = null;
        if (addReturn) {
            // we really should pull this out somewhere...
            ret = freshBody.append(
                InstructionFactory.createReturn(freshMethod.getReturnType()));
        }
		// and remap all the old targeters of the end handle of the range to the return.
        InstructionTargeter[] ts = end.getTargeters();
        if (ts != null) {  // shouldn't be the case, but let's test for paranoia
            for (int j = ts.length - 1; j >= 0; j--) {
                InstructionTargeter t = ts[j];
                if (t == this) continue;
                if (! addReturn) {
                    throw new BCException("range has target, but we aren't adding a return");
                } else {
                    t.updateTarget(end, ret);
                }
            }
        }

    	LazyMethodGen.assertGoodBody(getBody(), toString());
    	freshMethod.assertGoodBody();
    }

	public BcelShadow getShadow() {
		return shadow;
	}


}
