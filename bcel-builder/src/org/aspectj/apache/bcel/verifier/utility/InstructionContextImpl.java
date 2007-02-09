package org.aspectj.apache.bcel.verifier.utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.generic.ATHROW;
import org.aspectj.apache.bcel.generic.BranchInstruction;
import org.aspectj.apache.bcel.generic.GotoInstruction;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.JsrInstruction;
import org.aspectj.apache.bcel.generic.RET;
import org.aspectj.apache.bcel.generic.ReturnInstruction;
import org.aspectj.apache.bcel.generic.Select;
import org.aspectj.apache.bcel.verifier.exc.AssertionViolatedException;
import org.aspectj.apache.bcel.verifier.exc.StructuralCodeConstraintException;

/**
 * Objects of this class represent a node in a ControlFlowGraph.
 * These nodes are instructions, not basic blocks.
 */
class InstructionContextImpl implements InstructionContext{

	private final ControlFlowGraph graph;

	// The InstructionHandle this InstructionContext is wrapped around.
	private InstructionHandle instruction;

	// The 'incoming' execution Frames.
	private HashMap /* last executing JSR */ inFrames;	

	// The 'outgoing' execution Frames.
	private HashMap /* last executed JSR */ outFrames;  

	/**
	 * The 'execution predecessors' - a list of type InstructionContext 
	 * of those instances that have been execute()d before in that order.
	 */
	private List /*InstructionContext*/ executionPredecessors = null;

	// ---
	
	/**
	 * Creates an InstructionHandleImpl object from an InstructionHandle.
	 * Creation of one per InstructionHandle suffices. Don't create more.
	 */
	public InstructionContextImpl(ControlFlowGraph graph, InstructionHandle inst){
		if (inst == null) throw new AssertionViolatedException("Cannot instantiate InstructionContextImpl from NULL.");
	
		this.graph = graph;
		instruction = inst;
		inFrames    = new HashMap();
		outFrames   = new HashMap();
	}

	/**
	 * Returns the exception handlers of this instruction.
	 */
	public ExceptionHandler[] getExceptionHandlers(){
		return this.graph.exceptionhandlers.getExceptionHandlers(getInstruction());
	}

	/**
	 * Returns a clone of the "outgoing" frame situation with respect to the given ExecutionChain.
	 */	
	public Frame getOutFrame(ArrayList execChain){
		executionPredecessors = execChain;

		Frame org;

		InstructionContext jsr = lastExecutionJSR();

		org = (Frame) outFrames.get(jsr);

		if (org == null){
			throw new AssertionViolatedException("outFrame not set! This:\n"+this+"\nExecutionChain: "+getExecutionChain()+"\nOutFrames: '"+outFrames+"'.");
		}
		return org.getClone();
	}

	/**
	 * "Merges in" (vmspec2, page 146) the "incoming" frame situation;
	 * executes the instructions symbolically
	 * and therefore calculates the "outgoing" frame situation.
	 * Returns: True iff the "incoming" frame situation changed after
	 * merging with "inFrame".
	 * The execPreds ArrayList must contain the InstructionContext
	 * objects executed so far in the correct order. This is just
	 * one execution path [out of many]. This is needed to correctly
	 * "merge" in the special case of a RET's successor.
	 * <B>The InstConstraintVisitor and ExecutionVisitor instances
	 * must be set up correctly.</B>
	 * @return true - if and only if the "outgoing" frame situation
	 * changed from the one before execute()ing.
	 */
	
	private List subsetIt(List l) {
		List toKeep = new ArrayList();
		for (Iterator iter = l.iterator(); iter.hasNext();) {
			InstructionContext iCtx = (InstructionContext) iter.next();
			Instruction i = iCtx.getInstruction().getInstruction();
			if (i instanceof JsrInstruction || i instanceof RET) {
				toKeep.add(iCtx);
			}
		}
		if (toKeep.size()>0) { 
			System.err.println("keep>0!");
			return toKeep;
		} else {
			return Collections.EMPTY_LIST;
		}
	}
	
