package org.aspectj.apache.bcel.verifier.utility;

import java.util.ArrayList;
import java.util.Vector;

/**
 * An InstructionContextQueue is a utility class that holds
 * (InstructionContext, ArrayList) pairs in a Queue data structure.
 * This is used to hold information about InstructionContext objects
 * externally --- i.e. that information is not saved inside the
 * InstructionContext object itself. This is useful to save the
 * execution path of the symbolic execution of the
 * Pass3bVerifier - this is not information
 * that belongs into the InstructionContext object itself.
 * Only at "execute()"ing
 * time, an InstructionContext object will get the current information
 * we have about its symbolic execution predecessors.
 */
final class InstructionContextQueue{
	
	private Vector instructionContexts = new Vector(); // Type: InstructionContext
	private Vector executionChains = new Vector(); // Type: ArrayList (of InstructionContext)
	
	public void add(InstructionContext ic, ArrayList executionChain){
		instructionContexts.add(ic);
		executionChains.add(executionChain);
	}
	public InstructionContext getIC(int i) { return (InstructionContext) instructionContexts.get(i); }
	public ArrayList getEC(int i) { return (ArrayList) executionChains.get(i); }
	public int size() { return instructionContexts.size(); }
	public boolean isEmpty() { return instructionContexts.isEmpty(); }
	public void remove() { this.remove(0); }
	public void remove(int i){ instructionContexts.remove(i); executionChains.remove(i); }
}