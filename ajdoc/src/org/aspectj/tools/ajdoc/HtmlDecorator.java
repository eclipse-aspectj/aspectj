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
import java.util.*;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;

/**
 * @author Mik Kersten
 */
class HtmlDecorator {

    static List visibleFileList = new ArrayList();
    static Hashtable declIDTable = null;
    static SymbolManager symbolManager = null;
    static File rootDir = null;

    static void decorateHTMLFromInputFiles(Hashtable table,
                                           File newRootDir,
                                           SymbolManager sm,
                                           File[] inputFiles,
                                           String docModifier ) throws IOException {
        rootDir = newRootDir;
        declIDTable = table;
        symbolManager = sm;
        for (int i = 0; i < inputFiles.length; i++) {
            decorateHTMLFromDecls(symbolManager.getDeclarations(inputFiles[i].getCanonicalPath()),
                                  rootDir.getCanonicalPath() + Config.DIR_SEP_CHAR,
                                  docModifier,
                                  false);
        }
    }

    static void decorateHTMLFromDecls(Declaration[] decls, String base, String docModifier, boolean exceededNestingLevel) throws IOException {
    	if ( decls != null ) {
            for (int i = 0; i < decls.length; i++) {
                Declaration decl = decls[i];
                decorateHTMLFromDecl(decl, base, docModifier, exceededNestingLevel);
            }
        }
    }

    /**
     * Before attempting to decorate the HTML file we have to verify that it exists,
     * which depends on the documentation visibility specified to c.
     *
     * Depending on docModifier, can document
     *   - public: only public
     *   - protected: protected and public (default)
     *   - package: package protected and public
     *   - private: everything
     */
    static void decorateHTMLFromDecl(Declaration decl,
                                     String base,
                                     String docModifier,
                                     boolean exceededNestingLevel ) throws IOException {
        boolean nestedClass = false;
        if ( decl.isType() ) {
            boolean decorateFile = true;
            if ( (docModifier.equals("private")) || // everything
                 (docModifier.equals("package") && decl.getModifiers().indexOf( "private" ) == -1) || // package
                 (docModifier.equals("protected") && (decl.getModifiers().indexOf( "protected" ) != -1 ||
                                                        decl.getModifiers().indexOf( "public" ) != -1 )) ||
                 (docModifier.equals("public") && decl.getModifiers().indexOf( "public" ) != -1) ) {
                visibleFileList.add(decl.getSignature());
                String packageName = decl.getPackageName();
                String filename    = "";
                if ( packageName != null ) {
                   
                   int index1 = base.lastIndexOf(Config.DIR_SEP_CHAR);
                   int index2 = base.lastIndexOf(".");
                   String currFileClass = "";
                   if (index1 > -1 && index2 > 0 && index1 < index2) {
                      currFileClass = base.substring(index1+1, index2);
                   }
                   
                   // XXX only one level of nexting
                   if (currFileClass.equals(decl.getDeclaringType())) {
                   	  nestedClass = true;
                      packageName = packageName.replace( '.','/' );
                      String newBase = "";
                      if ( base.lastIndexOf(Config.DIR_SEP_CHAR) > 0 ) {
                         newBase = base.substring(0, base.lastIndexOf(Config.DIR_SEP_CHAR));
                      }
                      String signature = constructNestedTypeName(decl.getNode());
                     
                      filename = newBase + Config.DIR_SEP_CHAR + packageName +
                                 Config.DIR_SEP_CHAR + currFileClass + //"." +
                                 signature + ".html"; 
                   } else {
                       packageName = packageName.replace( '.','/' ); 
                       filename = base + packageName + Config.DIR_SEP_CHAR + decl.getSignature() + ".html";
                   }
                }
                else {
                    filename = base + decl.getSignature() + ".html";
                }
                if (!exceededNestingLevel) {
                   decorateHTMLFile(new File(filename));
                   
                   decorateHTMLFromDecls(decl.getDeclarations(),
                                         base + decl.getSignature() + ".",
                                         docModifier,
                                         nestedClass);
                }
                else {
                   System.out.println("Warning: can not generate documentation for nested " +
                                      "inner class: " + decl.getSignature() );
                }
            }
        }
    }

