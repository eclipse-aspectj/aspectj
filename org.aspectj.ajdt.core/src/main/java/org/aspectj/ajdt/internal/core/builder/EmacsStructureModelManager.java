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
 *     PARC     initial implementation
 * ******************************************************************/

package org.aspectj.ajdt.internal.core.builder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;

/**
 * @author Mik Kersten
 */
public class EmacsStructureModelManager {

	private static final String EXTERN_FILE_SUFFIX = ".ajesym";

	public EmacsStructureModelManager() {
		super();
	}

	public void externalizeModel(AsmManager model) {
		if (!model.getHierarchy().isValid())
			return;

		try {
			// Set fileSet = StructureModelManager.INSTANCE.getStructureModel().getFileMap().entrySet();
			Set fileSet = model.getHierarchy().getFileMapEntrySet();
			for (Object o : fileSet) {
				IProgramElement peNode = (IProgramElement) ((Map.Entry) o).getValue();
				dumpStructureToFile(peNode);
			}
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}

	// private void dumpStructureToFile(ProgramElementNode node) throws IOException {
	// String sourceName = node.getSourceLocation().getSourceFilePath();
	// String fileName = sourceName.substring(0, sourceName.lastIndexOf(".")) + EXTERN_FILE_SUFFIX;
	// BufferedWriter writer = new BufferedWriter(new FileWriter(new File(fileName)));
	// new SExpressionPrinter(writer).printDecls(node);
	// writer.flush();
	// }

	private void dumpStructureToFile(IProgramElement node) throws IOException {
		String s = node.getKind().toString();
		if (!(s.equals(IProgramElement.Kind.FILE_ASPECTJ.toString()) || s.equals(IProgramElement.Kind.FILE_JAVA.toString()))) {
			throw new IllegalArgumentException("externalize file, not " + node);
		}
		// source files have source locations
		String sourceName = node.getSourceLocation().getSourceFile().getAbsolutePath();
		String fileName = sourceName.substring(0, sourceName.lastIndexOf(".")) + EXTERN_FILE_SUFFIX;
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(new File(fileName)));
			new SExpressionPrinter(writer).printDecls(node);
			writer.flush();
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
				} // ignore
			}
		}
	}

	/**
	 * This class was not written in an OO style.
	 */
	private static class SExpressionPrinter {

		private BufferedWriter writer = null;

		public SExpressionPrinter(BufferedWriter writer) {
			this.writer = writer;
		}

		private void printDecls(IProgramElement node) {
			print("(");
			for (Object nodeObject : node.getChildren()) {
				// this ignores relations on the compile unit
				// throw new RuntimeException("unimplemented");
				// if (nodeObject instanceof IProgramElement) {
				IProgramElement child = (IProgramElement) nodeObject;
				printDecl(child, true);
				// }
				// else if (nodeObject instanceof LinkNode) {
				// LinkNode child = (LinkNode)nodeObject;
				// printDecl(child.getProgramElementNode(), false);
				// }
			}
			print(") ");
		}

		// private void printDecls(IRelationship node) {
		// // for (Iterator it = node.getTargets().iterator(); it.hasNext(); ) {
		// // // this ignores relations on the compile unit
		// // Object nodeObject = it.next();
		// // throw new RuntimeException("unimplemented");
		// //// if (nodeObject instanceof LinkNode) {
		// //// LinkNode child = (LinkNode)nodeObject;
		// //// if (//!child.getProgramElementNode().getKind().equals("stmnt") &&
		// //// !child.getProgramElementNode().getKind().equals("<undefined>")) {
		// //// printDecl(child.getProgramElementNode(), false);
		// ////// printDecl(child.getProgramElementNode(), false);
		// //// }
		// //// }
		// // }
		// }

		/**
		 * @param structureNode can be a ProgramElementNode or a LinkNode
		 */
		private void printDecl(IProgramElement node, boolean recurse) {
			if (node == null || node.getSourceLocation() == null)
				return;
			String kind = node.getKind().toString().toLowerCase();
			print("(");
			print("(" + node.getSourceLocation().getLine() + " . " + node.getSourceLocation().getColumn() + ") ");
			print("(" + node.getSourceLocation().getLine() + " . " + node.getSourceLocation().getColumn() + ") ");
			print(kind + " "); // 2

			// HACK:
			String displayName = node.toString().replace('\"', ' ');

			print("\"" + displayName + "\" ");
			if (node.getSourceLocation().getSourceFile().getAbsolutePath() != null) {
				print("\"" + fixFilename(node.getSourceLocation().getSourceFile().getAbsolutePath()) + "\""); // 4
			} else {
				print("nil");
			}
			if (node.getName() != null) {
				print("\"" + node.getDeclaringType() + "\" "); // 5
			} else {
				print("nil");
			}

			if (!recurse) {
				print("nil");
				print("nil");
				print("nil");
			} else {
				print("(");
				// if (node instanceof IProgramElement) {
				// java.util.List relations = ((IProgramElement)node).getRelations();
				// if (relations != null) {
				// for (Iterator it = relations.iterator(); it.hasNext(); ) {
				// IRelationship relNode = (IRelationship)it.next();
				// if (relNode.getKind() == IRelationship.Kind.ADVICE ||
				// relNode.getKind() == IRelationship.Kind.DECLARE) {
				// printDecls(relNode); // 6
				// }
				// }
				// }
				// }
				print(") ");
				print("(");
				print(") ");
				print("(");
				Iterator<IProgramElement> it3 = node.getChildren().iterator();
				if (it3.hasNext()) {
					while (it3.hasNext()) {
						// this ignores relations on the compile unit
						Object nodeObject = it3.next();
						if (nodeObject instanceof IProgramElement) {
							IProgramElement currNode = (IProgramElement) nodeObject;
							if (// !currNode.isStmntKind() &&
							!currNode.getKind().equals("<undefined>")) {
								printDecl(currNode, true);
							}
						}
					}
				}
				print(") ");
			}

			print(node.getKind().equals("class") ? "t " : "nil "); // 9
			// print(node.getKind().equals("introduction") ? "t " : "nil "); // 10
			print(node.getKind().equals("introduction") ? "nil " : "nil "); // 10
			print("nil "); // 11
			print("nil "); // 12
			print(")");
		}

		String fixFilename(String filename) {
			return subst("\\\\", "\\", filename);
		}

		private void print(String string) {
			try {
				writer.write(string + "\n");
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}

		private String subst(String n, String o, String in) {
			int pos = in.indexOf(o);
			if (pos == -1)
				return in;
			return in.substring(0, pos) + n + subst(n, o, (in.substring(pos + o.length())));
		}

		// private void lose(Error e) {
		// try {
		// print("(ERROR \"" + e.toString() + "\")");
		// }
		// catch(Error ex) { }
		// }
	}
}
