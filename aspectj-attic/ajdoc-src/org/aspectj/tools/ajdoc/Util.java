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

import org.aspectj.compiler.base.ast.Constructor;
import org.aspectj.compiler.base.ast.ConstructorDec;
import org.aspectj.compiler.base.ast.Exprs;
import org.aspectj.compiler.base.ast.Field;
import org.aspectj.compiler.base.ast.FieldDec;
import org.aspectj.compiler.base.ast.Formals;
import org.aspectj.compiler.base.ast.Method;
import org.aspectj.compiler.base.ast.MethodDec;
import org.aspectj.compiler.base.ast.NameType;
import org.aspectj.compiler.base.ast.Type;
import org.aspectj.compiler.base.ast.TypeDec;
import org.aspectj.compiler.crosscuts.ast.PointcutDec;
import org.aspectj.compiler.crosscuts.ast.PointcutSO;

import com.sun.javadoc.Doc;
import com.sun.javadoc.ExecutableMemberDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.Tag;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * A utility class used by lots of folks.
 *
 * @author Jeff Palm
 */
public class Util {

    /**
     * Delegate to Character.isJavaIdentifierStart(char).
     *
     * @return <code>true</code> if <code>c</code> can
     *         start a java identifier.
     */
    public final static boolean start(char c) {
        return Character.isJavaIdentifierStart(c);
    }

    /**
     * Delegate to Character.isJavaIdentifierPart(char).
     *
     * @return <code>true</code> if <code>c</code> can
     *         be a part of a java identifier.
     */
    public final static boolean ident(char c) {
        return Character.isJavaIdentifierPart(c);
    }

    /**
     * Delegate to Character.isWhitespace(char).
     *
     * @return <code>true</code> if <code>c</code> is
     *         valid white space.
     */
    public final static boolean space(char c) {
        return Character.isWhitespace(c);
    }

    /**
     * Returns true is <code>c</code> is a newline character.
     *
     * @return <code>true</code> if
     *         <code>c == '\n' || c == '\r'</code>.
     */
    public final static boolean newline(char c) {
        return c == '\n' || c == '\r';
    }

    /**
     * Returns two strings split at the first white space.
     *
     * @return an array of two Strings split at
     *         the first white space.
     */
    public static String[] split(String str) {
        String[] strs = new String[2];
        for (int i = 0; i < str.length(); i++) {
            if (space(str.charAt(i))) {
                strs[0] = str.substring(0, i);
                strs[1] = str.substring(i+1);
                break;
            }
        }
        if (strs[0] == null) {
            strs[0] = str;
            strs[1] = "";
        }
        return strs;
    }

    /**
     * Returns the inline tags found in <code>str</code>.
     *
     * @param doc Doc to give to the new tag.
     * @param str String from which to create the tags.
     * @param loc Locale to give to the new tag.
     * @param err ErrPrinter to give to the new tag.
     * @return    an array of Tag representing the inline
     *            tags found in str.
     */
    public final static Tag[] inlineTags(Doc doc,
                                         String str,
                                         Locale loc,
                                         ErrPrinter err) {
        if (str == null || str.length() < 1) {
            return new Tag[0];
        }
        
        int N = str.length();

        List list = new ArrayList();
       
        int i = 0;
        for (int j = i; i < N; j = i) {
            
            // Try to match a link tag
            int ileft = str.indexOf("{@", i);
            
            // If there are no more link tags, return the rest
            // of str as a 'Text' Tag
            if (ileft == -1) {
                list.add(new TagImpl(doc, "Text",
                                     str.substring(i),
                                     loc, err));
                break;
            }

            if (j < ileft) {
                list.add(new TagImpl(doc, "Text",
                                     str.substring(j, ileft),
                                     loc, err));
            }

            // If there is a tag and it's name is 'link ' try to
            // match it
            i = ileft;
            if (i+7 < N &&
                str.substring(i+2, i+7).toLowerCase().equals("link ")) {
                i += 7;
                for (; str.charAt(i) != '}'; i++) {
                    if (i == N-1) {
                        err.error("tag_unterminated_link_tag",
                                    str.substring(i));
                        break;
                    }
                }
                list.add(new SeeTagImpl(doc, "@link",
                                        str.substring(ileft+7, i),
                                        loc, err));
            } else {
                err.error("tag_invalid_link_tag",
                            str.substring(i));
            }

            // Don't want to include the right brace
            i += 1;           
        }
        return (Tag[])list.toArray(new Tag[list.size()]);
    }