    private static String constructNestedTypeName(IProgramElement node) {
    	if (node.getParent().getKind().isSourceFile()) {
    		return node.getName();
    	} else {
			String nodeName = "";
			if (node.getKind().isType()) nodeName += '.' + node.getName();
			return constructNestedTypeName(node.getParent()) + nodeName;
    	}
	}

	static void decorateHTMLFile(File file) throws IOException {
        System.out.println( "> Decorating " + file.getCanonicalPath() + "..." );
        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuffer fileContents = new StringBuffer();
        String line = reader.readLine();
        while( line != null ) {
            fileContents.append(line + "\n");
            line = reader.readLine();
        }

        boolean isSecond = false;
        int index = 0;
        IProgramElement decl;
        while ( true ) {

            //---this next part is an inlined procedure that returns two values---
            //---the next declaration and the index at which that declaration's---
            //---DeclID sits in the .html file                                 ---
            String contents = fileContents.toString();
            int start = contents.indexOf( Config.DECL_ID_STRING, index);
            int end   = contents.indexOf( Config.DECL_ID_TERMINATOR, index );
            if ( start == -1 )
                decl = null;
            else if ( end == -1 )
                throw new Error("Malformed DeclID.");
            else {
                String tid = contents.substring(start + Config.DECL_ID_STRING.length(), end);
                decl = (IProgramElement)declIDTable.get(tid);
                index = start;
            }
            //---                                                              ---
            //---                                                               ---

            if ( decl == null ) break;
            fileContents.delete(start, end + Config.DECL_ID_TERMINATOR.length());
            if ( decl.getKind().isType() ) {
                isSecond = true;
//                addIntroductionDocumentation(decl, fileContents, index);
//                addAdviceDocumentation(decl, fileContents, index);
//                addPointcutDocumentation(decl, fileContents, index);
                addAspectDocumentation(decl, fileContents, index);
            }
            else {
                decorateMemberDocumentation(decl, fileContents, index);
            } 
        } 
        
        // Change "Class" to "Aspect", HACK: depends on "affects:"
        int classStartIndex = fileContents.toString().indexOf("<BR>\nClass");
        if (classStartIndex != -1 &&
        	fileContents.toString().indexOf("Advises:") != -1) {
            int classEndIndex = fileContents.toString().indexOf("</H2>", classStartIndex);
            if (classStartIndex != -1 && classEndIndex != -1) { 
                String classLine = fileContents.toString().substring(classStartIndex, classEndIndex);
                String aspectLine = "<BR>\n" + "Aspect " + classLine.substring(11, classLine.length());
                fileContents.delete(classStartIndex, classEndIndex);
                fileContents.insert(classStartIndex, aspectLine);
            }
        }

        file.delete();
        FileOutputStream fos = new FileOutputStream( file );
        fos.write( fileContents.toString().getBytes() );
    }

    static void addAspectDocumentation(IProgramElement node, StringBuffer fileBuffer, int index ) {
//        List relations = AsmManager.getDefault().getRelationshipMap().get(node);
//        System.err.println("> node: " + node + ", " + "relations: " + relations);
        

    	List pointcuts = new ArrayList();
    	List advice = new ArrayList();
    	for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
    		IProgramElement member = (IProgramElement)it.next();
    		if (member.getKind().equals(IProgramElement.Kind.POINTCUT)) {
    			pointcuts.add(member);
    		} else if (member.getKind().equals(IProgramElement.Kind.ADVICE)) {
    			advice.add(member);
    		}
    	}
    	if (pointcuts.size() > 0) {
    		insertDeclarationsSummary(fileBuffer, pointcuts, "Pointcut Summary", index);
    		insertDeclarationsDetails(fileBuffer, pointcuts, "Pointcut Detail", index);
    	}
    	if (advice.size() > 0) {
    		insertDeclarationsSummary(fileBuffer, advice, "Advice Summary", index);
    		insertDeclarationsDetails(fileBuffer, advice, "Advice Detail", index);
    	}
    }
    
