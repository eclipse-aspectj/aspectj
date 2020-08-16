package org.aspectj.apache.bcel.generic;

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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.Constant;
import org.aspectj.apache.bcel.classfile.ConstantPool;
import org.aspectj.apache.bcel.util.ByteSequence;

/**
 * This class is a container for a list of <a href="Instruction.html">Instruction</a> objects. Instructions can be appended,
 * inserted, moved, deleted, etc.. Instructions are being wrapped into <a href="InstructionHandle.html">InstructionHandles</a>
 * objects that are returned upon append/insert operations. They give the user (read only) access to the list structure, such that
 * it can be traversed and manipulated in a controlled way.
 * 
 * A list is finally dumped to a byte code array with <a href="#getByteCode()">getByteCode</a>.
 * 
 * @version $Id: InstructionList.java,v 1.12 2011/09/02 22:33:04 aclement Exp $
 * @author <A HREF="mailto:markus.dahm@berlin.de">M. Dahm</A>
 * @author Abraham Nevado
 * @see Instruction
 * @see InstructionHandle
 * @see BranchHandle
 */
public class InstructionList implements Serializable {
	private InstructionHandle start = null, end = null;
	private int length = 0;
	private int[] positions; // byte code offsets corresponding to instructions

	public InstructionList() {
	}

	public InstructionList(Instruction i) {
		append(i);
	}

	public boolean isEmpty() {
		return start == null;
	} // && end == null

	public static InstructionHandle findHandle(InstructionHandle[] ihs, int[] pos, int count, int target) {
		return findHandle(ihs, pos, count, target, false);
	}

	/**
	 * Find the target instruction (handle) that corresponds to the given target position (byte code offset).
	 * 
	 * @param ihs array of instruction handles, i.e. il.getInstructionHandles()
	 * @param pos array of positions corresponding to ihs, i.e. il.getInstructionPositions()
	 * @param count length of arrays
	 * @param target target position to search for
	 * @return target position's instruction handle if available
	 */
	public static InstructionHandle findHandle(InstructionHandle[] ihs, int[] pos, int count, int target,
			boolean returnClosestIfNoExactMatch) {
		int l = 0, r = count - 1;
		// Do a binary search since the pos array is ordered
		int i, j;
		do {
			i = (l + r) / 2;
			j = pos[i];
			if (j == target) {
				return ihs[i]; // found it
			} else if (target < j) {
				r = i - 1; // else constrain search area
			} else {
				l = i + 1; // target > j
			}
		} while (l <= r);

		if (returnClosestIfNoExactMatch) {
			i = (l + r) / 2;
			if (i < 0) {
				i = 0;
			}
			return ihs[i];
		}
		return null;
	}

	/**
	 * Get instruction handle for instruction at byte code position pos. This only works properly, if the list is freshly
	 * initialized from a byte array or setPositions() has been called before this method.
	 * 
	 * @param pos byte code position to search for
	 * @return target position's instruction handle if available
	 */
	public InstructionHandle findHandle(int pos) {
		InstructionHandle[] ihs = getInstructionHandles();
		return findHandle(ihs, positions, length, pos);
	}

	public InstructionHandle[] getInstructionsAsArray() {
		return getInstructionHandles();
	}

	public InstructionHandle findHandle(int pos, InstructionHandle[] instructionArray) {
		return findHandle(instructionArray, positions, length, pos);
	}

	public InstructionHandle findHandle(int pos, InstructionHandle[] instructionArray, boolean useClosestApproximationIfNoExactFound) {
		return findHandle(instructionArray, positions, length, pos, useClosestApproximationIfNoExactFound);
	}

