/* *******************************************************************
 * Copyright (c) 1999-2001 Xerox Corporation,
 *               2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Xerox/PARC     initial implementation
 *     Mik Kersten	  port to AspectJ 1.1+ code base
 * ******************************************************************/

package org.aspectj.tools.ajdoc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.HierarchyWalker;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.util.TypeSafeEnum;

/**
 * @author Mik Kersten
 */
class HtmlDecorator {

	private static final String POINTCUT_DETAIL = "Pointcut Detail";
	private static final String ADVICE_DETAIL = "Advice Detail";
	private static final String DECLARE_DETAIL = "Declare Detail";
	private static final String ADVICE_SUMMARY = "Advice Summary";
	private static final String POINTCUT_SUMMARY = "Pointcut Summary";
	private static final String DECLARE_SUMMARY = "Declare Summary";
	private static final String ITD_METHOD_SUMMARY = "Inter-Type Method Summary";
	private static final String ITD_FIELD_SUMMARY = "Inter-Type Field Summary";
	private static final String ITD_CONSTRUCTOR_SUMMARY = "Inter-Type Constructor Summary";

	static List<String> visibleFileList = new ArrayList<>();
	static Hashtable declIDTable = null;
	static File rootDir = null;
	static String docVisibilityModifier;

	static void decorateHTMLFromInputFiles(AsmManager model, Hashtable table, File newRootDir, File[] inputFiles, String docModifier)
			throws IOException {
		rootDir = newRootDir;
		declIDTable = table;
		docVisibilityModifier = docModifier;
		for (File inputFile : inputFiles) {
			decorateHTMLFromIPEs(getProgramElements(model, inputFile.getCanonicalPath()), rootDir.getCanonicalPath()
					+ Config.DIR_SEP_CHAR, docModifier, false);
		}
	}

	static void decorateHTMLFromIPEs(IProgramElement[] decls, String base, String docModifier, boolean exceededNestingLevel)
			throws IOException {
		if (decls != null) {
			for (IProgramElement decl : decls) {
				decorateHTMLFromIPE(decl, base, docModifier, exceededNestingLevel);
			}
		}
	}

	/**
	 * Before attempting to decorate the HTML file we have to verify that it exists, which depends on the documentation visibility
	 * specified to c.
	 *
	 * Depending on docModifier, can document - public: only public - protected: protected and public (default) - package: package
	 * protected and public - private: everything
	 */
	static void decorateHTMLFromIPE(IProgramElement decl, String base, String docModifier, boolean exceededNestingLevel)
			throws IOException {
		boolean nestedClass = false;
		if (decl.getKind().isType()) {
			boolean decorateFile = true;
			if (isAboveVisibility(decl)) {
				visibleFileList.add(decl.toSignatureString());
				String packageName = decl.getPackageName();
				String filename = "";
				if (packageName != null) {

					int index1 = base.lastIndexOf(Config.DIR_SEP_CHAR);
					int index2 = base.lastIndexOf(".");
					String currFileClass = "";
					if (index1 > -1 && index2 > 0 && index1 < index2) {
						currFileClass = base.substring(index1 + 1, index2);
					}

					// XXX only one level of nexting
					if (currFileClass.equals(decl.getDeclaringType())) {
						nestedClass = true;
						packageName = packageName.replace('.', '/');
						String newBase = "";
						if (base.lastIndexOf(Config.DIR_SEP_CHAR) > 0) {
							newBase = base.substring(0, base.lastIndexOf(Config.DIR_SEP_CHAR));
						}
						String signature = constructNestedTypeName(decl);

						filename = newBase + Config.DIR_SEP_CHAR + packageName + Config.DIR_SEP_CHAR + currFileClass + // "." +
								signature + ".html";
					} else {
						packageName = packageName.replace('.', '/');
						filename = base + packageName + Config.DIR_SEP_CHAR + decl.toSignatureString() + ".html";
					}
				} else {
					filename = base + decl.toSignatureString() + ".html";
				}
				if (!exceededNestingLevel) {
					decorateHTMLFile(new File(filename));
				} else {
					System.out.println("Warning: can not generate documentation for nested " + "inner class: "
							+ decl.toSignatureString());
				}
			}
		}
	}

	private static String constructNestedTypeName(IProgramElement node) {
		if (node.getParent().getKind().isSourceFile()) {
			return node.getName();
		} else {
			String nodeName = "";
			if (node.getKind().isType())
				nodeName += '.' + node.getName();
			return constructNestedTypeName(node.getParent()) + nodeName;
		}
	}

