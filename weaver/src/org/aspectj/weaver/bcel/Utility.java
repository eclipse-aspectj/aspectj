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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.aspectj.apache.bcel.Constants;
import org.aspectj.apache.bcel.classfile.ClassParser;
import org.aspectj.apache.bcel.classfile.JavaClass;
import org.aspectj.apache.bcel.classfile.Method;
import org.aspectj.apache.bcel.classfile.annotation.ArrayElementValue;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePair;
import org.aspectj.apache.bcel.classfile.annotation.ElementValue;
import org.aspectj.apache.bcel.classfile.annotation.SimpleElementValue;
import org.aspectj.apache.bcel.generic.ArrayType;
import org.aspectj.apache.bcel.generic.BIPUSH;
import org.aspectj.apache.bcel.generic.BasicType;
import org.aspectj.apache.bcel.generic.BranchInstruction;
import org.aspectj.apache.bcel.generic.ConstantPushInstruction;
import org.aspectj.apache.bcel.generic.INSTANCEOF;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionConstants;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionHandle;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.InstructionTargeter;
import org.aspectj.apache.bcel.generic.InvokeInstruction;
import org.aspectj.apache.bcel.generic.LDC;
import org.aspectj.apache.bcel.generic.LineNumberTag;
import org.aspectj.apache.bcel.generic.ObjectType;
import org.aspectj.apache.bcel.generic.ReferenceType;
import org.aspectj.apache.bcel.generic.SIPUSH;
import org.aspectj.apache.bcel.generic.SWITCH;
import org.aspectj.apache.bcel.generic.Select;
import org.aspectj.apache.bcel.generic.TargetLostException;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.Lint;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.UnresolvedType;

public class Utility {

    private Utility() {
        super();
    }
	
	/*
	 * Ensure we report a nice source location - particular in the case
	 * where the source info is missing (binary weave).
	 */
	public static String beautifyLocation(ISourceLocation isl) {
		StringBuffer nice = new StringBuffer();
		if (isl==null || isl.getSourceFile()==null || isl.getSourceFile().getName().indexOf("no debug info available")!=-1) {
			nice.append("no debug info available");
	    } else {
	    	// can't use File.getName() as this fails when a Linux box encounters a path created on Windows and vice-versa
	    	int takeFrom = isl.getSourceFile().getPath().lastIndexOf('/');
	    	if (takeFrom == -1) {
	    		takeFrom = isl.getSourceFile().getPath().lastIndexOf('\\');
	    	}    			
	    	nice.append(isl.getSourceFile().getPath().substring(takeFrom +1));
	    	if (isl.getLine()!=0) nice.append(":").append(isl.getLine());
		}
		return nice.toString();
	}
    
    
    public static Instruction createSuperInvoke(
    		InstructionFactory fact,
    		BcelWorld world,
    		Member signature) {
        short kind;
        if (signature.isInterface()) {
            throw new RuntimeException("bad");
        } else if (signature.isPrivate() || signature.getName().equals("<init>")) {
           	throw new RuntimeException("unimplemented, possibly bad");
        } else if (signature.isStatic()) {
            throw new RuntimeException("bad");
        } else {
            kind = Constants.INVOKESPECIAL;
        }
        
        return fact.createInvoke(
            signature.getDeclaringType().getName(),
            signature.getName(),
            BcelWorld.makeBcelType(signature.getReturnType()),
            BcelWorld.makeBcelTypes(signature.getParameterTypes()),
            kind);
	}
    
    // XXX don't need the world now
    public static Instruction createInvoke(
    		InstructionFactory fact,
    		BcelWorld world,
    		Member signature) {
        short kind;
        if (signature.isInterface()) {
            kind = Constants.INVOKEINTERFACE;
        } else if (signature.isStatic()) {
            kind = Constants.INVOKESTATIC;
        } else if (signature.isPrivate() || signature.getName().equals("<init>")) {
            kind = Constants.INVOKESPECIAL;
        } else {
            kind = Constants.INVOKEVIRTUAL;
        }

        UnresolvedType targetType = signature.getDeclaringType();
        if (targetType.isParameterizedType()) {
        	targetType = targetType.resolve(world).getGenericType();
        }
        return fact.createInvoke(
            targetType.getName(),
            signature.getName(),
            BcelWorld.makeBcelType(signature.getReturnType()),
            BcelWorld.makeBcelTypes(signature.getParameterTypes()),
            kind);
	}