	/**
	 * Initialize instruction list from byte array.
	 * 
	 * @param code byte array containing the instructions
	 */
	public InstructionList(byte[] code) {
		ByteSequence bytes = new ByteSequence(code);
		InstructionHandle[] ihs = new InstructionHandle[code.length];
		int[] pos = new int[code.length]; // Can't be more than that
		int count = 0; // Contains actual length

		/*
		 * Pass 1: Create an object for each byte code and append them to the list.
		 */
		try {
			while (bytes.available() > 0) {
				// Remember byte offset and associate it with the instruction
				int off = bytes.getIndex();
				pos[count] = off;

				/*
				 * Read one instruction from the byte stream, the byte position is set accordingly.
				 */
				Instruction i = Instruction.readInstruction(bytes);
				InstructionHandle ih;
				if (i instanceof InstructionBranch) {
					ih = append((InstructionBranch) i);
				} else {
					ih = append(i);
				}

				ih.setPosition(off);
				ihs[count] = ih;

				count++;
			}
		} catch (IOException e) {
			throw new ClassGenException(e.toString());
		}

		positions = new int[count]; // Trim to proper size
		System.arraycopy(pos, 0, positions, 0, count);

		/*
		 * Pass 2: Look for BranchInstruction and update their targets, i.e., convert offsets to instruction handles.
		 */
		// OPTIMIZE better way of doing this? keep little map from earlier from pos -> instruction handle?
		for (int i = 0; i < count; i++) {
			if (ihs[i] instanceof BranchHandle) {
				InstructionBranch bi = (InstructionBranch) ihs[i].instruction;
				int target = bi.positionOfThisInstruction + bi.getIndex(); /*
																			 * Byte code position: relative -> absolute.
																			 */
				// Search for target position
				InstructionHandle ih = findHandle(ihs, pos, count, target);

				if (ih == null) {
					throw new ClassGenException("Couldn't find target for branch: " + bi);
				}

				bi.setTarget(ih); // Update target

				// If it is a Select instruction, update all branch targets
				if (bi instanceof InstructionSelect) { // Either LOOKUPSWITCH or TABLESWITCH
					InstructionSelect s = (InstructionSelect) bi;
					int[] indices = s.getIndices();

					for (int j = 0; j < indices.length; j++) {
						target = bi.positionOfThisInstruction + indices[j];
						ih = findHandle(ihs, pos, count, target);

						if (ih == null) {
							throw new ClassGenException("Couldn't find target for switch: " + bi);
						}

						s.setTarget(j, ih); // Update target
					}
				}
			}
		}
	}

	/**
	 * Append another list after instruction (handle) ih contained in this list. Consumes argument list, i.e., it becomes empty.
	 * 
	 * @param appendTo where to append the instruction list
	 * @param appendee Instruction list to append to this one
	 * @return instruction handle pointing to the <B>first</B> appended instruction
	 */
	public InstructionHandle append(InstructionHandle appendTo, InstructionList appendee) {
		assert appendee != null;

		if (appendee.isEmpty()) {
			return appendTo;
		}

		InstructionHandle next = appendTo.next;
		InstructionHandle ret = appendee.start;

		appendTo.next = appendee.start;
		appendee.start.prev = appendTo;

		appendee.end.next = next;

		if (next != null) {
			next.prev = appendee.end;
		} else {
			end = appendee.end; // Update end ...
		}

		length += appendee.length; // Update length

		appendee.clear();

		return ret;
	}

	/**
	 * Append another list after instruction i contained in this list. Consumes argument list, i.e., it becomes empty.
	 * 
	 * @param i where to append the instruction list
	 * @param il Instruction list to append to this one
	 * @return instruction handle pointing to the <B>first</B> appended instruction
	 */
	public InstructionHandle append(Instruction i, InstructionList il) {
		InstructionHandle ih;

		if ((ih = findInstruction2(i)) == null) {
			throw new ClassGenException("Instruction " + i + " is not contained in this list.");
		}

		return append(ih, il);
	}

	/**
	 * Append another list to this one. Consumes argument list, i.e., it becomes empty.
	 * 
	 * @param il list to append to end of this list
	 * @return instruction handle of the <B>first</B> appended instruction
	 */
	public InstructionHandle append(InstructionList il) {
		assert il != null;

		if (il.isEmpty()) {
			return null;
		}

		if (isEmpty()) {
			start = il.start;
			end = il.end;
			length = il.length;

			il.clear();

			return start;
		} else {
			return append(end, il); // was end.instruction
		}
	}

	/**
	 * Append an instruction to the end of this list.
	 * 
	 * @param ih instruction to append
	 */
	private void append(InstructionHandle ih) {
		if (isEmpty()) {
			start = end = ih;
			ih.next = ih.prev = null;
		} else {
			end.next = ih;
			ih.prev = end;
			ih.next = null;
			end = ih;
		}

		length++; // Update length
	}

	/**
	 * Append an instruction to the end of this list.
	 * 
	 * @param i instruction to append
	 * @return instruction handle of the appended instruction
	 */
	public InstructionHandle append(Instruction i) {
		InstructionHandle ih = InstructionHandle.getInstructionHandle(i);
		append(ih);

		return ih;
	}