	/**
	 * Copy of other execute method but doesnt worry about constraint visitor
	 */
	public boolean execute(Frame inFrame, ArrayList execPreds, ExecutionVisitor executionVisitor) {

		executionPredecessors = subsetIt(execPreds);//(ArrayList) execPreds.clone();

		//sanity check
		if ( (lastExecutionJSR() == null) && (this.graph.subroutines.subroutineOf(getInstruction()) != this.graph.subroutines.getTopLevel() ) ){
			throw new AssertionViolatedException("Huh?! Am I '"+this+"' part of a subroutine or not?");
		}
		if ( (lastExecutionJSR() != null) && (this.graph.subroutines.subroutineOf(getInstruction()) == this.graph.subroutines.getTopLevel() ) ){
			throw new AssertionViolatedException("Huh?! Am I '"+this+"' part of a subroutine or not?");
		}

		Frame inF = (Frame) inFrames.get(lastExecutionJSR());
		if (inF == null) {// no incoming frame was set, so set it.
			inFrames.put(lastExecutionJSR(), inFrame);
			inF = inFrame;
		} else {// if there was an "old" inFrame
			if (inF.equals(inFrame)){ //shortcut: no need to merge equal frames.
				return false;
			}
			if (! mergeInFrames(inFrame)){
				return false;
			}
		}
		
		// Now we're sure the inFrame has changed!
		
		// new inFrame is already merged in, see above.		
		Frame workingFrame = inF.getClone();

		// This executes the Instruction - modifying the workingFrame
		executionVisitor.setFrame(workingFrame);
		executionVisitor.setPosition(getInstruction().getPosition());
		getInstruction().accept(executionVisitor);
		outFrames.put(lastExecutionJSR(), workingFrame);

		return true;	// new inFrame was different from old inFrame so merging them
									// yielded a different this.inFrame.	
	}
	
	public boolean execute(Frame inFrame, ArrayList execPreds, InstConstraintVisitor icv, ExecutionVisitor ev){

		executionPredecessors = subsetIt(execPreds);//(ArrayList) execPreds.clone();

		//sanity check
		if ( (lastExecutionJSR() == null) && (this.graph.subroutines.subroutineOf(getInstruction()) != this.graph.subroutines.getTopLevel() ) ){
			throw new AssertionViolatedException("Huh?! Am I '"+this+"' part of a subroutine or not?");
		}
		if ( (lastExecutionJSR() != null) && (this.graph.subroutines.subroutineOf(getInstruction()) == this.graph.subroutines.getTopLevel() ) ){
			throw new AssertionViolatedException("Huh?! Am I '"+this+"' part of a subroutine or not?");
		}

		Frame inF = (Frame) inFrames.get(lastExecutionJSR());
		if (inF == null){// no incoming frame was set, so set it.
			inFrames.put(lastExecutionJSR(), inFrame);
			inF = inFrame;
		}
		else{// if there was an "old" inFrame
			if (inF.equals(inFrame)){ //shortcut: no need to merge equal frames.
				return false;
			}
			if (! mergeInFrames(inFrame)){
				return false;
			}
		}
		
		// Now we're sure the inFrame has changed!
		
		// new inFrame is already merged in, see above.		
		Frame workingFrame = inF.getClone();

		try{
			// This verifies the InstructionConstraint for the current
			// instruction, but does not modify the workingFrame object.
//InstConstraintVisitor icv = InstConstraintVisitor.getInstance(VerifierFactory.getVerifier(method_gen.getClassName()));
			icv.setFrame(workingFrame);
			// will go bang...
			getInstruction().accept(icv);
		}
		catch(StructuralCodeConstraintException ce){
			ce.extendMessage("","\nInstructionHandle: "+getInstruction()+"\n");
			ce.extendMessage("","\nExecution Frame:\n"+workingFrame);
			extendMessageWithFlow(ce);
			throw ce;
		}

		// This executes the Instruction.
		// Therefore the workingFrame object is modified.
//ExecutionVisitor ev = ExecutionVisitor.getInstance(VerifierFactory.getVerifier(method_gen.getClassName()));
		ev.setFrame(workingFrame);
		ev.setPosition(getInstruction().getPosition());
		getInstruction().accept(ev);
		//getInstruction().accept(ExecutionVisitor.withFrame(workingFrame));
		outFrames.put(lastExecutionJSR(), workingFrame);

		return true;	// new inFrame was different from old inFrame so merging them
									// yielded a different this.inFrame.
	}

	/**
	 * Returns a simple String representation of this InstructionContext.
	 */
	public String toString(){
	//TODO: Put information in the brackets, e.g.
	//      Is this an ExceptionHandler? Is this a RET? Is this the start of
	//      a subroutine?
		String ret = getInstruction().toString(false)+"\t[InstructionContext]";
		return ret;
	}

	/**
	 * Does the actual merging (vmspec2, page 146).
	 * Returns true IFF this.inFrame was changed in course of merging with inFrame.
	 */
	private boolean mergeInFrames(Frame inFrame){
		// TODO: Can be performance-improved.
		Frame inF = (Frame) inFrames.get(lastExecutionJSR());
		OperandStack oldstack = inF.getStack().getClone();
		LocalVariables oldlocals = inF.getLocals().getClone();
		try{
			inF.getStack().merge(inFrame.getStack());
			inF.getLocals().merge(inFrame.getLocals());
		}
		catch (StructuralCodeConstraintException sce){
			extendMessageWithFlow(sce);
			throw sce;
		}
		if (	oldstack.equals(inF.getStack()) &&
					oldlocals.equals(inF.getLocals()) ){
			return false;
		}
		else{
			return true;
		}
	}