    /**
     * Returns the first sentence tags found in <code>str</code>.
     *
     * @param doc Doc to give to the new tag.
     * @param str String from which to create the tags.
     * @param loc Locale to give to the new tag.
     * @param err ErrPrinter to give to the new tag.
     * @return    an array of Tag representing the first
     *            sentence tags found in str.
     */
    public final static Tag[] firstSentenceTags(Doc doc,
                                                String str,
                                                Locale loc,
                                                ErrPrinter err) {
        return inlineTags(doc, firstSentenceText(str, loc, err), loc, err);
    }

    /**
     * Returns the first sentence tags found in <code>str</code>,
     * using <code>Locale.US</code> as the default locale.
     *
     * @param doc Doc to give to the new tag.
     * @param str String from which to create the tags.
     * @param err ErrPrinter to give to the new tag.
     * @return    an array of Tag representing the first
     *            sentence tags found in str.
     */
    private static String firstSentenceText(String str,
                                            Locale loc,
                                            ErrPrinter err) {
        if (str == null || loc == null || !loc.equals(Locale.US)) {
            return "";
        }
        final int N = str.length();
        int i;
        for (i = 0; i < N; i++) {
            
            // A period at the end of the text or a
            // period followed by white space is the
            // end of a sentence
            if (str.charAt(i) == '.') {
                if (i == N-1) {
                    return str.substring(0, i+1);
                }
                if (space(str.charAt(i+1))) {
                    return str.substring(0, i+2);
                }
            }
            
            // An HTML tag the signals the end -- one of:
            // <p> </p> <h1> <h2> <h3> <h4>
            // <h5> <h6> <hr> <pre> or </pre>
            if (str.charAt(i) == '<') {
                int j = i+1;
                
                // Find the closing '>'
                while (j < N && str.charAt(j) != '>') j++;
                
                // If there's no closing '>' signal an error
                if (j == N) {
                    err.error("unterminated_html_tag", str);
                    return str;
                }
                
                // Inspect the inside of the tag
                String innards = str.substring(i+1, j).trim().toLowerCase();
                if (innards.equals("p")  || innards.equals("pre") ||
                    innards.equals("h1") || innards.equals("h2")  ||
                    innards.equals("h3") || innards.equals("h4")  ||
                    innards.equals("h5") || innards.equals("h6")  ||
                    innards.equals("hr")) {
                    return str.substring(0, i+1);
                }
            }
        }
        return str;
    }

    /**
     * Returns the tags found in <code>str</code>.
     *
     * @param doc Doc to give to the new tag.
     * @param str String from which to create the tags.
     * @param loc Locale to give to the new tag.
     * @param err ErrPrinter to give to the new tag.
     * @return    an array of Tag representing the
     *            tags found in str.
     */
    public final static List findTags(Doc doc,
                                      String str,
                                      Locale loc,
                                      ErrPrinter err) {
                                      
        //XXX This sucks!!! Will redo later.
        boolean newline = true;
        List result = new ArrayList();
        if (str == null) return result;
        final int N = str.length();
        int lastTag = -1;
        for (int i = 0; i < N; i++) {
            if (newline(str.charAt(i))) {
                newline = true;
                // XXX need to evaluate - some tags can span newlines?
//                if (lastTag != -1) {  // now requiring tags not to span newlines
//                    result.add(parse(doc, str.substring(lastTag, i),
//                                     loc, err));
//                }
//                lastTag = -1
            } else if (space(str.charAt(i)) && newline) {
            } else if (str.charAt(i) == '@' && newline) {
                if (lastTag != -1) {
                    result.add(parse(doc, str.substring(lastTag, i),
                                     loc, err));
                }
                lastTag = i;
            } else {
                newline = false;
            }
        }
        if (lastTag != -1) {
            result.add(parse(doc, str.substring(lastTag),
                             loc, err));
        }
        return result;
    }

