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

import org.apache.bcel.Constants;
import org.apache.bcel.generic.InstructionFactory;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.generic.ReferenceType;
import org.apache.bcel.generic.Type;
import org.aspectj.weaver.BCException;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ast.And;
import org.aspectj.weaver.ast.Call;
import org.aspectj.weaver.ast.CallExpr;
import org.aspectj.weaver.ast.Expr;
import org.aspectj.weaver.ast.FieldGet;
import org.aspectj.weaver.ast.FieldGetCall;
import org.aspectj.weaver.ast.IExprVisitor;
import org.aspectj.weaver.ast.ITestVisitor;
import org.aspectj.weaver.ast.Instanceof;
import org.aspectj.weaver.ast.Literal;
import org.aspectj.weaver.ast.Not;
import org.aspectj.weaver.ast.Or;
import org.aspectj.weaver.ast.Test;
import org.aspectj.weaver.ast.Var;

// we generate right to left, btw.
public class BcelRenderer implements ITestVisitor, IExprVisitor {

    private InstructionList instructions;
    private InstructionFactory fact;
    private BcelWorld world;

    InstructionHandle sk, fk, next = null;

    private BcelRenderer(InstructionFactory fact, BcelWorld world) {
        super();
        this.fact = fact;
        this.world = world;
        this.instructions = new InstructionList();
    }

    // ---- renderers
    
    public static InstructionList renderExpr(
        InstructionFactory fact,
        BcelWorld world,
        Expr e) 
    {
        BcelRenderer renderer = new BcelRenderer(fact, world);
        e.accept(renderer);
        return renderer.instructions;
    }
    public static InstructionList renderExpr(
        InstructionFactory fact,
        BcelWorld world,
        Expr e,
        Type desiredType) 
    {
        BcelRenderer renderer = new BcelRenderer(fact, world);
        e.accept(renderer);
        InstructionList il = renderer.instructions;
        il.append(Utility.createConversion(fact, world.makeBcelType(e.getType()), desiredType));
        return il;
    }

    public static InstructionList renderExprs(
        InstructionFactory fact,
        BcelWorld world,
        Expr[] es) 
    {
        BcelRenderer renderer = new BcelRenderer(fact, world);
        for (int i = es.length - 1; i >= 0; i--) {
            es[i].accept(renderer);
        }
        return renderer.instructions;
    }

    /*
     * Get the instructions representing this test.
     * 
     * @param e test to render
     * @param sk instructionHandle to jump to if our rendered check succeeds (typically start of advice)
     * @param fk instructionHandle to jump to if our rendered check fails (typically after end of advice)
     * @param next instructionHandle that will follow this generated code.  Passing in null will generate
     *             one unnecessary GOTO instruction.
     * 
     * @returns the instruction list representing this expression
     */
    public static InstructionList renderTest(
        InstructionFactory fact,
        BcelWorld world,
        Test e,
        InstructionHandle sk,
        InstructionHandle fk,
        InstructionHandle next) 
    {
        BcelRenderer renderer = new BcelRenderer(fact, world);
        renderer.recur(e, sk, fk, next);
        return renderer.instructions;
    }

    /*
     * Get the instructions representing this test.
     * 
     * @param e test to render
     * @param sk instructionHandle to jump to if our rendered check succeeds (typically start of advice)
     * @param fk instructionHandle to jump to if our rendered check fails (typically after end of advice)
     * 
     * @returns the instruction list representing this expression
     */
    public static InstructionList renderTest(
        InstructionFactory fact,
        BcelWorld world,
        Test e,
        InstructionHandle sk,
        InstructionHandle fk) 
    {
        return renderTest(fact, world, e, sk, fk, null);
    }

    // ---- recurrers

    private void recur(
        Test e,
        InstructionHandle sk,
        InstructionHandle fk,
        InstructionHandle next) 
    {
        this.sk = sk;
        this.fk = fk;
        this.next = next;
        e.accept(this);
    }

    // ---- test visitors

    public void visit(And e) {
        InstructionHandle savedFk = fk;
        recur(e.getRight(), sk, fk, next);
        InstructionHandle ning = instructions.getStart();
        recur(e.getLeft(), ning, savedFk, ning);
    }

    public void visit(Or e) {
        InstructionHandle savedSk = sk;
        recur(e.getRight(), sk, fk, next);
        recur(e.getLeft(), savedSk, instructions.getStart(), instructions.getStart());
    }

    public void visit(Not e) {
        recur(e.getBody(), fk, sk, next);
    }

    public void visit(Instanceof i) {
        instructions.insert(createJumpBasedOnBooleanOnStack());
        instructions.insert(
            Utility.createInstanceof(fact, (ReferenceType) world.makeBcelType(i.getType())));
        i.getVar().accept(this);
    }

	private InstructionList createJumpBasedOnBooleanOnStack() {
		InstructionList il = new InstructionList();
        if (sk == fk) {
            // don't bother generating if it doesn't matter
            if (sk != next) {
                il.insert(fact.createBranchInstruction(Constants.GOTO, sk));
            }
            return il;
        }

        if (fk == next) {
            il.insert(fact.createBranchInstruction(Constants.IFNE, sk));
        } else if (sk == next) {
            il.insert(fact.createBranchInstruction(Constants.IFEQ, fk));
        } else {
            il.insert(fact.createBranchInstruction(Constants.GOTO, sk));
            il.insert(fact.createBranchInstruction(Constants.IFEQ, fk));
        }
        return il;		
	}


    public void visit(Literal literal) {
        if (literal == Literal.FALSE)
            throw new BCException("bad");
    }

	public void visit(Call call) {
		Member method = call.getMethod();
		// assert method.isStatic()
		Expr[] args = call.getArgs();
		//System.out.println("args: " + Arrays.asList(args));
		InstructionList callIl = new InstructionList();
		for (int i=0, len=args.length; i < len; i++) {
			//XXX only correct for static method calls
			Type desiredType = BcelWorld.makeBcelType(method.getParameterTypes()[i]);
			callIl.append(renderExpr(fact, world, args[i], desiredType));
		}
		//System.out.println("rendered args: " + callIl);
		callIl.append(Utility.createInvoke(fact, world, method));
		callIl.append(createJumpBasedOnBooleanOnStack());
		instructions.insert(callIl);		
	}

	public void visit(FieldGetCall fieldGetCall) {
		Member field = fieldGetCall.getField();
		Member method = fieldGetCall.getMethod();
		InstructionList il = new InstructionList();
		il.append(Utility.createGet(fact, field));
		// assert !method.isStatic()
		Expr[] args = fieldGetCall.getArgs();
		//System.out.println("args: " + Arrays.asList(args));
		il.append(renderExprs(fact, world, args));	
		//System.out.println("rendered args: " + callIl);
		il.append(Utility.createInvoke(fact, world, method));
		il.append(createJumpBasedOnBooleanOnStack());
		instructions.insert(il);	
	}

    // ---- expr visitors

    public void visit(Var var) {
        BcelVar bvar = (BcelVar) var;
        bvar.insertLoad(instructions, fact);
    }   
    
    public void visit(FieldGet fieldGet) {
		Member field = fieldGet.getField();
		// assert field.isStatic()
		instructions.insert(Utility.createGet(fact, field));		
    }
    
	public void visit(CallExpr call) {
		Member method = call.getMethod();
		// assert method.isStatic()
		Expr[] args = call.getArgs();
		InstructionList callIl = renderExprs(fact, world, args);	
		callIl.append(Utility.createInvoke(fact, world, method));
		instructions.insert(callIl);		
	}
}
