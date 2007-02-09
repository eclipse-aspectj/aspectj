package org.aspectj.apache.bcel.verifier.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.StackMapFrame;
import org.aspectj.apache.bcel.classfile.StackMapTable;
import org.aspectj.apache.bcel.generic.BranchInstruction;
import org.aspectj.apache.bcel.generic.CodeExceptionGen;
import org.aspectj.apache.bcel.generic.ConstantPoolGen;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionTargeter;
import org.aspectj.apache.bcel.generic.MethodGen;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.RET;
import org.aspectj.apache.bcel.generic.ReturnaddressType;
import org.aspectj.apache.bcel.generic.Type;


public class StackMapHelper {

	public static int count = 0;
	private Map framesAtBranchDestinations /*<InstructionHandle,Frame>*/= new HashMap();
	private boolean debugPumping=false;

	public StackMapHelper() {}
		
	/** Merge the new frame with anything we already know */
	private void storeFrameAtBranchDestination(InstructionHandle iHandle,Object newFrame) {
		if (!iHandle.isJumpDestination()) return;
		Frame existingFrame = (Frame)framesAtBranchDestinations.get(iHandle);
		if (debugPumping) System.out.println(((Frame)newFrame).toString("Storing at "+iHandle.getPosition()+":"));
		if (existingFrame==null) {
			framesAtBranchDestinations.put(iHandle,newFrame);
		} else { // merge
			Frame f = (Frame)newFrame;
			existingFrame.getLocals().merge(f.getLocals());
			existingFrame.getStack().merge(f.getStack());
		}
		count++; // number of frames stored
	}
	
	/**
	 * For everything targeted as a jump destination, create the frame. needs to return offsets to
	 */
	private Frame[] getFramesAtBranchDestinations(InstructionHandle iHandle) {	
		Frame[] outputFrames = new Frame[framesAtBranchDestinations.size()];
		int i=0;
		while (iHandle!=null) {
			if (iHandle.isJumpDestination()) {
			    Frame f = (Frame)framesAtBranchDestinations.get(iHandle);
		    	f.position = iHandle.getPosition(); // fix it for StackMapFrame creation
		    	outputFrames[i++]=f;
			}
			iHandle = iHandle.getNext();
		}
		return outputFrames;
	}
	
	/**
	 * Drive the lightweight verifier process to produce the Frames that will be used
	 * to construct the StackMapTable attribute.
	 */
	public StackMapTable produceStackMapTableAttribute(MethodGen mg) {
		// Don't bother if there is no code
		if (mg.isAbstract() || mg.isNative() ) return null;
		
		ConstantPoolGen cpg = mg.getConstantPool();
		mg.getInstructionList().setPositions(); // fixes up the stupid bloody optimizations of LDC
		ExecutionVisitor           executionVisitor = new ExecutionVisitor(cpg);
		ControlFlowGraph                 cflowGraph = new ControlFlowGraph(mg);
		Frame                          initialFrame = createInitialFrame(mg);
		CodeExceptionGen[] handlers                 = mg.getExceptionHandlers();

		// now the initial frame is setup and we can start 'running the code'
		if (debugPumping) System.err.println("Initial frame at start of method: "+initialFrame.toString());
		
		InstructionContext contextOfFirstInstruction = cflowGraph.contextOf(mg.getInstructionList().getStart());
		processInstructions(cflowGraph,contextOfFirstInstruction,initialFrame,executionVisitor,handlers);
		Frame[] frames = getFramesAtBranchDestinations(mg.getInstructionList().getStart());
		StackMapTable table = StackMapTable.forFrames(frames,cpg);
		return table;
	}
	
	private boolean isRETInstruction(InstructionContext ic) {
		return (ic.getInstruction().getInstruction() instanceof RET);
	}

	/**
	 * Whenever the outgoing frame situation of an InstructionContext changes, 
	 * all its successors are put [back] into the queue [as if they were unvisited].
	 * The proof of termination is about the existence of a fix point of frame merging.
	 */
	