    private final static Tag parse(Doc doc,
                                   String str,
                                   Locale loc,
                                   ErrPrinter err) {
        Tag result = null;
        String[] split = split(str);
        String name = split[0];
        String rest = split[1];
        if (name.equals("@see")) {
            result = new SeeTagImpl(doc, name, rest, loc, err);
        } else if (name.equals("@exception") || name.equals("@throws")) {
            result = new ThrowsTagImpl(doc, name, rest, loc, err);
        } else if (name.equals("@serialField")) {
            result = new SerialFieldTagImpl(doc, name, rest, loc, err);
        } else if (name.equals("@param")) {
            result = new ParamTagImpl(doc, name, rest, loc, err);
        } else {
            result = new TagImpl(doc, name, rest, loc, err);
        }
        return result;
    }

    /**
     * Returns the raw comment text found in <code>str</code>.
     *
     * @param str String containing comment from which
     *            the raw comment is found.
     * @return    String with the raw comment taken
     *            from <code>str</code>.
     */
    public final static String rawCommentText(String str) {
        if (str == null) return "";
        if (str.length() < 3) return "";
        String withstars = "";
        int islash = str.indexOf('/');
        if (islash == -1 || islash+2 >= str.length()) {
            return "";
        }
        if (str.charAt(islash+1) != '*' ||
            str.charAt(islash+2) != '*') {
            return "";
        }
        int start = islash+2+1;
        while (str.charAt(start) == '*' || space(str.charAt(start))) start++;
        int end = str.length()-2;
        while (str.charAt(end) == '*') end--;
        if (start != -1 && end > start) {
            withstars = str.substring(start, end+1);
        }
        //String result = "";
        StringBuffer result = new StringBuffer(withstars.length());
        for (StringTokenizer t = new StringTokenizer(withstars, "\n", true);
             t.hasMoreTokens();) {
            String line = t.nextToken();
            if (line == null || line.length() == 0) continue;
            int i;
            for (i = 0; i < line.length(); i++) {
                if (!(line.charAt(i) == '*' ||
                      line.charAt(i) == ' ')) {
                    break;
                }
            }
            //result += line.substring(i);
            result.append(line.substring(i));
        }
        //return result;
        return result.toString();
    }

    /**
     * Returns the comment text from the passed in
     * raw comment text -- e.g. no tags at the end.
     *
     * @param rawCommentText raw comment text to search.
     * @return               the comment text from 
     *                       <code>rawCommentText</code>.
     */
    public final static String commentText(String rawCommentText) {
        //String result = "";
        if (rawCommentText == null) {
            return "";
        }
        StringBuffer result = new StringBuffer(rawCommentText.length());
    outer:
        for (StringTokenizer t = new StringTokenizer(rawCommentText, "\n", true);
             t.hasMoreTokens();) {
            String line = t.nextToken();
            if (line == null || line.length() == 0) continue;
            int i;
            for (i = 0; i < line.length(); i++) {
                char c = line.charAt(i);
                if (c == ' ' || c == '\t') {
                } else if (c == '@') {
                    break outer;
                } else {
                    //result += line;
                    result.append(line);
                    continue outer;
                }
            }
        }
        //return result;
        return result.toString();
    }

    /**
     * Compares using names.
     *
     * @param $ First Doc.
     * @param _ Second Doc.
     * @return  -1 if either are null, else
     *          <code>$.name.compareTo(_.name</code>.
     */
    public final static int compareTo(Doc $, Doc _) {
        return ($ == null || _ == null) ? -1 : $.name().compareTo(_.name());
    }