	public static Instruction createGet(InstructionFactory fact, Member signature) {
        short kind;
        if (signature.isStatic()) {
            kind = Constants.GETSTATIC;
        } else {
            kind = Constants.GETFIELD;
        }

        return fact.createFieldAccess(
            signature.getDeclaringType().getName(),
            signature.getName(),
            BcelWorld.makeBcelType(signature.getReturnType()),
            kind);
	}

    /**
     * Creae a field GET instruction
     *
     * @param fact
     * @param signature
     * @param declaringType
     * @return
     */
    public static Instruction createGetOn(InstructionFactory fact, Member signature, UnresolvedType declaringType) {
        short kind;
        if (signature.isStatic()) {
            kind = Constants.GETSTATIC;
        } else {
            kind = Constants.GETFIELD;
        }

        return fact.createFieldAccess(
            declaringType.getName(),
            signature.getName(),
            BcelWorld.makeBcelType(signature.getReturnType()),
            kind);
    }

	public static Instruction createSet(InstructionFactory fact, Member signature) {
        short kind;
        if (signature.isStatic()) {
            kind = Constants.PUTSTATIC;
        } else {
            kind = Constants.PUTFIELD;
        }

        return fact.createFieldAccess(
            signature.getDeclaringType().getName(),
            signature.getName(),
            BcelWorld.makeBcelType(signature.getReturnType()),
            kind);
	}

	public static Instruction createInvoke(
    		InstructionFactory fact,
    		JavaClass declaringClass,
    		Method newMethod) {
        short kind;
        if (newMethod.isInterface()) {
            kind = Constants.INVOKEINTERFACE;
        } else if (newMethod.isStatic()) {
            kind = Constants.INVOKESTATIC;
        } else if (newMethod.isPrivate() || newMethod.getName().equals("<init>")) {
            kind = Constants.INVOKESPECIAL;
        } else {
            kind = Constants.INVOKEVIRTUAL;
        }

        return fact.createInvoke(
            declaringClass.getClassName(),
            newMethod.getName(),
            Type.getReturnType(newMethod.getSignature()),
            Type.getArgumentTypes(newMethod.getSignature()),
            kind);
	}
	
	public static byte[] stringToUTF(String s) {
		try {
			ByteArrayOutputStream out0 = new ByteArrayOutputStream();
			DataOutputStream out1 = new DataOutputStream(out0);
			out1.writeUTF(s);
			return out0.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("sanity check");
		}
	}

    public static Instruction createInstanceof(InstructionFactory fact, ReferenceType t) {
		int cpoolEntry =
			(t instanceof ArrayType)
			? fact.getConstantPool().addArrayClass((ArrayType)t)
			: fact.getConstantPool().addClass((ObjectType)t);
		return new INSTANCEOF(cpoolEntry);
    }

    public static Instruction createInvoke(
            InstructionFactory fact,
            LazyMethodGen m) {
        short kind;
        if (m.getEnclosingClass().isInterface()) {
            kind = Constants.INVOKEINTERFACE;
        } else if (m.isStatic()) {
            kind = Constants.INVOKESTATIC;
        } else if (m.isPrivate() || m.getName().equals("<init>")) {
            kind = Constants.INVOKESPECIAL;
        } else {
            kind = Constants.INVOKEVIRTUAL;
        }

        return fact.createInvoke(
            m.getClassName(),
            m.getName(),
            m.getReturnType(),
            m.getArgumentTypes(),
            kind);
    }

    /**
     * Create an invoke instruction
     *
     * @param fact
     * @param kind INVOKEINTERFACE, INVOKEVIRTUAL..
     * @param member
     * @return
     */
    public static Instruction createInvoke(
            InstructionFactory fact,
            short kind,
            Member member) {
        return fact.createInvoke(
            member.getDeclaringType().getName(),
            member.getName(),
            BcelWorld.makeBcelType(member.getReturnType()),
            BcelWorld.makeBcelTypes(member.getParameterTypes()),
            kind);
    }