	private void processInstructions( ControlFlowGraph flowGraph, InstructionContext start, Frame initialFrame, ExecutionVisitor executionVis, CodeExceptionGen[] handlers) {
		
		InstructionContextQueue queue = new InstructionContextQueue();
		
		// Passing in an empty array list here indicates no instruction was executed previously, 
		// which is interpreted as this being a 'top level' routine - no previous Jsr
		start.execute(initialFrame, new ArrayList(), executionVis);	
		storeFrameAtBranchDestination(start.getInstruction(),initialFrame.clone());
		queue.add(start, new ArrayList());
		
		while (!queue.isEmpty()){			
			
			// Retrieve the next instruction context to look at and the execution chain we followed to get to it
			InstructionContext instructionToProcess = queue.getIC(0);
			ArrayList          executionChain       = queue.getEC(0);
			queue.remove(0);
			
			
			Frame incomingFrame = instructionToProcess.getOutFrame(executionChain);
			executionChain.add(instructionToProcess);
//			Frame outgoingFrame = null;

			if (isRETInstruction(instructionToProcess)) {
				// We can only follow *one* successor, the one after the JSR that was recently executed.
				RET ret = (RET) (instructionToProcess.getInstruction().getInstruction());
				ReturnaddressType t = (ReturnaddressType) incomingFrame.getLocals().get(ret.getIndex());
				InstructionContext theSuccessor = flowGraph.contextOf(t.getTarget());				
				if (theSuccessor.execute((Frame)incomingFrame.clone(), executionChain, executionVis)) {
					queue.add(theSuccessor, executionChain);
				}
			} else { // not a RET instruction
				// Normal successors - add them to the queue of successors
				InstructionContext[] successors = instructionToProcess.getSuccessors();
				for (int s=0; s<successors.length; s++) {
					InstructionContext nextSuccessor = successors[s];
					Frame outgoingFrame = (Frame)incomingFrame.clone();
//					System.out.println("Processed: "+instructionToProcess);
					storeFrameAtBranchDestination(nextSuccessor.getInstruction(),outgoingFrame.clone());
					if (nextSuccessor.execute(outgoingFrame, executionChain, executionVis)){
						queue.add(nextSuccessor,  executionChain);
					}
				}
			}
			
			// Exception Handlers. Add them to the queue of successors.
			// [subroutines are never protected; mandated by JustIce]
			ExceptionHandler[] exc_hds = instructionToProcess.getExceptionHandlers();
			for (int s=0; s<exc_hds.length; s++){
				// TODO: the "oldchain" and "newchain" is used to determine the subroutine
				// we're in (by searching for the last JSR) by the InstructionContext
				// implementation. Therefore, we should not use this chain mechanism
				// when dealing with exception handlers.
				// Example: a JSR with an exception handler as its successor does not
				// mean we're in a subroutine if we go to the exception handler.
				// We should address this problem later; by now we simply "cut" the chain
				// by using an empty chain for the exception handlers.
				InstructionContext theExceptionHandlerBlock = flowGraph.contextOf(exc_hds[s].getHandlerStart());
				Frame f = new Frame(incomingFrame.getLocals(), new OperandStack (incomingFrame.getStack().maxStack(), (exc_hds[s].getExceptionType()==null? Type.THROWABLE : exc_hds[s].getExceptionType())) );
//				System.out.println("Processed: "+instructionToProcess);
				storeFrameAtBranchDestination(theExceptionHandlerBlock.getInstruction(), f.clone());
				if (theExceptionHandlerBlock.execute(f, new ArrayList(), executionVis)){
					queue.add(theExceptionHandlerBlock, new ArrayList());
				}
			}
		}	
 	}

	// ---
	public static Frame createInitialFrame(MethodGen mg) {
		Frame initialFrame = new Frame(mg.getMaxLocals(),mg.getMaxStack());
		int pos=0;
		if ( !mg.isStatic() ) {
			if (mg.getName().equals(Constants.CONSTRUCTOR_NAME)){
				Frame._this = new UninitializedObjectType(new ObjectType(mg.getClassName()),true,-1); // was clazz.getClassName
				initialFrame.getLocals().set(pos++, Frame._this);
			} else {
				Frame._this = null;
				initialFrame.getLocals().set(pos++, new ObjectType(mg.getClassName()));
			}
		}
		Type[] argtypes = mg.getArgumentTypes();
		int twoslotoffset = 0;
		for (int j=0; j<argtypes.length; j++){
			if (argtypes[j] == Type.SHORT || argtypes[j] == Type.BYTE || argtypes[j] == Type.CHAR || argtypes[j] == Type.BOOLEAN){
				argtypes[j] = Type.INT;
			}
			initialFrame.getLocals().set(twoslotoffset + j + (mg.isStatic()?0:1), argtypes[j]);
			if (argtypes[j].getSize() == 2) {
				twoslotoffset++;
				initialFrame.getLocals().set(twoslotoffset + j + (mg.isStatic()?0:1), Type.TOP); // should that be 'top' too (as in filler...)
			}
			pos = twoslotoffset + j + (mg.isStatic()?0:1);
		}
		initialFrame.getLocals().haveSet(pos+1);
		return initialFrame;
	}

	
	
	/**
	 * Constructs the initial frame for a method then applies each of the table entries in turn, the return value is
	 * an array of complete 'unpacked' frames.
	 */
	public static Frame[] reconstructFrames(MethodGen method,StackMapTable table) {
		boolean debug=true;
		StringBuffer offsets = new StringBuffer();
		Frame theFrame = StackMapHelper.createInitialFrame(method);
		
		if (debug) System.out.println("Initial frame is "+theFrame);
		StackMapFrame[] frames = table.getStackMap();
		int offset=0;
		Frame[] fullFrames = new Frame[frames.length+1];
		fullFrames[0] = (Frame)theFrame.clone();
		for (int f = 0; f < frames.length; f++) {
			StackMapFrame frame = frames[f];
			if (f==0) offset=frame.getByteCodeOffset();
			else offset+=1+frame.getByteCodeOffset();
			theFrame.apply(frame);
			fullFrames[f+1]=(Frame)theFrame.clone();
			offsets.append(offset+" ");
			if (debug) System.out.println("Current frame (offset="+offset+") now "+theFrame);
		}
		System.err.println("Frame offsets: "+offsets.toString());
		return fullFrames;
	}
}