	/**
	 * Skips files that are public in the model but not public in the source, e.g. nested aspects.
	 */
	static void decorateHTMLFile(File file) throws IOException {
		if (!file.exists())
			return;

		System.out.println("> Decorating " + file.getCanonicalPath() + "...");
		BufferedReader reader = new BufferedReader(new FileReader(file));

		StringBuffer fileContents = new StringBuffer();
		String line = reader.readLine();
		while (line != null) {
			fileContents.append(line + "\n");
			line = reader.readLine();
		}

		boolean isSecond = false;
		int index = 0;
		IProgramElement decl;
		while (true) {

			// ---this next part is an inlined procedure that returns two values---
			// ---the next declaration and the index at which that declaration's---
			// ---DeclID sits in the .html file ---
			String contents = fileContents.toString();
			int start = contents.indexOf(Config.DECL_ID_STRING, index);
			int end = contents.indexOf(Config.DECL_ID_TERMINATOR, index);
			if (start == -1)
				decl = null;
			else if (end == -1)
				throw new Error("Malformed DeclID.");
			else {
				String tid = contents.substring(start + Config.DECL_ID_STRING.length(), end);
				decl = (IProgramElement) declIDTable.get(tid);
				index = start;
			}
			// --- ---
			// --- ---

			if (decl == null)
				break;
			fileContents.delete(start, end + Config.DECL_ID_TERMINATOR.length());
			if (decl.getKind().isType()) {
				isSecond = true;
				String fullname = "";
				if (decl.getParent().getKind().equals(IProgramElement.Kind.ASPECT)
						|| decl.getParent().getKind().equals(IProgramElement.Kind.CLASS)) {
					fullname += decl.getParent().toSignatureString().concat(".").concat(decl.toSignatureString());
				} else {
					fullname += decl.toSignatureString();
				}
				// only add aspect documentation if we're in the correct
				// file for the given IProgramElement
				if (file.getName().contains(fullname + ".html")) {
					addAspectDocumentation(decl, fileContents, index);
				}
			} else {
				decorateMemberDocumentation(decl, fileContents, index);
			}
			// Change "Class" to "Aspect"
			// moved this here because then can use the IProgramElement.Kind
			// rather than checking to see if there's advice - this fixes
			// the case with an inner aspect not having the title "Aspect"
			if (decl.getKind().equals(IProgramElement.Kind.ASPECT) && file.getName().contains(decl.toSignatureString())) {
				// only want to change "Class" to "Aspect" if we're in the
				// file corresponding to the IProgramElement
				String fullname = "";
				if (decl.getParent().getKind().equals(IProgramElement.Kind.ASPECT)
						|| decl.getParent().getKind().equals(IProgramElement.Kind.CLASS)) {
					fullname += decl.getParent().toSignatureString().concat(".").concat(decl.toSignatureString());
				} else {
					fullname += decl.toSignatureString();
				}
				if (!file.getName().contains(fullname + ".html")) {
					// we're still in the file for a parent IPE
					continue;
				}

				boolean br = true;
				int classStartIndex = fileContents.toString().indexOf("<BR>\nClass ");
				if (classStartIndex == -1) {
					classStartIndex = fileContents.toString().indexOf("<H2>\nClass ");
					br = false;
				}
				if (classStartIndex == -1) {
					// Java8 looks more like this:
					// <h2 title="Class A" class="title">Class A</h2>
					classStartIndex = fileContents.toString().indexOf("<h2 title=\"Class ");
					int classEndIndex = fileContents.toString().indexOf("</h2>", classStartIndex);
					if (classStartIndex == -1) {
						// Java 13 - replaced h2 with h1 here
						classStartIndex = fileContents.toString().indexOf("<h1 title=\"Class ");
						classEndIndex = fileContents.toString().indexOf("</h1>", classStartIndex);
					}
					if (classEndIndex != -1) {
						// Convert it to "<h2 title="Aspect A" class="title">Aspect A</h2>"
						String classLine = fileContents.toString().substring(classStartIndex, classEndIndex);
						String aspectLine = classLine.replaceAll("Class ","Aspect ");
						fileContents.delete(classStartIndex, classEndIndex);
						fileContents.insert(classStartIndex, aspectLine);
					}
				}
				else if (classStartIndex != -1) {
					int classEndIndex = fileContents.toString().indexOf("</H2>", classStartIndex);
					if (classStartIndex != -1 && classEndIndex != -1) {
						String classLine = fileContents.toString().substring(classStartIndex, classEndIndex);
						String aspectLine = "";
						if (br) {
							aspectLine += "<BR>\n" + "Aspect " + classLine.substring(11, classLine.length());
						} else {
							aspectLine += "<H2>\n" + "Aspect " + classLine.substring(11, classLine.length());
						}
						fileContents.delete(classStartIndex, classEndIndex);
						fileContents.insert(classStartIndex, aspectLine);
					}
				}
				int secondClassStartIndex = fileContents.toString().indexOf("class <B>");
				if (secondClassStartIndex != -1) {
					String name = decl.toSignatureString();
					int classEndIndex = fileContents.toString().indexOf(name + "</B><DT>");
					if (secondClassStartIndex != -1 && classEndIndex != -1) {
						StringBuffer sb = new StringBuffer(fileContents.toString().substring(secondClassStartIndex, classEndIndex));
						sb.replace(0, 5, "aspect");
						fileContents.delete(secondClassStartIndex, classEndIndex);
						fileContents.insert(secondClassStartIndex, sb.toString());
					}
				}
				else {
					// Java8:
					// <pre>static class <span class="typeNameLabel">ClassA.InnerAspect</span>
					classStartIndex = fileContents.toString().indexOf("class <span class=\"typeNameLabel\">");
					if (classStartIndex == -1) {
						// Java7: 464604
						// <pre>public class <span class="strong">Azpect</span>
						classStartIndex = fileContents.toString().indexOf("class <span class=\"strong\">");
					}
					int classEndIndex = fileContents.toString().indexOf("</span>", classStartIndex);
					if (classEndIndex != -1) {
						// Convert it to "aspect <span class="typeNameLabel">ClassA.InnerAspect</span>"
						String classLine = fileContents.toString().substring(classStartIndex, classEndIndex);
						String aspectLine = "aspect"+fileContents.substring(classStartIndex+5,classEndIndex);
						fileContents.delete(classStartIndex, classEndIndex);
						fileContents.insert(classStartIndex, aspectLine);
					}
				}
			}
		}
		file.delete();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(fileContents.toString().getBytes());

		reader.close();
		fos.close();
	}

