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
     * which depends on the documentation visibility specified to javadoc.
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
            if ( (docModifier.equals( "private" )) || // everything
                 (docModifier.equals( "package" ) && decl.getModifiers().indexOf( "private" ) == -1) || // package
                 (docModifier.equals( "protected" ) && (decl.getModifiers().indexOf( "protected" ) != -1 ||
                                                        decl.getModifiers().indexOf( "public" ) != -1 )) ||
                 (docModifier.equals( "public" ) && decl.getModifiers().indexOf( "public" ) != -1) ) {
                visibleFileList.add( getName( decl ) );
                String packageName = decl.getPackageName();
                String filename    = "";
                //System.out.println(">>>" + decl.getDeclaringType() + ", " + base);
                if ( packageName != null ) {
                   int index1 = base.lastIndexOf(Config.DIR_SEP_CHAR);
                   int index2 = base.lastIndexOf(".");
                   String currFileClass = "";
                   if (index1 > -1 && index2 > 0 && index1 < index2) {
                      currFileClass = base.substring(index1+1, index2);
                   }
                   if (currFileClass.equals(decl.getDeclaringType())) {
                      // !!! hack for inner class !!!
                      packageName = packageName.replace( '.','/' );
                      String newBase = "";
                      if ( base.lastIndexOf(Config.DIR_SEP_CHAR) > 0 ) {
                         newBase = base.substring(0, base.lastIndexOf(Config.DIR_SEP_CHAR));
                      }
                      filename = newBase + Config.DIR_SEP_CHAR + packageName +
                                 Config.DIR_SEP_CHAR + currFileClass + //"." +
                                 getName(decl) + ".html";
                      nestedClass = true;
                    }
                   else {
                       packageName = packageName.replace( '.','/' ); // !!!
                       filename = base + packageName + Config.DIR_SEP_CHAR + getName(decl) + ".html";
                   }
                }
                else {
                    filename = base + getName(decl) + ".html";
                }
                if (!exceededNestingLevel) {
                   decorateHTMLFile(new File(filename));
                   decorateHTMLFromDecls(decl.getDeclarations(),
                                         base + getName(decl) + ".",
                                         docModifier,
                                         nestedClass);
                }
                else {
                   System.out.println("Warning: can not generate documentation for nested " +
                                      "inner class: " + getName(decl) );
                }
            }
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
            if ( decl.getKind().isTypeKind() ) {
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
        if (fileContents.toString().indexOf("affects: ") != -1) {
            int classStartIndex = fileContents.toString().indexOf("<BR>\nClass  ");
            int classEndIndex = fileContents.toString().indexOf("</H2>", classStartIndex);
            if (classStartIndex != -1 && classEndIndex != -1) {
                String classLine = fileContents.toString().substring(classStartIndex, classEndIndex);
                String aspectLine = "<BR>\n" + "Aspect" + classLine.substring(11, classLine.length());
                fileContents.delete(classStartIndex, classEndIndex);
                fileContents.insert(classStartIndex, aspectLine);
            }
        }

        file.delete();
        FileOutputStream fos = new FileOutputStream( file );
        fos.write( fileContents.toString().getBytes() );
    }

    static void addAspectDocumentation(IProgramElement node, StringBuffer fileBuffer, int index ) {
        List relations = AsmManager.getDefault().getRelationshipMap().get(node);
        System.err.println("> relations: " + relations);
        
//        if ( crosscuts.length > 0 ) {
//            insertDeclarationsSummary(fileBuffer, crosscuts, "Pointcut Summary", index);
//            insertDeclarationsDetails(fileBuffer, crosscuts, "Pointcut Detail", index);
//        }
    }
    
    