    /**
     * Returns the signature, given <code>parameters</code>,
     * without flattening.
     *
     * @param parameters an array of Parameter.
     * @return           String representation of the parameters.
     * @see              #signature(Parameter[],boolean)
     */
    public final static String signature(Parameter[] parameters) {
        return signature(parameters, false);
    }

    /**
     * Returns the signature, given <code>parameters</code>,
     * with flattening.
     *
     * @param parameters an array of Parameter.
     * @return           String representation of the parameters.
     * @see              #signature(Parameter[],boolean)
     */
    public final static String flatSignature(Parameter[] parameters) {
        return signature(parameters, true);
    }

    /**
     * Returns the signature, given <code>parameters</code>
     * and flattens if <code>flatten</code>.
     *
     * @param parameters an array of Parameter.
     * @param flatten    <code>true</code> if the parameter names
     *                   should be flattened.
     * @return           String representation of the parameters.
     */
    public final static String signature(Parameter[] parameters,
                                         boolean flatten) {
        if (parameters == null || parameters.length == 0) {
            return "()";
        }
        //String str = "(";
        StringBuffer str = new StringBuffer((flatten ? 8 : 20) *
                                            parameters.length);
        str.append("(");
        final int N = parameters.length;
        for (int i = 0; i < N; i++) {
            String typeName = parameters[i].typeName();
            if (flatten) {
                int idot = typeName.lastIndexOf('.');
                if (idot != -1) {
                    typeName = typeName.substring(idot+1);
                }
            }
            //str += typeName + (i < N-1 ? "," : "");
            str.append(typeName + (i < N-1 ? "," : ""));
                
        }
        //str += ")";
        str.append(")");
        //return str;
        return str.toString();
    }

    /**
     * Returns <code>true</code> -- include all members for now.
     *
     * @param doc   member to consider.
     * @param flags access flags.
     */
    public final static boolean isIncluded(MemberDoc doc, long flags) {
        return true;
    }

    /**
     * Returns <code>true</code> if <code>dec</code>
     * isn't local or annonymous or <code>null</code>.
     *
     * @param dec TypeDec to consider.
     * @return    <code>true</code> isn't dec is local or
     *            annonymous or <code>null</code>.
     */
    public final static boolean isIncluded(TypeDec dec) {
        if (dec == null) {
            return false;
        }
        if (dec.isLocal() && dec.isAnonymous()) {
            return false;
        }
        return true; //XXX to do
    }

    //XXX Debugging
    public final static void dump(Object o, String prefix) {
        System.err.println(">> Dumping:"+o);
        java.lang.reflect.Method[] ms = o.getClass().getMethods();
        List list = new ArrayList();
        for (int i = 0; i < ms.length; i++) {
            list.add(ms[i]);
        }
        Collections.sort(list, new Comparator() {
                public int compare(Object o1, Object o2) {
                    return str(o1).compareTo(str(o2));
                }
                public boolean equals(Object o1, Object o2) {
                    return str(o1).equals(str(o2));
                }
                private String str(Object _) {
                    return (_ != null && _ instanceof java.lang.reflect.Method)
                        ? ((java.lang.reflect.Method)_).getName() : _+"";
                }
            });
        for (Iterator i = list.iterator(); i.hasNext();) {
            java.lang.reflect.Method m = (java.lang.reflect.Method)i.next();
            if (m.getParameterTypes().length == 0 &&
                m.getName().startsWith(prefix)) {
                try {                    
                    System.err.println("  "+m.getName()+":"+
                                       m.invoke(o, new Object[0]));
                } catch (Throwable _) {}
            }
        }
    }
    public final static void gets(Object o) {
        dump(o, "get");
    }
    public final static void array(Object[] os) {
        array(os, false);
    }
    public final static void array(Object[] os, boolean gets) {
        if (os == null) {
            System.err.println("NULL");
            return;
        }
        System.err.println(os.getClass().getName()+":" + os.length);
        for (int i = 0; i < os.length; i++) {
            System.err.println(" [" + i +"]:" + os[i]);
            if (gets) gets(os[i]);
        }
    }

