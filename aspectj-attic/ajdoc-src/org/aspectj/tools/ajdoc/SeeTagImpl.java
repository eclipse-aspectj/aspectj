/* -*- Mode: JDE; tab-width: 4; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This file is part of the debugger and core tools for the AspectJ(tm)
 * programming language; see http://aspectj.org
 *
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * either http://www.mozilla.org/MPL/ or http://aspectj.org/MPL/.
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is AspectJ.
 *
 * The Initial Developer of the Original Code is Xerox Corporation. Portions
 * created by Xerox Corporation are Copyright (C) 1999-2002 Xerox Corporation.
 * All Rights Reserved.
 */
package org.aspectj.tools.ajdoc;

import org.aspectj.ajdoc.AdviceDoc;
import org.aspectj.ajdoc.AspectDoc;
import org.aspectj.ajdoc.PointcutDoc;
import org.aspectj.compiler.base.JavaCompiler;
import org.aspectj.compiler.base.ast.ASTObject;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.ConstructorDoc;
import com.sun.javadoc.Doc;
import com.sun.javadoc.FieldDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.MethodDoc;
import com.sun.javadoc.PackageDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.SeeTag;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Implementation of see tags in the aspectj-world.
 * See source for bug comments
 * @author Jeff Palm
 */
public class SeeTagImpl extends TagImpl implements SeeTag {
 /*
 * This implementation handles
 *<pre>{@link  package.class#member  label}
 * @see  package.class#member  label
 * @see "some reference"
 * @see <any tag - should be url></pre>.
 * It has errors since the tag-separating code which provides
 * input is wrong, and it's very weak in how it handles
 * text and URLs.  Most of the action is in resolve().
 */

    /** The package name specified by the user -- may be null. */
    private String packageName;

    /** The class name specified by the user -- may be null. */
    private String className;

    /** The member name specified by the user -- may be null. */
    private String memberName;

    /** The referenced package. */
    private PackageDoc packageDoc;

    /** The referenced class. */
    private ClassDoc classDoc;

    /** The referenced member. */
    private MemberDoc memberDoc;

    /** The label associated with the tag. */
    private String label = "";

    /** A shared instance of the compiler -- a little hacky. */
    private static JavaCompiler ajc = AjdocCompiler.instance();

    /**
     * Constructs the new tag with given parameters and
     * calls resolve to parse the class, package, and members.
     *
     * @param doc    the new value for <code>doc</code>.
     * @param name   the new value for <code>name</code>.
     * @param text   the new value for <code>text</code>.    
     * @param locale the new value for <code>locale</code>.
     * @param err    the new value for <code>err</code>.
     */
    public SeeTagImpl(Doc doc,
                      String name,
                      String text,
                      Locale loc,
                      ErrPrinter err) {
        super(doc, name, text, loc, err);
        resolve();
    }
    
    /**
     * Returns the label.
     *
     * @return the label.
     */
    public String label() {
        return label;
    }

    /**
     * Returns the referenced package's name.
     * This <b>can</b> be <code>null</code>.
     *
     * @return the referenced package's name.
     */
    public String referencedPackageName() {
        return packageName;
    }

    /**
     * Returns the referenced package.
     * This <b>can</b> be <code>null</code>.
     *
     * @return a PackageDoc with name packageName.
     */
    public PackageDoc referencedPackage() {
        look();
        return packageDoc;
    }

    /**
     * Returns the referenced class's name.
     * This <b>can</b> be <code>null</code>.
     *
     * @return the referenced class's name.
     */
    public String referencedClassName() {
        return className;
    }

    /**
     * Returns the referenced class.
     * This <b>can</b> be <code>null</code>.
     *
     * @return a ClassDoc with name className.
     */
    public ClassDoc referencedClass() {
        look();
        return classDoc;
    }

    /**
     * Returns the referenced members's name.
     * This <b>can</b> be <code>null</code>.
     * This can be null.
     *
     * @return the referenced class's name.
     */
    public String referencedMemberName() {
        return memberName;
    }

