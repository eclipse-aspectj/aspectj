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
package org.aspectj.tools.doclets.standard;
import org.aspectj.ajdoc.PointcutDoc;

import com.sun.javadoc.ClassDoc;
import com.sun.javadoc.MemberDoc;
import com.sun.javadoc.ParamTag;
import com.sun.javadoc.Parameter;
import com.sun.javadoc.ProgramElementDoc;
import com.sun.javadoc.SeeTag;
import com.sun.javadoc.Tag;
import com.sun.javadoc.Type;
import com.sun.tools.doclets.Util;

import java.util.List;

public class PointcutSubWriter extends AbstractSubWriter {

    protected Class delegateClass() {
        return null;
    }

    public PointcutSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer,
         ClassDoc classdoc)
    {
        super(writer, classdoc);
    }

    public PointcutSubWriter
        (com.sun.tools.doclets.standard.SubWriterHolderWriter writer)
    {
        super(writer);
    }

    protected final String keyName() { return "Pointcut"; }

//      public void printInheritedSummaryAnchor(ClassDoc cd) {
//          writer.anchor("pointcuts_inherited_from_class_" + cd.qualifiedName());
//      }

//      public void printInheritedSummaryLabel(ClassDoc cd) {
//          String classlink = writer.getPreQualifiedClassLink(cd);
//          writer.bold();
//          writer.printText("doclet.Pointcuts_Inherited_From",
//                           Statics.type(cd),
//                           classlink);
//          writer.boldEnd();
//      }

    void printSignature(MemberDoc member) {
        PointcutDoc pcd = (PointcutDoc)member;
        writer.displayLength = 0;
	writer.pre();
        printModifiers(pcd);
        bold(pcd.name());
        printParameters(pcd);
        printResultType(pcd);
	writer.preEnd();
    }

    void printResultType(PointcutDoc pcd) {
        Type result = pcd.resultType();
        if (result != null) {
            writer.code();
            print(" returns ");
            printTypeLink(result);
            writer.codeEnd();
        }
    }

    protected void printSummaryLink(ClassDoc cd, ProgramElementDoc member) {
        PointcutDoc pcd = (PointcutDoc)member;
        String name = member.name();
        writer.bold();
        writer.printClassLink(cd, name + pcd.signature(), name, false);
        writer.boldEnd();
        writer.displayLength = name.length();
        printParameters(pcd);
        printResultType(pcd);

    }
    
    protected void printInheritedSummaryLink(ClassDoc cd, 
                                             ProgramElementDoc member) {
        PointcutDoc pcd = (PointcutDoc)member;
        String name = member.name();
        writer.printClassLink(cd, name + pcd.signature(), name, false);
    }

    protected void printSummaryType(ProgramElementDoc member) {
        PointcutDoc pcd = (PointcutDoc)member;
        writer.printTypeSummaryHeader();
        printModifier(pcd);
        writer.printTypeSummaryFooter();
        
    }

    protected void printBodyHtmlEnd(ClassDoc cd) {
    }

    protected void nonfinalPrintMember(ProgramElementDoc member) {
        PointcutDoc pcd = (PointcutDoc)member;
        writer.anchor(pcd.name() + pcd.signature());
        printHead(pcd);
        printSignature(pcd);
        printFullComment(pcd);
    }

    protected void printDeprecatedLink(ProgramElementDoc member) {
        writer.printClassLink(member.containingClass(),
                              member.name(), 
                              ((PointcutDoc)member).qualifiedName());
    }

    protected List getMembers(ClassDoc cd) {
        return Util.asList(((org.aspectj.ajdoc.ClassDoc)cd).pointcuts());
    }

    protected void printParameters(PointcutDoc member) {
        print('(');
        Parameter[] params = member.parameters();
        for (int i = 0; i < params.length; i++) {
            printParam(params[i]);
            if (i < params.length-1) {
                writer.print(',');
                writer.print(' ');
            }
        }
        writer.print(')');
    }

    protected void printParam(Parameter param) {
        printTypedName(param.type(), param.name());
    }

    protected void printParamTags(ParamTag[] params) {
        if (params.length > 0) {
            writer.dt();
            writer.boldText("doclet.Parameters");
            for (int i = 0; i < params.length; ++i) {
                ParamTag pt = params[i];
                writer.dd();
                writer.code();
                print(pt.parameterName());
                writer.codeEnd();
                print(" - ");
                writer.printInlineComment(pt);
            }
        }
    }

    protected void printReturnTag(Tag[] returnsTag) {
        if (returnsTag.length > 0) {
            writer.dt();
            writer.boldText("doclet.Returns");
            writer.dd();
            writer.printInlineComment(returnsTag[0]);
        }
    }
    
    protected void printOverridden(ClassDoc overridden, PointcutDoc pcd) {
        if (overridden != null) {
            String name = pcd.name();
            writer.dt();
            writer.boldText("doclet.Overrides");
            writer.dd();
            writer.printText("doclet.in_class",
                             writer.codeText
                             (writer.getClassLink(overridden, 
                                                  name + pcd.signature(), 
                                                  name, false)),
                             writer.codeText
                             (writer.getClassLink(overridden)));
        }
    }

    protected void printTags(ProgramElementDoc member) {
        PointcutDoc pcd = (PointcutDoc)member;
        ParamTag[] params = pcd.paramTags();
        Tag[] returnsTag = pcd.tags("return");
        Tag[] sinces = pcd.tags("since");
        SeeTag[] sees = pcd.seeTags();
        ClassDoc[] intfacs = member.containingClass().interfaces();
        ClassDoc overridden = pcd.overriddenClass();
        if (intfacs.length > 0 || overridden != null) {
            writer.dd();
            writer.dl();
            printOverridden(overridden, pcd);
            writer.dlEnd();
            writer.ddEnd();
        }
        if (params.length +
            returnsTag.length +
            sinces.length +
            sees.length > 0) {
            writer.dd();
            writer.dl();
            printParamTags(params);
            printReturnTag(returnsTag);
            writer.printSinceTag(pcd);
            writer.printSeeTags(pcd);
            writer.dlEnd();
            writer.ddEnd();
        }
    }
}