	public InstructionHandle appendDUP() {
		InstructionHandle ih = InstructionHandle.getInstructionHandle(InstructionConstants.DUP);
		append(ih);
		return ih;
	}

	public InstructionHandle appendNOP() {
		InstructionHandle ih = InstructionHandle.getInstructionHandle(InstructionConstants.NOP);
		append(ih);
		return ih;
	}

	public InstructionHandle appendPOP() {
		InstructionHandle ih = InstructionHandle.getInstructionHandle(InstructionConstants.POP);
		append(ih);
		return ih;
	}

	/**
	 * Append a branch instruction to the end of this list.
	 * 
	 * @param i branch instruction to append
	 * @return branch instruction handle of the appended instruction
	 */
	public BranchHandle append(InstructionBranch i) {
		BranchHandle ih = BranchHandle.getBranchHandle(i);
		append(ih);

		return ih;
	}

	/**
	 * Append a single instruction j after another instruction i, which must be in this list of course!
	 * 
	 * @param i Instruction in list
	 * @param j Instruction to append after i in list
	 * @return instruction handle of the first appended instruction
	 */
	public InstructionHandle append(Instruction i, Instruction j) {
		return append(i, new InstructionList(j));
	}

	/**
	 * Append an instruction after instruction (handle) ih contained in this list.
	 * 
	 * @param ih where to append the instruction list
	 * @param i Instruction to append
	 * @return instruction handle pointing to the <B>first</B> appended instruction
	 */
	public InstructionHandle append(InstructionHandle ih, Instruction i) {
		return append(ih, new InstructionList(i));
	}

	/**
	 * Append an instruction after instruction (handle) ih contained in this list.
	 * 
	 * @param ih where to append the instruction list
	 * @param i Instruction to append
	 * @return instruction handle pointing to the <B>first</B> appended instruction
	 */
	public BranchHandle append(InstructionHandle ih, InstructionBranch i) {
		BranchHandle bh = BranchHandle.getBranchHandle(i);
		InstructionList il = new InstructionList();
		il.append(bh);

		append(ih, il);

		return bh;
	}

	/**
	 * Insert another list before Instruction handle ih contained in this list. Consumes argument list, i.e., it becomes empty.
	 * 
	 * @param i where to append the instruction list
	 * @param il Instruction list to insert
	 * @return instruction handle of the first inserted instruction
	 */
	public InstructionHandle insert(InstructionHandle ih, InstructionList il) {
		if (il == null) {
			throw new ClassGenException("Inserting null InstructionList");
		}

		if (il.isEmpty()) {
			return ih;
		}

		InstructionHandle prev = ih.prev, ret = il.start;

		ih.prev = il.end;
		il.end.next = ih;

		il.start.prev = prev;

		if (prev != null) {
			prev.next = il.start;
		} else {
			start = il.start; // Update start ...
		}

		length += il.length; // Update length

		il.clear();

		return ret;
	}

	/**
	 * Insert another list.
	 * 
	 * @param il list to insert before start of this list
	 * @return instruction handle of the first inserted instruction
	 */
	public InstructionHandle insert(InstructionList il) {
		if (isEmpty()) {
			append(il); // Code is identical for this case
			return start;
		} else {
			return insert(start, il);
		}
	}

	/**
	 * Insert an instruction at start of this list.
	 * 
	 * @param ih instruction to insert
	 */
	private void insert(InstructionHandle ih) {
		if (isEmpty()) {
			start = end = ih;
			ih.next = ih.prev = null;
		} else {
			start.prev = ih;
			ih.next = start;
			ih.prev = null;
			start = ih;
		}

		length++;
	}

	/**
	 * Insert another list before Instruction i contained in this list. Consumes argument list, i.e., it becomes empty.
	 * 
	 * @param i where to append the instruction list
	 * @param il Instruction list to insert
	 * @return instruction handle pointing to the first inserted instruction, i.e., il.getStart()
	 */
	public InstructionHandle insert(Instruction i, InstructionList il) {
		InstructionHandle ih;

		if ((ih = findInstruction1(i)) == null) {
			throw new ClassGenException("Instruction " + i + " is not contained in this list.");
		}

		return insert(ih, il);
	}

	/**
	 * Insert an instruction at start of this list.
	 * 
	 * @param i instruction to insert
	 * @return instruction handle of the inserted instruction
	 */
	public InstructionHandle insert(Instruction i) {
		InstructionHandle ih = InstructionHandle.getInstructionHandle(i);
		insert(ih);

		return ih;
	}

