/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver.bcel;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Attribute;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.BranchHandle;
import org.apache.bcel.generic.BranchInstruction;
import org.apache.bcel.generic.CPInstruction;
import org.apache.bcel.generic.CodeExceptionGen;
import org.apache.bcel.generic.Instruction;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.InstructionTargeter;
import org.apache.bcel.generic.LineNumberGen;
import org.apache.bcel.generic.LocalVariableGen;
import org.apache.bcel.generic.LocalVariableInstruction;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.ObjectType;
import org.apache.bcel.generic.Select;
import org.apache.bcel.generic.Type;
import org.aspectj.weaver.AjAttribute;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.ISourceContext;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedTypeX;


/** 
 * A LazyMethodGen should be treated as a MethodGen.  It's our way of abstracting over the 
 * low-level Method objects.  It converts through {@link MethodGen} to create
 * and to serialize, but that's it. 
 * 
 * <p> At any rate, there are two ways to create LazyMethodGens.  
 * One is from a method, which
 * does work through MethodGen to do the correct thing.  
 * The other is the creation of a completely empty
 * LazyMethodGen, and it is used when we're constructing code from scratch.
 * 
 * <p> We stay away from targeters for rangey things like Shadows and Exceptions.
 */

public final class LazyMethodGen {
    private        int             accessFlags;
    private final Type            returnType;
    private final String          name;
    private final Type[]          argumentTypes;
    //private final String[]        argumentNames;
    private final String[]        declaredExceptions;
    private final InstructionList body; // leaving null for abstracts
    private final Attribute[]     attributes;
    private final LazyClassGen    enclosingClass;   
    private final BcelMethod      memberView;

    private int             maxLocals; 
    
    private boolean canInline = true;
    private boolean hasExceptionHandlers;
    
    /**
     * only used by {@link BcelClassWeaver}
     */
    List /*ShadowMungers*/ matchedShadows;
    
    // Used for interface introduction
    // this is the type of the interface the method is technically on
    public ResolvedTypeX definingType = null;
    
    public LazyMethodGen(
        int accessFlags,
        Type returnType,
        String name,
        Type[] paramTypes,
        String[] declaredExceptions,
        LazyClassGen enclosingClass) 
    {
		this.memberView = null; // ??? should be okay, since constructed ones aren't woven into
        this.accessFlags = accessFlags;
        this.returnType = returnType;
        this.name = name;
        this.argumentTypes = paramTypes;
        //this.argumentNames = Utility.makeArgNames(paramTypes.length);
        this.declaredExceptions = declaredExceptions;
        if (!Modifier.isAbstract(accessFlags)) {
            body = new InstructionList();
            setMaxLocals(calculateMaxLocals());
        } else {
            body = null;
        }
        this.attributes = new Attribute[0];
        this.enclosingClass = enclosingClass;
        assertGoodBody();
    }
       
    private int calculateMaxLocals() {
        int ret = 0;
        if (!Modifier.isStatic(accessFlags)) ret++;
        for (int i = 0, len = argumentTypes.length; i < len; i++) {
            ret += argumentTypes[i].getSize();
        }
        return ret;   
    }
    
    // build from an existing method
    public LazyMethodGen(Method m, LazyClassGen enclosingClass) {
        this.enclosingClass = enclosingClass;
        if (!(m.isAbstract() || m.isNative()) && m.getCode() == null) {
        	throw new RuntimeException("bad non-abstract method with no code: " + m + " on " + enclosingClass);
        }
        if ((m.isAbstract() || m.isNative()) && m.getCode() != null) {
        	throw new RuntimeException("bad abstract method with code: " + m + " on " + enclosingClass);
        }
        MethodGen gen = new MethodGen(m, enclosingClass.getName(), enclosingClass.getConstantPoolGen());
		this.memberView = new BcelMethod(enclosingClass.getBcelObjectType(), m);
        this.accessFlags = gen.getAccessFlags();
        this.returnType = gen.getReturnType();
        this.name = gen.getName();
        this.argumentTypes = gen.getArgumentTypes();
        //this.argumentNames = gen.getArgumentNames();
        this.declaredExceptions = gen.getExceptions();
        this.attributes = gen.getAttributes();
        this.maxLocals = gen.getMaxLocals();
        if (gen.isAbstract() || gen.isNative()) {
            body = null;
        } else {
            body = gen.getInstructionList();
            unpackHandlers(gen);
            unpackLineNumbers(gen);
            unpackLocals(gen);
        }
        assertGoodBody();
    }
    