    // ??? these should perhaps be cached.  Remember to profile this to see if it's a problem.
    public static String[] makeArgNames(int n) {
        String[] ret = new String[n];
        for (int i=0; i<n; i++) {
            ret[i] = "arg" + i;
        }
        return ret;
    }
    
    // Lookup table, for converting between pairs of types, it gives
    // us the method name in the Conversions class
    private static Hashtable validBoxing = new Hashtable();
    
    static {
      validBoxing.put("Ljava/lang/Byte;B","byteObject");
      validBoxing.put("Ljava/lang/Character;C","charObject");
      validBoxing.put("Ljava/lang/Double;D","doubleObject");
      validBoxing.put("Ljava/lang/Float;F","floatObject");
      validBoxing.put("Ljava/lang/Integer;I","intObject");
      validBoxing.put("Ljava/lang/Long;J","longObject");
      validBoxing.put("Ljava/lang/Short;S","shortObject");
      validBoxing.put("Ljava/lang/Boolean;Z","booleanObject");
      validBoxing.put("BLjava/lang/Byte;","byteValue");
      validBoxing.put("CLjava/lang/Character;","charValue");
      validBoxing.put("DLjava/lang/Double;","doubleValue");
      validBoxing.put("FLjava/lang/Float;","floatValue");
      validBoxing.put("ILjava/lang/Integer;","intValue");
      validBoxing.put("JLjava/lang/Long;","longValue");
      validBoxing.put("SLjava/lang/Short;","shortValue");
      validBoxing.put("ZLjava/lang/Boolean;","booleanValue");
    }
    
    public static void appendConversion(
        InstructionList il,
        InstructionFactory fact,
        ResolvedType fromType,
        ResolvedType toType)
    {
        if (! toType.isConvertableFrom(fromType) &&
        	 ! fromType.isConvertableFrom(toType)) {
            throw new BCException("can't convert from " + fromType + " to " + toType);
        }
	    // XXX I'm sure this test can be simpler but my brain hurts and this works
        if (!toType.getWorld().isInJava5Mode()) {
        	if (toType.needsNoConversionFrom(fromType)) return;
        } else {
        	if (toType.needsNoConversionFrom(fromType) && !(toType.isPrimitiveType()^fromType.isPrimitiveType())) return;
        }
        if (toType.equals(ResolvedType.VOID)) {
            // assert fromType.equals(UnresolvedType.OBJECT)
            il.append(InstructionFactory.createPop(fromType.getSize()));
        } else if (fromType.equals(ResolvedType.VOID)) {
            // assert toType.equals(UnresolvedType.OBJECT)
            il.append(InstructionFactory.createNull(Type.OBJECT));
            return;
        } else if (fromType.equals(UnresolvedType.OBJECT)) {
            Type to = BcelWorld.makeBcelType(toType);
            if (toType.isPrimitiveType()) {
                String name = toType.toString() + "Value";
                il.append(
                    fact.createInvoke(
                        "org.aspectj.runtime.internal.Conversions",
                        name,
                        to,
                        new Type[] { Type.OBJECT },
                        Constants.INVOKESTATIC));
            } else {
                il.append(fact.createCheckCast((ReferenceType)to));
            }
        } else if (toType.equals(UnresolvedType.OBJECT)) {
            // assert fromType.isPrimitive()
            Type from = BcelWorld.makeBcelType(fromType);
            String name = fromType.toString() + "Object";
            il.append(
                fact.createInvoke(
                    "org.aspectj.runtime.internal.Conversions",
                    name,
                    Type.OBJECT,
                    new Type[] { from },
                    Constants.INVOKESTATIC));
        } else if (toType.getWorld().isInJava5Mode() && validBoxing.get(toType.getSignature()+fromType.getSignature())!=null) {
        	// XXX could optimize by using any java boxing code that may be just before the call...
        	Type from   = BcelWorld.makeBcelType(fromType);
        	Type to     = BcelWorld.makeBcelType(toType);
            String name = (String)validBoxing.get(toType.getSignature()+fromType.getSignature());
            if (toType.isPrimitiveType()) {
            	il.append(
                        fact.createInvoke(
                          "org.aspectj.runtime.internal.Conversions",
                          name,
						  to,
                          new Type[]{Type.OBJECT},
                          Constants.INVOKESTATIC));
            } else {
                il.append(
                  fact.createInvoke(
                    "org.aspectj.runtime.internal.Conversions",
                    name,
                    Type.OBJECT,
                    new Type[] { from },
                    Constants.INVOKESTATIC));
                il.append(fact.createCheckCast((ReferenceType) to));
            }
        } else if (fromType.isPrimitiveType()) {
            // assert toType.isPrimitive()
            Type from = BcelWorld.makeBcelType(fromType);
            Type to = BcelWorld.makeBcelType(toType);
            try {
                il.append(fact.createCast(from, to));
            } catch (RuntimeException e) {
                il.append(fact.createCast(from, Type.INT));
                il.append(fact.createCast(Type.INT, to));
            }
        } else {
            Type to = BcelWorld.makeBcelType(toType);
            // assert ! fromType.isPrimitive() && ! toType.isPrimitive()
            il.append(fact.createCheckCast((ReferenceType) to));
        }
    }