	static void addAspectDocumentation(IProgramElement node, StringBuffer fileBuffer, int index) {
		List<IProgramElement> pointcuts = new ArrayList<>();
		List<IProgramElement> advice = new ArrayList<>();
		List<IProgramElement> declares = new ArrayList<>();
		List<IProgramElement> methodsDeclaredOn = StructureUtil.getDeclareInterTypeTargets(node, IProgramElement.Kind.INTER_TYPE_METHOD);
		if (methodsDeclaredOn != null && !methodsDeclaredOn.isEmpty()) {
			insertDeclarationsSummary(fileBuffer, methodsDeclaredOn, ITD_METHOD_SUMMARY, index);
		}
		List<IProgramElement> fieldsDeclaredOn = StructureUtil.getDeclareInterTypeTargets(node, IProgramElement.Kind.INTER_TYPE_FIELD);
		if (fieldsDeclaredOn != null && !fieldsDeclaredOn.isEmpty()) {
			insertDeclarationsSummary(fileBuffer, fieldsDeclaredOn, ITD_FIELD_SUMMARY, index);
		}
		List<IProgramElement> constDeclaredOn = StructureUtil.getDeclareInterTypeTargets(node, IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR);
		if (fieldsDeclaredOn != null && !constDeclaredOn.isEmpty()) {
			insertDeclarationsSummary(fileBuffer, constDeclaredOn, ITD_CONSTRUCTOR_SUMMARY, index);
		}
		for (IProgramElement member : node.getChildren()) {
			if (member.getKind().equals(IProgramElement.Kind.POINTCUT)) {
				pointcuts.add(member);
			} else if (member.getKind().equals(IProgramElement.Kind.ADVICE)) {
				advice.add(member);
			} else if (member.getKind().isDeclare() || member.getKind().isInterTypeMember()) {
				declares.add(member);
			}
		}
		if (declares.size() > 0) {
			insertDeclarationsDetails(fileBuffer, declares, DECLARE_DETAIL, index);
			insertDeclarationsSummary(fileBuffer, declares, DECLARE_SUMMARY, index);
		}
		if (pointcuts.size() > 0) {
			insertDeclarationsSummary(fileBuffer, pointcuts, POINTCUT_SUMMARY, index);
			insertDeclarationsDetails(fileBuffer, pointcuts, POINTCUT_DETAIL, index);
		}
		if (advice.size() > 0) {
			insertDeclarationsSummary(fileBuffer, advice, ADVICE_SUMMARY, index);
			insertDeclarationsDetails(fileBuffer, advice, ADVICE_DETAIL, index);
		}
		// add the 'aspect declarations' information against the type
		List<IProgramElement> parentsDeclaredOn = StructureUtil.getDeclareInterTypeTargets(node, IProgramElement.Kind.DECLARE_PARENTS);
		if (parentsDeclaredOn != null && parentsDeclaredOn.size() > 0) {
			decorateDocWithRel(node, fileBuffer, index, parentsDeclaredOn, HtmlRelationshipKind.ASPECT_DECLARATIONS);
		}
		// add the 'annotated by' information against the type
		List<String> annotatedBy = StructureUtil.getTargets(node, IRelationship.Kind.DECLARE_INTER_TYPE, "annotated by");
		if (annotatedBy != null && annotatedBy.size() > 0) {
			decorateDocWithRel(node, fileBuffer, index, annotatedBy, HtmlRelationshipKind.ANNOTATED_BY);
		}
		// add the 'advised by' information against the type
		List<String> advisedBy = StructureUtil.getTargets(node, IRelationship.Kind.ADVICE);
		if (advisedBy != null && advisedBy.size() > 0) {
			decorateDocWithRel(node, fileBuffer, index, advisedBy, HtmlRelationshipKind.ADVISED_BY);
		}
	}