    // XXX we're relying on the javac promise I've just made up that we won't have an early exception
    // in the list mask a later exception:  That is, for two exceptions E and F, 
    // if E preceeds F, then either E \cup F = {}, or E \nonstrictsubset F.  So when we add F,
    // we add it on the _OUTSIDE_ of any handlers that share starts or ends with it.

	// with that in mind, we merrily go adding ranges for exceptions.  

    private void unpackHandlers(MethodGen gen) {
        CodeExceptionGen[] exns = gen.getExceptionHandlers();
        if (exns != null) {
            int len = exns.length;
            if (len > 0) hasExceptionHandlers = true;
            int priority = len - 1;
            for (int i = 0; i < len; i++, priority--) {
                CodeExceptionGen exn = exns[i];
				
                InstructionHandle start = 
                    Range.genStart(
                        body, 
                        getOutermostExceptionStart(exn.getStartPC()));
                InstructionHandle end = Range.genEnd(body, getOutermostExceptionEnd(exn.getEndPC()));
                // this doesn't necessarily handle overlapping correctly!!!
                ExceptionRange er =
                    new ExceptionRange(
                        body,
                        exn.getCatchType() == null
                        ? null
                        : BcelWorld.fromBcel(exn.getCatchType()),
                        priority);
                er.associateWithTargets(start, end, exn.getHandlerPC());
                exn.setStartPC(null); // also removes from target
                exn.setEndPC(null); // also removes from target
                exn.setHandlerPC(null); // also removes from target
            }
            gen.removeExceptionHandlers();
        }
    }

	private InstructionHandle getOutermostExceptionStart(InstructionHandle ih) {
		while (true) {
			if (ExceptionRange.isExceptionStart(ih.getPrev())) {
				ih = ih.getPrev();
			} else {
				return ih;
			}
		}
	}    
	private InstructionHandle getOutermostExceptionEnd(InstructionHandle ih) {
		while (true) {
			if (ExceptionRange.isExceptionEnd(ih.getNext())) {
				ih = ih.getNext();
			} else {
				return ih;
			}
		}
	}    


    private void unpackLineNumbers(MethodGen gen) {
        LineNumberTag lr = null;
        for (InstructionHandle ih = body.getStart(); ih != null; ih = ih.getNext()) {
            InstructionTargeter[] targeters = ih.getTargeters();
            if (targeters != null) {
                for (int i = targeters.length - 1; i >= 0; i--) {
                    InstructionTargeter targeter = targeters[i];
                    if (targeter instanceof LineNumberGen) {
                        LineNumberGen lng = (LineNumberGen) targeter;
                        lng.updateTarget(ih, null);
                        lr = new LineNumberTag(lng.getSourceLine());
                    }
                }
            }
            if (lr != null) {
                ih.addTargeter(lr);
            }
        }
        gen.removeLineNumbers();
    }

    private void unpackLocals(MethodGen gen) {
        Set locals = new HashSet();
        for (InstructionHandle ih = body.getStart(); ih != null; ih = ih.getNext()) {
            InstructionTargeter[] targeters = ih.getTargeters();
            List ends = new ArrayList(0);
            if (targeters != null) {
                for (int i = targeters.length - 1; i >= 0; i--) {
                    InstructionTargeter targeter = targeters[i];
                    if (targeter instanceof LocalVariableGen) {
                        LocalVariableGen lng = (LocalVariableGen) targeter;
                        LocalVariableTag lr = new LocalVariableTag(BcelWorld.fromBcel(lng.getType()), lng.getName(), lng.getIndex());
                        if (lng.getStart() == ih) {
                            lng.setStart(null);
                            locals.add(lr);
                        } else {
                            lng.setEnd(null);
                            ends.add(lr);
                        }
                    }
                }
            }
            for (Iterator i = locals.iterator(); i.hasNext(); ) {
                ih.addTargeter((LocalVariableTag) i.next());
            }
            locals.removeAll(ends);
        }
        gen.removeLocalVariables();
    }
           
    // ===============
    
    public int allocateLocal(Type type) {
        return allocateLocal(type.getSize());
    }

    public int allocateLocal(int slots) {
        int max = getMaxLocals();
        setMaxLocals(max + slots);
        return max;
    }

    public Method getMethod() {
        MethodGen gen = pack();
        return gen.getMethod();
    }
    
    // =============================

	public String toString() {
		return toLongString();
	}