    /**
     * Returns the referenced member.
     * This <b>can</b> be <code>null</code>.
     *
     * @return a ClassDoc with name memberName.
     */    
    public MemberDoc referencedMember() {
        look();
        return memberDoc;
    }

    /**
     * Returns <code>see</code>.
     *
     * @return <code>see</code>.
     */
    public String kind() {
        return "@see";
    }

    protected JavaCompiler ajc() {
        if (ajc != null) {
            return ajc;
        } else if (doc() instanceof ASTObject) {
            return ajc = ((ASTObject)doc()).getCompiler();
        } else if (doc() instanceof PackageDocImpl) {
            return ajc = ((PackageDocImpl)doc()).ajc();
        }
        return null;
    }

	void debugState(String m, PrintStream err) {
        if (System.getProperty("seetag.debug") != null) {
            if (null == err) err = System.err;
            err.println("\t______________________ " + m
            					+ "\n\tpackageName: "+packageName
            					+ "\n\t packageDoc: " +packageDoc
            					+ "\n\t  className: " +className
            					+ "\n\t   classDoc: " +classDoc
            					+ "\n\t memberName: " +memberName
            					+ "\n\t  memberDoc: " +memberDoc
            					+ "\n\t      label: " +label
            					);
        }
	}
    private boolean looked = false;
    private void look() {
        if (!looked) {
            looked = true;
            dolook();
        }
		debugState("SeeTagImpl.look()", null);
    }

    private void dolook() {

        // For null or empty classnames set the current doc
        // to the referenced class
        if (className == null || className.length() < 1) {
            classDoc = classDoc(doc());
        } else {
            
            // Use the class in which this doc is contained
            // as a starting point...
            ClassDoc container = classDoc(doc());

            // ..and try to find the class from there
            if (container == null) {
                //TODO: Find class somewhere else
            } else {
                String fullName;
                if (packageName == null || packageName.length() < 1) {
                    fullName = className;
                } else {
                    fullName = packageName + '.' + className;
                }

                // As per the language spec...
                // http://java.sun.com/docs/books/jls/second_edition/ (con't)
                // html/names.doc.html#32725
                // We must first consider types before identifiers,
                // therefore, if there is no right parent in the member name
                // we have to consider this first an inner class, and if
                // one is found set the member name to nul
                if (memberName != null &&
                    memberName.indexOf('(') == -1) {
                    classDoc = container.findClass(fullName + '.' + memberName);

                    // If we found an inner class, don't look for a member
                    if (classDoc != null) {
                        memberName = null;
                    }
                }

                // Now if we didn't find an inner class, just look
                // up the full name
                if (classDoc == null) {
                    classDoc = container.findClass(fullName);
                }
            }
        }

        // If we found a class, then the package is that
        // class's contained package
        if (classDoc != null) {
            packageDoc = classDoc.containingPackage();
        } else if (packageName == null) {
            
            // If we didn't find a class, but the class name
            // contains no periods, maybe the class name's really
            // a package name
            if (classDoc == null && className != null &&
                className.indexOf('.') == -1) {
                packageDoc = PackageDocImpl.getPackageDoc(className);
            }
            
            // Otherwise, if the referenced class isn't null, set the
            // referenced package to that class's contained package
            else if (classDoc != null) {
                packageDoc = classDoc.containingPackage();
            }
            
            // Lastly, if the current doc is a package (i.e. package.html)
            // then set the referenced package to the current doc
            else if (doc() instanceof PackageDoc) {
                packageDoc = (PackageDoc)doc();
            }
        } else {
            
            // Look for the fully qualified name of the
            // package elsewhere
            packageDoc = PackageDocImpl.getPackageDoc(packageName != null
                                                      ? packageName
                                                      : "");
        }

        // Look for the member if the member name wasn't null
        if (memberName != null) {
            lookForMember(memberName, (org.aspectj.ajdoc.ClassDoc)classDoc);
        }
    }