	/**
	 * Insert a branch instruction at start of this list.
	 * 
	 * @param i branch instruction to insert
	 * @return branch instruction handle of the appended instruction
	 */
	public BranchHandle insert(InstructionBranch i) {
		BranchHandle ih = BranchHandle.getBranchHandle(i);
		insert(ih);
		return ih;
	}

	/**
	 * Insert a single instruction j before another instruction i, which must be in this list of course!
	 * 
	 * @param i Instruction in list
	 * @param j Instruction to insert before i in list
	 * @return instruction handle of the first inserted instruction
	 */
	public InstructionHandle insert(Instruction i, Instruction j) {
		return insert(i, new InstructionList(j));
	}

	/**
	 * Insert an instruction before instruction (handle) ih contained in this list.
	 * 
	 * @param ih where to insert to the instruction list
	 * @param i Instruction to insert
	 * @return instruction handle of the first inserted instruction
	 */
	public InstructionHandle insert(InstructionHandle ih, Instruction i) {
		return insert(ih, new InstructionList(i));
	}

	/**
	 * Insert an instruction before instruction (handle) ih contained in this list.
	 * 
	 * @param ih where to insert to the instruction list
	 * @param i Instruction to insert
	 * @return instruction handle of the first inserted instruction
	 */
	public BranchHandle insert(InstructionHandle ih, InstructionBranch i) {
		BranchHandle bh = BranchHandle.getBranchHandle(i);
		InstructionList il = new InstructionList();
		il.append(bh);

		insert(ih, il);

		return bh;
	}

	/**
	 * Take all instructions (handles) from "start" to "end" and append them after the new location "target". Of course, "end" must
	 * be after "start" and target must not be located withing this range. If you want to move something to the start of the list
	 * use null as value for target.<br>
	 * Any instruction targeters pointing to handles within the block, keep their targets.
	 * 
	 * @param start of moved block
	 * @param end of moved block
	 * @param target of moved block
	 */
	public void move(InstructionHandle start, InstructionHandle end, InstructionHandle target) {
		// Step 1: Check constraints

		if (start == null || end == null) {
			throw new ClassGenException("Invalid null handle: From " + start + " to " + end);
		}

		if (target == start || target == end) {
			throw new ClassGenException("Invalid range: From " + start + " to " + end + " contains target " + target);
		}

		for (InstructionHandle ih = start; ih != end.next; ih = ih.next) {
			if (ih == null) {
				throw new ClassGenException("Invalid range: From " + start + " to " + end);
			} else if (ih == target) {
				throw new ClassGenException("Invalid range: From " + start + " to " + end + " contains target " + target);
			}
		}

		// Step 2: Temporarily remove the given instructions from the list

		InstructionHandle prev = start.prev, next = end.next;

		if (prev != null) {
			prev.next = next;
		} else {
			this.start = next;
		}

		if (next != null) {
			next.prev = prev;
		} else {
			this.end = prev;
		}

		start.prev = end.next = null;

		// Step 3: append after target

		if (target == null) { // append to start of list
			end.next = this.start;
			this.start = start;
		} else {
			next = target.next;

			target.next = start;
			start.prev = target;
			end.next = next;

			if (next != null) {
				next.prev = end;
			}
		}
	}

	/**
	 * Move a single instruction (handle) to a new location.
	 * 
	 * @param ih moved instruction
	 * @param target new location of moved instruction
	 */
	public void move(InstructionHandle ih, InstructionHandle target) {
		move(ih, ih, target);
	}

