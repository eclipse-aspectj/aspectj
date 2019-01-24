package org.aspectj.apache.bcel.classfile.tests;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.generic.FieldInstruction;
import org.aspectj.apache.bcel.generic.IINC;
import org.aspectj.apache.bcel.generic.INVOKEINTERFACE;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionBranch;
import org.aspectj.apache.bcel.generic.InstructionByte;
import org.aspectj.apache.bcel.generic.InstructionCP;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionLV;
import org.aspectj.apache.bcel.generic.InstructionShort;
import org.aspectj.apache.bcel.generic.InvokeInstruction;
import org.aspectj.apache.bcel.generic.LOOKUPSWITCH;
import org.aspectj.apache.bcel.generic.MULTIANEWARRAY;
import org.aspectj.apache.bcel.generic.RET;
import org.aspectj.apache.bcel.generic.TABLESWITCH;

import junit.framework.TestCase;

// Check things that have to be true based on the specification
public class Fundamentals extends TestCase {
  
	// Checking: opcode, length, consumed stack entries, produced stack entries
	public void testInstructions() {
		
		// Instructions 000-009
		checkInstruction(InstructionConstants.NOP,0,1,0,0);
		checkInstruction(InstructionConstants.ACONST_NULL,1,1,0,1);
		checkInstruction(InstructionConstants.ICONST_M1,2,1,0,1);
		checkInstruction(InstructionConstants.ICONST_0,3,1,0,1);
		checkInstruction(InstructionConstants.ICONST_1,4,1,0,1);
		checkInstruction(InstructionConstants.ICONST_2,5,1,0,1);
		checkInstruction(InstructionConstants.ICONST_3,6,1,0,1);
		checkInstruction(InstructionConstants.ICONST_4,7,1,0,1);
		checkInstruction(InstructionConstants.ICONST_5,8,1,0,1);
		checkInstruction(InstructionConstants.LCONST_0,9,1,0,2);

		// Instructions 010-019
		checkInstruction(InstructionConstants.LCONST_1,10,1,0,2);
		checkInstruction(InstructionConstants.FCONST_0,11,1,0,1);
		checkInstruction(InstructionConstants.FCONST_1,12,1,0,1);
		checkInstruction(InstructionConstants.FCONST_2,13,1,0,1);
		checkInstruction(InstructionConstants.DCONST_0,14,1,0,2);
		checkInstruction(InstructionConstants.DCONST_1,15,1,0,2);
		checkInstruction(new InstructionByte(Constants.BIPUSH,b0),16,2,0,1);
		checkInstruction(new InstructionShort(Constants.SIPUSH,s0),17,3,0,1);
		checkInstruction(new InstructionCP(Constants.LDC,b0),18,2,0,1);
		checkInstruction(new InstructionCP(Constants.LDC_W,s0),19,2,0,1);
		
		// Instructions 020-029
		checkInstruction(new InstructionCP(Constants.LDC2_W,s0),20,3,0,2);
		checkInstruction(new InstructionLV(Constants.ILOAD,s20),21,2,0,1);
		checkInstruction(new InstructionLV(Constants.LLOAD,s20),22,2,0,2);
		checkInstruction(new InstructionLV(Constants.FLOAD,s20),23,2,0,1);
		checkInstruction(new InstructionLV(Constants.DLOAD,s20),24,2,0,2);
		checkInstruction(new InstructionLV(Constants.ALOAD,s20),25,2,0,1);
		checkInstruction(InstructionConstants.ILOAD_0,26,1,0,1);
		checkInstruction(InstructionConstants.ILOAD_1,27,1,0,1);
		checkInstruction(InstructionConstants.ILOAD_2,28,1,0,1);
		checkInstruction(InstructionConstants.ILOAD_3,29,1,0,1);
		
		// Instructions 030-039
		checkInstruction(InstructionConstants.LLOAD_0,30,1,0,2);
		checkInstruction(InstructionConstants.LLOAD_1,31,1,0,2);
		checkInstruction(InstructionConstants.LLOAD_2,32,1,0,2);
		checkInstruction(InstructionConstants.LLOAD_3,33,1,0,2);
		checkInstruction(InstructionConstants.FLOAD_0,34,1,0,1);
		checkInstruction(InstructionConstants.FLOAD_1,35,1,0,1);
		checkInstruction(InstructionConstants.FLOAD_2,36,1,0,1);
		checkInstruction(InstructionConstants.FLOAD_3,37,1,0,1);
		checkInstruction(InstructionConstants.DLOAD_0,38,1,0,2);
		checkInstruction(InstructionConstants.DLOAD_1,39,1,0,2);
		
		// Instructions 040-049
		checkInstruction(InstructionConstants.DLOAD_2,40,1,0,2);
		checkInstruction(InstructionConstants.DLOAD_3,41,1,0,2);
		checkInstruction(InstructionConstants.ALOAD_0,42,1,0,1);
		checkInstruction(InstructionConstants.ALOAD_1,43,1,0,1);
		checkInstruction(InstructionConstants.ALOAD_2,44,1,0,1);
		checkInstruction(InstructionConstants.ALOAD_3,45,1,0,1);
		checkInstruction(InstructionConstants.IALOAD,46,1,2,1);
		checkInstruction(InstructionConstants.LALOAD,47,1,2,2);
		checkInstruction(InstructionConstants.FALOAD,48,1,2,1);
		checkInstruction(InstructionConstants.DALOAD,49,1,2,2);

		// Instructions 050-059
		checkInstruction(InstructionConstants.AALOAD,50,1,2,1);
		checkInstruction(InstructionConstants.BALOAD,51,1,2,1);
		checkInstruction(InstructionConstants.CALOAD,52,1,2,1);
		checkInstruction(InstructionConstants.SALOAD,53,1,2,1);
		checkInstruction(new InstructionLV(Constants.ISTORE,s20),54,2,1,0);
		checkInstruction(new InstructionLV(Constants.LSTORE,s20),55,2,2,0);
		checkInstruction(new InstructionLV(Constants.FSTORE,s20),56,2,1,0);
		checkInstruction(new InstructionLV(Constants.DSTORE,s20),57,2,2,0);
		checkInstruction(new InstructionLV(Constants.ASTORE,s20),58,2,1,0);
		checkInstruction(InstructionConstants.ISTORE_0,59,1,1,0);
		
		// Instructions 060-069
		checkInstruction(InstructionConstants.ISTORE_1,60,1,1,0);
		checkInstruction(InstructionConstants.ISTORE_2,61,1,1,0);
		checkInstruction(InstructionConstants.ISTORE_3,62,1,1,0);
		checkInstruction(InstructionConstants.LSTORE_0,63,1,2,0);
		checkInstruction(InstructionConstants.LSTORE_1,64,1,2,0);
		checkInstruction(InstructionConstants.LSTORE_2,65,1,2,0);
		checkInstruction(InstructionConstants.LSTORE_3,66,1,2,0);
		checkInstruction(InstructionConstants.FSTORE_0,67,1,1,0);
		checkInstruction(InstructionConstants.FSTORE_1,68,1,1,0);
		checkInstruction(InstructionConstants.FSTORE_2,69,1,1,0);
		
		// Instructions 070-079
		checkInstruction(InstructionConstants.FSTORE_3,70,1,1,0);
		checkInstruction(InstructionConstants.DSTORE_0,71,1,2,0);
		checkInstruction(InstructionConstants.DSTORE_1,72,1,2,0);
		checkInstruction(InstructionConstants.DSTORE_2,73,1,2,0);
		checkInstruction(InstructionConstants.DSTORE_3,74,1,2,0);
		checkInstruction(InstructionConstants.ASTORE_0,75,1,1,0);
		checkInstruction(InstructionConstants.ASTORE_1,76,1,1,0);
		checkInstruction(InstructionConstants.ASTORE_2,77,1,1,0);
		checkInstruction(InstructionConstants.ASTORE_3,78,1,1,0);
		checkInstruction(InstructionConstants.IASTORE,79,1,3,0);
		
		// Instructions 080-089
		checkInstruction(InstructionConstants.LASTORE,80,1,4,0);
		checkInstruction(InstructionConstants.FASTORE,81,1,3,0);
		checkInstruction(InstructionConstants.DASTORE,82,1,4,0);
		checkInstruction(InstructionConstants.AASTORE,83,1,3,0);
		checkInstruction(InstructionConstants.BASTORE,84,1,3,0);
		checkInstruction(InstructionConstants.CASTORE,85,1,3,0);
		checkInstruction(InstructionConstants.SASTORE,86,1,3,0);
		checkInstruction(InstructionConstants.POP,87,1,1,0);
		checkInstruction(InstructionConstants.POP2,88,1,2,0);
		checkInstruction(InstructionConstants.DUP,89,1,1,2);
		
		// Instructions 090-099
		checkInstruction(InstructionConstants.DUP_X1,90,1,2,3);
		checkInstruction(InstructionConstants.DUP_X2,91,1,3,4);
		checkInstruction(InstructionConstants.DUP2,92,1,2,4);
		checkInstruction(InstructionConstants.DUP2_X1,93,1,3,5);
		checkInstruction(InstructionConstants.DUP2_X2,94,1,4,6);
		checkInstruction(InstructionConstants.SWAP,95,1,2,2);
		checkInstruction(InstructionConstants.IADD,96,1,2,1);
		checkInstruction(InstructionConstants.LADD,97,1,4,2);
		checkInstruction(InstructionConstants.FADD,98,1,2,1);
		checkInstruction(InstructionConstants.DADD,99,1,4,2);

		// Instructions 100-109
		checkInstruction(InstructionConstants.ISUB,100,1,2,1);
		checkInstruction(InstructionConstants.LSUB,101,1,4,2);
		checkInstruction(InstructionConstants.FSUB,102,1,2,1);
		checkInstruction(InstructionConstants.DSUB,103,1,4,2);
		checkInstruction(InstructionConstants.IMUL,104,1,2,1);
		checkInstruction(InstructionConstants.LMUL,105,1,4,2);
		checkInstruction(InstructionConstants.FMUL,106,1,2,1);
		checkInstruction(InstructionConstants.DMUL,107,1,4,2);
		checkInstruction(InstructionConstants.IDIV,108,1,2,1);
		checkInstruction(InstructionConstants.LDIV,109,1,4,2);
		
		// Instructions 110-119
		checkInstruction(InstructionConstants.FDIV,110,1,2,1);
		checkInstruction(InstructionConstants.DDIV,111,1,4,2);
		checkInstruction(InstructionConstants.IREM,112,1,2,1);
		checkInstruction(InstructionConstants.LREM,113,1,4,2);
		checkInstruction(InstructionConstants.FREM,114,1,2,1);
		checkInstruction(InstructionConstants.DREM,115,1,4,2);
		checkInstruction(InstructionConstants.INEG,116,1,1,1);
		checkInstruction(InstructionConstants.LNEG,117,1,2,2);
		checkInstruction(InstructionConstants.FNEG,118,1,1,1);
		checkInstruction(InstructionConstants.DNEG,119,1,2,2);
		
		// Instructions 120-129
		checkInstruction(InstructionConstants.ISHL,120,1,2,1);
		checkInstruction(InstructionConstants.LSHL,121,1,3,2);
		checkInstruction(InstructionConstants.ISHR,122,1,2,1);
		checkInstruction(InstructionConstants.LSHR,123,1,3,2);
		checkInstruction(InstructionConstants.IUSHR,124,1,2,1);
		checkInstruction(InstructionConstants.LUSHR,125,1,3,2);
		checkInstruction(InstructionConstants.IAND,126,1,2,1);
		checkInstruction(InstructionConstants.LAND,127,1,4,2);
		checkInstruction(InstructionConstants.IOR,128,1,2,1);
		checkInstruction(InstructionConstants.LOR,129,1,4,2);
		
		// Instructions 130-139
		checkInstruction(InstructionConstants.IXOR,130,1,2,1);
		checkInstruction(InstructionConstants.LXOR,131,1,4,2);
		checkInstruction(new IINC(0,0,false),132,3,0,0);
		checkInstruction(InstructionConstants.I2L,133,1,1,2);
		checkInstruction(InstructionConstants.I2F,134,1,1,1);
		checkInstruction(InstructionConstants.I2D,135,1,1,2);
		checkInstruction(InstructionConstants.L2I,136,1,2,1);
		checkInstruction(InstructionConstants.L2F,137,1,2,1);
		checkInstruction(InstructionConstants.L2D,138,1,2,2);
		checkInstruction(InstructionConstants.F2I,139,1,1,1);

		// Instructions 140-149
		checkInstruction(InstructionConstants.F2L,140,1,1,2);
		checkInstruction(InstructionConstants.F2D,141,1,1,2);
		checkInstruction(InstructionConstants.D2I,142,1,2,1);
		checkInstruction(InstructionConstants.D2L,143,1,2,2);
		checkInstruction(InstructionConstants.D2F,144,1,2,1);
		checkInstruction(InstructionConstants.I2B,145,1,1,1);
		checkInstruction(InstructionConstants.I2C,146,1,1,1);
		checkInstruction(InstructionConstants.I2S,147,1,1,1);
		checkInstruction(InstructionConstants.LCMP,148,1,4,1);
		checkInstruction(InstructionConstants.FCMPL,149,1,2,1);

		// Instructions 150-159
		checkInstruction(InstructionConstants.FCMPG,150,1,2,1);
		checkInstruction(InstructionConstants.DCMPL,151,1,4,1);
		checkInstruction(InstructionConstants.DCMPG,152,1,4,1);
		checkInstruction(new InstructionBranch(Constants.IFEQ,s0),153,3,1,0);
		checkInstruction(new InstructionBranch(Constants.IFNE,s0),154,3,1,0);
		checkInstruction(new InstructionBranch(Constants.IFLT,s0),155,3,1,0);
		checkInstruction(new InstructionBranch(Constants.IFGE,s0),156,3,1,0);
		checkInstruction(new InstructionBranch(Constants.IFGT,s0),157,3,1,0);
		checkInstruction(new InstructionBranch(Constants.IFLE,s0),158,3,1,0);
		checkInstruction(new InstructionBranch(Constants.IF_ICMPEQ,s0),159,3,2,0);

		// Instructions 160-169
		checkInstruction(new InstructionBranch(Constants.IF_ICMPNE,s0),160,3,2,0);
		checkInstruction(new InstructionBranch(Constants.IF_ICMPLT,s0),161,3,2,0);
		checkInstruction(new InstructionBranch(Constants.IF_ICMPGE,s0),162,3,2,0);
		checkInstruction(new InstructionBranch(Constants.IF_ICMPGT,s0),163,3,2,0);
		checkInstruction(new InstructionBranch(Constants.IF_ICMPLE,s0),164,3,2,0);
		checkInstruction(new InstructionBranch(Constants.IF_ACMPEQ,s0),165,3,2,0);
		checkInstruction(new InstructionBranch(Constants.IF_ACMPNE,s0),166,3,2,0);
		checkInstruction(new InstructionBranch(Constants.GOTO,s0),167,3,0,0);
		checkInstruction(new InstructionBranch(Constants.JSR,s0),168,3,0,1);
		checkInstruction(new RET(0,false),169,2,0,0);
		
		// Instructions 170-179
		checkInstruction(new TABLESWITCH(new int[]{},new InstructionHandle[]{},null),170,VARIES,1,0);
		checkInstruction(new LOOKUPSWITCH(new int[]{},new InstructionHandle[]{},null),171,VARIES,1,0);
		checkInstruction(InstructionConstants.IRETURN,172,1,1,0);
		checkInstruction(InstructionConstants.LRETURN,173,1,2,0);
		checkInstruction(InstructionConstants.FRETURN,174,1,1,0);
		checkInstruction(InstructionConstants.DRETURN,175,1,2,0);
		checkInstruction(InstructionConstants.ARETURN,176,1,1,0);
		checkInstruction(InstructionConstants.RETURN,177,1,0,0);
		checkInstruction(new FieldInstruction(Constants.GETSTATIC,0),178,3,0,VARIES);
		checkInstruction(new FieldInstruction(Constants.PUTSTATIC,0),179,3,VARIES,0);

		// Instructions 180-189
		checkInstruction(new FieldInstruction(Constants.GETFIELD,0),180,3,1,VARIES);
		checkInstruction(new FieldInstruction(Constants.PUTFIELD,0),181,3,VARIES,0);
		checkInstruction(new InvokeInstruction(Constants.INVOKEVIRTUAL,0),182,3,VARIES,VARIES); // PRODUCE STACK VARIES OK HERE? (AND NEXT COUPLE)
		checkInstruction(new InvokeInstruction(Constants.INVOKESPECIAL,0),183,3,VARIES,VARIES);
		checkInstruction(new InvokeInstruction(Constants.INVOKESTATIC,0),184,3,VARIES,VARIES);
		checkInstruction(new INVOKEINTERFACE(0,1,0),185,5,VARIES,VARIES);
		// 186 does not exist
		checkInstruction(new InstructionCP(Constants.NEW,b0),187,3,0,1);
		checkInstruction(new InstructionByte(Constants.NEWARRAY,b0),188,2,1,1);
		checkInstruction(new InstructionCP(Constants.ANEWARRAY,0),189,3,1,1);

		// Instructions 190-199
		checkInstruction(InstructionConstants.ARRAYLENGTH,190,1,1,1);
		checkInstruction(InstructionConstants.ATHROW,191,1,1,1);
		checkInstruction(new InstructionCP(Constants.CHECKCAST,s0),192,3,1,1);
		checkInstruction(new InstructionCP(Constants.INSTANCEOF,s0),193,3,1,1);
		checkInstruction(InstructionConstants.MONITORENTER,194,1,1,0);
		checkInstruction(InstructionConstants.MONITOREXIT,195,1,1,0);
		// 196 is 'wide' tag
		checkInstruction(new MULTIANEWARRAY(s0,b0),197,4,VARIES,1);
		checkInstruction(new InstructionBranch(Constants.IFNULL,s0),198,3,1,0);
		checkInstruction(new InstructionBranch(Constants.IFNONNULL,s0),199,3,1,0);
		
		// Instructions 200-209		
		checkInstruction(new InstructionBranch(Constants.GOTO_W,0),200,5,0,0);
		checkInstruction(new InstructionBranch(Constants.JSR_W,0),201,5,0,1);
		
		// Internally used instructions skipped
	}
	
