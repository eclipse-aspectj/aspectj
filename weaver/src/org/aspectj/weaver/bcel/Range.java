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

import java.util.Iterator;

import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.InstructionTargeter;
import org.aspectj.weaver.BCException;

abstract class Range implements InstructionTargeter {

	protected InstructionList body;
	protected InstructionHandle start;
	protected InstructionHandle end;

	// ---- initialization

	protected Range(InstructionList il) {
		this.body = il;
	}

	// ----

	final InstructionList getBody() {
		return body;
	}

	final InstructionHandle getStart() {
		return start;
	}

	final InstructionHandle getEnd() {
		return end;
	}

	// ----

	boolean isEmpty() {
		InstructionHandle ih = start;
		// System.err.println("  looking for " + end);
		while (ih != end) {
			// System.err.println("    ih " + ih);
			if (!Range.isRangeHandle(ih)) {
				return false;
			}
			ih = ih.getNext();
		}
		return true;
	}

	static InstructionHandle getRealStart(InstructionHandle ih) {
		while (Range.isRangeHandle(ih)) {
			ih = ih.getNext();
		}
		return ih;
	}

	InstructionHandle getRealStart() {
		return getRealStart(start);
	}

	static InstructionHandle getRealEnd(InstructionHandle ih) {
		while (Range.isRangeHandle(ih)) {
			ih = ih.getPrev();
		}
		return ih;
	}

	InstructionHandle getRealEnd() {
		return getRealEnd(end);
	}

	InstructionHandle getRealNext() {
		return getRealStart(end);
	}

	// ----

	InstructionHandle insert(Instruction i, Where where) {
		InstructionList il = new InstructionList();
		InstructionHandle ret = il.insert(i);
		insert(il, where);
		return ret;
	}

	void insert(InstructionList freshIl, Where where) {
		InstructionHandle h;
		if (where == InsideBefore || where == OutsideBefore) {
			h = getStart();
		} else {
			h = getEnd();
		}
		if (where == InsideBefore || where == OutsideAfter) {
			body.append(h, freshIl);
		} else {
			InstructionHandle newStart = body.insert(h, freshIl);
			if (where == OutsideBefore) {
				// XXX this is slow. There's a better design than this. We should
				// never have to retarget branches apart from the creation of ranges.
				// basically, we should never weave OutsideBefore.
				BcelShadow.retargetAllBranches(h, newStart);
			}
		}

	}

	InstructionHandle append(Instruction i) {
		return insert(i, InsideAfter);
	}

	void append(InstructionList i) {
		insert(i, InsideAfter);
	}

	private static void setLineNumberFromNext(InstructionHandle ih) {
		int lineNumber = Utility.getSourceLine(ih.getNext());
		if (lineNumber != -1) {
			Utility.setSourceLine(ih, lineNumber);
		}
	}

	static InstructionHandle genStart(InstructionList body) {
		InstructionHandle ih = body.insert(Range.RANGEINSTRUCTION);
		setLineNumberFromNext(ih);
		return ih;
	}

	static InstructionHandle genEnd(InstructionList body) {
		return body.append(Range.RANGEINSTRUCTION);
	}

	static InstructionHandle genStart(InstructionList body, InstructionHandle ih) {
		if (ih == null) {
			return genStart(body);
		}
		InstructionHandle freshIh = body.insert(ih, Range.RANGEINSTRUCTION);
		setLineNumberFromNext(freshIh);
		return freshIh;
	}

	static InstructionHandle genEnd(InstructionList body, InstructionHandle ih) {
		if (ih == null) {
			return genEnd(body);
		}
		return body.append(ih, Range.RANGEINSTRUCTION);
	}

	// -----

	public boolean containsTarget(InstructionHandle ih) {
		return false;
	}

	public final void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih) {
		throw new RuntimeException("Ranges must be updated with an enclosing instructionList");
	}

	protected void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih, InstructionList new_il) {
		old_ih.removeTargeter(this);
		if (new_ih != null) {
			new_ih.addTargeter(this);
		}
		body = new_il;

		if (old_ih == start) {
			start = new_ih;
		}
		if (old_ih == end) {
			end = new_ih;
		}
	}

	public static final boolean isRangeHandle(InstructionHandle ih) {
		if (ih == null) {
			return false;
		}
		return ih.getInstruction() == Range.RANGEINSTRUCTION;
	}

	protected static final Range getRange(InstructionHandle ih) {
		// assert isRangeHandle(ih)
		Range ret = null;
		Iterator<InstructionTargeter> tIter = ih.getTargeters().iterator();
		while (tIter.hasNext()) {
			InstructionTargeter targeter = tIter.next();
			if (targeter instanceof Range) {
				Range r = (Range) targeter;
				if (r.getStart() != ih && r.getEnd() != ih) {
					continue;
				}
				if (ret != null) {
					throw new BCException("multiple ranges on same range handle: " + ret + ",  " + targeter);
				}
				ret = r;
			}
		}
		if (ret == null) {
			throw new BCException("shouldn't happen");
		}
		return ret;
	}

	// ----

	static final Where InsideBefore = new Where("insideBefore");
	static final Where InsideAfter = new Where("insideAfter");
	static final Where OutsideBefore = new Where("outsideBefore");
	static final Where OutsideAfter = new Where("outsideAfter");

	// ---- constants

	// note that this is STUPIDLY copied by Instruction.copy(), so don't do that.

	public static final Instruction RANGEINSTRUCTION = InstructionConstants.IMPDEP1;

	// ----

	static class Where {
		private String name;

		public Where(String name) {
			this.name = name;
		}

		public String toString() {
			return name;
		}
	}
}