    public static InstructionList createConversion(InstructionFactory factory,Type fromType,Type toType) {
    	return createConversion(factory,fromType,toType,false);
    }
    
    public static InstructionList createConversion(
            InstructionFactory fact,
            Type fromType,
            Type toType,
            boolean allowAutoboxing) {
        //System.out.println("cast to: " + toType);

        InstructionList il = new InstructionList();
        
        //PR71273
        if ((fromType.equals(Type.BYTE) || fromType.equals(Type.CHAR) || fromType.equals(Type.SHORT)) &&
            (toType.equals(Type.INT))) {
        	return il;
        }
        
        if (fromType.equals(toType))
            return il;
        if (toType.equals(Type.VOID)) {
            il.append(InstructionFactory.createPop(fromType.getSize()));
            return il;
        }

        if (fromType.equals(Type.VOID)) {
            if (toType instanceof BasicType) 
                throw new BCException("attempting to cast from void to basic type");
            il.append(InstructionFactory.createNull(Type.OBJECT));
            return il;
        }

        if (fromType.equals(Type.OBJECT)) {
            if (toType instanceof BasicType) {
                String name = toType.toString() + "Value";
                il.append(
                    fact.createInvoke(
                        "org.aspectj.runtime.internal.Conversions",
                        name,
                        toType,
                        new Type[] { Type.OBJECT },
                        Constants.INVOKESTATIC));
                return il;
            }
        }

        if (toType.equals(Type.OBJECT)) {
            if (fromType instanceof BasicType) {
                String name = fromType.toString() + "Object";
                il.append(
                    fact.createInvoke(
                        "org.aspectj.runtime.internal.Conversions",
                        name,
                        Type.OBJECT,
                        new Type[] { fromType },
                        Constants.INVOKESTATIC));
                return il;
            } else if (fromType instanceof ReferenceType) {
                return il;
            } else {
                throw new RuntimeException();
            }
        }
        
        if (fromType instanceof ReferenceType 
                && ((ReferenceType)fromType).isAssignmentCompatibleWith(toType)) {
            return il;
        }
        
        if (allowAutoboxing) {
	        if (toType instanceof BasicType && fromType instanceof ReferenceType) {
	        	// unboxing
	        	String name = toType.toString() + "Value";
	            il.append(
	                fact.createInvoke(
	                    "org.aspectj.runtime.internal.Conversions",
	                    name,
	                    toType,
	                    new Type[] { Type.OBJECT },
	                    Constants.INVOKESTATIC));
	            return il;
	        }
	        
	        if (fromType instanceof BasicType && toType instanceof ReferenceType) {
	        	// boxing
	        	String name = fromType.toString() + "Object";
	            il.append(
	                fact.createInvoke(
	                    "org.aspectj.runtime.internal.Conversions",
	                    name,
	                    Type.OBJECT,
	                    new Type[] { fromType },
	                    Constants.INVOKESTATIC));
	            il.append(fact.createCast(Type.OBJECT, toType));
	            return il;
	        }
        }

        il.append(fact.createCast(fromType, toType));
        return il;
    }