    public String toShortString() {
        org.apache.bcel.classfile.Utility util = null; // EVIL!
        String access = util.accessToString(getAccessFlags());
        
        StringBuffer buf = new StringBuffer();
        
        if (!access.equals("")) {
            buf.append(access);
            buf.append(" ");
        }
        buf.append(util.signatureToString(getReturnType().getSignature(), true));
        buf.append(" ");
        buf.append(getName());
        buf.append("(");
        {
            int len = argumentTypes.length;
            if (len > 0) {
                buf.append(util.signatureToString(argumentTypes[0].getSignature(), true));  
                for (int i = 1; i < argumentTypes.length; i++) {
                    buf.append(", ");
                    buf.append(util.signatureToString(argumentTypes[i].getSignature(), true));
                }
            }
        }
        buf.append(")");
        
        {
            int len = declaredExceptions != null ? declaredExceptions.length : 0;
            if (len > 0) {
                buf.append(" throws ");
                buf.append(declaredExceptions[0]);
                for (int i = 1; i < declaredExceptions.length; i++) {
                    buf.append(", ");
                    buf.append(declaredExceptions[i]);
                }  
            }
        }      
        return buf.toString();
    }

    public String toLongString() {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        print(new PrintStream(s));
        return new String(s.toByteArray());
    }

    public void print() {
        print(System.out);
    }

    public void print(PrintStream out) {
        out.print("  " + toShortString());
        printAspectAttributes(out);
        
        InstructionList body = getBody();
        if (body == null) {
            out.println(";");
            return;
        }
        out.println(":");
        new BodyPrinter(out).run();
        out.println("  end " + toShortString());
    }


	private void printAspectAttributes(PrintStream out) {
		ISourceContext context = null;
		if (enclosingClass != null && enclosingClass.getType() != null) {
			context = enclosingClass.getType().getSourceContext();
		}
		List as = BcelAttributes.readAjAttributes(attributes, context);
		if (! as.isEmpty()) {
			out.println("    " + as.get(0)); // XXX assuming exactly one attribute, munger...
		}
	}


    private class BodyPrinter {
        Map prefixMap = new HashMap();
        Map suffixMap = new HashMap();
        Map labelMap = new HashMap();

        InstructionList body;
        PrintStream out;
        ConstantPool pool;
        List ranges;

        BodyPrinter(PrintStream out) {
            this.pool = enclosingClass.getConstantPoolGen().getConstantPool();
            this.body = getBody();
            this.out = out;
        }

        void run() {
        	//killNops();
            assignLabels();
            print();
        }

        // label assignment
        void assignLabels() {
            LinkedList exnTable = new LinkedList();
            String pendingLabel = null;
//            boolean hasPendingTargeters = false;
            int lcounter = 0;
            for (InstructionHandle ih = body.getStart(); ih != null; ih = ih.getNext()) {
                InstructionTargeter[] targeters = ih.getTargeters();
                if (targeters != null) {
                    for (int i = targeters.length - 1; i >= 0; i--) {
                        InstructionTargeter t = targeters[i];
                        if (t instanceof ExceptionRange) {
                            // assert isRangeHandle(h);
                            ExceptionRange r = (ExceptionRange) t;
                            if (r.getStart() == ih) {
                                insertHandler(r, exnTable);
                            }
                        } else if (t instanceof BranchInstruction) {
                            if (pendingLabel == null) {
                                pendingLabel = "L" + lcounter++;
                            }
                        } else {
                            // assert isRangeHandle(h)
                        }
                    }
                }
                if (pendingLabel != null) {
                    labelMap.put(ih, pendingLabel);
                    if (! Range.isRangeHandle(ih)) { 
                        pendingLabel = null;
                    }
                }
            }
            int ecounter = 0;
            for (Iterator i = exnTable.iterator(); i.hasNext();) {
                ExceptionRange er = (ExceptionRange) i.next();
                String exceptionLabel =  "E" + ecounter++;
                labelMap.put(Range.getRealStart(er.getHandler()), exceptionLabel);
				labelMap.put(er.getHandler(), exceptionLabel);                
            }
        }
        
        // printing

        void print() {
            int depth = 0;
            int currLine = -1;
          bodyPrint:
            for (InstructionHandle ih = body.getStart(); ih != null; ih = ih.getNext()) {
                if (Range.isRangeHandle(ih)) {
                    Range r = Range.getRange(ih);
                    // don't print empty ranges, that is, ranges who contain no actual instructions
          			for (InstructionHandle xx = r.getStart(); Range.isRangeHandle(xx); xx = xx.getNext()) {
          				 if (xx == r.getEnd()) continue bodyPrint;
          			}

                    // doesn't handle nested: if (r.getStart().getNext() == r.getEnd()) continue;
                    if (r.getStart() == ih) {
                        printRangeString(r, depth++);
                    } else {
                    	if (r.getEnd() != ih) throw new RuntimeException("bad");
                        printRangeString(r, --depth);
                    }                    
                } else {
                    printInstruction(ih, depth);
                    int line = getLineNumber(ih, currLine);
                    if (line != currLine) {
                        currLine = line;
                        out.println("   (line " + line + ")");
                    } else {
                        out.println();
                    }
                }
            }
        }
        