	/**
	 * Remove from instruction 'prev' to instruction 'next' both contained in this list.
	 * 
	 * If careAboutLostTargeters is true then this method will throw a TargetLostException when one of the removed instruction
	 * handles is still being targeted.
	 * 
	 * @param prev where to start deleting (predecessor, exclusive)
	 * @param next where to end deleting (successor, exclusive)
	 */
	private void remove(InstructionHandle prev, InstructionHandle next, boolean careAboutLostTargeters) throws TargetLostException {
		InstructionHandle first, last; // First and last deleted instruction

		if (prev == null && next == null) { // singleton list
			first = last = start;
			start = end = null;
		} else {
			if (prev == null) { // At start of list
				first = start;
				start = next;
			} else {
				first = prev.next;
				prev.next = next;
			}
			if (next == null) { // At end of list
				last = end;
				end = prev;
			} else {
				last = next.prev;
				next.prev = prev;
			}
		}

		first.prev = null; // Completely separated from rest of list
		last.next = null;

		if (!careAboutLostTargeters) {
			return;
		}

		List<InstructionHandle> target_vec = new ArrayList<>();

		for (InstructionHandle ih = first; ih != null; ih = ih.next) {
			ih.getInstruction().dispose(); // e.g. BranchInstructions release their targets
		}

		StringBuffer buf = new StringBuffer("{ ");
		for (InstructionHandle ih = first; ih != null; ih = next) {
			next = ih.next;
			length--;

			Set<InstructionTargeter> targeters = ih.getTargeters();
			boolean isOK = false;
			for (InstructionTargeter instructionTargeter : targeters) {
				if (instructionTargeter.getClass().getName().endsWith("ShadowRange")
						|| instructionTargeter.getClass().getName().endsWith("ExceptionRange")
						|| instructionTargeter.getClass().getName().endsWith("LineNumberTag")) {
					isOK = true;
				} else {
					System.out.println(instructionTargeter.getClass());
				}
			}
			if (!isOK) {
				target_vec.add(ih);
				buf.append(ih.toString(true) + " ");
				ih.next = ih.prev = null;
			} else {
				ih.dispose();
			}

			// if (ih.hasTargeters()) { // Still got targeters?
			// InstructionTargeter[] targeters = ih.getTargeters();
			// boolean isOK = false;
			// for (int i = 0; i < targeters.length; i++) {
			// InstructionTargeter instructionTargeter = targeters[i];
			// if (instructionTargeter.getClass().getName().endsWith("ShadowRange")
			// || instructionTargeter.getClass().getName().endsWith("ExceptionRange")
			// || instructionTargeter.getClass().getName().endsWith("LineNumberTag")) {
			// isOK = true;
			// } else {
			// System.out.println(instructionTargeter.getClass());
			// }
			// }
			// if (!isOK) {
			// target_vec.add(ih);
			// buf.append(ih.toString(true) + " ");
			// ih.next = ih.prev = null;
			// } else {
			// ih.dispose();
			// }
			// } else {
			// ih.dispose();
			// }
		}

		buf.append("}");

		if (!target_vec.isEmpty()) {
			InstructionHandle[] targeted = new InstructionHandle[target_vec.size()];
			target_vec.toArray(targeted);
			throw new TargetLostException(targeted, buf.toString());
		}
	}

	/**
	 * Remove instruction from this list. The corresponding Instruction handles must not be reused!
	 * 
	 * @param ih instruction (handle) to remove
	 */
	public void delete(InstructionHandle ih) throws TargetLostException {
		remove(ih.prev, ih.next, false);
	}

	/**
	 * Remove instruction from this list. The corresponding Instruction handles must not be reused!
	 * 
	 * @param i instruction to remove
	 */
	// public void delete(Instruction i) throws TargetLostException {
	// InstructionHandle ih;
	//
	// if((ih = findInstruction1(i)) == null)
	// throw new ClassGenException("Instruction " + i +
	// " is not contained in this list.");
	// delete(ih);
	// }
	/**
	 * Remove instructions from instruction `from' to instruction `to' contained in this list. The user must ensure that `from' is
	 * an instruction before `to', or risk havoc. The corresponding Instruction handles must not be reused!
	 * 
	 * @param from where to start deleting (inclusive)
	 * @param to where to end deleting (inclusive)
	 */
	public void delete(InstructionHandle from, InstructionHandle to) throws TargetLostException {
		remove(from.prev, to.next, false);
	}

	/**
	 * Remove instructions from instruction `from' to instruction `to' contained in this list. The user must ensure that `from' is
	 * an instruction before `to', or risk havoc. The corresponding Instruction handles must not be reused!
	 * 
	 * @param from where to start deleting (inclusive)
	 * @param to where to end deleting (inclusive)
	 */
	public void delete(Instruction from, Instruction to) throws TargetLostException {
		InstructionHandle from_ih, to_ih;

		if ((from_ih = findInstruction1(from)) == null) {
			throw new ClassGenException("Instruction " + from + " is not contained in this list.");
		}

		if ((to_ih = findInstruction2(to)) == null) {
			throw new ClassGenException("Instruction " + to + " is not contained in this list.");
		}
		delete(from_ih, to_ih);
	}

