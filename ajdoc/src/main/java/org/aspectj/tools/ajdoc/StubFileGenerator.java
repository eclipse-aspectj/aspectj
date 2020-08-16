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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.util.FileUtil;

/**
 * @author Mik Kersten
 */
class StubFileGenerator {

	static Map declIDTable = null;

	static void doFiles(AsmManager model, Hashtable table, File[] inputFiles, File[] signatureFiles) throws DocException {
		declIDTable = table;
		for (int i = 0; i < inputFiles.length; i++) {
			processFile(model, inputFiles[i], signatureFiles[i]);
		}
	}

	static void processFile(AsmManager model, File inputFile, File signatureFile) throws DocException {
		try {
			// Special Case for package-info.java just copy file directly.
			if(signatureFile.getName().equals("package-info.java")) {
			   FileUtil.copyFile(inputFile, signatureFile);
			   return;
			}
			
			String path = StructureUtil.translateAjPathName(signatureFile.getCanonicalPath());
			PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(path)));

			String packageName = StructureUtil.getPackageDeclarationFromFile(model, inputFile);

			if (packageName != null && packageName != "") {
				writer.println("package " + packageName + ";");
			}

			IProgramElement fileNode = model.getHierarchy().findElementForSourceFile(inputFile.getAbsolutePath());
			for (IProgramElement node : fileNode.getChildren()) {
				if (node.getKind().isPackageDeclaration()) {
					// skip
				} else if (node.getKind().equals(IProgramElement.Kind.IMPORT_REFERENCE)) {
					processImportDeclaration(node, writer);
				} else {
					try {
						processTypeDeclaration(node, writer);
					} catch (DocException d) {
						throw new DocException("File name invalid: " + inputFile.toString());
					}
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
		for (Object anImport : imports) {
			IProgramElement importNode = (IProgramElement) anImport;
			writer.println(importNode.getSourceSignature());
		}
	}

	private static void processTypeDeclaration(IProgramElement classNode, PrintWriter writer) throws DocException {

		String formalComment = addDeclID(classNode, classNode.getFormalComment());
		writer.println(formalComment);

		String signature = genSourceSignature(classNode);// StructureUtil.genSignature(classNode);
		if (signature == null) {
			throw new DocException("The java file is invalid");
		}

		// System.err.println("######" + signature + ", " + classNode.getName());
		if (!StructureUtil.isAnonymous(classNode) && !classNode.getName().equals("<undefined>")) {
			writer.println(signature + " { ");
			processMembers(classNode.getChildren(), writer, classNode.getKind().equals(IProgramElement.Kind.INTERFACE));
			writer.println();
			writer.println("}");
		}
	}

	private static void processMembers(List/* IProgramElement */members, PrintWriter writer, boolean declaringTypeIsInterface)
			throws DocException {
		for (Object o : members) {
			IProgramElement member = (IProgramElement) o;

			if (member.getKind().isType()) {
				if (!member.getParent().getKind().equals(IProgramElement.Kind.METHOD) && !StructureUtil.isAnonymous(member)) {// don't
					// print
					// anonymous
					// types
					// System.err.println(">>>>>>>>>>>>>" + member.getName() + "<<<<" + member.getParent());
					processTypeDeclaration(member, writer);
				}
			} else {
				String formalComment = addDeclID(member, member.getFormalComment());
				;
				writer.println(formalComment);

				String signature = "";
				if (!member.getKind().equals(IProgramElement.Kind.POINTCUT)
						&& !member.getKind().equals(IProgramElement.Kind.ADVICE)) {
					signature = member.getSourceSignature();// StructureUtil.genSignature(member);
					if (member.getKind().equals(IProgramElement.Kind.ENUM_VALUE)) {
						int index = members.indexOf(member);
						if ((index + 1 < members.size())
								&& ((IProgramElement) members.get(index + 1)).getKind().equals(IProgramElement.Kind.ENUM_VALUE)) {
							// if the next member is also an ENUM_VALUE:
							signature = signature + ",";
						} else {
							signature = signature + ";";
						}
					}
				}

				if (member.getKind().isDeclare()) {
					// System.err.println("> Skipping declare (ajdoc limitation): " + member.toLabelString());
				} else if (signature != null && signature != "" && !member.getKind().isInterTypeMember()
						&& !member.getKind().equals(IProgramElement.Kind.INITIALIZER) && !StructureUtil.isAnonymous(member)) {
					writer.print(signature);
				} else {
					// System.err.println(">> skipping: " + member.getKind());
				}

				if (member.getKind().equals(IProgramElement.Kind.METHOD)
						|| member.getKind().equals(IProgramElement.Kind.CONSTRUCTOR)) {
					if (member.getParent().getKind().equals(IProgramElement.Kind.INTERFACE) || signature.contains("abstract ")) {
						writer.println(";");
					} else {
						writer.println(" { }");
					}

				} else if (member.getKind().equals(IProgramElement.Kind.FIELD)) {
					// writer.println(";");
				}
			}
		}
	}

	/**
	 * Translates "aspect" to "class", as long as its not ".aspect"
	 */
	private static String genSourceSignature(IProgramElement classNode) {
		String signature = classNode.getSourceSignature();
		if (signature != null) {
			int index = signature.indexOf("aspect");
			if (index == 0 || (index != -1 && signature.charAt(index - 1) != '.')) {
				signature = signature.substring(0, index) + "class " + signature.substring(index + 6, signature.length());
			}
		}
		return signature;
	}

	static int nextDeclID = 0;

	static String addDeclID(IProgramElement decl, String formalComment) {
		String declID = "" + ++nextDeclID;
		declIDTable.put(declID, decl);
		return addToFormal(formalComment, Config.DECL_ID_STRING + declID + Config.DECL_ID_TERMINATOR);
	}

	/**
	 * We want to go: just before the first period just before the first @ just before the end of the comment
	 * 
	 * Adds a place holder for the period ('#') if one will need to be replaced.
	 */
	static String addToFormal(String formalComment, String string) {
		if (string == null || string.equals("")) {
			return formalComment;
		}
		// boolean appendPeriod = true;
		if ((formalComment == null) || formalComment.equals("")) {
			// formalComment = "/**\n * . \n */\n";
			formalComment = "/**\n * \n */\n";
			// appendPeriod = false;
		}
		formalComment = formalComment.trim();

		int atsignPos = formalComment.indexOf('@');
		int endPos = formalComment.indexOf("*/");
		int periodPos = formalComment.indexOf("/**");
		int position = 0;
		String periodPlaceHolder = "";
		if (periodPos != -1) {
			position = periodPos + 3;// length of "/**"
		} else if (atsignPos != -1) {
			string = string + "\n * ";
			position = atsignPos;
		} else if (endPos != -1) {
			string = "* " + string + "\n";
			position = endPos;
		} else {
			// !!! perhaps this error should not be silent
			throw new Error("Failed to append to formal comment for comment: " + formalComment);
		}

		return formalComment.substring(0, position) + periodPlaceHolder + string + formalComment.substring(position);
	}

}