//    static void addPointcutDocumentation(IProgramElement decl, StringBuffer fileBuffer, int index ) {
//        List AsmManager.getDefault().getRelationshipMap().get()
//    	Declaration[] crosscuts = decl.getCrosscutDeclarations();
//        if ( crosscuts.length > 0 ) {
//            insertDeclarationsSummary(fileBuffer, crosscuts, "Pointcut Summary", index);
//            insertDeclarationsDetails(fileBuffer, crosscuts, "Pointcut Detail", index);
//        }
//    }
//
//    static void addAdviceDocumentation(IProgramElement decl, StringBuffer fileBuffer, int index ) {
//        Declaration[] advice = decl.getAdviceDeclarations();
//        if ( advice.length > 0 ) {
//            insertDeclarationsSummary(fileBuffer, advice, "Advice Summary", index);
//            insertDeclarationsDetails(fileBuffer, advice, "Advice Detail", index);
//        }
//    }
//
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
                                          Declaration[] decls,
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
        for ( int i = 0; i < decls.length; i++ ) {
            Declaration decl = decls[i];

            // insert the table row accordingly
            String comment = generateSummaryComment(decl);
            String entry = "";
            if ( kind.equals( "Advice Summary" ) ) {
                entry +=
                        "<TR><TD>" +
                        "<A HREF=\"#" + generateHREFName(decl) + "\">" +
                        "<TT>advice " + decl.getCrosscutDesignator() + "</TT></A><BR><TT>" +
                        generateAdviceSignatures(decl) + "</TT><BR>&nbsp;";
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
                        "<FONT SIZE=-1><TT>" + decl.getModifiers() + "</TT></FONT>" +
                        "</TD>\n" +
                        "<TD>" +
                        "<TT><A HREF=\"#" + generateHREFName(decl) + "\">" +
                        decl.getSignature() + "</A></TT><BR>&nbsp;";
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
                        "<TT>introduction " + decl.getCrosscutDesignator() + "</TT></A><P>" +
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

    static void insertDeclarationsDetails(StringBuffer  fileBuffer,
                                          Declaration[] decls,
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
        for ( int i = 0; i < decls.length; i++ ) {
            Declaration decl = decls[i];
            String entry = "";

            // insert the table row accordingly
            entry +=  "<A NAME=\"" + generateHREFName(decl) + "\"><!-- --></A>\n";
            if ( kind.equals( "Advice Detail" ) ) {
                String designatorHREFLink = generateDesignatorHREFLink(decl);
                if (designatorHREFLink != null) {
                    entry +=
                            "<H3>advice " + designatorHREFLink + "</H3><P>";
                }
                else {
                    entry +=
                            "<H3>advice " + decl.getCrosscutDesignator() + "</H3><P>";
                }
                entry +=
                        "<TT>" +
                        generateAdviceSignatures(decl) + "</TT>\n" + "<P>" +
                        generateDetailsComment(decl) + "<P>" +
                        generateAffects(decl, false);
            }
            else if (kind.equals("Pointcut Detail")) {
                entry +=
                        "<H3>" +
                        decl.getSignature() +
                        "</H3><P>" +
                        generateDetailsComment(decl);
            }
            else if (kind.equals("Introduction Detail")) {
                //String designatorHREFLink = generateDesignatorHREFLink(decl);
                //if (designatorHREFLink != null) {
                //    entry +=
                //            "<H3>introduction " + designatorHREFLink + "</H3><P>";
                //}
                //else {
                    entry +=
                            "<H3>introduction " + decl.getCrosscutDesignator() + "</H3><P>";
                //}
                entry +=
                        generateIntroductionSignatures(decl, true) +
                        generateAffects(decl, true) +
                        generateDetailsComment(decl);
            }

            // insert the entry
            if (i != decls.length-1) {
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
//        if (decl.isIntroduced()) {
//           // !!! HACK, THIS HAS TO BE CHANGED WITH THE SYMBOL MANAGER
//           String fname = decl.getFilename();
//           int index1 = fname.lastIndexOf('\\');
//           int index2 = fname.lastIndexOf(".java");
//           String introducingType = fname;
//           if (index1 != -1 && index2 != -1) {
//              introducingType = fname.substring(index1+1, index2);
//           }
//           //System.out.println( "decl: " + decl.getSignature() + ", ptb: " + decl.getFilename());
//           String hrefName = "";
//           if (decl.getPackageName() != null ) {
//              hrefName = decl.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR +
//                         introducingType;
//           }
//           else {
//                hrefName = introducingType;
//           }
//           String hrefLink = generateAffectsHREFLink( hrefName );
//           fileContentsBuffer.insert( index,
//                                      "<BR><B><FONT COLOR=CC6699>Introduced by: </FONT></B>" +
//                                      "<A HREF=\"" + hrefLink + "\">" +
//                                      hrefName.replace('/', '.') + "</A>" ); // !!! don't replace
//           return;
//        }
    	
    	List relations = AsmManager.getDefault().getRelationshipMap().get(node);
   
//        Declaration[] ptb = decl.getPointedToBy();
        if (relations != null && !relations.isEmpty()) {
            String prevName = "";
            String adviceDoc = "<BR><B><FONT COLOR=CC6699>Advised by: </FONT></B>";
            for (Iterator it = relations.iterator(); it.hasNext(); ) {
//            for ( int i = 0; i < ptb.length; i++ ) {
            	IRelationship curr = (IRelationship)it.next();
//                Declaration currDecl = ptb[i];
                String hrefName = "";
                
//                if (currDecl.getPackageName() != null ) {
//                   hrefName = currDecl.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR +
//                              currDecl.getDeclaringType();
//                }
//                else {
//                   hrefName = currDecl.getDeclaringType();
//                }
//                String hrefLink = generateAffectsHREFLink( hrefName );
//                if (!hrefName.equals(prevName)) { // !!! eliminates dupilcates since it's ordered
//                    if ( currDecl.getKind().equals( "advice" ) ) {
//                       if ( i > 0 ) {
//                          adviceDoc = adviceDoc + ", ";
//                        }
//                        adviceDoc = adviceDoc +
//                                "<A HREF=\"" + hrefLink + "\">"
//                                + hrefName.replace('/', '.') + "</A>";  // !!! don't replace
//                    }
//                 }
                prevName = hrefName;
            }
            //adviceDoc += "<BR>&nbsp;";
            fileContentsBuffer.insert( index, adviceDoc );
            //return lineHead + adviceDoc + lineTail;
        }
        else {
            ;// nop return lineHead + lineTail;
        }
    }

    /**
     * TODO: probably want to make this the same for intros and advice.
     */
    static String generateAffects( Declaration decl, boolean isIntroduction) {
        Declaration[] decls = null;
        if ( isIntroduction ) {
            decls = decl.getTargets(); // !!!
        }
        else {
            decls = decl.getPointsTo();
        }
        List addedDecls = new ArrayList();
        List packageList = new ArrayList();
        for ( int i = 0; i < decls.length; i++ ) {
            Declaration currDecl = decls[i];
            //if ( currDecl.getDeclaringType().equals( "not$found" ) ) {
            //   System.out.println( "!!!!!! " + currDecl.getSignature() );
            //}
            if ( currDecl != null ) {
               String extendedName = "";
               String packageName = currDecl.getPackageName();

               // !!! HACK FOR INNER CLASSES, ONLY WORKS FOR 1 LEVEL OF NESTING !!!
               String declaringType = currDecl.getDeclaringType();
               if (packageName != null && !packageName.equals("")) {
                  if (currDecl.isType() && declaringType != null && !declaringType.equals("not$found")) {
                      extendedName = packageName.replace('.', '/') + Config.DIR_SEP_CHAR + declaringType + ".";
                  }
                  else {
                      extendedName = packageName.replace('.', '/') + Config.DIR_SEP_CHAR;
                  }
               }

               //System.out.println("extendedName: " + extendedName);
               if ( isIntroduction ) {
                    if ( !addedDecls.contains(currDecl.getSignature() ) ) {
                        //addedDecls.add(currDecl.getPackageName() + "." + currDecl.getSignature());
                        addedDecls.add(extendedName + currDecl.getSignature());
                    }
                }
                else if ( !addedDecls.contains(currDecl.getDeclaringType() ) ) {
                    //addedDecls.add(currDecl.getPackageName() + "." + currDecl.getDeclaringType());
                    addedDecls.add(extendedName + currDecl.getDeclaringType());
                }
            }
        }
        Collections.sort(addedDecls,
                         new Comparator() {
            public int compare(Object o1, Object o2) {
            String s1 = (String)o1;
            String s2 = (String)o2;
            return s1.compareTo(s2);
            }
            }
                         );

        String entry
            = "<TABLE WIDTH=\"100%\" BGCOLOR=#FFFFFF><TR><TD WIDTH=\"20\">&nbsp;</TD>" +
              "<TD><FONT SIZE=-1>affects: ";
        String prevType = "";
        for ( int j = 0; j < addedDecls.size(); j++ ) {
            String currType = (String)addedDecls.get(j);
            // don't add duplicates
            if ( !currType.equals( prevType ) && currType.indexOf("not$found") == -1 ) { //!!!
                if ( j > 0 ) {
                    entry += ", ";
                }
                if ( generateAffectsHREFLink(currType) != "" ) {
                    entry += "<A HREF=\"" + generateAffectsHREFLink(currType) +
                             "\">" + currType.replace('/', '.') + "</A>";  // !!! don't replace
                }
                else {
                    entry += currType;
                }
            }
            prevType = currType;
        }
        entry += "</FONT></TD></TR></TABLE>\n</TR></TD>\n";
        return entry;
    }

    static String generateIntroductionSignatures(Declaration decl, boolean isDetails) {
        Declaration[] decls = decl.getDeclarations();
        String entry = "";
        for ( int j = 0; j < decls.length; j++ ) {
            Declaration currDecl = decls[j];
            if ( currDecl != null ) {
                entry +=
                        "<TT><B>" +
                        currDecl.getSignature() +
                        "</B></TT><BR>";
            }
            if (isDetails) {
                entry += generateDetailsComment(currDecl) + "<P>";
            }
            else {
                entry += generateSummaryComment(currDecl) + "<P>";
            }
        }
        return entry;
    }

    static String generateAdviceSignatures( Declaration decl ) {
        return "<B>" + decl.getSignature() + "</B>";
    }

    static String generateSummaryComment(Declaration decl) {
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

    static String generateDetailsComment(Declaration decl) {
        return getFormattedComment(decl);
    }

    static String generateHREFName(Declaration decl) {
        String hrefLink = decl.getSignature(); // !!!
        return hrefLink;
    }


    /**
     * Figure out the link relative to the package.
     */
    static String generateAffectsHREFLink(String declaringType) {
        //String offset = rootDir.getAbsolutePath() + "/" + declaringType.replace('.', '/') + ".html";
        String link = rootDir.getAbsolutePath() + "/" + declaringType + ".html";
        //System.out.println(">>" + link);
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
    static String getFormattedComment(Declaration decl) {
        String formattedComment = "";

        // strip the comment markers
        String comment = decl.getFormalComment();

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
                decl.getSignature() );
        }
        return formattedComment;
    }

    static String generateDesignatorHREFLink(Declaration decl) {
        Declaration ccutDecl = decl.getCrosscutDeclaration();
        if (ccutDecl != null) {
            // !!! the following stuff should use ccutDecl
            return
                "<A HREF=" +
                ccutDecl.getDeclaringType() + ".html#" + generateHREFName(ccutDecl) + ">" +
                ccutDecl.getSignature() + "</A>";
        }
        else {
            //String link = decl.getCrosscutDesignator();
            //System.out.println(">> link: " + link);
            //return
            //    "<A HREF=\"TransportAspect.html#" + generateHREFName( decl ) + "\">" +
            //    decl.getCrosscutDesignator() + "</A>";
            //return null;
            return null;
        }
    }


    // *************************************************************************** //
    // ** This stuff should be in Declaration                                   ** //
    // *************************************************************************** //

    static Declaration getCrosscutDeclaration(Declaration decl) {
        //String filename = "D:\\Projects\\AJDoc\\apples\\TransportAspect.java";
        //Declaration[] decls = symbolManager.getDeclarations(filename);
        //decls = decls[0].getDeclarations();
        //return decls[decls.length-2]; !!!
        return null;
    }

    static String getName(Declaration decl) {
        return decl.getSignature();
    }
}


    //
    // !!! this stub only guaranteed to work for classes or interfaces
    //
    /*
    static String getFullyQualifiedName(Declaration decl) {
    if ( decl.getDeclaringType() == null )
    return getName(decl);
    else
    return getFullyQualifiedName(decl.getDeclaringType()) + "$" + getName(decl);
    }
    */