    private void lookForMember(String spec,
                               org.aspectj.ajdoc.ClassDoc container) {
        int ilparen = spec.indexOf('(');
        String name;
        
        // No parens mean a field or a method
        // The order is for looking is:
        //  [1] field
        //  [2] method
        //  [3] pointcut
        if (ilparen == -1) {
            name = spec;
            if ((memberDoc = fieldDoc(name, container)) != null) {
                return;
            }
            if ((memberDoc = methodDoc(name, container, null)) != null) {
                return;
            }
            if ((memberDoc = pointcutDoc(name, container, null)) != null) {
                return;
            }
        } else {
            name = spec.substring(0, ilparen);
        }
        int irparen = spec.lastIndexOf(')');

        // Crop out the parameters
        String paramsString;
        if (irparen != -1) {
            paramsString = spec.substring(ilparen+1, irparen);
        } else {
            paramsString = spec.substring(ilparen+1, spec.length()-1);
        }

        // Convert the raw parameters to an array
        String[] paramNames = paramNames(paramsString);

        // Try to match the name and parameters if the following order:
        //  [1] method
        //  [2] constructor
        //  [3] pointcut
        //  [4] advice
        if ((memberDoc = methodDoc(name, container, paramNames)) != null) {
            return;
        }
        if ((memberDoc = constructorDoc(container, paramNames)) != null) {
            return;
        }
        if ((memberDoc = pointcutDoc(name, container, paramNames)) != null) {
            return;
        }
        if (container instanceof AspectDoc) {
            if ((memberDoc = adviceDoc(name,
                                       (AspectDoc)container,
                                       paramNames)) != null) {
                return;
            }
        }
    }

    private String[] paramNames(String restNoParens) {
        if (restNoParens == null || restNoParens.length() == 0) {
            return new String[0];
        }
        List params = new ArrayList();
        for (StringTokenizer t = new StringTokenizer(restNoParens, ",", false);
             t.hasMoreTokens();) {
            String spec = t.nextToken().trim();
            int ispace = spec.indexOf(' ');
            if (ispace != -1) {
                spec = spec.substring(0, ispace);
            }
            params.add(spec);
        }
        return (String[])params.toArray(new String[params.size()]);
    }

    private FieldDoc fieldDoc(String name, ClassDoc container) {
        for (ClassDoc cd = container; cd != null; cd = cd.superclass()) {
            FieldDoc[] docs = cd.fields();
            for (int i = 0, N = docs.length; i < N; i++) {
                if (docs[i].name().equals(name)) {
                    return docs[i];
                }
            }
        }
        return null;
    }

    private PointcutDoc pointcutDoc(String name,
                                    org.aspectj.ajdoc.ClassDoc container,
                                    String[] paramNames) {
        if (null == name) return null; // XXX warn or error                                           
        
        for (org.aspectj.ajdoc.ClassDoc cd = container;
             cd != null;
             cd = (org.aspectj.ajdoc.ClassDoc)cd.superclass()) {
            PointcutDoc[] docs = cd.pointcuts();
            if (null == docs) {
                continue;
            }
            for (int i = 0, N = docs.length; i < N; i++) {
                PointcutDoc md = docs[i];
                if ((null != md) 
                    && (name.equals(md.name()))
                    && ((null == paramNames)
                        || parametersMatch(md.parameters(), paramNames, container))) {
                    return md;
                }
            }
        }
        return null;
    }

    private MethodDoc methodDoc(String name, ClassDoc container,
                                String[] paramNames) {
        for (ClassDoc cd = container; cd != null; cd = cd.superclass()) {
            MethodDoc[] docs = cd.methods();
            for (int i = 0, N = docs.length; i < N; i++) {
                MethodDoc md = docs[i];
                if (!md.name().equals(name)) {
                    continue;
                }
                if (paramNames == null) {
                    return md;
                } else {
                    if (parametersMatch(md.parameters(),
                                        paramNames,
                                        container)) {
                        return md;
                    }
                }
            }
        }
        return null;
    }