	public void checkInstruction(Instruction i,int opcode, int length, int stackConsumed, int stackProduced) {
		String header = new String("Checking instruction '"+i+"' ");
		if (i.opcode!=opcode)
			fail(header+" expected opcode "+opcode+" but it is "+i.opcode);
		
		if (length!=VARIES && i.getLength()!=length)
			fail(header+" expected length "+length+" but it is "+i.getLength());
//		if (stackConsumed>0) {
//			if ((Constants.instFlags[opcode]&Constants.STACK_CONSUMER)==0)
//				fail(header+" expected it to be a STACK_CONSUMER but it is not");
//		} else {
//			if ((Constants.instFlags[opcode]&Constants.STACK_CONSUMER)!=0)
//				fail(header+" expected it not to be a STACK_CONSUMER but it is");
//		}
		if (stackConsumed==VARIES) {
			if (Constants.CONSUME_STACK[opcode]!=Constants.UNPREDICTABLE)
				fail("Instruction '"+i+"' should be consuming some unpredictable number of stack entries but it says it will consume "+Constants.CONSUME_STACK[opcode]);
			
		} else {
			if (Constants.CONSUME_STACK[opcode]!=stackConsumed)
				fail("Instruction '"+i+"' should be consuming "+stackConsumed+" stack entries but it says it will consume "+Constants.CONSUME_STACK[opcode]);
		}
//		if (stackProduced>0) {
//			if ((Constants.instFlags[opcode]&Constants.STACK_PRODUCER)==0)
//				fail(header+" expected it to be a STACK_PRODUCER but it is not");
//		} else {
//			if ((Constants.instFlags[opcode]&Constants.STACK_PRODUCER)!=0)
//				fail(header+" expected it not to be a STACK_PRODUCER but it is");
//		}
		if (stackProduced==VARIES) {
			if (Constants.stackEntriesProduced[opcode]!=Constants.UNPREDICTABLE)
				fail(header+" should be producing some unpredictable number of stack entries but it says it will produce "+Constants.stackEntriesProduced[opcode]);
		
		} else {	
			if (Constants.stackEntriesProduced[opcode]!=stackProduced)
				fail(header+" should be producing "+stackProduced+" stack entries but it says it will produce "+Constants.stackEntriesProduced[opcode]);
		}
	}

	private final static byte b0 = 0;
	private final static short s0 = 0;
	private final static short s20 = 20;
	private final static int VARIES = -1;
}