	/**
	 * Search for given Instruction reference, start at beginning of list.
	 * 
	 * @param i instruction to search for
	 * @return instruction found on success, null otherwise
	 */
	private InstructionHandle findInstruction1(Instruction i) {
		for (InstructionHandle ih = start; ih != null; ih = ih.next) {
			if (ih.instruction == i) {
				return ih;
			}
		}

		return null;
	}

	/**
	 * Search for given Instruction reference, start at end of list
	 * 
	 * @param i instruction to search for
	 * @return instruction found on success, null otherwise
	 */
	private InstructionHandle findInstruction2(Instruction i) {
		for (InstructionHandle ih = end; ih != null; ih = ih.prev) {
			if (ih.instruction == i) {
				return ih;
			}
		}

		return null;
	}

	public boolean contains(InstructionHandle i) {
		if (i == null) {
			return false;
		}

		for (InstructionHandle ih = start; ih != null; ih = ih.next) {
			if (ih == i) {
				return true;
			}
		}

		return false;
	}

	public boolean contains(Instruction i) {
		return findInstruction1(i) != null;
	}

	public void setPositions() {
		setPositions(false);
	}

	/**
	 * Give all instructions their position number (offset in byte stream), i.e., make the list ready to be dumped.
	 * 
	 * @param check Perform sanity checks, e.g. if all targeted instructions really belong to this list
	 */
	public void setPositions(boolean check) {
		int maxAdditionalBytes = 0;
		int index = 0, count = 0;
		int[] pos = new int[length];

		// Pass 0: Sanity checks
		if (check) {
			checkInstructionList();
		}

		// Pass 1: Set position numbers and sum up the maximum number of bytes an
		// instruction may be shifted.
		for (InstructionHandle ih = start; ih != null; ih = ih.next) {
			Instruction i = ih.instruction;
			ih.setPosition(index);
			pos[count++] = index;

			/*
			 * Get an estimate about how many additional bytes may be added, because BranchInstructions may have variable length
			 * depending on the target offset (short vs. int) or alignment issues (TABLESWITCH and LOOKUPSWITCH).
			 */
			switch (i.opcode) {
			case Constants.JSR:
			case Constants.GOTO:
				maxAdditionalBytes += 2;
				break;

			case Constants.TABLESWITCH:
			case Constants.LOOKUPSWITCH:
				maxAdditionalBytes += 3;
				break;
			}
			index += i.getLength();
		}

		// OPTIMIZE positions will only move around if there have been expanding instructions
		// if (max_additional_bytes==0...) {
		//
		// }

		/*
		 * Pass 2: Expand the variable-length (Branch)Instructions depending on the target offset (short or int) and ensure that
		 * branch targets are within this list.
		 */
		boolean nonZeroOffset = false;
		int offset = 0;
		for (InstructionHandle ih = start; ih != null; ih = ih.next) {
			if (ih instanceof BranchHandle) {
				offset += ((BranchHandle) ih).updatePosition(offset, maxAdditionalBytes);
				if (offset != 0) {
					nonZeroOffset = true;
				}
			}
		}
		if (nonZeroOffset) {
			/*
			 * Pass 3: Update position numbers (which may have changed due to the preceding expansions), like pass 1.
			 */
			index = count = 0;
			for (InstructionHandle ih = start; ih != null; ih = ih.next) {
				Instruction i = ih.instruction;
				ih.setPosition(index);
				pos[count++] = index;
				index += i.getLength();
			}
		}

		positions = new int[count]; // Trim to proper size
		System.arraycopy(pos, 0, positions, 0, count);
	}

	private void checkInstructionList() {
		for (InstructionHandle ih = start; ih != null; ih = ih.next) {
			Instruction i = ih.instruction;

			if (i instanceof InstructionBranch) { // target instruction within list?
				Instruction inst = ((InstructionBranch) i).getTarget().instruction;
				if (!contains(inst)) {
					throw new ClassGenException("Branch target of " + Constants.OPCODE_NAMES[i.opcode] + ":" + inst
							+ " not in instruction list");
				}

				if (i instanceof InstructionSelect) {
					InstructionHandle[] targets = ((InstructionSelect) i).getTargets();

					for (InstructionHandle target : targets) {
						inst = target.instruction;
						if (!contains(inst)) {
							throw new ClassGenException("Branch target of " + Constants.OPCODE_NAMES[i.opcode] + ":" + inst
									+ " not in instruction list");
						}
					}
				}

				if (!(ih instanceof BranchHandle)) {
					throw new ClassGenException("Branch instruction " + Constants.OPCODE_NAMES[i.opcode] + ":" + inst
							+ " not contained in BranchHandle.");
				}

			}
		}
	}