	public static Instruction createConstant(
    		InstructionFactory fact,
    		int i) {
		Instruction inst;
		switch(i) {
			case -1: inst =  InstructionConstants.ICONST_M1; break;
			case 0: inst =  InstructionConstants.ICONST_0;	break;			
			case 1: inst =  InstructionConstants.ICONST_1; break;
			case 2: inst =  InstructionConstants.ICONST_2; break;				
			case 3: inst =  InstructionConstants.ICONST_3; break;
			case 4: inst =  InstructionConstants.ICONST_4;	break;			
			case 5: inst =  InstructionConstants.ICONST_5;	break;	
		}
		if (i <= Byte.MAX_VALUE && i >= Byte.MIN_VALUE) {
	     	inst =  new BIPUSH((byte)i);
		} else if (i <= Short.MAX_VALUE && i >= Short.MIN_VALUE) {
			inst =  new SIPUSH((short)i);
		} else {
			inst =  new LDC(fact.getClassGen().getConstantPool().addInteger(i));
		}
		return inst;
	}
	
	/** For testing purposes: bit clunky but does work */
	public static int testingParseCounter=0;

	public static JavaClass makeJavaClass(String filename, byte[] bytes) {
		try {
			testingParseCounter++;
		    ClassParser parser = new ClassParser(new ByteArrayInputStream(bytes), filename);
            return parser.parse();
		} catch (IOException e) {
			throw new BCException("malformed class file");
		}		
	}
	
    public static String arrayToString(int[] a) {
        int len = a.length;
        if (len == 0) return "[]";
        StringBuffer buf = new StringBuffer("[");
        buf.append(a[0]);
        for (int i = 1; i < len; i++) {
            buf.append(", ");
            buf.append(a[i]);
        }
        buf.append("]");
        return buf.toString();
    }

	/**
	 * replace an instruction handle with another instruction, in this case, a branch instruction.
	 * 
	 * @param ih the instruction handle to replace.
	 * @param branchInstruction the branch instruction to replace ih with
	 * @param enclosingMethod where to find ih's instruction list.
	 */
    public static void replaceInstruction(
        InstructionHandle ih,
        BranchInstruction branchInstruction,
        LazyMethodGen enclosingMethod) 
    {
        
        InstructionList il = enclosingMethod.getBody();
        InstructionHandle fresh = il.append(ih, branchInstruction);
		deleteInstruction(ih, fresh, enclosingMethod);
    }
    
    public static void replaceInstruction(
    	InstructionHandle ih,
    	InstructionList replacementInstructions,
    	LazyMethodGen enclosingMethod) {
    	InstructionList il = enclosingMethod.getBody();
    	InstructionHandle fresh = il.append(ih, replacementInstructions);
    	deleteInstruction(ih,fresh,enclosingMethod);
    }
       
	/** delete an instruction handle and retarget all targeters of the deleted instruction
	 * to the next instruction.  Obviously, this should not be used to delete
	 * a control transfer instruction unless you know what you're doing.
	 * 
	 * @param ih the instruction handle to delete.
	 * @param enclosingMethod where to find ih's instruction list.
	 */
	public static void deleteInstruction(
    	InstructionHandle ih,
    	LazyMethodGen enclosingMethod) 
    {
    	deleteInstruction(ih, ih.getNext(), enclosingMethod);
    }
    		
		
	/** delete an instruction handle and retarget all targeters of the deleted instruction
	 * to the provided target.
	 * 
	 * @param ih the instruction handle to delete
	 * @param retargetTo the instruction handle to retarget targeters of ih to.
	 * @param enclosingMethod where to find ih's instruction list.
	 */
    public static void deleteInstruction(
    	InstructionHandle ih,
    	InstructionHandle retargetTo,
    	LazyMethodGen enclosingMethod) 
    {
        InstructionList il = enclosingMethod.getBody();
        InstructionTargeter[] targeters = ih.getTargeters();
        if (targeters != null) {
            for (int i = targeters.length - 1; i >= 0; i--) {
                InstructionTargeter targeter = targeters[i];
                targeter.updateTarget(ih, retargetTo);
            }
            ih.removeAllTargeters();
        }
        try {
            il.delete(ih);
        } catch (TargetLostException e) {
            throw new BCException("this really can't happen");
        }
   	}
   	
