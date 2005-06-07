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


package org.aspectj.weaver;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.FastMatchInfo;
import org.aspectj.weaver.patterns.PerClause;
import org.aspectj.weaver.patterns.PointcutVisitor;
import org.aspectj.weaver.patterns.PatternNode;
import org.aspectj.weaver.patterns.PerThisOrTargetPointcutVisitor;
import org.aspectj.weaver.patterns.PerObject;
import org.aspectj.weaver.patterns.TypePattern;
import org.aspectj.weaver.patterns.PerFromSuper;
import org.aspectj.util.FuzzyBoolean;

public class PerObjectInterfaceTypeMunger extends ResolvedTypeMunger {

    // key is advisedType, value is Set of aspect type that advise the type and are perObject
    public static Map s_advisedTypeToAspects = new HashMap();
    public static void registerAsAdvisedBy(ResolvedTypeX matchType, ResolvedTypeX aspectType) {
        if (PerClause.PEROBJECT.equals(aspectType.getPerClause().getKind())) {
            Set aspects = (Set)s_advisedTypeToAspects.get(matchType);
            if (aspects == null) {
                aspects = new HashSet(1);
                s_advisedTypeToAspects.put(matchType, aspects);
            }
            aspects.add(aspectType);
        }
    }
    public static void unregisterFromAsAdvisedBy(ResolvedTypeX matchType) {
        s_advisedTypeToAspects.remove(matchType);
    }

	private ResolvedMember getMethod;
	private ResolvedMember setMethod;
	private TypeX aspectType;
	private TypeX interfaceType;
	private Pointcut testPointcut;


	public PerObjectInterfaceTypeMunger(TypeX aspectType, Pointcut testPointcut) {
		super(PerObjectInterface, null);
		this.aspectType = aspectType;
		this.testPointcut = testPointcut;
		this.interfaceType = AjcMemberMaker.perObjectInterfaceType(aspectType);
		this.getMethod = AjcMemberMaker.perObjectInterfaceGet(aspectType);
		this.setMethod = AjcMemberMaker.perObjectInterfaceSet(aspectType);
	}
	

	public void write(DataOutputStream s) throws IOException {
		throw new RuntimeException("shouldn't be serialized");
	}
	public TypeX getAspectType() {
		return aspectType;
	}

	public ResolvedMember getGetMethod() {
		return getMethod;
	}

	public TypeX getInterfaceType() {
		return interfaceType;
	}

	public ResolvedMember getSetMethod() {
		return setMethod;
	}

	public Pointcut getTestPointcut() {
		return testPointcut;
	}
	
	public boolean matches(ResolvedTypeX matchType, ResolvedTypeX aspectType) {
        if (matchType.isInterface()) return false;

        //FIXME AV - cache that
        final boolean isPerThis;
        if (aspectType.getPerClause() instanceof PerFromSuper) {
            PerFromSuper ps = (PerFromSuper) aspectType.getPerClause();
            isPerThis = ((PerObject)ps.lookupConcretePerClause(aspectType)).isThis();
        } else {
            isPerThis = ((PerObject)aspectType.getPerClause()).isThis();
        }
        PerThisOrTargetPointcutVisitor v = new PerThisOrTargetPointcutVisitor(
                !isPerThis,
                aspectType
        );
        TypePattern tp = v.getPerTypePointcut(testPointcut);

        if (true) return tp.matchesStatically(matchType);


        //FIXME ATAJ waiting Andy patch...
        // comment from Andy - this is hard to fix...

        // right now I filter @AJ aspect else it end up with duplicate members
        //return !matchType.isInterface() && !matchType.isAnnotationStyleAspect();
        Set aspects = (Set)s_advisedTypeToAspects.get(matchType);
        if (aspects == null) {
            //return false;
            // FIXME AV - #75442 see thread
            // back off on old style : it can happen for perTarget that target type is presented first to the weaver
            // while caller side is not thus we have advisedTypeToAspects still empty..

            // note: needed only for perTarget if lateMunger is used (see PerObject)
            return !matchType.isInterface() && !matchType.isAnnotationStyleAspect();
        } else {
            return aspects.contains(aspectType);
        }

    }

    private FuzzyBoolean isWithinType(ResolvedTypeX type) {
        return testPointcut.fastMatch(new FastMatchInfo(type, null));
    }

}