        void printRangeString(Range r, int depth) {
            printDepth(depth);
            out.println(getRangeString(r, labelMap));
        }

        
        String getRangeString(Range r, Map labelMap) {
            if (r instanceof ExceptionRange) {
                ExceptionRange er = (ExceptionRange) r;
                return er.toString() + " -> " + labelMap.get(er.getHandler()); 
//                
//                + " PRI " + er.getPriority();
            } else {
                return r.toString();
            }
        }
        
        void printDepth(int depth) {
            pad(BODY_INDENT);
            while (depth > 0) {
                out.print("| ");
                depth--;
            }
        }


        void printLabel(String s, int depth) {
            int space = Math.max(CODE_INDENT - depth * 2, 0);
            if (s == null) {
                pad(space);
            } else {
                space = Math.max(space - (s.length() + 2), 0);
                pad(space);
                out.print(s);
                out.print(": ");
            }
        }

        void printInstruction(InstructionHandle h, int depth) {
            printDepth(depth);
            printLabel((String) labelMap.get(h), depth);   
           
            Instruction inst = h.getInstruction();
            if (inst instanceof CPInstruction) {
                CPInstruction cpinst = (CPInstruction) inst;
                out.print(Constants.OPCODE_NAMES[cpinst.getOpcode()].toUpperCase());
                out.print(" ");
                out.print(pool.constantToString(pool.getConstant(cpinst.getIndex())));
            } else if (inst instanceof Select) {
                Select sinst = (Select) inst;
                out.println(Constants.OPCODE_NAMES[sinst.getOpcode()].toUpperCase());
                int[] matches = sinst.getMatchs();
                InstructionHandle[] targets = sinst.getTargets();
                InstructionHandle defaultTarget = sinst.getTarget();
                for (int i = 0, len = matches.length; i < len; i++) {
                    printDepth(depth);
                    printLabel(null, depth);
                    out.print("  ");
                    out.print(matches[i]);
                    out.print(": \t");
                    out.println(labelMap.get(targets[i]));
                }
                printDepth(depth);
                printLabel(null, depth);
                out.print("  ");
                out.print("default: \t");
                out.print(labelMap.get(defaultTarget));
            } else if (inst instanceof BranchInstruction) {
                BranchInstruction brinst = (BranchInstruction) inst;
                out.print(Constants.OPCODE_NAMES[brinst.getOpcode()].toUpperCase());
                out.print(" ");
                out.print(labelMap.get(brinst.getTarget()));
            } else if (inst instanceof LocalVariableInstruction) {
                LocalVariableInstruction lvinst = (LocalVariableInstruction) inst;
                out.print(inst.toString(false).toUpperCase());
                int index = lvinst.getIndex();
                LocalVariableTag tag = getLocalVariableTag(h, index);
                if (tag != null) {
                    out.print("     // ");
                    out.print(tag.getType());
                    out.print(" ");
                    out.print(tag.getName());
                }
            } else {
                out.print(inst.toString(false).toUpperCase());
            }
        }




        static final int BODY_INDENT = 4;
        static final int CODE_INDENT = 16;

        void pad(int size) {
            for (int i = 0; i < size; i++) {
                out.print(" ");
            }
        }
    }


    static LocalVariableTag getLocalVariableTag(
        InstructionHandle ih,
        int index) 
    {
        InstructionTargeter[] targeters = ih.getTargeters();
        if (targeters == null) return null;
        for (int i = targeters.length - 1; i >= 0; i--) {
            InstructionTargeter t = targeters[i];
            if (t instanceof LocalVariableTag) {
                LocalVariableTag lvt = (LocalVariableTag) t;
                if (lvt.getSlot() == index) return lvt;
            }
        } 
        return null;
    }
    
    static int getLineNumber(
        InstructionHandle ih,
        int prevLine) 
    {
        InstructionTargeter[] targeters = ih.getTargeters();
        if (targeters == null) return prevLine;
        for (int i = targeters.length - 1; i >= 0; i--) {
            InstructionTargeter t = targeters[i];
            if (t instanceof LineNumberTag) {
                return ((LineNumberTag)t).getLineNumber();
            }
        } 
        return prevLine;
    }