    /**
     * Returns the HTML documentation found in <code>html</code>
     * using <code>err</code> to report errors.
     *
     * @param html File in which to look.
     * @param err  ErrPrinter to use to report errors.
     * @return     HTML documentaiton found in <code>html</code>.
     */
    public static String documentation(File html, ErrPrinter err) {
        String str = "";
        InputStream in = null;
        try {
            in = new FileInputStream(html);
        } catch (IOException ioe) {
            err.ex(ioe, "ioexception_open", html.getAbsolutePath());
            return "";
        }
        try {
            byte[] bytes = new byte[in.available()];
            in.read(bytes, 0, bytes.length);
            in.close();
            str = new String(bytes);
        } catch (IOException ioe) {
            err.ex(ioe, "ioexception_reading", html.getAbsolutePath());
        }

        int[] is = new int[]{-10,-1};
        int i = 0;
        final char[] chars = new char[]{'/','B','O','D','Y','>'};
        for (int j = 1; j >= 0; j--) {
        nextTag:
            for (; i != -1; i = str.indexOf('<', i+1)) {
            nextLt:
                for (int s = i+1, k = j; s < str.length(); s++, k++) {
                    char c = str.charAt(s);
                    if (k == chars.length) {
                        is[j] += s+2;
                        break nextTag;
                    }
                    if (!(c == chars[k] || c == (chars[k] | 0x01000000))) {
                        break nextLt;
                    }
                }
            }
        }
        if (is[0] > -1 && is[1] > -1) {
            str = str.substring(is[1], is[0]);
        }
        return str;
    }

    /**
     * Returns the result of invoking the method <code>name</code>
     * on <code>target</code> with parameters <code>params</code>
     * declared in the <code>target</code>'s class using arguments
     * <code>args</code>.
     *
     * @param target target Object.
     * @param name   name of the method.
     * @param params array of Class of parameters of the method.
     * @param args   array of Object of arguments to the method.
     * @return       the result of invoking the method.
     * @see          #invoke(Class,Object,String,Class[],Object[])
     */
    public static Object invoke(Object target, String name,
                                Class[] params, Object[] args) {
        return invoke(target.getClass(), target, name, params, args);
    }

    /**
     * Returns the result of invoking the method <code>name</code>
     * on <code>target</code> with parameters <code>params</code>
     * declared in the <code>type</code> using arguments
     * <code>args</code>.
     * This method handles any errors that arise in doing so.
     *
     * @param type   type in which the method is declared.
     * @param target target Object -- null for static methods.
     * @param name   name of the method.
     * @param params array of Class of parameters of the method.
     * @param args   array of Object of arguments to the method.
     * @return       the result of invoking the method.
     */
    public static Object invoke(Class type, Object target, String name,
                                Class[] params, Object[] args) {
        try {
            java.lang.reflect.Method method = type.getDeclaredMethod(name, params);
            method.setAccessible(true);
            return method.invoke(target, args);
        } catch (Exception e) {
            e.printStackTrace(); //TODO
        }
        return null;
    }