	static void insertDeclarationsSummary(StringBuffer fileBuffer, List decls, String kind, int index) {
		if (!declsAboveVisibilityExist(decls))
			return;

		int insertIndex = findSummaryIndex(fileBuffer, index);

		// insert the head of the table
		String tableHead = "<!-- ======== " + kind.toUpperCase() + " ======= -->\n\n"
				+ "<TABLE BORDER=\"1\" WIDTH=\"100%\" CELLPADDING=\"1\""
				+ "CELLSPACING=\"0\"><TR><TD COLSPAN=2 BGCOLOR=\"#CCCCFF\">" + "<FONT SIZE=\"+2\"><B>" + kind
				+ "</B></FONT></TD></TR>\n";
		fileBuffer.insert(insertIndex, tableHead);
		insertIndex += tableHead.length();

		// insert the body of the table
		for (Object o : decls) {
			IProgramElement decl = (IProgramElement) o;
			if (isAboveVisibility(decl)) {
				// insert the table row accordingly
				String comment = generateSummaryComment(decl);
				String entry = "";
				if (kind.equals(ADVICE_SUMMARY)) {
					entry += "<TR><TD>" + "<A HREF=\"#" + generateHREFName(decl) + "\">" + "<TT>" + generateSignatures(decl)
							+ "</TT></A><BR>&nbsp;";
					if (!comment.equals("")) {
						entry += comment + "<P>";
					}
					entry += generateAffects(decl) + "</TD>" + "</TR><TD>\n";
				} else if (kind.equals(POINTCUT_SUMMARY)) {
					entry += "<TR><TD WIDTH=\"1%\">" + "<FONT SIZE=-1><TT>" + genAccessibility(decl) + "</TT></FONT>" + "</TD>\n"
							+ "<TD>" + "<TT><A HREF=\"#" + generateHREFName(decl) + "\">" + decl.toLabelString()
							+ "</A></TT><BR>&nbsp;";
					if (!comment.equals("")) {
						entry += comment + "<P>";
					}
					entry += "</TR></TD>\n";
				} else if (kind.equals(DECLARE_SUMMARY)) {
					entry += "<TR><TD WIDTH=\"1%\">" + "<FONT SIZE=-1><TT>" + generateModifierInformation(decl, false)
							+ "</TT></FONT>" + "</TD>" + "<TD>" + "<A HREF=\"#" + generateHREFName(decl) + "\">" + "<TT>"
							+ decl.toLabelString() + "</TT></A><P>" + generateAffects(decl);
				} else if (kind.equals(ITD_FIELD_SUMMARY) || kind.equals(ITD_METHOD_SUMMARY)) {
					entry += "<TR><TD WIDTH=\"1%\">" + "<FONT SIZE=-1><TT>" + generateModifierInformation(decl, false)
							+ "</TT></FONT>" + "</TD>" + "<TD>" + "<A HREF=\"#" + generateHREFName(decl) + "\">" + "<TT>"
							+ decl.toLabelString() + "</TT></A><P>" + generateDeclaredBy(decl);
				} else if (kind.equals(ITD_CONSTRUCTOR_SUMMARY)) {
					entry += "<TD>" + "<A HREF=\"#" + generateHREFName(decl) + "\">" + "<TT>" + decl.toLabelString()
							+ "</TT></A><P>" + generateDeclaredBy(decl);
				}

				// insert the entry
				fileBuffer.insert(insertIndex, entry);
				insertIndex += entry.length();
			}
		}

		// insert the end of the table
		String tableTail = "</TABLE><P>&nbsp;\n";
		fileBuffer.insert(insertIndex, tableTail);
		insertIndex += tableTail.length();
	}

	private static boolean declsAboveVisibilityExist(List decls) {
		boolean exist = false;
		for (Object decl : decls) {
			IProgramElement element = (IProgramElement) decl;
			if (isAboveVisibility(element))
				exist = true;
		}
		return exist;
	}

	private static boolean isAboveVisibility(IProgramElement element) {
		IProgramElement.Accessibility acc = element.getAccessibility();
		if (docVisibilityModifier.equals("private")) {
			// show all classes and members
			return true;
		} else if (docVisibilityModifier.equals("package")) {
			// show package, protected and public classes and members
			return acc.equals(IProgramElement.Accessibility.PACKAGE) || acc.equals(IProgramElement.Accessibility.PROTECTED)
					|| acc.equals(IProgramElement.Accessibility.PUBLIC);
		} else if (docVisibilityModifier.equals("protected")) {
			// show protected and public classes and members
			return acc.equals(IProgramElement.Accessibility.PROTECTED) || acc.equals(IProgramElement.Accessibility.PUBLIC);
		} else if (docVisibilityModifier.equals("public")) {
			// show public classes and members
			return acc.equals(IProgramElement.Accessibility.PUBLIC);
		}
		return false;
	}

	private static String genAccessibility(IProgramElement decl) {
		if (decl.getAccessibility().equals(IProgramElement.Accessibility.PACKAGE)) {
			return "(package private)";
		} else {
			return decl.getAccessibility().toString();
		}
	}