    private ConstructorDoc constructorDoc(ClassDoc container,
                                          String[] paramNames) {
        for (ClassDoc cd = container; cd != null; cd = cd.superclass()) {
            ConstructorDoc[] docs = cd.constructors();
            for (int i = 0, N = docs.length; i < N; i++) {
                ConstructorDoc md = docs[i];
                if (paramNames == null) {
                    return md;
                } else {
                    if (parametersMatch(md.parameters(),
                                        paramNames,
                                        container)) {
                        return md;
                    }
                }
            }
        }
        return null;
    }

    private AdviceDoc adviceDoc(String name,
                                AspectDoc container,
                                String[] paramNames) {
        
        AspectDoc cd = container;
        while (cd != null) {
            AdviceDoc[] docs = cd.advice();
            for (int i = 0, N = docs.length; i < N; i++) {
                AdviceDoc md = docs[i];
                if (!(name.equals(md.name()))) {
                    continue;
                }
                if (paramNames == null) {
                    return md;
                } else {
                    if (parametersMatch(md.parameters(),
                                        paramNames,
                                        container)) {
                        return md;
                    }
                }
            }
            Object o = cd.superclass();
            if (o instanceof AspectDoc) {
                cd = (AspectDoc) o;
            } else {
                cd = null;
            }
        }
        return null;
    }

    private boolean parametersMatch(Parameter[] params,
                                    String[] paramNames,
                                    ClassDoc container) {
        if ((null == params) || (null == paramNames) || (null == container)) {
            return false;
        }
        if (params.length != paramNames.length) {
            return false;
        }
        for (int i = 0, N = params.length; i < N; i++) {
            com.sun.javadoc.Type type1 = params[i].type();
            com.sun.javadoc.Type type2 = TypeImpl.getInstance(paramNames[i],
                                                              container);
            if ((null == type1) || (!type1.equals(type2))) {
                return false;
            }
        }
        return true;
    }

    private ClassDoc classDoc(Doc d) {
        if (d instanceof ClassDoc) {
            return (ClassDoc)d;
        }
        if (d instanceof MemberDoc) {
            return ((MemberDoc)d).containingClass();
        }
        return null;
    }
   
    /** find next (matching) char x, ignoring instances
     * preceded by escape character '\'.
     */
    private static int findNextChar(final String s, final int start, final char x) {  // XXX to Util
        if ((null != s) && (start >= 0)) {
            boolean escaped = false;
            for (int i = start; i < s.length(); i++) {
                char c = s.charAt(i);
                if (('\\' == c) && !escaped) {
                    escaped = true;
                    continue;
                } else if ((x == c) && !escaped) {
                    return i;
                }
                if (escaped) {
                    escaped = false;
                }
            }
        }
        return -1;
    }
    