	public boolean isStatic() {
		return Modifier.isStatic(getAccessFlags());
	}
    
	public boolean isAbstract() {
		return Modifier.isAbstract(getAccessFlags());
	}
    
    public void addExceptionHandler(
            InstructionHandle start,
            InstructionHandle end,
            InstructionHandle handlerStart,
            ObjectType catchType,
            boolean highPriority) {
    
        InstructionHandle start1 = Range.genStart(body, start);
        InstructionHandle end1 = Range.genEnd(body, end);
                
        ExceptionRange er = 
        	new ExceptionRange(body, BcelWorld.fromBcel(catchType), highPriority);
        er.associateWithTargets(start1, end1, handlerStart);
    }

    public int getAccessFlags() {
        return accessFlags;
    }

    public Type[] getArgumentTypes() {
        return argumentTypes;
    }

//	public String[] getArgumentNames() {
//		return argumentNames;
//	}

    public LazyClassGen getEnclosingClass() {
        return enclosingClass;
    }

    public int getMaxLocals() {
        return maxLocals;
    }

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return returnType;
    }

    public void setMaxLocals(int maxLocals) {
        this.maxLocals = maxLocals;
    }
    
    public InstructionList getBody() {
        return body;
    }
       
    public boolean hasBody() { return body != null; }


    public Attribute[] getAttributes() {
        return attributes;
    }

    public String[] getDeclaredExceptions() {
        return declaredExceptions;
    }

    public String getClassName() {
        return enclosingClass.getName();
    }
    
    
    // ---- packing!
    
    public MethodGen pack() {
    	//killNops();
        MethodGen gen =
            new MethodGen(
                getAccessFlags(),
                getReturnType(),
                getArgumentTypes(),
                null, //getArgumentNames(),
                getName(),
                getEnclosingClass().getName(),
                new InstructionList(),
                getEnclosingClass().getConstantPoolGen());
        for (int i = 0, len = declaredExceptions.length; i < len; i++) {
            gen.addException(declaredExceptions[i]);
        }
        for (int i = 0, len = attributes.length; i < len; i++) {
            gen.addAttribute(attributes[i]);
        }
        if (hasBody()) {
            packBody(gen);
            gen.setMaxLocals();
            gen.setMaxStack();
        } else {
        	gen.setInstructionList(null);
        }
        return gen;
    }
        
    /** fill the newly created method gen with our body, 
     * inspired by InstructionList.copy()
     */
    public void packBody(MethodGen gen) {
        HashMap map = new HashMap();
        InstructionList fresh = gen.getInstructionList();
        
        /* Make copies of all instructions, append them to the new list
         * and associate old instruction references with the new ones, i.e.,
         * a 1:1 mapping.
         */
        for (InstructionHandle ih = getBody().getStart(); ih != null; ih = ih.getNext()) {
            if (Range.isRangeHandle(ih)) {
                continue;
            }
            Instruction i = ih.getInstruction();
            Instruction c = i.copy(); // Use clone for shallow copy

            if (c instanceof BranchInstruction)
                map.put(ih, fresh.append((BranchInstruction) c));
            else
                map.put(ih, fresh.append(c));
        }
        // at this point, no rangeHandles are in fresh.  Let's use that...

        /* Update branch targets and insert various attributes.  
         * Insert our exceptionHandlers
         * into a sorted list, so they can be added in order later.
         */
        InstructionHandle ih = getBody().getStart();
        InstructionHandle jh = fresh.getStart();

        LinkedList exnList = new LinkedList();   

        Map localVariableStarts = new HashMap();
        Map localVariableEnds = new HashMap();

        int currLine = -1;
        
        while (ih != null) {
            if (map.get(ih) == null) {
                // we're a range instruction
                Range r = Range.getRange(ih);
                if (r instanceof ExceptionRange) {
                    ExceptionRange er = (ExceptionRange) r;
                    if (er.getStart() == ih) {
                    	//System.err.println("er " + er);
                    	if (!er.isEmpty()){
                        	// order is important, insert handlers in order of start
                        	insertHandler(er, exnList);
                    	}
                    }
                } else {
                    // we must be a shadow range or something equally useless, 
                    // so forget about doing anything
                }
                // just increment ih. 
                ih = ih.getNext();
            } else {
                // assert map.get(ih) == jh
                Instruction i = ih.getInstruction();
                Instruction j = jh.getInstruction();
    
                if (i instanceof BranchInstruction) {
                    BranchInstruction bi = (BranchInstruction) i;
                    BranchInstruction bj = (BranchInstruction) j;
                    InstructionHandle itarget = bi.getTarget(); // old target
    
//    				try {
                    // New target is in hash map
                    bj.setTarget(remap(itarget, map));
//    				} catch (NullPointerException e) {
//    					print();
//    					System.out.println("Was trying to remap " + bi);
//    					System.out.println("who's target was supposedly " + itarget);
//    					throw e;
//    				}
    
                    if (bi instanceof Select) { 
                        // Either LOOKUPSWITCH or TABLESWITCH
                        InstructionHandle[] itargets = ((Select) bi).getTargets();
                        InstructionHandle[] jtargets = ((Select) bj).getTargets();
    
                        for (int k = itargets.length - 1; k >= 0; k--) { 
                            // Update all targets
                            jtargets[k] = remap(itargets[k], map);
                            jtargets[k].addTargeter(bj);
                        }
                    }
                }
                
                // now deal with line numbers 
                // and store up info for local variables
                InstructionTargeter[] targeters = ih.getTargeters();
                if (targeters != null) {
                    for (int k = targeters.length - 1; k >= 0; k--) {
                        InstructionTargeter targeter = targeters[k];
                        if (targeter instanceof LineNumberTag) {
                            int line = ((LineNumberTag)targeter).getLineNumber();
                            if (line != currLine) {
                                gen.addLineNumber(jh, line);
                                currLine = line;
                            }
                        } else if (targeter instanceof LocalVariableTag) {
                            LocalVariableTag lvt = (LocalVariableTag) targeter;
                            if (i instanceof LocalVariableInstruction) {
                                int index = ((LocalVariableInstruction)i).getIndex();
                                if (lvt.getSlot() == index) {
                                    if (localVariableStarts.get(lvt) == null) {
                                        localVariableStarts.put(lvt, jh);
                                    }
                                    localVariableEnds.put(lvt, jh);
                                }
                            }
                        }
                    }
                }
                // now continue
                ih = ih.getNext();
                jh = jh.getNext();
            }
        }

        // now add exception handlers
        for (Iterator iter = exnList.iterator(); iter.hasNext();) {
            ExceptionRange r = (ExceptionRange) iter.next();
            if (r.isEmpty()) continue;
            gen.addExceptionHandler(
                remap(r.getRealStart(), map), 
                remap(r.getRealEnd(), map),
                remap(r.getHandler(), map),
                (r.getCatchType() == null)
                ? null 
                : (ObjectType) BcelWorld.makeBcelType(r.getCatchType()));
        }
        // now add local variables
        gen.removeLocalVariables();
        for (Iterator iter = localVariableStarts.keySet().iterator(); iter.hasNext(); ) {
            LocalVariableTag tag = (LocalVariableTag) iter.next();
            gen.addLocalVariable(
                tag.getName(), 
                BcelWorld.makeBcelType(tag.getType()),
                tag.getSlot(),
                (InstructionHandle) localVariableStarts.get(tag),
                (InstructionHandle) localVariableEnds.get(tag));
        }

    }

	/** This proedure should not currently be used.
	 */
