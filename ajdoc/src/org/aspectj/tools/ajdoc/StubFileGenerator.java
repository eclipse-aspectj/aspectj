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

import java.io.*;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;

class StubFileGenerator {

    static Hashtable declIDTable = null;

    static void doFiles(Hashtable table,
                        SymbolManager symbolManager,
                        File[] inputFiles,
                        File[] signatureFiles) {
        declIDTable = table;
        for (int i = 0; i < inputFiles.length; i++) {
            processFile(symbolManager, inputFiles[i], signatureFiles[i]);
        }
    }

    static void processFile(SymbolManager symbolManager, File inputFile, File signatureFile) {
        try {
 //            Declaration[] decls = symbolManager.getDeclarations(inputFile.getCanonicalPath());

            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(signatureFile.getCanonicalPath())));
            
            String packageName = StructureUtil.getPackageDeclarationFromFile(inputFile);
            
            if (packageName != null ) {
                writer.println( "package " + packageName + ";" );
            }

           	IProgramElement fileNode = (IProgramElement)AsmManager.getDefault().getHierarchy().findElementForSourceFile(inputFile.getAbsolutePath());
        	for (Iterator it = fileNode.getChildren().iterator(); it.hasNext(); ) {
        		IProgramElement node = (IProgramElement)it.next();
        		if (node.getKind().equals(IProgramElement.Kind.IMPORT_REFERENCE)) {
        			processImportDeclaration(node, writer);
        		} else {
        			processTypeDeclaration(node, writer);
        		}
        	}
           	
            // if we got an error we don't want the contents of the file
            writer.close(); 
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        } 
    }

    private static void processImportDeclaration(IProgramElement node, PrintWriter writer) throws IOException {
    	List imports = node.getChildren();
    	for (Iterator i = imports.iterator(); i.hasNext();) {
			IProgramElement importNode = (IProgramElement) i.next();
			writer.print("import ");
			writer.print(importNode.getName());
			writer.println(';');
		}  	 
    }
    
    private static void processTypeDeclaration(IProgramElement classNode, PrintWriter writer) throws IOException {
    	
//        String formalComment = classNode.getFormalComment();
//        String fullSignature = classNode.getFullSignature();
//        Declaration[]   ptbs = classNode.getPointedToBy();
//        Declaration[]   subs = classNode.getDeclarations();
    	
    	String formalComment = addDeclID(classNode, classNode.getFormalComment());
    	writer.println(formalComment);
    	
    	String signature = classNode.getSourceSignature();// StructureUtil.genSignature(classNode);
    	writer.println(signature + " {" );
    	processMembers(classNode.getChildren(), writer, classNode.getKind().equals(IProgramElement.Kind.INTERFACE));
    	writer.println();
		writer.println("}");
    }
      
    private static void processMembers(List/*IProgramElement*/ members, PrintWriter writer, boolean declaringTypeIsInterface) throws IOException {
    	for (Iterator it = members.iterator(); it.hasNext();) {
			IProgramElement member = (IProgramElement) it.next();
		
	    	if (member.getKind().isTypeKind()) {
				processTypeDeclaration(member, writer);
			} else {
		    	String formalComment = addDeclID(member, member.getFormalComment());;
		    	writer.println(formalComment);
		    	String signature = member.getSourceSignature();//StructureUtil.genSignature(member);
		    	writer.print(signature);
		    	
		    	if (member.getKind().equals(IProgramElement.Kind.METHOD)) {
		    		writer.println(" { }");
		    		
		    	} else if (member.getKind().equals(IProgramElement.Kind.FIELD)) {
		    		writer.println(";");
		    	}
			}
		}
    }

//  static void processClassDeclarations(IProgramElement fileNode,
//  PrintWriter writer,
//  boolean declaringDeclIsInterface) throws IOException {
//for (Iterator it = fileNode.getChildren().iterator(); it.hasNext(); ) {
//IProgramElement node = (IProgramElement)it.next();
//proc
//}
//for (int i = 0; i < decls.length; i++) {
//Declaration decl = decls[i];
//
////System.out.println( ">> sig: " + decl.getSignature() );
//doDecl(decl, writer, declaringDeclIsInterface);
//}
//}

    
//    	
//        if (decl.hasSignature()) {
//            formalComment = addDeclID(decl, formalComment);
//
//            writer.println(formalComment);
//
//            // HACK: this should be in Declaration
//            int implementsClauseIndex = fullSignature.indexOf(" implements");
//            if (implementsClauseIndex != -1) {
//                String newSignature = "";
//                StringTokenizer st = new StringTokenizer(fullSignature.substring(implementsClauseIndex, fullSignature.length()));
//                for (String element = (String)st.nextElement(); st.hasMoreElements(); element = (String)st.nextElement()) {
//                    if (element.indexOf("$MightHaveAspect") != -1
//                        && element.indexOf("implements") != -1) {
//                        newSignature += element;
//                    }
//                }
//                if (!newSignature.equals("")) {
//                    writer.print(fullSignature.substring(0, implementsClauseIndex)
//                                 + " implements " + newSignature + " " );
//                } else {
//                    writer.print(fullSignature.substring(0, implementsClauseIndex) + " " );
//                }
//            } else {
//                writer.print(fullSignature + " " );
//            }
//            
//            
//            if ((!decl.hasBody() && !decl.getKind().equals( "interface" ) ||
//                (decl.getKind().equals( "method" ) && declaringDeclIsInterface)) && // !!! bug in Jim's API?
//                !(decl.getKind().equals("initializer") && decl.getModifiers().indexOf("static") != -1 ) ) {
//
//                if (decl.getModifiers().indexOf("static final") != -1) {
//                    String fullSig = decl.getFullSignature().trim();
//                    String stripped = fullSig.substring(0, fullSig.lastIndexOf(' '));
//                    String type = stripped.substring(stripped.lastIndexOf(' '), stripped.length());
//
//                    if (type.equals("boolean")) {
//                        writer.println(" = false");
//                    } else if (type.equals("char")) {
//                        writer.println(" = '0'");
//                    } else if (type.equals("byte")) {
//                        writer.println(" = 0");
//                    } else if (type.equals("short")) {
//                        writer.println(" = 0");
//                    } else if (type.equals("int")) {
//                        writer.println(" = 0");
//                    } else if (type.equals("long")) {
//                        writer.println(" = 0");
//                    } else if (type.equals("float")) {
//                        writer.println(" = 0");
//                    } else if (type.equals("double")) {
//                        writer.println(" = 0");
//                    } else if (type.equals("String")) {
//                        writer.println(" = \"\"");
//                    } else {
//                        writer.println(" = null");
//                    }
//                }
//                writer.println(";");
////            } else if ((!decl.hasBody() && !decl.getKind().equals( "interface" ) ||
////                (decl.getKind().equals( "method" ) && declaringDeclIsInterface)) && // !!! bug in Jim's API?
////                !(decl.getKind().equals("initializer") && decl.getModifiers().indexOf("static") != -1 ) ) {
////
////                writer.println(";");
//
//            } else {
//                if (subs != null) {
//                   if ( decl.getKind().equals( "interface" ) ) {
//                        declaringDeclIsInterface = true;
//                    }
//                    writer.println("{");
//                    processDeclarations(subs, writer, declaringDeclIsInterface);
//                    writer.println("}");
//                }
//            }
//            writer.println();
//        }

    static int nextDeclID = 0;
    static String addDeclID(IProgramElement decl, String formalComment) {
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