	/**
	 * When everything is finished, use this method to convert the instruction list into an array of bytes.
	 * 
	 * @return the byte code ready to be dumped
	 */
	public byte[] getByteCode() {
		// Update position indices of instructions
		setPositions();

		ByteArrayOutputStream b = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(b);

		try {
			for (InstructionHandle ih = start; ih != null; ih = ih.next) {
				Instruction i = ih.instruction;
				i.dump(out); // Traverse list
			}
		} catch (IOException e) {
			System.err.println(e);
			return null;
		}
		byte[] byteCode = b.toByteArray();
		if (byteCode.length > Constants.MAX_CODE_SIZE) {
			throw new ClassGenException("Code size too big: " + byteCode.length);
		}

		return byteCode;
	}

	/**
	 * @return an array of instructions without target information for branch instructions.
	 */
	public Instruction[] getInstructions() {
		ByteSequence bytes = new ByteSequence(getByteCode());
		List<Instruction> instructions = new ArrayList<>();

		try {
			while (bytes.available() > 0) {
				instructions.add(Instruction.readInstruction(bytes));
			}
		} catch (IOException e) {
			throw new ClassGenException(e.toString());
		}

		Instruction[] result = new Instruction[instructions.size()];
		instructions.toArray(result);
		return result;
	}

	@Override
	public String toString() {
		return toString(true);
	}

	/**
	 * @param verbose toggle output format
	 * @return String containing all instructions in this list.
	 */
	public String toString(boolean verbose) {
		StringBuffer buf = new StringBuffer();

		for (InstructionHandle ih = start; ih != null; ih = ih.next) {
			buf.append(ih.toString(verbose) + "\n");
		}

		return buf.toString();
	}

	/**
	 * @return Enumeration that lists all instructions (handles)
	 */
	public Iterator iterator() {
		return new Iterator() {
			private InstructionHandle ih = start;

			public Object next() {
				InstructionHandle i = ih;
				ih = ih.next;
				return i;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}

			public boolean hasNext() {
				return ih != null;
			}
		};
	}

	/**
	 * @return array containing all instructions (handles)
	 */
	public InstructionHandle[] getInstructionHandles() {
		InstructionHandle[] ihs = new InstructionHandle[length];
		InstructionHandle ih = start;

		for (int i = 0; i < length; i++) {
			ihs[i] = ih;
			ih = ih.next;
		}

		return ihs;
	}

	/**
	 * Get positions (offsets) of all instructions in the list. This relies on that the list has been freshly created from an byte
	 * code array, or that setPositions() has been called. Otherwise this may be inaccurate.
	 * 
	 * @return array containing all instruction's offset in byte code
	 */
	public int[] getInstructionPositions() {
		return positions;
	}

	/**
	 * @return complete, i.e., deep copy of this list
	 */
	public InstructionList copy() {
		HashMap<InstructionHandle, InstructionHandle> map = new HashMap<>();
		InstructionList il = new InstructionList();

		/*
		 * Pass 1: Make copies of all instructions, append them to the new list and associate old instruction references with the
		 * new ones, i.e., a 1:1 mapping.
		 */
		for (InstructionHandle ih = start; ih != null; ih = ih.next) {
			Instruction i = ih.instruction;
			Instruction c = i.copy(); // Use clone for shallow copy

			if (c instanceof InstructionBranch) {
				map.put(ih, il.append((InstructionBranch) c));
			} else {
				map.put(ih, il.append(c));
			}
		}

		/*
		 * Pass 2: Update branch targets.
		 */
		InstructionHandle ih = start;
		InstructionHandle ch = il.start;

		while (ih != null) {
			Instruction i = ih.instruction;
			Instruction c = ch.instruction;

			if (i instanceof InstructionBranch) {
				InstructionBranch bi = (InstructionBranch) i;
				InstructionBranch bc = (InstructionBranch) c;
				InstructionHandle itarget = bi.getTarget(); // old target

				// New target is in hash map
				bc.setTarget(map.get(itarget));

				if (bi instanceof InstructionSelect) { // Either LOOKUPSWITCH or TABLESWITCH
					InstructionHandle[] itargets = ((InstructionSelect) bi).getTargets();
					InstructionHandle[] ctargets = ((InstructionSelect) bc).getTargets();

					for (int j = 0; j < itargets.length; j++) { // Update all targets
						ctargets[j] = map.get(itargets[j]);
					}
				}
			}

			ih = ih.next;
			ch = ch.next;
		}

		return il;
	}