	/**
	 * Returns the control flow execution chain. This is built
	 * while execute(Frame, ArrayList)-ing the code represented
	 * by the surrounding ControlFlowGraph.
	 */
	private String getExecutionChain(){
		String s = this.toString();
		for (int i=executionPredecessors.size()-1; i>=0; i--){
			s = executionPredecessors.get(i)+"\n" + s;
		}
		return s;
	}


	/**
	 * Extends the StructuralCodeConstraintException ("e") object with an at-the-end-extended message.
	 * This extended message will then reflect the execution flow needed to get to the constraint
	 * violation that triggered the throwing of the "e" object.
	 */
	private void extendMessageWithFlow(StructuralCodeConstraintException e){
		String s = "Execution flow:\n";
		e.extendMessage("", s+getExecutionChain());
	}

	/*
	 * Fulfils the contract of InstructionContext.getInstruction().
	 */
	public InstructionHandle getInstruction(){
		return instruction;
	}

	/**
	 * Returns the InstructionContextImpl with an JSR/JSR_W
	 * that was last in the ExecutionChain, without
	 * a corresponding RET, i.e.
	 * we were called by this one.
	 * Returns null if we were called from the top level.
	 */
	private InstructionContextImpl lastExecutionJSR(){
		
		int size = executionPredecessors.size();
		int retcount = 0;
		
		for (int i=size-1; i>=0; i--){
			InstructionContextImpl current = (InstructionContextImpl) (executionPredecessors.get(i));
			Instruction currentlast = current.getInstruction().getInstruction();
			if (currentlast instanceof RET) retcount++;
			if (currentlast instanceof JsrInstruction){
				retcount--;
				if (retcount == -1) {
					throw new RuntimeException();
//					return current;
				}
			}
		}
		return null;
	}

	/* Satisfies InstructionContext.getSuccessors(). */
	public InstructionContext[] getSuccessors(){
		return this.graph.contextsOf(_getSuccessors());
	}

	/**
	 * A utility method that calculates the successors of a given InstructionHandle
	 * That means, a RET does have successors as defined here.
	 * A JsrInstruction has its target as its successor
	 * (opposed to its physical successor) as defined here.
	 */
// TODO: implement caching!
	private InstructionHandle[] _getSuccessors(){
		final InstructionHandle[] empty = new InstructionHandle[0];
		final InstructionHandle[] single = new InstructionHandle[1];
		final InstructionHandle[] pair = new InstructionHandle[2];
	
		Instruction inst = getInstruction().getInstruction();
	
		if (inst instanceof RET){
			Subroutine s = this.graph.subroutines.subroutineOf(getInstruction());
			if (s==null){ //return empty; // RET in dead code. "empty" would be the correct answer, but we know something about the surrounding project...
				throw new AssertionViolatedException("Asking for successors of a RET in dead code?!");
			}
//TODO: remove
throw new AssertionViolatedException("DID YOU REALLY WANT TO ASK FOR RET'S SUCCS?");
/*
				InstructionHandle[] jsrs = s.getEnteringJsrInstructions();
				InstructionHandle[] ret = new InstructionHandle[jsrs.length];
				for (int i=0; i<jsrs.length; i++){
					ret[i] = jsrs[i].getNext();
				}
				return ret;
*/
		}
	
		// Terminates method normally.
		if (inst instanceof ReturnInstruction){
			return empty;
		}
	
		// Terminates method abnormally, because JustIce mandates
		// subroutines not to be protected by exception handlers.
		if (inst instanceof ATHROW){
			return empty;
		}
	
		// See method comment.
		if (inst instanceof JsrInstruction){
			single[0] = ((JsrInstruction) inst).getTarget();
			return single;
		}

		if (inst instanceof GotoInstruction){
			single[0] = ((GotoInstruction) inst).getTarget();
			return single;
		}

		if (inst instanceof BranchInstruction){
			if (inst instanceof Select){
				// BCEL's getTargets() returns only the non-default targets,
				// thanks to Eli Tilevich for reporting.
				InstructionHandle[] matchTargets = ((Select) inst).getTargets();
				InstructionHandle[] ret = new InstructionHandle[matchTargets.length+1];
				ret[0] = ((Select) inst).getTarget();
				System.arraycopy(matchTargets, 0, ret, 1, matchTargets.length);
				return ret;
			}
			else{
				pair[0] = getInstruction().getNext();
				pair[1] = ((BranchInstruction) inst).getTarget();
				return pair;
			}
		}

		// default case: Fall through.		
		single[0] = getInstruction().getNext();
		return single;
	}

}