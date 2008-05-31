/* *******************************************************************
 * Copyright (c) 2008 Contributors
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Andy Clement     initial implementation 
 * ******************************************************************/
package org.aspectj.weaver.bcel;

import java.util.Iterator;
import java.util.List;

import org.aspectj.apache.bcel.classfile.annotation.AnnotationGen;
import org.aspectj.apache.bcel.classfile.annotation.ElementNameValuePairGen;
import org.aspectj.apache.bcel.classfile.annotation.EnumElementValueGen;
import org.aspectj.apache.bcel.generic.Instruction;
import org.aspectj.apache.bcel.generic.InstructionFactory;
import org.aspectj.apache.bcel.generic.InstructionList;
import org.aspectj.apache.bcel.generic.Type;
import org.aspectj.weaver.AnnotationX;
import org.aspectj.weaver.Member;
import org.aspectj.weaver.ResolvedMember;
import org.aspectj.weaver.ResolvedType;
import org.aspectj.weaver.Shadow;
import org.aspectj.weaver.UnresolvedType;

/**
 * For creating the code that accesses a field of an annotation.
 * 
 */
 // TODO 2 tidy this code up, don't make it a delegator for AnnotationAccessVar - don't override irrelevant methods
public class AnnotationAccessFieldVar extends BcelVar {

    AnnotationAccessVar aav;
    ResolvedType f;

    public AnnotationAccessFieldVar(AnnotationAccessVar aav, ResolvedType field) {
        super(field, 0);
        this.aav = aav;
        this.f = field;
    }

    void appendConvertableArrayLoad(InstructionList il, InstructionFactory fact, int index, ResolvedType convertTo) {
        // TODO Auto-generated method stub
        super.appendConvertableArrayLoad(il, fact, index, convertTo);
    }

    void appendConvertableArrayStore(InstructionList il, InstructionFactory fact, int index, BcelVar storee) {
        // TODO Auto-generated method stub
        super.appendConvertableArrayStore(il, fact, index, storee);
    }

    public void appendLoad(InstructionList il, InstructionFactory fact) {
        // TODO Auto-generated method stub
        super.appendLoad(il, fact);
    }

    public void appendLoadAndConvert(InstructionList il, InstructionFactory fact, ResolvedType toType) {
        System.err.println("Loading " + toType);
        if (aav.getKind() == Shadow.MethodExecution) {
            // So we have an entity that has an annotation on and within it is the value we want
            Member holder = aav.getMember();
            AnnotationX[] annos = holder.getAnnotations();
            for (int i = 0; i < annos.length; i++) {
                AnnotationGen ag = annos[i].getBcelAnnotation();
                List vals = ag.getValues();
                boolean doneAndDusted = false;
                for (Iterator iterator = vals.iterator(); iterator.hasNext();) {
                    ElementNameValuePairGen object = (ElementNameValuePairGen) iterator.next();
                    String name = object.getNameString();
                    EnumElementValueGen v = (EnumElementValueGen) object.getValue();
                    String s = v.getEnumTypeString();
                    ResolvedType rt = toType.getWorld().resolve(UnresolvedType.forSignature(s));
                    if (rt.equals(toType)) {
                        il.append(fact.createGetStatic(rt.getName(), v.getEnumValueString(), Type.getType(rt.getSignature())));
                        doneAndDusted = true;
                    }
                }
                if (!doneAndDusted) {
                    ResolvedMember[] annotationFields = toType.getWorld().resolve(UnresolvedType.forSignature(ag.getTypeSignature())).getDeclaredMethods();
                
                    // ResolvedMember[] fs = rt.getDeclaredFields();
                     for (int ii = 0; ii < annotationFields.length; ii++) {
                        if (annotationFields[ii].getType().equals(f)) {
                            String dvalue = annotationFields[ii].getAnnotationDefaultValue();
                            // form will be LBLAHBLAHBLAH;X where X is the field within X
                            String typename = dvalue.substring(0, dvalue.lastIndexOf(';') + 1);
                            String field = dvalue.substring(dvalue.lastIndexOf(';') + 1);
                            ResolvedType rt = toType.getWorld().resolve(UnresolvedType.forSignature(typename));
                            il.append(fact.createGetStatic(rt.getName(), field, Type.getType(rt.getSignature())));
                        }
                    }
                }
            }
        } else {
            throw new RuntimeException("You, sir, are having a laugh");
        }
    }

    public void appendStore(InstructionList il, InstructionFactory fact) {
        // TODO Auto-generated method stub
        super.appendStore(il, fact);
    }

    InstructionList createConvertableArrayLoad(InstructionFactory fact, int index, ResolvedType convertTo) {
        // TODO Auto-generated method stub
        return super.createConvertableArrayLoad(fact, index, convertTo);
    }

    InstructionList createConvertableArrayStore(InstructionFactory fact, int index, BcelVar storee) {
        // TODO Auto-generated method stub
        return super.createConvertableArrayStore(fact, index, storee);
    }

    public InstructionList createCopyFrom(InstructionFactory fact, int oldSlot) {
        // TODO Auto-generated method stub
        return super.createCopyFrom(fact, oldSlot);
    }

    public Instruction createLoad(InstructionFactory fact) {
        
        return null;
    }

    public Instruction createStore(InstructionFactory fact) {
        // TODO Auto-generated method stub
        return super.createStore(fact);
    }

    public int getPositionInAroundState() {
        // TODO Auto-generated method stub
        return super.getPositionInAroundState();
    }

    public int getSlot() {
        // TODO Auto-generated method stub
        return super.getSlot();
    }

    public void insertLoad(InstructionList il, InstructionFactory fact) {
        // TODO Auto-generated method stub
        super.insertLoad(il, fact);
    }

    public void setPositionInAroundState(int positionInAroundState) {
        // TODO Auto-generated method stub
        super.setPositionInAroundState(positionInAroundState);
    }

    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }
}
