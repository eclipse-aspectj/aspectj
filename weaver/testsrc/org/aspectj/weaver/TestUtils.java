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
package org.aspectj.weaver;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.aspectj.weaver.bcel.BcelAdvice;
import org.aspectj.weaver.patterns.FormalBinding;
import org.aspectj.weaver.patterns.Pointcut;
import org.aspectj.weaver.patterns.SimpleScope;


public class TestUtils {
    private static final String[] ZERO_STRINGS = new String[0];

    /**
     * Build a member from a string representation:
     * <blockquote><pre>
     * static? TypeName TypeName.Id
     * </pre></blockquote>
     */
    public static MemberImpl fieldFromString(String str) {
        str = str.trim();
        final int len = str.length();
        int i = 0;
        int mods = 0;
        if (str.startsWith("static", i)) {
            mods = Modifier.STATIC;
            i += 6;
            while (Character.isWhitespace(str.charAt(i))) i++;
        }
        int start = i;
        while (! Character.isWhitespace(str.charAt(i))) i++;
        UnresolvedType retTy = UnresolvedType.forName(str.substring(start, i));

        start = i;
        i = str.lastIndexOf('.');
        UnresolvedType declaringTy = UnresolvedType.forName(str.substring(start, i).trim());
        start = ++i;
        String name = str.substring(start, len).trim();
        return new MemberImpl(
            Member.FIELD,
            declaringTy,
            mods,
            retTy,
            name,
            UnresolvedType.NONE);
    }

    /**
     * Build a member from a string representation:
     * <blockquote><pre>
     * (static|interface|private)? TypeName TypeName . Id ( TypeName , ...)
     * </pre></blockquote>
     */
    
    public static Member methodFromString(String str) {
        str = str.trim();
        // final int len = str.length();
        int i = 0;

        int mods = 0;
        if (str.startsWith("static", i)) {
            mods = Modifier.STATIC;
            i += 6;
        } else if (str.startsWith("interface", i)) {
            mods = Modifier.INTERFACE;
            i += 9;
        } else if (str.startsWith("private", i)) {
            mods = Modifier.PRIVATE;
            i += 7;
        }            
        while (Character.isWhitespace(str.charAt(i))) i++;
        
        int start = i;
        while (! Character.isWhitespace(str.charAt(i))) i++;
        UnresolvedType returnTy = UnresolvedType.forName(str.substring(start, i));

        start = i;
        i = str.indexOf('(', i);
        i = str.lastIndexOf('.', i);
        UnresolvedType declaringTy = UnresolvedType.forName(str.substring(start, i).trim());
        
        start = ++i;
        i = str.indexOf('(', i);
        String name = str.substring(start, i).trim();
        start = ++i;
        i = str.indexOf(')', i);
    
        String[] paramTypeNames = parseIds(str.substring(start, i).trim());

        return MemberImpl.method(declaringTy, mods, returnTy, name, UnresolvedType.forNames(paramTypeNames));
    }

    private static String[] parseIds(String str) {
        if (str.length() == 0) return ZERO_STRINGS;
        List l = new ArrayList();
        int start = 0;
        while (true) {
            int i = str.indexOf(',', start);
            if (i == -1) {
                l.add(str.substring(start).trim());
                break;
            }
            l.add(str.substring(start, i).trim());
            start = i+1;
        }
        return (String[]) l.toArray(new String[l.size()]);
    }

    /**
     * Moved from BcelWorld to here
     * 
     * Parse a string into advice.
     * 
     * <blockquote><pre>
     * Kind ( Id , ... ) : Pointcut -> MethodSignature
     * </pre></blockquote>
     */
    public static Advice shadowMunger(World w,String str, int extraFlag) {
        str = str.trim();
        int start = 0;
        int i = str.indexOf('(');
        AdviceKind kind = 
            AdviceKind.stringToKind(str.substring(start, i));
        start = ++i;
        i = str.indexOf(')', i);
        String[] ids = parseIds(str.substring(start, i).trim());
        //start = ++i;
        
        
        
        i = str.indexOf(':', i);        
        start = ++i;        
        i = str.indexOf("->", i);
        Pointcut pointcut = Pointcut.fromString(str.substring(start, i).trim());
        Member m = TestUtils.methodFromString(str.substring(i+2, str.length()).trim());

        // now, we resolve
        UnresolvedType[] types = m.getParameterTypes();
        FormalBinding[] bindings = new FormalBinding[ids.length];
        for (int j = 0, len = ids.length; j < len; j++) {
            bindings[j] = new FormalBinding(types[j], ids[j], j, 0, 0, "fromString");
        }

        Pointcut p =
        	pointcut.resolve(new SimpleScope(w, bindings));

        return new BcelAdvice(kind, p, m, extraFlag, 0, 0, null, null);
    }
}