    /**
     * Returns the value of access the field <code>name</code>
     * declared in <code>type</code> on <code>target</code>.
     * This method handles any errors that arise in doing so.
     *
     * @param type   type in which the field is declared.
     * @param target target that is currently holding the field --
     *               null for static fields.
     * @param name   name of the field.
     * @return       the result of accessing this field.
     */
    public static Object access(Class type, Object target, String name) {
        try {
            java.lang.reflect.Field field = type.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(target);
        } catch (Exception e) { //TODO
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns the ExecutableMemberDoc from the array passed
     * in whose parameters <i>weakly</i> match those of
     * <code>params</code> and whose name matches exactly
     * with <code>name</code>.
     * This method <b>can</b> return <code>null</code>.
     *
     * @param emds   an array of ExecutableMemberDoc from which
     *               the returned value comes.
     * @param name   the name of the member to return.
     * @param params an array of Parameter that represent
     *               the parameters we're trying to match.
     * @return       an ExecutableMemberDoc whose parameters
     *               match the names and order found in
     *               <code>params</code> and whose name
     *               exactly equals <code>name</code>.
     */
    public final static ExecutableMemberDoc executableMemberDoc
        (ExecutableMemberDoc[] emds,
         String name,
         Parameter[] params) {
        ExecutableMemberDoc result = null;
        next:
        for (int i = 0; i < emds.length; i++) {
            ExecutableMemberDoc emd = emds[i];
            if (emd.name().equals(name) &&
                params.length == emd.parameters().length) {
                for (int j = 0; j < params.length; j++) {
                    if (!params[j].typeName().equals
                        (emd.parameters()[j].typeName())) {
                        continue next;
                    }
                    result = emd;
                    break next;
                }
            }
        }
        return result;
    }

    /**
     * Returns the PointcutDoc from the array passed
     * in whose parameters <i>weakly</i> match those of
     * <code>formals</code> and whose name matches exactly
     * with <code>id</code>.
     * This method <b>can</b> return <code>null</code>.
     *
     * @param nameType the type in which we're searching.
     * @param id       the name of the pointcut to return.
     * @param formals  the Formals whose name and order
     *                 must match to return a pointcut.
     * @return         a PointcutDoc whose parameters
     *                 match the names and order found in
     *                 <code>formals</code> and whose name
     *                 exactly equals <code>id</code>.
     */
    public final static PointcutDec pointcutDec(NameType nameType,
                                                String id,
                                                Formals formals) {
        PointcutDec result = null;
    next:
        for (Iterator i = nameType.getPointcuts().iterator(); i.hasNext();) {
            PointcutDec md = ((PointcutSO)i.next()).getPointcutDec();
            if (md.getFormals().size() == formals.size() &&
                id.equals(md.getId())) {
                for (int j = 0; j < formals.size(); j++) {
                    if (!md.getFormals().get(j).getType().getString().
                        equals(formals.get(j).getType().getString())) {
                        continue next;
                    }
                }
                result = md;
                break next;
            }
        }
        return result;
    }

    

    /**
     * Returns the MethodDoc from the array passed
     * in whose parameters <i>weakly</i> match those of
     * <code>formals</code> and whose name matches exactly
     * with <code>id</code>.
     * This method <b>can</b> return <code>null</code>.
     *
     * @param nameType the type in which we're searching.
     * @param id       the name of the method to return.
     * @param formals  the Formals whose name and order
     *                 must match to return a method.
     * @return         a MethodDoc whose parameters
     *                 match the names and order found in
     *                 <code>formals</code> and whose name
     *                 exactly equals <code>id</code>.
     */
    public final static MethodDec methodDec(NameType nameType,
                                            String id,
                                            Formals formals) {
        MethodDec result = null;
    next:
        for (Iterator i = nameType.getMethods().iterator(); i.hasNext();) {
            //MethodDec md = (MethodDec)i.next();
            MethodDec md = ((Method)i.next()).getMethodDec();
            if (md.getFormals().size() == formals.size() &&
                id.equals(md.getId())) {
                for (int j = 0; j < formals.size(); j++) {
                    if (!md.getFormals().get(j).getType().getString().
                        equals(formals.get(j).getType().getString())) {
                        continue next;
                    }
                }
                result = md;
                break next;
            }
        }
        return result;
    }

    /**
     * Returns the PointcutDec named <code>name</code>,
     * contained in <code>typeDec</code> with type <code>type</code>.
     * <code>showError</code> is passed to subsequent methods
     * to supress or warrant error printing.
     * This may return null.
     *
     * @param type      Type in which this pointcut was declared.
     * @param name      name of the pointcut.
     * @param typeDec   TypeDec in which we're searching.
     * @param showError <code>true</code> is an error should
     *                  be printed upon not finding a pointcut.
     * @return          the pointcut declared in <code>type</code>
     *                  named <code>name</code>, found by searching
     *                  from <code>typeDec</code>.  This may be
     *                  null.
     */
    public static PointcutDec getPointcutDec(Type type,
                                             String name,
                                             TypeDec typeDec,
                                             boolean showError) {
        PointcutSO so = ((NameType)type).getPointcut(name, typeDec, showError);
        PointcutDec dec = null;
        if (so != null) {
            dec = (PointcutDec)so.getCorrespondingDec();
        }
        return dec;
    }
    
    /**
     * Returns the FieldDec named <code>name</code>,
     * contained in <code>typeDec</code> with type <code>type</code>.
     * <code>showError</code> is passed to subsequent methods
     * to supress or warrant error printing.
     * This may return null.
     *
     * @param type      Type in which this field was declared.
     * @param name      name of the field.
     * @param typeDec   TypeDec in which we're searching.
     * @param showError <code>true</code> is an error should
     *                  be printed upon not finding a field.
     * @return          the field declared in <code>type</code>
     *                  named <code>name</code>, found by searching
     *                  from <code>typeDec</code>.  This may be
     *                  null.
     */
    public static FieldDec getFieldDec(Type type,
                                       String name,
                                       TypeDec typeDec,
                                       boolean showError) {
        Field so = ((NameType)type).getField(name, typeDec, showError);
        FieldDec dec = null;
        if (so != null) {
            dec = (FieldDec)so.getCorrespondingDec();
        }
        return dec;
    }

    /**
     * Returns the MethodDec named <code>name</code>, with
     * formals <code>params</code>,
     * contained in <code>typeDec</code> with type <code>type</code>.
     * <code>showError</code> is passed to subsequent methods
     * to supress or warrant error printing.
     * This may return null.
     *
     * @param type      Type in which this method was declared.
     * @param name      name of the method.
     * @param typeDec   TypeDec in which we're searching.
     * @param params    the method's formal parameters.
     * @param showError <code>true</code> is an error should
     *                  be printed upon not finding a method.
     * @return          the method declared in <code>type</code>
     *                  named <code>name</code>, found by searching
     *                  from <code>typeDec</code>.  This may be
     *                  null.
     */
    public static MethodDec getMethodDec(Type type,
                                         String name,
                                         TypeDec typeDec,
                                         Exprs params,
                                         boolean showError) {
        Method so = ((NameType)type).getMethod(name, typeDec, params, showError);
        MethodDec dec = null;
        if (so != null) {
            dec = so.getMethodDec();
        }
        return dec;
    }
    
    /**
     * Returns the ConstructorDec named <code>name</code>, with
     * formals <code>params</code>,
     * contained in <code>typeDec</code> with type <code>type</code>.
     * <code>showError</code> is passed to subsequent constructors
     * to supress or warrant error printing.
     * This may return null.
     *
     * @param type      Type in which this constructor was declared.
     * @param name      name of the constructor.
     * @param typeDec   TypeDec in which we're searching.
     * @param params    the constructor's formal parameters.
     * @param showError <code>true</code> is an error should
     *                  be printed upon not finding a constructor.
     * @return          the constructor declared in <code>type</code>
     *                  named <code>name</code>, found by searching
     *                  from <code>typeDec</code>.  This may be
     *                  null.
     */
    public static ConstructorDec getConstructorDec(Type type,
                                                   TypeDec typeDec,
                                                   Exprs params,
                                                   boolean showError) {
        Constructor so = ((NameType)type).getConstructor(typeDec,
                                                         params, showError);
        ConstructorDec dec = null;
        if (so != null) {
            dec = (ConstructorDec)so.getCorrespondingDec();
        }
        return dec;
    }    
}
