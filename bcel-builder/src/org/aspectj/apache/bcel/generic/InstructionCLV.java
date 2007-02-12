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
		if (localVariableIndex!=getIndex())//allow this, shouldnt really...
		throw new ClassGenException("Do not attempt to modify the index for this constant instruction: "+this);
	}

}