    /**
     * This looks a bit hideous, and it is, I had a state diagram
     * with every thing labled, but I lost it -- sorry ;).
   	 * <pre>{@link  package.class#member  label} </pre>
   	 * http://java.sun.com/j2se/1.3/docs/tooldocs/solaris/javadoc.html#\{@link}
     */
    private void resolve() {
        String str = text();

        if (str == null || str.length() < 1) {
            return;
        }

        str = str.trim();

        int N = str.length();
        
        char first = str.charAt(0);
        if (first == '<') {
            if ((N < 4) || (str.charAt(N-1) != '>')) {
                err().error("see_tag_unterminated_url",str);
            } else {
                char second = str.charAt(1);
                if ((second == 'a') || (second == 'A')) {
                    label = str;
                } else {
                    err().error("see_tag_unterminated_url",str); // XXX wrong message
                }
            }
            return;
        }
        
        if (first == '"') {
            if (N == 1) {
                err().error("see_tag_unterminated_string",str);
            } else if (str.charAt(N-1) == '"') {
                label = str;
            } else {
                int loc = findNextChar(str, 1, '"');
                if (-1 == loc) {
                    err().error("see_tag_unterminated_string",str);
                } else {
                    label = str.substring(0, loc+1);
                }
            }
            return;
        }
        // XXX but does not handle URLs?
        char c = 0;
        int state = 0, next = 0;
        boolean finished = false;

        int iclassEnd = -1;
        int isharp = -1;
        int ilastDot = -1;
        int iStartLabel = -1;
		int iEndMemberLabel = -1; // membername plus parameters
        boolean sharp = false;
        int i;
    done:
        for (i = 0; i < N; i++, state = next) {
            c = str.charAt(i);
            switch (state) {
                
            case 0: // seeking initial: [type|memberName]
                if      (ident(c)) { next = 1; }
                else if (c == '#') { next = 2; iclassEnd = i-1; }
                else {
                    err().error("see_tag_dot_sharp_or_id","\""+c+"\"@0",str);
                    return;
                }
                break;

            case 1: // reading initial [type|memberName] 
                if      (ident(c)) { next = 1; }
                else if (c == '#') { next = 2; iclassEnd = i-1; }
                else if (c == '.') { next = 3; ilastDot = i; }
                else if (space(c)) { iclassEnd = i-1; next = 16; }
                else {
                    err().error("see_tag_invalid_package_or_class","\""+c+"\"@1",str);
                    return;
                }
                break;
                
            case 2: // start reading membername (field only?)
                sharp = true;
                if      (ident(c)) { next = 4; isharp = i; }
                else {
                    err().error("see_tag_expecting_field_name","\""+c+"\"@2",str);
                    return;
                }
                break;

            case 3: // reading qualified type name
                if      (ident(c)) { next = 1; }
                else {
                    err().error("see_tag_invalid_id","\""+c+"\"@3",str);
                    return;
                }
                break;

            case 4: //  reading membername
                if      (ident(c)) { next =  4; }
                else if (space(c)) { next = 13; iEndMemberLabel = i-1;}
                else if (c == '(') { next = 15; }
                else {
                    err().error("see_tag_invalid_param_start","\""+c+"\"@4",str);
                    return;
                }
                break;

            case 5: // start reading parms
                if      (ident(c)) { next =  6; }
				else if (space(c)) { next = 5; }
                else if (c == ')') { next = 13; iEndMemberLabel = i;}
                else {
                    err().error("see_tag_premature_param_end","\""+c+"\"@5",str);
                    return;
                }
                break;

            case 6: // reading parm (or type?)
                if      (ident(c)) { next =  6; }
                else if (c == '.') { next =  7; }
                else if (c == '[') { next =  8; }
                else if (space(c)) { next = 10; }
                else if (c == ',') { next = 12; }
                else if (c == ')') { iEndMemberLabel = i; next = 16; }
                else {
                    err().error("see_tag_invalid_parameter_type","\""+c+"\"@6",str);
                    return;
                }
                break;

            case 7: // reading qualified parameter type .
                if      (ident(c)) { next =  6; }
                else {
                    err().error("see_tag_invalid_parameter_type_ident","\""+c+"\"@7",str);
                    return;
                }
                break;

            case 8: // reading end of [] 
                if      (c == ']') { next =  9; }
                else if (space(c)) { next = 8; }
                else {
                    err().error("see_tag_unterminated_array_type","\""+c+"\"@8",str);
                    return;
                }
                break;

            case 9: // maybe completed parameter type 
                if      (c == '[') { next =  8; }
                else if (space(c)) { next = 10; }
                else if (c == ',') { next = 12; }
                else if (c == ')') { iEndMemberLabel = i; next = 16; }
                else {
                    err().error("see_tag_invalid_parameter_type","\""+c+"\"@9",str);
                    return;
                }
                break;

            case 10: // completed parm type?
                if      (ident(c)) { next = 11; }
                else if (space(c)) { next = 12; }
                else if (c == ',') { next = 14; }
                else if (c == ')') { iEndMemberLabel = i; next = 16; }
                else {
                    err().error("see_tag_invalid_parameters","\""+c+"\"@10",str);
                    return;
                }
                break;

            case 11: // reading parm type?
                if      (ident(c)) { next = 11; }
                else if (space(c)) { next = 12; }
                else if (c == ',') { next = 14; }
                else if (c == ')') { iEndMemberLabel = i; next = 16; }
                 else {
                    err().error("see_tag_invalid_parameters","\""+c+"\"@11",str);
                    return;
                }
                break;

            case 12: // looking for next parm? 
                if      (space(c)) { next = 12; }
                else if (c == ',') { next = 14; }
                else if (ident(c)) { next = 15; }
                else if (c == ')') { iEndMemberLabel = i; next = 16; }
                else {
                    err().error("see_tag_invalid_parameters","\""+c+"\"@12",str);
                    return;
                }
                break;

            case 13: // seeking parms or label
                if (space(c)) { next = 13; } 
                else if (c == '(') { next =  5; } // start reading parms
                else if (ident(c)) {    // start reading label
                    iStartLabel = i; next = 17; 
                 }
                else {
                    err().error("see_tag_invalid_parameters","\""+c+"\"@13",str);
                    return;
                }
                break;

            case 14: // type name (or identifier)
                if      (ident(c)) { next =  6; }
                else if (space(c)) { next = 14; }
                else {
                    err().error("see_tag_expecting_typename_or_whitespace","\""+c+"\"@14",str);
                    return;
                }
                break;

            case 15: // reading parms
                if      (ident(c)) { next =  6; }
                else if (space(c)) { next = 15; }
                else if (c == ')') { iEndMemberLabel = i; next = 16; }
                else {
                    err().error("see_tag_premature_param_end","\""+c+"\"@15",str);
                    return;
                }
                break;
             case 16 :  // seeking label
                if      (ident(c)) { iStartLabel = i; next = 17; }
                else if (space(c)) { next = 16; }
                else {
                    String s = "\"" + c + "\" in \"" + text() + "\"";
                    err().error("see_tag_premature_param_end",s + "@16",str);
                    return;
                }
             	break;
             case 17 :  // reading label - may have internal spaces
                if      (ident(c)) { next =  17; }
                else if (space(c)) { next = 17; }
                // XXX known limitation - labels may only be ident + whitespace (no -)
                else {
                    err().error("see_tag_premature_param_end","\""+c+"\"@17",str);
                    return;
                }
             	break;
            }

            if (i == N-1) {
                finished = next == -1
                    || next == 1 || next == 13
					|| next == 16 || next == 17
                    || next == 4 || next == 12;
            }
            
        }

        if (sharp) {
            if (ilastDot != -1) {
                packageName = str.substring(0, ilastDot);
            }
        } else {
            if (ilastDot == -1) {
                //packageName = str;
            } else {
                packageName = str.substring(0, ilastDot);
            }
        }

        if (sharp) {
            if (iclassEnd != -1) {
                if (ilastDot == -1) {
                    className = str.substring(0, iclassEnd+1);
                } else {
                    className = str.substring(ilastDot+1, iclassEnd+1);
                }
            }
        } else {
            if (ilastDot == -1) {
                if (iclassEnd != -1) {
    	            className = str.substring(0, iclassEnd+1);
                } else {
	                className = str;
                }
            } else {
                if (iclassEnd != -1) {
    	            className = str.substring(ilastDot+1, iclassEnd+1);
                } else {
    	            className = str.substring(ilastDot+1);
	            }
            }
        }

        if (sharp) {
            if (-1 != iEndMemberLabel) {
	            memberName = str.substring(isharp, iEndMemberLabel+1).trim();
            } else {
	            memberName = str.substring(isharp).trim();
            }
            // hack to remove spaces between method name and parms
            int parmLoc = memberName.indexOf("(");
            if (-1 != parmLoc) {
				int spaceLoc = memberName.indexOf(" ");
				if ((-1 != spaceLoc) && (spaceLoc < parmLoc)) {
					memberName = memberName.substring(0,spaceLoc)
						+ memberName.substring(parmLoc).trim();
				}
            }
        }

        if (!finished) {
            err().error("see_tag_prematurely_done",str);
        } else {
            if (iStartLabel != -1) {
                label = str.substring(iStartLabel).trim();
            } else if (i < N-1) { // when does this happen?
                label = str.substring(i).trim();
            }        
        }
    }
    
    // test-only methods
    String getPackageName() {return packageName;}
	String getClassName() { return className;}
	String getMemberName() { return memberName; }
	String getLabel() { return label; }
}