	static void insertDeclarationsDetails(StringBuffer fileBuffer, List decls, String kind, int index) {
		if (!declsAboveVisibilityExist(decls))
			return;
		int insertIndex = findDetailsIndex(fileBuffer, index);

		// insert the table heading
		String detailsHeading = "<P>&nbsp;\n" + "<!-- ======== " + kind.toUpperCase() + " SUMMARY ======= -->\n\n"
				+ "<TABLE BORDER=\"1\" CELLPADDING=\"3\" CELLSPACING=\"0\" WIDTH=\"100%\">\n"
				+ "<TR BGCOLOR=\"#CCCCFF\" CLASS=\"TableHeadingColor\">\n" + "<TD COLSPAN=1><FONT SIZE=\"+2\">\n" + "<B>" + kind
				+ "</B></FONT></TD>\n" + "</TR>\n" + "</TABLE>";
		fileBuffer.insert(insertIndex, detailsHeading);
		insertIndex += detailsHeading.length();

		// insert the details
		for (int i = 0; i < decls.size(); i++) {
			IProgramElement decl = (IProgramElement) decls.get(i);
			if (isAboveVisibility(decl)) {
				String entry = "";

				// insert the table row accordingly
				entry += "<A NAME=\"" + generateHREFName(decl) + "\"><!-- --></A>\n";
				if (kind.equals(ADVICE_DETAIL)) {
					entry += "<H3>" + decl.getName() + "</H3><P>";
					entry += "<TT>" + generateSignatures(decl) + "</TT>\n" + "<P>" + generateDetailsComment(decl) + "<P>"
							+ generateAffects(decl);
				} else if (kind.equals(POINTCUT_DETAIL)) {
					entry += "<H3>" + decl.toLabelString() + "</H3><P>" + generateDetailsComment(decl);
				} else if (kind.equals(DECLARE_DETAIL)) {
					entry += "<H3>" + decl.toLabelString() + "</H3><P>" + generateModifierInformation(decl, true);
					if (!decl.getKind().equals(IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR)) {
						entry += "&nbsp;&nbsp;";
					}
					// if we're not a declare statement then we need to generate the signature.
					// If we did this for declare statements we get two repeated lines
					if (!decl.getKind().isDeclare()) {
						String sigs = generateSignatures(decl);
						entry += sigs + "<P>";
					}
					entry += generateAffects(decl) + generateDetailsComment(decl);
				}

				// insert the entry
				if (i != decls.size() - 1) {
					entry += "<P><HR>\n";
				} else {
					entry += "<P>";
				}
				fileBuffer.insert(insertIndex, entry);
				insertIndex += entry.length();
			}
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
		if (index1 < index2 && index1 != -1) {
			return index1;
		} else if (index2 != -1) {
			return index2;
		} else {
			return index;
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
		if (index1 != -1 && index1 < index2 && index1 < index3) {
			return index1;
		} else if (index2 != -1 && index2 < index1 && index2 < index3) {
			return index2;
		} else if (index3 != -1) {
			return index3;
		} else {
			return index;
		}
	}

	static void decorateDocWithRel(IProgramElement node, StringBuffer fileContentsBuffer, int index, List targets,
			HtmlRelationshipKind relKind) {
		if (targets != null && !targets.isEmpty()) {
			String adviceDoc = "<TABLE WIDTH=\"100%\" BGCOLOR=#FFFFFF><TR>"
					+ "<TD width=\"15%\" bgcolor=\"#FFD8B0\"><B><FONT COLOR=000000>" + relKind.toString() + "</font></b></td><td>";

			String relativePackagePath = getRelativePathFromHere(node.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR);

			List addedNames = new ArrayList();
			for (Iterator it = targets.iterator(); it.hasNext();) {
				Object o = it.next();
				IProgramElement currDecl = null;
				if (o instanceof String) {
					String currHandle = (String) o;
					currDecl = node.getModel().getHierarchy().findElementForHandle(currHandle);
				} else if (o instanceof IProgramElement) {
					currDecl = (IProgramElement) o;
				} else {
					return;
				}

				String packagePath = "";
				if (currDecl.getPackageName() != null && !currDecl.getPackageName().equals("")) {
					packagePath = currDecl.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR;
				}

				String hrefName = "";
				String hrefLink = "";

				// Start the hRefLink with the relative path based on where
				// *this* type (i.e. the advised) is in the package structure.
				hrefLink = relativePackagePath + packagePath;

				if (currDecl.getPackageName() != null) {
					hrefName = currDecl.getPackageName().replace('.', '/');
				}

				// in the case of nested classes, in order for the links to work,
				// need to have the correct file name which is something of the
				// form parentClass.nestedAspect.html
				List names = new ArrayList();
				IProgramElement parent = currDecl;
				while (parent != null
						&& parent.getParent() != null
						&& (!parent.getParent().getKind().equals(IProgramElement.Kind.FILE_JAVA) && !parent.getParent().getKind()
								.equals(IProgramElement.Kind.FILE_ASPECTJ))) {
					parent = parent.getParent();
					names.add(parent.toLinkLabelString());
				}
				StringBuffer sbuff = new StringBuffer();
				for (int i = names.size() - 1; i >= 0; i--) {
					String element = (String) names.get(i);
					if (i == 0) {
						sbuff.append(element);
					} else {
						sbuff.append(element + ".");
					}
				}
				// use the currDecl.toLabelString rather than currDecl.getName()
				// because two distinct advice blocks can have the same
				// currDecl.getName() and wouldn't both appear in the ajdoc
				hrefName += Config.DIR_SEP_CHAR + sbuff.toString() + "." + currDecl.toLabelString();

				// need to replace " with quot; otherwise the links wont work
				// for 'matches declare' relationship
				StringBuffer sb = new StringBuffer(currDecl.toLabelString());
				int nextQuote = sb.toString().indexOf("\"");
				while (nextQuote != -1) {
					sb.deleteCharAt(nextQuote);
					sb.insert(nextQuote, "quot;");
					nextQuote = sb.toString().indexOf("\"");
				}
				hrefLink += sbuff.toString() + ".html" + "#" + sb.toString();

				if (!addedNames.contains(hrefName)) {
					adviceDoc = adviceDoc + "<A HREF=\"" + hrefLink + "\"><tt>" + hrefName.replace('/', '.') + "</tt></A>";

					if (it.hasNext())
						adviceDoc += ", ";
					addedNames.add(hrefName);
				}
			}
			adviceDoc += "</TR></TD></TABLE>\n";
			fileContentsBuffer.insert(index, adviceDoc);
		}
	}

	static void decorateMemberDocumentation(IProgramElement node, StringBuffer fileContentsBuffer, int index) {
		List<String> targets = StructureUtil.getTargets(node, IRelationship.Kind.ADVICE);
		decorateDocWithRel(node, fileContentsBuffer, index, targets, HtmlRelationshipKind.ADVISED_BY);

		List<String> warnings = StructureUtil.getTargets(node, IRelationship.Kind.DECLARE, "matches declare");
		decorateDocWithRel(node, fileContentsBuffer, index, warnings, HtmlRelationshipKind.MATCHES_DECLARE);

		List<String> softenedBy = StructureUtil.getTargets(node, IRelationship.Kind.DECLARE, "softened by");
		decorateDocWithRel(node, fileContentsBuffer, index, softenedBy, HtmlRelationshipKind.SOFTENED_BY);

		List<String> annotatedBy = StructureUtil.getTargets(node, IRelationship.Kind.DECLARE_INTER_TYPE, "annotated by");
		decorateDocWithRel(node, fileContentsBuffer, index, annotatedBy, HtmlRelationshipKind.ANNOTATED_BY);
	}

	/**
	 * pr119453 - adding "declared by" relationship
	 */
	static String generateDeclaredBy(IProgramElement decl) {
		String entry = "<TABLE WIDTH=\"100%\" BGCOLOR=#FFFFFF><TR>"
				+ "<TD width=\"10%\" bgcolor=\"#FFD8B0\"><B><FONT COLOR=000000>" + "&nbsp;Declared&nbsp;by:</b></font></td><td>";

		String relativePackagePath = getRelativePathFromHere(decl.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR);

		if (decl != null && !StructureUtil.isAnonymous(decl.getParent())) {
			String packagePath = "";
			if (decl.getPackageName() != null && !decl.getPackageName().equals("")) {
				packagePath = decl.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR;
			}

			String typeSignature = constructNestedTypeName(decl);

			String hrefName = packagePath + typeSignature;

			// The hrefLink needs to just be the corresponding aspect
			String hrefLink = relativePackagePath + packagePath + typeSignature + ".html";

			entry += "<A HREF=\"" + hrefLink + "\"><tt>" + hrefName.replace('/', '.') + "</tt></A>"; // !!! don't replace
		}
		entry += "</B></FONT></TD></TR></TABLE>\n</TR></TD>\n";
		return entry;
	}

	/**
	 * TODO: probably want to make this the same for intros and advice.
	 */
	static String generateAffects(IProgramElement decl) {
		List targets = null;
		if (decl.getKind().isDeclare() || decl.getKind().isInterTypeMember()) {
			targets = StructureUtil.getDeclareTargets(decl);
		} else {
			targets = StructureUtil.getTargets(decl, IRelationship.Kind.ADVICE);
		}
		if (targets == null)
			return "";
		String entry = "<TABLE WIDTH=\"100%\" BGCOLOR=#FFFFFF><TR>";

		IProgramElement.Kind kind = decl.getKind();
		if (kind.equals(IProgramElement.Kind.ADVICE)) {
			entry += "<TD width=\"10%\" bgcolor=\"#FFD8B0\"><B><FONT COLOR=000000>" + HtmlRelationshipKind.ADVISES.toString()
					+ "</b></font></td><td>";
		} else if (kind.equals(IProgramElement.Kind.DECLARE_WARNING) || kind.equals(IProgramElement.Kind.DECLARE_ERROR)) {
			entry += "<TD width=\"10%\" bgcolor=\"#FFD8B0\"><B><FONT COLOR=000000>" + HtmlRelationshipKind.MATCHED_BY.toString()
					+ "</b></font></td><td>";
		} else if (kind.isDeclareAnnotation()) {
			entry += "<TD width=\"10%\" bgcolor=\"#FFD8B0\"><B><FONT COLOR=000000>" + HtmlRelationshipKind.ANNOTATES.toString()
					+ "</b></font></td><td>";
		} else if (kind.equals(IProgramElement.Kind.DECLARE_SOFT)) {
			entry += "<TD width=\"10%\" bgcolor=\"#FFD8B0\"><B><FONT COLOR=000000>" + HtmlRelationshipKind.SOFTENS.toString()
					+ "</b></font></td><td>";
		} else {
			entry += "<TD width=\"10%\" bgcolor=\"#FFD8B0\"><B><FONT COLOR=000000>" + HtmlRelationshipKind.DECLARED_ON.toString()
					+ "</b></font></td><td>";
		}

		String relativePackagePath = getRelativePathFromHere(decl.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR);

		List addedNames = new ArrayList(); // for ensuring that we don't add duplciates
		for (Iterator it = targets.iterator(); it.hasNext();) {
			String currHandle = (String) it.next();
			IProgramElement currDecl = decl.getModel().getHierarchy().findElementForHandle(currHandle);
			if (currDecl.getKind().equals(IProgramElement.Kind.CODE)) {
				currDecl = currDecl.getParent(); // promote to enclosing
			}
			if (currDecl != null && !StructureUtil.isAnonymous(currDecl.getParent())) {
				String packagePath = "";
				if (currDecl.getPackageName() != null && !currDecl.getPackageName().equals("")) {
					packagePath = currDecl.getPackageName().replace('.', '/') + Config.DIR_SEP_CHAR;
				}

				String typeSignature = constructNestedTypeName(currDecl);

				String hrefName = packagePath + typeSignature;

				// Start the hRefLink with the relative path based on where
				// *this* type (i.e. the advisor) is in the package structure.
				String hrefLink = relativePackagePath + packagePath + typeSignature + ".html";

				if (!currDecl.getKind().isType()) {
					hrefName += '.' + currDecl.getName();
					hrefLink += "#" + currDecl.toLabelString();
				}

				if (!addedNames.contains(hrefName)) {
					entry += "<A HREF=\"" + hrefLink + "\"><tt>" + hrefName.replace('/', '.') + "</tt></A>"; // !!! don't replace
					if (it.hasNext())
						entry += ", ";
					addedNames.add(hrefName);
				}
			}
		}
		entry += "</B></FONT></TD></TR></TABLE>\n</TR></TD>\n";
		return entry;
	}

	/**
	 * Generates a relative directory path fragment that can be used to navigate "upwards" from the directory location implied by
	 * the argument.
	 *
	 * @param packagePath
	 * @return String consisting of multiple "../" parts, one for each component part of the input <code>packagePath</code>.
	 */
	private static String getRelativePathFromHere(String packagePath) {
		StringBuffer result = new StringBuffer("");
		if (packagePath != null && (packagePath.contains("/"))) {
			StringTokenizer sTok = new StringTokenizer(packagePath, "/", false);
			while (sTok.hasMoreTokens()) {
				sTok.nextToken(); // don't care about the token value
				result.append(".." + Config.DIR_SEP_CHAR);
			}// end while
		}// end if

		return result.toString();
	}

	/**
	 * Generate the "public int"-type information about the given IProgramElement. Used when dealing with ITDs. To mirror the
	 * behaviour of methods and fields in classes, if we're generating the summary information we don't want to include "public" if
	 * the accessibility of the IProgramElement is public.
	 *
	 */
	private static String generateModifierInformation(IProgramElement decl, boolean isDetails) {
		String intro = "";
		if (decl.getKind().isDeclare()) {
			return intro + "</TT>";
		}
		if (isDetails || !decl.getAccessibility().equals(IProgramElement.Accessibility.PUBLIC)) {
			intro += "<TT>" + decl.getAccessibility().toString() + "&nbsp;";
		}
		if (decl.getKind().equals(IProgramElement.Kind.INTER_TYPE_FIELD)) {
			return intro + decl.getCorrespondingType() + "</TT>";
		} else if (decl.getKind().equals(IProgramElement.Kind.INTER_TYPE_CONSTRUCTOR) && isDetails) {
			return intro + "</TT>";
		} else {
			return intro + decl.getCorrespondingType(true) + "</TT>";
		}
	}

	static String generateIntroductionSignatures(IProgramElement decl, boolean isDetails) {
		return "<not implemented>";
	}

	static String generateSignatures(IProgramElement decl) {
		return "<B>" + decl.toLabelString() + "</B>";
	}

	static String generateSummaryComment(IProgramElement decl) {
		String COMMENT_INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"; // !!!
		String formattedComment = getFormattedComment(decl);
		int periodIndex = formattedComment.indexOf('.');
		if (formattedComment.equals("")) {
			return "";
		} else if (periodIndex != -1) {
			return COMMENT_INDENT + formattedComment.substring(0, periodIndex + 1);
		} else {
			return COMMENT_INDENT + formattedComment;
		}
	}

	static String generateDetailsComment(IProgramElement decl) {
		return getFormattedComment(decl);
	}

	static String generateHREFName(IProgramElement decl) {
		StringBuffer hrefLinkBuffer = new StringBuffer();
		char[] declChars = decl.toLabelString().toCharArray();
		for (char declChar : declChars) {
			if (declChar == '"') {
				hrefLinkBuffer.append("quot;");
			} else {
				hrefLinkBuffer.append(declChar);
			}
		}
		return hrefLinkBuffer.toString();
	}

	/**
	 * Figure out the link relative to the package.
	 */
	static String generateAffectsHREFLink(String declaringType) {
		String link = rootDir.getAbsolutePath() + "/" + declaringType + ".html";
		return link;
	}

	/**
	 * This formats a comment according to the rules in the Java Langauge Spec: <I>The text of a docuemntation comment consists of
	 * the characters between the /** that begins the comment and the 'star-slash' that ends it. The text is devided into one or
	 * more lines. On each of these lines, the leading * characters are ignored; for lines other than the first, blanks and tabs
	 * preceding the initial * characters are also discarded.</I>
	 *
	 * TODO: implement formatting or linking for tags.
	 */
	static String getFormattedComment(IProgramElement decl) {

		String comment = decl.getFormalComment();
		if (comment == null)
			return "";

		String formattedComment = "";
		// strip the comment markers

		int startIndex = comment.indexOf("/**");
		int endIndex = comment.indexOf("*/");
		if (startIndex == -1) {
			startIndex = 0;
		} else {
			startIndex += 3;
		}
		if (endIndex == -1) {
			endIndex = comment.length();
		}
		comment = comment.substring(startIndex, endIndex);

		// string the leading whitespace and '*' characters at the beginning of each line
		BufferedReader reader = new BufferedReader(new StringReader(comment));
		try {
			for (String line = reader.readLine(); line != null; line = reader.readLine()) {
				line = line.trim();
				for (int i = 0; i < line.length(); i++) {
					if (line.charAt(0) == '*') {
						line = line.substring(1, line.length());
					} else {
						break;
					}
				}
				// !!! remove any @see and @link tags from the line
				// int seeIndex = line.indexOf("@see");
				// int linkIndex = line.indexOf("@link");
				// if ( seeIndex != -1 ) {
				// line = line.substring(0, seeIndex) + line.substring(seeIndex);
				// }
				// if ( linkIndex != -1 ) {
				// line = line.substring(0, linkIndex) + line.substring(linkIndex);
				// }
				formattedComment += line;
			}
		} catch (IOException ioe) {
			throw new Error("Couldn't format comment for declaration: " + decl.getName());
		}
		return formattedComment;
	}

	static public IProgramElement[] getProgramElements(AsmManager model, String filename) {

		IProgramElement file = model.getHierarchy().findElementForSourceFile(filename);
		final List nodes = new ArrayList();
		HierarchyWalker walker = new HierarchyWalker() {
			public void preProcess(IProgramElement node) {
				IProgramElement p = node;
				if (accept(node))
					nodes.add(p);
			}
		};

		file.walk(walker);
		return (IProgramElement[]) nodes.toArray(new IProgramElement[0]);
	}

	/**
	 * Rejects anonymous kinds by checking if their name is an integer
	 */
	static private boolean accept(IProgramElement node) {
		if (node.getKind().isType()) {
			boolean isAnonymous = StructureUtil.isAnonymous(node);
			return !node.getParent().getKind().equals(IProgramElement.Kind.METHOD) && !isAnonymous;
		} else {
			return !node.getKind().equals(IProgramElement.Kind.IMPORT_REFERENCE);
		}
	}

	/**
	 * TypeSafeEnum for the entries which need to be put in the html doc
	 */
	public static class HtmlRelationshipKind extends TypeSafeEnum {

		public HtmlRelationshipKind(String name, int key) {
			super(name, key);

		}

		public static HtmlRelationshipKind read(DataInputStream s) throws IOException {
			int key = s.readByte();
			switch (key) {
			case 1:
				return ADVISES;
			case 2:
				return ADVISED_BY;
			case 3:
				return MATCHED_BY;
			case 4:
				return MATCHES_DECLARE;
			case 5:
				return DECLARED_ON;
			case 6:
				return ASPECT_DECLARATIONS;
			case 7:
				return SOFTENS;
			case 8:
				return SOFTENED_BY;
			case 9:
				return ANNOTATES;
			case 10:
				return ANNOTATED_BY;
			case 11:
				return USES_POINTCUT;
			case 12:
				return POINTCUT_USED_BY;
			}
			throw new Error("weird relationship kind " + key);
		}

		public static final HtmlRelationshipKind ADVISES = new HtmlRelationshipKind("&nbsp;Advises:", 1);
		public static final HtmlRelationshipKind ADVISED_BY = new HtmlRelationshipKind("&nbsp;Advised&nbsp;by:", 2);
		public static final HtmlRelationshipKind MATCHED_BY = new HtmlRelationshipKind("&nbsp;Matched&nbsp;by:", 3);
		public static final HtmlRelationshipKind MATCHES_DECLARE = new HtmlRelationshipKind("&nbsp;Matches&nbsp;declare:", 4);
		public static final HtmlRelationshipKind DECLARED_ON = new HtmlRelationshipKind("&nbsp;Declared&nbsp;on:", 5);
		public static final HtmlRelationshipKind ASPECT_DECLARATIONS = new HtmlRelationshipKind("&nbsp;Aspect&nbsp;declarations:",
				6);
		public static final HtmlRelationshipKind SOFTENS = new HtmlRelationshipKind("&nbsp;Softens:", 7);
		public static final HtmlRelationshipKind SOFTENED_BY = new HtmlRelationshipKind("&nbsp;Softened&nbsp;by:", 8);
		public static final HtmlRelationshipKind ANNOTATES = new HtmlRelationshipKind("&nbsp;Annotates:", 9);
		public static final HtmlRelationshipKind ANNOTATED_BY = new HtmlRelationshipKind("&nbsp;Annotated&nbsp;by:", 10);
		public static final HtmlRelationshipKind USES_POINTCUT = new HtmlRelationshipKind("&nbsp;Uses&nbsp;pointcut:", 11);
		public static final HtmlRelationshipKind POINTCUT_USED_BY = new HtmlRelationshipKind("&nbsp;Pointcut&nbsp;used&nbsp;by:",
				12);

	}
}
