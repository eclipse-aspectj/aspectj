/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation, 
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 *     Mik Kersten	  port to AspectJ 1.1+ code base
 * ******************************************************************/

package org.aspectj.tools.ajdoc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.StringTokenizer;

import java.util.*;

class Phase1 {

    static Hashtable declIDTable = null;

    static void doFiles(Hashtable table,
                        SymbolManager symbolManager,
                        File[] inputFiles,
                        File[] signatureFiles) {
        declIDTable = table;
        for (int i = 0; i < inputFiles.length; i++) {
            doFile(symbolManager, inputFiles[i], signatureFiles[i]);
        }
    }

    static void doFile(SymbolManager symbolManager, File inputFile, File signatureFile) {
        try {

            Declaration[] decls = symbolManager.getDeclarations(inputFile.getCanonicalPath());

            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(signatureFile.getCanonicalPath())));

            // write the package info
            if ( decls != null && decls[0] != null && decls[0].getPackageName() != null ) {
                writer.println( "package " + decls[0].getPackageName() + ";" );
            }

            if (decls != null) {
                doDecls(decls, writer, false);
            }
            writer.close(); //this isn't in a finally, because if we got an
            //error we don't really want the contents anyways
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    static void doDecls (Declaration[] decls,
                         PrintWriter writer,
                         boolean declaringDeclIsInterface) throws IOException {
        for (int i = 0; i < decls.length; i++) {
            Declaration decl = decls[i];
            //System.out.println( ">> sig: " + decl.getSignature() );
            doDecl(decl, writer, declaringDeclIsInterface);
        }
    }

    static void doDecl(Declaration decl,
                       PrintWriter writer,
                       boolean declaringDeclIsInterface) throws IOException {
        String formalComment = decl.getFormalComment();
        String fullSignature = decl.getFullSignature();
        System.err.println("> full: " + fullSignature);
        Declaration[]   ptbs = decl.getPointedToBy();
        Declaration[]   subs = decl.getDeclarations();
        if (decl.hasSignature()) {
            formalComment = addDeclID(decl, formalComment);

            writer.println(formalComment);

            // HACK: this should be in Declaration
            int implementsClauseIndex = fullSignature.indexOf(" implements");
            if (implementsClauseIndex != -1) {
                String newSignature = "";
                StringTokenizer st = new StringTokenizer(fullSignature.substring(implementsClauseIndex, fullSignature.length()));
                for (String element = (String)st.nextElement(); st.hasMoreElements(); element = (String)st.nextElement()) {
                    if (element.indexOf("$MightHaveAspect") != -1
                        && element.indexOf("implements") != -1) {
                        newSignature += element;
                    }
                }
                if (!newSignature.equals("")) {
                    writer.print(fullSignature.substring(0, implementsClauseIndex)
                                 + " implements " + newSignature + " " );
                } else {
                    writer.print(fullSignature.substring(0, implementsClauseIndex) + " " );
                }
            } else {
                writer.print(fullSignature + " " );
            }

            if ((!decl.hasBody() && !decl.getKind().equals( "interface" ) ||
                (decl.getKind().equals( "method" ) && declaringDeclIsInterface)) && // !!! bug in Jim's API?
                !(decl.getKind().equals("initializer") && decl.getModifiers().indexOf("static") != -1 ) ) {

				System.err.println(">>>> kind: " + decl.getKind());

                if (decl.getModifiers().indexOf("static final") != -1) {
                    String fullSig = decl.getFullSignature().trim();
                    String stripped = fullSig.substring(0, fullSig.lastIndexOf(' '));
                    //System.err.println(">>> " + fullSig);
                    String type = stripped.substring(stripped.lastIndexOf(' '), stripped.length());
                    //System.err.println("> type: " + type);

                    if (type.equals("boolean")) {
                        writer.println(" = false");
                    } else if (type.equals("char")) {
                        writer.println(" = '0'");
                    } else if (type.equals("byte")) {
                        writer.println(" = 0");
                    } else if (type.equals("short")) {
                        writer.println(" = 0");
                    } else if (type.equals("int")) {
                        writer.println(" = 0");
                    } else if (type.equals("long")) {
                        writer.println(" = 0");
                    } else if (type.equals("float")) {
                        writer.println(" = 0");
                    } else if (type.equals("double")) {
                        writer.println(" = 0");
                    } else if (type.equals("String")) {
                        writer.println(" = \"\"");
                    } else {
                        writer.println(" = null");
                    }
                }
                writer.println(";");
//            } else if ((!decl.hasBody() && !decl.getKind().equals( "interface" ) ||
//                (decl.getKind().equals( "method" ) && declaringDeclIsInterface)) && // !!! bug in Jim's API?
//                !(decl.getKind().equals("initializer") && decl.getModifiers().indexOf("static") != -1 ) ) {
//
//                writer.println(";");

            } else {
                if (subs != null) {
                   if ( decl.getKind().equals( "interface" ) ) {
                        declaringDeclIsInterface = true;
                    }
                    writer.println("{");
                    doDecls(subs, writer, declaringDeclIsInterface);
                    writer.println("}");
                }
            }
            writer.println();
        }
    }

    static int nextDeclID = 0;
    static String addDeclID(Declaration decl, String formalComment) {
        String declID = "" + ++nextDeclID;
        declIDTable.put(declID, decl);
        return addToFormal(formalComment, Config.DECL_ID_STRING + declID + Config.DECL_ID_TERMINATOR);
    }

    /**
     * We want to go:
     *   just before the first period
     *   just before the first @
     *   just before the end of the comment
     *
     * Adds a place holder for the period ('#') if one will need to be
     * replaced.
     */
    static String addToFormal(String formalComment, String string) {
        boolean appendPeriod = true;
        if ( (formalComment == null) || formalComment.equals("")) {
            //formalComment = "/**\n * . \n */\n";
            formalComment = "/**\n * \n */\n";
            appendPeriod = false;
        }
        formalComment = formalComment.trim();

        int atsignPos = formalComment.indexOf('@');
        int    endPos = formalComment.indexOf("*/");
        int periodPos = formalComment.indexOf("/**")+2;
        //if ( atsignPos == -1 ) {
        //   periodPos = formalComment.lastIndexOf(".");
        //} else {
        //   periodPos = formalComment.substring(0, atsignPos).lastIndexOf(".");
        //}
        int position  = 0;
        String periodPlaceHolder = "";
        if ( periodPos != -1 ) {
            position = periodPos+1;
            //if ( appendPeriod ) {
            //periodPlaceHolder = "#";
            //}
        }
        else if ( atsignPos != -1 ) {
            string = string + "\n * ";
            position = atsignPos;
        }
        else if ( endPos != -1 ) {
            string = "* " + string + "\n";
            position = endPos;
        }
        else {
            // !!! perhaps this error should not be silent
            throw new Error("Failed to append to formal comment for comment: " +
                formalComment );
        }

        return
            formalComment.substring(0, position) + periodPlaceHolder +
            string +
            formalComment.substring(position);
    }

}