   	/**
   	 * Fix for Bugzilla #39479, #40109 patch contributed by Andy Clement
   	 * 
   	 * Need to manually copy Select instructions - if we rely on the the 'fresh' object
   	 * created by copy(), the InstructionHandle array 'targets' inside the Select
   	 * object will not have been deep copied, so modifying targets in fresh will modify
   	 * the original Select - not what we want !  (It is a bug in BCEL to do with cloning
   	 * Select objects).
   	 * 
   	 * <pre>
   	 * declare error:
   	 *     call(* Instruction.copy()) && within(org.aspectj.weaver)
   	 *       && !withincode(* Utility.copyInstruction(Instruction)):
   	 *     "use Utility.copyInstruction to work-around bug in Select.copy()";
   	 * </pre>
   	 */
	public static Instruction copyInstruction(Instruction i) {
		if (i instanceof Select) {
			Select freshSelect = (Select)i;
				  
			// Create a new targets array that looks just like the existing one
			InstructionHandle[] targets = new InstructionHandle[freshSelect.getTargets().length];
			for (int ii = 0; ii < targets.length; ii++) {
			  targets[ii] = freshSelect.getTargets()[ii];
			}
				  
			// Create a new select statement with the new targets array
			SWITCH switchStatement =
				new SWITCH(freshSelect.getMatchs(), targets, freshSelect.getTarget());
			return (Select)switchStatement.getInstruction();	
		} else {
			return i.copy(); // Use clone for shallow copy...
		}
	}
   	

	/** returns -1 if no source line attribute */
	// this naive version overruns the JVM stack size, if only Java understood tail recursion...
//	public static int getSourceLine(InstructionHandle ih) {
//		if (ih == null) return -1;
//		
//		InstructionTargeter[] ts = ih.getTargeters();
//		if (ts != null) { 
//			for (int j = ts.length - 1; j >= 0; j--) {
//				InstructionTargeter t = ts[j];
//				if (t instanceof LineNumberTag) {
//					return ((LineNumberTag)t).getLineNumber();
//				}
//			}
//		}
//		return getSourceLine(ih.getNext());
//	}

	public static int getSourceLine(InstructionHandle ih) {//,boolean goforwards) {
		int lookahead=0;
		// arbitrary rule that we will never lookahead more than 100 instructions for a line #
		while (lookahead++ < 100) {
			if (ih == null) return -1;
			
	        InstructionTargeter[] ts = ih.getTargeters();
	        if (ts != null) { 
	            for (int j = ts.length - 1; j >= 0; j--) {
	                InstructionTargeter t = ts[j];
	                if (t instanceof LineNumberTag) {
	                	return ((LineNumberTag)t).getLineNumber();
	                }
	            }
	        }
//	        if (goforwards) ih=ih.getNext(); else 
	        	ih=ih.getPrev();
		}
		//System.err.println("no line information available for: " + ih);
        return -1;
	}

//	public static int getSourceLine(InstructionHandle ih) {
//		return getSourceLine(ih,false);
//	}
	
	// assumes that there is no already extant source line tag.  Otherwise we'll have to be better.
	public static void setSourceLine(InstructionHandle ih, int lineNumber) {
		ih.addTargeter(new LineNumberTag(lineNumber));
	}

	public static int makePublic(int i) {
		return i & ~(Modifier.PROTECTED | Modifier.PRIVATE) | Modifier.PUBLIC;
	}
	public static int makePrivate(int i) {
		return i & ~(Modifier.PROTECTED | Modifier.PUBLIC) | Modifier.PRIVATE;
	}
	public static BcelVar[] pushAndReturnArrayOfVars(
		ResolvedType[] proceedParamTypes,
		InstructionList il,
		InstructionFactory fact,
		LazyMethodGen enclosingMethod) 
	{
		int len = proceedParamTypes.length;
		BcelVar[] ret = new BcelVar[len];

		for (int i = len - 1; i >= 0; i--) {
			ResolvedType typeX = proceedParamTypes[i];
			Type type = BcelWorld.makeBcelType(typeX);
			int local = enclosingMethod.allocateLocal(type);
			
			il.append(InstructionFactory.createStore(type, local));
			ret[i] = new BcelVar(typeX, local);
		}		
		return ret;
	}