	/**
	 * Replace all references to the old constant pool with references to the new constant pool
	 */
	public void replaceConstantPool(ConstantPool old_cp, ConstantPool new_cp) {
		for (InstructionHandle ih = start; ih != null; ih = ih.next) {
			Instruction i = ih.instruction;
			if (i.isConstantPoolInstruction()) {
				InstructionCP ci = (InstructionCP) i;
				Constant c = old_cp.getConstant(ci.getIndex());
				ci.setIndex(new_cp.addConstant(c, old_cp));
			}
		}
	}

	private void clear() {
		start = end = null;
		length = 0;
	}

	/**
	 * Delete contents of list. Provides better memory utilization, because the system then may reuse the instruction handles. This
	 * method is typically called right after <href="MethodGen.html#getMethod()">MethodGen.getMethod()</a>.
	 */
	public void dispose() {
		// Traverse in reverse order, because ih.next is overwritten
		for (InstructionHandle ih = end; ih != null; ih = ih.prev) {
			/*
			 * Causes BranchInstructions to release target and targeters, because it calls dispose() on the contained instruction.
			 */
			ih.dispose();
		}

		clear();
	}

	/**
	 * @return start of list
	 */
	public InstructionHandle getStart() {
		return start;
	}

	/**
	 * @return end of list
	 */
	public InstructionHandle getEnd() {
		return end;
	}

	/**
	 * @return length of list (Number of instructions, not bytes)
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @return length of list (Number of instructions, not bytes)
	 */
	public int size() {
		return length;
	}

	/**
	 * Redirect all references from old_target to new_target, i.e., update targets of branch instructions.
	 * 
	 * @param old_target the old target instruction handle
	 * @param new_target the new target instruction handle
	 */
	public void redirectBranches(InstructionHandle old_target, InstructionHandle new_target) {
		for (InstructionHandle ih = start; ih != null; ih = ih.next) {
			Instruction i = ih.getInstruction();

			if (i instanceof InstructionBranch) {
				InstructionBranch b = (InstructionBranch) i;
				InstructionHandle target = b.getTarget();

				if (target == old_target) {
					b.setTarget(new_target);
				}

				if (b instanceof InstructionSelect) { // Either LOOKUPSWITCH or TABLESWITCH
					InstructionHandle[] targets = ((InstructionSelect) b).getTargets();

					for (int j = 0; j < targets.length; j++) {
						if (targets[j] == old_target) {
							((InstructionSelect) b).setTarget(j, new_target);
						}
					}
				}
			}
		}
	}

	/**
	 * Redirect all references of local variables from old_target to new_target.
	 * 
	 * @param lg array of local variables
	 * @param old_target the old target instruction handle
	 * @param new_target the new target instruction handle
	 * @see MethodGen
	 */
	public void redirectLocalVariables(LocalVariableGen[] lg, InstructionHandle old_target, InstructionHandle new_target) {
		for (LocalVariableGen localVariableGen : lg) {
			InstructionHandle start = localVariableGen.getStart();
			InstructionHandle end = localVariableGen.getEnd();

			if (start == old_target) {
				localVariableGen.setStart(new_target);
			}
			if (end == old_target) {
				localVariableGen.setEnd(new_target);
			}
		}
	}

	/**
	 * Redirect all references of exception handlers from old_target to new_target.
	 * 
	 * @param exceptions array of exception handlers
	 * @param old_target the old target instruction handle
	 * @param new_target the new target instruction handle
	 * @see MethodGen
	 */
	public void redirectExceptionHandlers(CodeExceptionGen[] exceptions, InstructionHandle old_target, InstructionHandle new_target) {
		for (CodeExceptionGen exception : exceptions) {
			if (exception.getStartPC() == old_target) {
				exception.setStartPC(new_target);
			}

			if (exception.getEndPC() == old_target) {
				exception.setEndPC(new_target);
			}

			if (exception.getHandlerPC() == old_target) {
				exception.setHandlerPC(new_target);
			}
		}
	}

}