//    static void addIntroductionDocumentation(IProgramElement decl,
//                                   StringBuffer fileBuffer,
//                                   int index ) {
//        Declaration[] introductions = decl.getIntroductionDeclarations();
//        if ( introductions.length > 0 ) {
//            insertDeclarationsSummary(fileBuffer,
//                                      introductions,
//                                      "Introduction Summary",
//                                      index);
//            insertDeclarationsDetails(fileBuffer,
//                                      introductions,
//                                      "Introduction Detail",
//                                      index);
//        }
//    }

    static void insertDeclarationsSummary(StringBuffer  fileBuffer,
                                          List decls,
                                          String        kind,
                                          int           index) {
        int insertIndex = findSummaryIndex(fileBuffer, index);

        // insert the head of the table
        String tableHead =
                          "<!-- ======== " + kind.toUpperCase() + " ======= -->\n\n" +
                          "<TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"1\"" +
                          "CELLSPACING=\"0\"><TR><TD COLSPAN=2 BGCOLOR=\"#CCCCFF\">" +
                          "<FONT SIZE=\"+2\"><B>" + kind + "</B></FONT></TD></TR>\n";
        fileBuffer.insert(insertIndex, tableHead);
        insertIndex += tableHead.length();

        // insert the body of the table
        for ( int i = 0; i < decls.size(); i++ ) {
            IProgramElement decl = (IProgramElement)decls.get(i);

            // insert the table row accordingly
            String comment = generateSummaryComment(decl);
            String entry = "";
            if ( kind.equals( "Advice Summary" ) ) {
                entry +=
                        "<TR><TD>" +
                        "<A HREF=\"#" + generateHREFName(decl) + "\">" +
                        "<TT>" + generateAdviceSignatures(decl) +
						"</TT></A><BR>&nbsp;";
                if (!comment.equals("")) {
                    entry += comment + "<P>";
                }
                entry +=
                        generateAffects(decl, false) + "</TD>" +
                        "</TR><TD>\n";
            }
            else if ( kind.equals( "Pointcut Summary" ) ) {
                entry +=
                        "<TR><TD WIDTH=\"1%\">" +
                        "<FONT SIZE=-1><TT>" + genAccessibility(decl) + "</TT></FONT>" +
                        "</TD>\n" +
                        "<TD>" +
                        "<TT><A HREF=\"#" + generateHREFName(decl) + "\">" +
                        decl.toLabelString() + "</A></TT><BR>&nbsp;";
                if (!comment.equals("")) {
                    entry += comment + "<P>";
                }
                entry +=
                        "</TR></TD>\n";
            }
            else if ( kind.equals( "Introduction Summary" ) ) {
                entry +=
                        "<TR><TD WIDTH=\"1%\">" +
                        "<FONT SIZE=-1><TT>" + decl.getModifiers() + "</TT></FONT>" +
                        "</TD>" +
                        "<TD>" +
                        "<A HREF=\"#" + generateHREFName(decl) + "\">" +
                        "<TT>introduction " + decl.toLabelString() + "</TT></A><P>" +
                        generateIntroductionSignatures(decl, false) +
                        generateAffects(decl, true);
            }

            // insert the entry
            fileBuffer.insert(insertIndex, entry);
            insertIndex += entry.length();
        }

        // insert the end of the table
        String tableTail = "</TABLE><P>&nbsp;\n";
        fileBuffer.insert(insertIndex, tableTail);
        insertIndex += tableTail.length();
    }

    private static String genAccessibility(IProgramElement decl) {
    	if (decl.getAccessibility().equals(IProgramElement.Accessibility.PACKAGE)) {
    		return "(package private)";
    	} else {
    		return decl.getAccessibility().toString();
    	}
	}

	static void insertDeclarationsDetails(StringBuffer  fileBuffer,
                                          List decls,
                                          String        kind,
                                          int           index) {
        int insertIndex = findDetailsIndex(fileBuffer, index);

        // insert the table heading
        String detailsHeading
            = "<P>&nbsp;\n" +
              "<!-- ======== " + kind.toUpperCase() + " SUMMARY ======= -->\n\n" +
              "<TABLE BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">\n" +
              "<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n" +
              "<TD COLSPAN=1><FONT SIZE=\"+2\">\n" +
              "<B>" + kind + "</B></FONT></TD>\n" +
              "</TR>\n" +
              "</TABLE>";
        fileBuffer.insert(insertIndex, detailsHeading);
        insertIndex += detailsHeading.length();

        // insert the details
        for ( int i = 0; i < decls.size(); i++ ) {
            IProgramElement decl = (IProgramElement)decls.get(i);
            String entry = "";

            // insert the table row accordingly
            entry +=  "<A NAME=\"" + generateHREFName(decl) + "\"><!-- --></A>\n";
            if ( kind.equals( "Advice Detail" ) ) {
                entry += "<H3>" + decl.getName() + "</H3><P>";
                entry +=
                        "<TT>" +
                        generateAdviceSignatures(decl) + "</TT>\n" + "<P>" +
                        generateDetailsComment(decl) + "<P>" +
                        generateAffects(decl, false);
            }
            else if (kind.equals("Pointcut Detail")) {
                entry +=
                        "<H3>" +
                        decl.toLabelString() +
                        "</H3><P>" +
                        generateDetailsComment(decl);
            }
            else if (kind.equals("Introduction Detail")) {
            	entry += "<H3>introduction " + decl.toLabelString() + "</H3><P>";
                entry +=
                        generateIntroductionSignatures(decl, true) +
                        generateAffects(decl, true) +
                        generateDetailsComment(decl);
            }

            // insert the entry
            if (i != decls.size()-1) {
                entry += "<P><HR>\n";
            }
            else {
                entry += "<P>";
            }
            fileBuffer.insert(insertIndex, entry);
            insertIndex += entry.length();
        }
    }

    /**
     * TODO: don't place the summary first.
     */
    static int findSummaryIndex(StringBuffer fileBuffer, int index) {
        String fbs = fileBuffer.toString();
        String MARKER_1 = "<!-- =========== FIELD SUMMARY =========== -->";
        String MARKER_2 = "<!-- ======== CONSTRUCTOR SUMMARY ======== -->";
        int index1 = fbs.indexOf(MARKER_1, index);
        int index2 = fbs.indexOf(MARKER_2, index);
        if (index1 < index2) {
            return index1;
        }
        else {
            return index2;
        }
    }

    static int findDetailsIndex(StringBuffer fileBuffer, int index) {
        String fbs = fileBuffer.toString();
        String MARKER_1 = "<!-- ========= CONSTRUCTOR DETAIL ======== -->";
        String MARKER_2 = "<!-- ============ FIELD DETAIL =========== -->";
        String MARKER_3 = "<!-- ============ METHOD DETAIL ========== -->";
        int index1 = fbs.indexOf(MARKER_1, index);
        int index2 = fbs.indexOf(MARKER_2, index);
        int index3 = fbs.indexOf(MARKER_3, index);
        if (index1 < index2 && index1 < index3) {
            return index1;
        }
        else if (index2 < index1 && index2 < index3) {
            return index2;
        }
        else {
            return index3;
        }
    }

    static void decorateMemberDocumentation(IProgramElement node,
                                             StringBuffer fileContentsBuffer,
                                              int index ) {
    	List targets = StructureUtil.getTargets(node, IRelationship.Kind.ADVICE);
        if (targets != null && !targets.isEmpty()) {
            String prevName = "";
            
            String adviceDoc
            = "<TABLE WIDTH=\"100%\" BGCOLOR=#FFFFFF><TR>" +
              "<TD width=\"15%\" bgcolor=\"#FFD8B0\"><B><FONT COLOR=000000>&nbsp;Advised&nbsp;by:</font></b></td><td>";

			String relativePackagePath =
				getRelativePathFromHere(
					node.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR);

            List addedNames = new ArrayList();
            for (Iterator it = targets.iterator(); it.hasNext(); ) {
            	String currHandle = (String)it.next();
            	IProgramElement currDecl = AsmManager.getDefault().getHierarchy().findElementForHandle(currHandle);
            	
        		String packagePath = "";
        		if (currDecl.getPackageName() != null && !currDecl.getPackageName().equals("")) {
        			packagePath = currDecl.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR;
        		}
            	
            	String hrefName = "";  
                String hrefLink = "";

				// Start the hRefLink with the relative path based on where
				// *this* type (i.e. the advised) is in the package structure.  
				hrefLink = relativePackagePath + packagePath;

                if (currDecl.getPackageName() != null ) {
                   hrefName = currDecl.getPackageName().replace('.', '/');
//                   hrefLink = "";//+ currDecl.getPackageName() + Config.DIR_SEP_CHAR;
                } 
                hrefName += Config.DIR_SEP_CHAR +
                              currDecl.getParent().toLinkLabelString()
							  + "." + currDecl.getName();
                  
                hrefLink += currDecl.getParent().toLinkLabelString() + ".html"
					  + "#" + currDecl.toLabelString(); 

                if (!addedNames.contains(hrefName)) {
	                adviceDoc = adviceDoc +
	                        "<A HREF=\"" + hrefLink + "\"><tt>"
	                        + hrefName.replace('/', '.') + "</tt></A>";  
	                
	                if (it.hasNext()) adviceDoc += ", ";
	                addedNames.add(hrefName);
                }
            }
            adviceDoc += "</TR></TD></TABLE>\n";
            fileContentsBuffer.insert( index, adviceDoc );
        }
    }

    /**
     * TODO: probably want to make this the same for intros and advice.
     */
    static String generateAffects(IProgramElement decl, boolean isIntroduction) {

      List targets = StructureUtil.getTargets(decl, IRelationship.Kind.ADVICE);
      if (targets == null) return null;
        List packageList = new ArrayList();
        String entry
        = "<TABLE WIDTH=\"100%\" BGCOLOR=#FFFFFF><TR>" +
          "<TD width=\"10%\" bgcolor=\"#FFD8B0\"><B><FONT COLOR=000000>&nbsp;Advises:</b></font></td><td>";
    
		String relativePackagePath =
			getRelativePathFromHere(
				decl.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR);    
    
        List addedNames = new ArrayList(); // for ensuring that we don't add duplciates
        for (Iterator it = targets.iterator(); it.hasNext(); ) {
        	String currHandle = (String)it.next();
        	IProgramElement currDecl = AsmManager.getDefault().getHierarchy().findElementForHandle(currHandle);
            if (currDecl.getKind().equals(IProgramElement.Kind.CODE)) {
            	currDecl = currDecl.getParent(); // promote to enclosing
            }
        	if (currDecl != null && !StructureUtil.isAnonymous(currDecl.getParent())) {
        		String packagePath = "";
        		if (currDecl.getPackageName() != null && !currDecl.getPackageName().equals("")) {
        			packagePath = currDecl.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR;
        		}
        		
				String typeSignature = constructNestedTypeName(currDecl);
        		
        		String hrefName = 
        			packagePath
					+ typeSignature;       		
        		
        		// Start the hRefLink with the relative path based on where
        		// *this* type (i.e. the advisor) is in the package structure.  
        		String hrefLink = 
					relativePackagePath
					+ packagePath 
					+ typeSignature
        			+ ".html";
        		
        		if (!currDecl.getKind().isType()) {
        			hrefName += '.' + currDecl.getName();
					hrefLink += "#" + currDecl.toLabelString();
        		}
        		
        		if (!addedNames.contains(hrefName)) {
	                entry += "<A HREF=\"" + hrefLink +
	                             "\"><tt>" + hrefName.replace('/', '.') + "</tt></A>";  // !!! don't replace
	                if (it.hasNext()) entry += ", ";
	                addedNames.add(hrefName);
        		}
        	}
        }
        entry += "</B></FONT></TD></TR></TABLE>\n</TR></TD>\n";
        return entry;
    }

    /**
     * Generates a relative directory path fragment that can be 
     * used to navigate "upwards" from the directory location
     * implied by the argument.
	 * @param packagePath
	 * @return String consisting of multiple "../" parts, one for 
	 * 		each component part of the input <code>packagePath</code>. 
	 */   
	private static String getRelativePathFromHere(String packagePath) {
        StringBuffer result = new StringBuffer(""); 
        if (packagePath != null && (packagePath.indexOf("/") != -1)) { 
                StringTokenizer sTok = new StringTokenizer(packagePath, "/", false); 
			while (sTok.hasMoreTokens()) {
				sTok.nextToken(); // don't care about the token value
				result.append(".." + Config.DIR_SEP_CHAR);
			}// end while
		}// end if
		
		return result.toString();
	}

	static String generateIntroductionSignatures(IProgramElement decl, boolean isDetails) {
    	return "<not implemented>";
    	//        Declaration[] decls = decl.getDeclarations();
//        String entry = "";
//        for ( int j = 0; j < decls.length; j++ ) {
//            Declaration currDecl = decls[j];
//            if ( currDecl != null ) {
//                entry +=
//                        "<TT><B>" +
//                        currDecl.getSignature() +
//                        "</B></TT><BR>";
//            }
//            if (isDetails) {
//                entry += generateDetailsComment(currDecl) + "<P>";
//            }
//            else {
//                entry += generateSummaryComment(currDecl) + "<P>";
//            }
//        }
//        return entry;
    }

    static String generateAdviceSignatures(IProgramElement decl ) {
        return "<B>" + decl.toLabelString() + "</B>";
    }

    static String generateSummaryComment(IProgramElement decl) {
        String COMMENT_INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; // !!!
        String formattedComment = getFormattedComment(decl);
        int periodIndex = formattedComment.indexOf( '.' );
        if (formattedComment.equals("")) {
            return "";
        }
        else if ( periodIndex != -1 ) {
            return COMMENT_INDENT + formattedComment.substring( 0, periodIndex+1 ) ;
        }
        else {
            return COMMENT_INDENT + formattedComment;
        }
    }

    static String generateDetailsComment(IProgramElement decl) {
        return getFormattedComment(decl);
    }

    static String generateHREFName(IProgramElement decl) {
        String hrefLink = decl.toLabelString(); // !!!
        return hrefLink;
    }


    /**
     * Figure out the link relative to the package.
     */
    static String generateAffectsHREFLink(String declaringType) {
        String link = rootDir.getAbsolutePath() + "/" + declaringType + ".html";
        return link;
    }


    /**
     * This formats a comment according to the rules in the Java Langauge Spec:
     * <I>The text of a docuemntation comment consists of the characters between
     * the /** that begins the comment and the 'star-slash' that ends it.  The text is
     * devided into one or more lines.  On each of these lines, the leading *
     * characters are ignored; for lines other than the first, blanks and
     * tabs preceding the initial * characters are also discarded.</I>
     *
     * TODO: implement formatting or linking for tags.
     */
    static String getFormattedComment(IProgramElement decl) {

        String comment = decl.getFormalComment();
        if (comment == null) return "";

        String formattedComment = "";
        // strip the comment markers
        
        int startIndex = comment.indexOf("/**");
        int endIndex   = comment.indexOf("*/");
        if ( startIndex == -1 ) {
            startIndex = 0;
        }
        else {
            startIndex += 3;
        }
        if ( endIndex == -1 ) {
            endIndex = comment.length();
        }
        comment = comment.substring( startIndex, endIndex );

        // string the leading whitespace and '*' characters at the beginning of each line
        BufferedReader reader
            = new BufferedReader( new StringReader( comment ) );
        try {
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                line = line.trim();
                for (int i = 0; i < line.length(); i++ ) {
                    if ( line.charAt(0) == '*' ) {
                        line = line.substring(1, line.length());
                    }
                    else {
                        break;
                    }
                }
                // !!! remove any @see and @link tags from the line
                //int seeIndex  = line.indexOf("@see");
                //int linkIndex = line.indexOf("@link");
                //if ( seeIndex != -1 ) {
                //    line = line.substring(0, seeIndex) + line.substring(seeIndex);
                //}
                //if ( linkIndex != -1 ) {
                //    line = line.substring(0, linkIndex) + line.substring(linkIndex);
                //}
                formattedComment += line;
            }
        } catch ( IOException ioe ) {
            throw new Error( "Couldn't format comment for declaration: " +
                decl.getName() );
        }
        return formattedComment;
    }
}