	public static boolean isConstantPushInstruction(Instruction i) {
		return (i instanceof ConstantPushInstruction) || (i instanceof LDC);
	}
	
	/**
     * Check if the annotations contain a SuppressAjWarnings annotation and
     * if that annotation specifies that the given lint message (identified
     * by its key) should be ignored.
     *
     */
    public static boolean isSuppressing(AnnotationX[] anns,String lintkey) {
    	if (anns == null) return false;
        boolean suppressed = false;
        // Go through the annotation types on the advice
        for (int i = 0;!suppressed && i<anns.length;i++) {
          // Check for the SuppressAjWarnings annotation
          if (UnresolvedType.SUPPRESS_AJ_WARNINGS.getSignature().equals(anns[i].getBcelAnnotation().getTypeSignature())) {
            // Two possibilities:
            // 1. there are no values specified (i.e. @SuppressAjWarnings)
            // 2. there are values specified (i.e. @SuppressAjWarnings("A") or @SuppressAjWarnings({"A","B"})
            List vals = anns[i].getBcelAnnotation().getValues();
            if (vals == null || vals.size()==0) { // (1)
                suppressed = true;
            } else { // (2)
            	// We know the value is an array value
            	ArrayElementValue array = (ArrayElementValue)((ElementNameValuePair)vals.get(0)).getValue();
            	ElementValue[] values = array.getElementValuesArray();
            	for (int j = 0; j < values.length; j++) {
            		// We know values in the array are strings
					SimpleElementValue value = (SimpleElementValue)values[j];
					if (value.getValueString().equals(lintkey)) {
						suppressed = true;
					}
				}
            }
          }
        }
        return suppressed;
    }
    
    public static List/*Lint.Kind*/ getSuppressedWarnings(AnnotationX[] anns, Lint lint) {
      	if (anns == null) return Collections.EMPTY_LIST;
        // Go through the annotation types
      	List suppressedWarnings = new ArrayList();
      	boolean found = false;
        for (int i = 0;!found && i<anns.length;i++) {
          // Check for the SuppressAjWarnings annotation
          if (UnresolvedType.SUPPRESS_AJ_WARNINGS.getSignature().equals(anns[i].getBcelAnnotation().getTypeSignature())) {
        	found = true;
            // Two possibilities:
            // 1. there are no values specified (i.e. @SuppressAjWarnings)
            // 2. there are values specified (i.e. @SuppressAjWarnings("A") or @SuppressAjWarnings({"A","B"})
            List vals = anns[i].getBcelAnnotation().getValues();
            if (vals == null || vals.size()==0) { // (1)
               suppressedWarnings.addAll(lint.allKinds());
            } else { // (2)
            	// We know the value is an array value
            	ArrayElementValue array = (ArrayElementValue)((ElementNameValuePair)vals.get(0)).getValue();
            	ElementValue[] values = array.getElementValuesArray();
            	for (int j = 0; j < values.length; j++) {
            		// We know values in the array are strings
					SimpleElementValue value = (SimpleElementValue)values[j];
					Lint.Kind lintKind = lint.getLintKind(value.getValueString());
					if (lintKind != null) suppressedWarnings.add(lintKind);
				}
            }
          }
        }
        return suppressedWarnings;
     }
    
     // not yet used...
	 public static boolean isSimple(Method method) {
		if (method.getCode()==null) return true;
		if (method.getCode().getCode().length>10) return false;
		InstructionList instrucs = new InstructionList(method.getCode().getCode()); // expensive!
		InstructionHandle InstrHandle = instrucs.getStart();
		while (InstrHandle != null) {                  
		  Instruction Instr = InstrHandle.getInstruction();
		  int opCode = Instr.getOpcode();
		  // if current instruction is a branch instruction, see if it's a backward branch.
		  // if it is return immediately (can't be trivial)
		  if (Instr instanceof BranchInstruction) {
		      BranchInstruction BI = (BranchInstruction) Instr;
		      if (BI.getIndex() < 0) return false;
		  } else if (Instr instanceof InvokeInstruction) {
			  // if current instruction is an invocation, indicate that it can't be trivial
		      return false;
		  }
		  InstrHandle = InstrHandle.getNext();
		}
		return true;
	 }
}