//	public void killNops() {
//    	InstructionHandle curr = body.getStart();
//    	while (true) {
//			if (curr == null) break;
//			InstructionHandle next = curr.getNext();
//    		if (curr.getInstruction() instanceof NOP) {
//    			InstructionTargeter[] targeters = curr.getTargeters();
//    			if (targeters != null) {
//    				for (int i = 0, len = targeters.length; i < len; i++) {
//						InstructionTargeter targeter = targeters[i];
//						targeter.updateTarget(curr, next);
//    				}
//    			}
//				try {
//					body.delete(curr);
//				} catch (TargetLostException e) {
//				}
//    		}
//			curr = next;
//    	}
//	}

    private static InstructionHandle remap(InstructionHandle ih, Map map) {
        while (true) {
            Object ret = map.get(ih);
            if (ret == null) {
                ih = ih.getNext();
            } else {
                return (InstructionHandle) ret;
            }
        }
    }

    // exception ordering.
    // What we should be doing is dealing with priority inversions way earlier than we are
    // and counting on the tree structure.  In which case, the below code is in fact right.
    
    // XXX THIS COMMENT BELOW IS CURRENTLY WRONG. 
    // An exception A preceeds an exception B in the exception table iff:
    
    // * A and B were in the original method, and A preceeded B in the original exception table
    // * If A has a higher priority than B, than it preceeds B.
    // * If A and B have the same priority, then the one whose START happens EARLIEST has LEAST priority.
    //   in short, the outermost exception has least priority.
    // we implement this with a LinkedList.  We could possibly implement this with a java.util.SortedSet,
    // but I don't trust the only implementation, TreeSet, to do the right thing.
    
    private static void insertHandler(ExceptionRange fresh, LinkedList l) {
//        for (ListIterator iter = l.listIterator(); iter.hasNext();) {
//            ExceptionRange r = (ExceptionRange) iter.next();
//            if (fresh.getPriority() >= r.getPriority()) {
//                iter.previous();
//                iter.add(fresh);
//                return;
//            }            
//        }
        l.add(0, fresh);        
    }    



    public boolean isPrivate() {
        return Modifier.isPrivate(getAccessFlags());
    }


	// ----
	
	/** A good body is a body with the following properties:
	 *
	 * <ul>
	 * <li> For each branch instruction S in body, target T of S is in body.
	 * <li> For each branch instruction S in body, target T of S has S as a targeter.
	 * <li> For each instruction T in body, for each branch instruction S that is a 
	 *      targeter of T, S is in body.
	 * <li> For each non-range-handle instruction T in body, for each instruction S 
	 *      that is a targeter of T, S is 
	 * 		either a branch instruction, an exception range or a tag
	 * <li> For each range-handle instruction T in body, there is exactly one targeter S 
	 *      that is a range.
	 * <li> For each range-handle instruction T in body, the range R targeting T is in body.
	 * <li> For each instruction T in body, for each exception range R targeting T, R is 
	 * 		in body.
	 * <li> For each exception range R in body, let T := R.handler.  T is in body, and R is one
	 *      of T's targeters
	 * <li> All ranges are properly nested: For all ranges Q and R, if Q.start preceeds 
	 *      R.start, then R.end preceeds Q.end. 
	 * </ul>
	 *
	 * Where the shorthand "R is in body" means "R.start is in body, R.end is in body, and 
	 * any InstructionHandle stored in a field of R (such as an exception handle) is in body".
	 */
	
	public void assertGoodBody() {
		// XXX this is REALLY slow since we get the string first...
		assertGoodBody(getBody(), toString());
	}
	
	// XXX we might not want to release with any calls to this incredibly inefficient method.
	public static void assertGoodBody(InstructionList il, String from) {
		if (true) return;
		if (il == null) return;
		Set body = new HashSet();
		Stack ranges = new Stack();
		for (InstructionHandle ih = il.getStart(); ih != null; ih = ih.getNext()) {
			body.add(ih);
			if (ih.getInstruction() instanceof BranchInstruction) {
				body.add(ih.getInstruction());
			}
		}
		
		for (InstructionHandle ih = il.getStart(); ih != null; ih = ih.getNext()) {
			assertGoodHandle(ih, body, ranges, from);
			InstructionTargeter[] ts = ih.getTargeters();
			if (ts != null) {
				for (int i = ts.length - 1; i >= 0; i--) {
					assertGoodTargeter(ts[i], ih, body, from);
				}
			}
		}
	}

	private static void assertGoodHandle(InstructionHandle ih, Set body, Stack ranges, String from) {
		Instruction inst = ih.getInstruction();
		if ((inst instanceof BranchInstruction) ^ (ih instanceof BranchHandle)) {
			throw new BCException("bad instruction/handle pair in " + from);
		}
		if (Range.isRangeHandle(ih)) {
			assertGoodRangeHandle(ih, body, ranges, from);
		} else if (inst instanceof BranchInstruction) {
			assertGoodBranchInstruction((BranchHandle) ih, (BranchInstruction) inst, body, ranges, from);
		}
	}

	private static void assertGoodBranchInstruction(
		BranchHandle ih,
		BranchInstruction inst,
		Set body,
		Stack ranges,
		String from)
	{
		if (ih.getTarget() != inst.getTarget()) {
			throw new BCException("bad branch instruction/handle pair in " + from);
		}
		InstructionHandle target = ih.getTarget();
		assertInBody(target, body, from);
		assertTargetedBy(target, inst, from);
		if (inst instanceof Select) {
			Select sel = (Select) inst;
            InstructionHandle[] itargets = sel.getTargets();
            for (int k = itargets.length - 1; k >= 0; k--) { 
				assertInBody(itargets[k], body, from);
				assertTargetedBy(itargets[k], inst, from);
            }
        }
	}

	/** ih is an InstructionHandle or a BranchInstruction */
	private static void assertInBody(Object ih, Set body, String from) {
		if (! body.contains(ih)) throw new BCException("thing not in body in " + from);
	}

    private static void assertGoodRangeHandle(InstructionHandle ih, Set body, Stack ranges, String from) {
		Range r = getRangeAndAssertExactlyOne(ih, from);
		assertGoodRange(r, body, from);
		if (r.getStart() == ih) {
			ranges.push(r);
		} else if (r.getEnd() == ih) {
			if (ranges.peek() != r) throw new BCException("bad range inclusion in " + from);
			ranges.pop();
		}
    }
    
    private static void assertGoodRange(Range r, Set body, String from) {
		assertInBody(r.getStart(), body, from);
		assertRangeHandle(r.getStart(), from);
		assertTargetedBy(r.getStart(), r, from);

		assertInBody(r.getEnd(), body, from);
		assertRangeHandle(r.getEnd(), from);
		assertTargetedBy(r.getEnd(), r, from);
		
		if (r instanceof ExceptionRange) {
			ExceptionRange er = (ExceptionRange) r;
			assertInBody(er.getHandler(), body, from);
			assertTargetedBy(er.getHandler(), r, from);
		}    	
    }

	private static void assertRangeHandle(InstructionHandle ih, String from) {
		if (! Range.isRangeHandle(ih)) throw new BCException("bad range handle " + ih + " in " + from);		
	}


    private static void assertTargetedBy(
        InstructionHandle target,
        InstructionTargeter targeter,
        String from) 
    {
    	InstructionTargeter[] ts = target.getTargeters();
    	if (ts == null) throw new BCException("bad targeting relationship in " + from);
    	for (int i = ts.length - 1; i >= 0; i--) {
    		if (ts[i] == targeter) return;
    	}
		throw new RuntimeException("bad targeting relationship in " + from);
    }
	
	private static void assertTargets(InstructionTargeter targeter, InstructionHandle target, String from) {
		if (targeter instanceof Range) {
			Range r = (Range) targeter;
			if (r.getStart() == target || r.getEnd() == target) return;
			if (r instanceof ExceptionRange) {
				if (((ExceptionRange)r).getHandler() == target) return;
			}
		} else if (targeter instanceof BranchInstruction) {
			BranchInstruction bi = (BranchInstruction) targeter;
			if (bi.getTarget() == target) return;
			if (targeter instanceof Select) {
				Select sel = (Select) targeter;
	            InstructionHandle[] itargets = sel.getTargets();
	            for (int k = itargets.length - 1; k >= 0; k--) { 
					if (itargets[k] == target) return;
	            }
			}			
		} else if (targeter instanceof Tag) {
			return;
		}
		throw new BCException(targeter + " doesn't target " + target + " in " + from );
	}

    private static Range getRangeAndAssertExactlyOne(InstructionHandle ih, String from) {
    	Range ret = null;
    	InstructionTargeter[] ts = ih.getTargeters();
    	if (ts == null) throw new BCException("range handle with no range in " + from);
    	for (int i = ts.length - 1; i >= 0; i--) {
    		if (ts[i] instanceof Range) {
    			if (ret != null) throw new BCException("range handle with multiple ranges in " + from);
    			ret = (Range) ts[i];
    		}
    	}
    	if (ret == null) throw new BCException("range handle with no range in " + from);
		return ret;
    }

    private static void assertGoodTargeter(
        InstructionTargeter t,
        InstructionHandle ih,
        Set body,
        String from)
    {
    	assertTargets(t, ih, from);
		if (t instanceof Range) {
			assertGoodRange((Range) t, body, from);
		} else if (t instanceof BranchInstruction) {
			assertInBody(t, body, from);
		}
    }
    
    
    // ----
    
    boolean isAdviceMethod() {
    	return memberView.getAssociatedShadowMunger() != null;
    }
    
    boolean isAjSynthetic() {
    	if (memberView == null) return true;
    	return memberView.isAjSynthetic();
    }
    
    public AjAttribute.EffectiveSignatureAttribute getEffectiveSignature() {
    	//if (memberView == null) return null;
    	return memberView.getEffectiveSignature();
    }
    
	public String getSignature() {
		return Member.typesToSignature(BcelWorld.fromBcel(getReturnType()), 
										BcelWorld.fromBcel(getArgumentTypes()));
	}

	public BcelMethod getMemberView() {
		return memberView;
	}

	public void forcePublic() {
		accessFlags = Utility.makePublic(accessFlags);
	}

	public boolean getCanInline() {
		return canInline;
	}

	public void setCanInline(boolean canInline) {
		this.canInline = canInline;
	}
	public boolean hasExceptionHandlers() {
		return hasExceptionHandlers;
	}

}
