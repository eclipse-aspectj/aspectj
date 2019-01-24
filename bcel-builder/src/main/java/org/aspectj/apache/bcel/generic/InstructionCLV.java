package org.aspectj.apache.bcel.generic;

/**
 * A small subclass of the local variable accessing instruction class InstructionLV - this subclass does
 * not allow the index to be altered.
 */
public class InstructionCLV extends InstructionLV {

	public InstructionCLV(short opcode) {
		super(opcode);
	}
	
	public InstructionCLV(short opcode,int localVariableIndex) {
		super(opcode,localVariableIndex);
	}
	
	public void setIndex(int localVariableIndex) {
		if (localVariableIndex!=getIndex()) {
			throw new ClassGenException("Do not attempt to modify the index to '"+localVariableIndex+"' for this constant instruction: "+this);
		}
	}

	public boolean canSetIndex() {
	    return false;
	}

}
