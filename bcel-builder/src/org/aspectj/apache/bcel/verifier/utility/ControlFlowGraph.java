package org.aspectj.apache.bcel.verifier.utility;

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

import java.util.Hashtable;

import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.verifier.exc.AssertionViolatedException;

/**
 * This class represents a control flow graph of a method.
 *
 * @version $Id$
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public class ControlFlowGraph {

	// The MethodGen object we're working on
	private final MethodGen method;

	// The Subroutines for the method
	final Subroutines subroutines;

	// The ExceptionHandlers object for the method
	final ExceptionHandlers exceptionhandlers;

	// All InstructionContext instances of this ControlFlowGraph
	private Hashtable /*InstructionHandle > InstructionContextImpl*/ instructionContexts = new Hashtable(); 

	// ---
	public Subroutines getSubroutines() {
		return subroutines;
	}
	
	public ControlFlowGraph(MethodGen method){
		subroutines       = new Subroutines(method);
		exceptionhandlers = new ExceptionHandlers(method);

		InstructionHandle[] instructionhandles = method.getInstructionList().getInstructionHandles();
		for (int i=0; i<instructionhandles.length; i++) {
			instructionContexts.put(instructionhandles[i], new InstructionContextImpl(this, instructionhandles[i]));
		}
		
		this.method = method;
	}

	/**
	 * Returns the InstructionContext of a given instruction.
	 */
	public InstructionContext contextOf(InstructionHandle inst){
		InstructionContext ic = (InstructionContext) instructionContexts.get(inst);
		if (ic == null){
			throw new AssertionViolatedException("InstructionContext requested for an InstructionHandle that's not known!");
		}
		return ic;
	}

	/**
	 * Returns the InstructionContext[] of a given InstructionHandle[],
	 * in a naturally ordered manner.
	 */
	public InstructionContext[] contextsOf(InstructionHandle[] insts){
		InstructionContext[] ret = new InstructionContext[insts.length];
		for (int i=0; i<insts.length; i++){
			ret[i] = contextOf(insts[i]);
		}
		return ret;
	}

	/**
	 * Returns an InstructionContext[] with all the InstructionContext instances
	 * for the method whose control flow is represented by this ControlFlowGraph
	 * <B>(NOT ORDERED!)</B>.
	 */
	public InstructionContext[] getInstructionContexts(){
		InstructionContext[] ret = new InstructionContext[instructionContexts.values().size()];
		return (InstructionContext[]) instructionContexts.values().toArray(ret);
	}

	/**
	 * Returns true, if and only if the said instruction is not reachable; that means,
	 * if it not part of this ControlFlowGraph.
	 */
	public boolean isDead(InstructionHandle i){
		return instructionContexts.containsKey(i);
	}	 
}
