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
 * ******************************************************************/


package org.aspectj.ajde.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.LinkNode;
import org.aspectj.asm.ProgramElementNode;
import org.aspectj.asm.RelationNode;
import org.aspectj.asm.StructureModel;
import org.aspectj.asm.StructureModelManager;
import org.aspectj.asm.StructureNode;

/**
 * Prototype functionality for package view clients.
 */  
public class StructureModelUtil {

	/**
	 * This method returns a map from affected source lines in a class to
	 * a List of aspects affecting that line.
	 * Based on method of same name by mik kirsten. To be replaced when StructureModelUtil
	 * corrects its implementation
	 * 
	 * @param the full path of the source file to get a map for
	 * 
	 * @return a Map from line numbers to a List of ProgramElementNodes.
	 */
	public static Map getLinesToAspectMap(String sourceFilePath) {

		Map annotationsMap =
			StructureModelManager.getDefault().getInlineAnnotations(
				sourceFilePath,
				true,
				true);

		Map aspectMap = new HashMap();
		Set keys = annotationsMap.keySet();
		for (Iterator it = keys.iterator(); it.hasNext();) {
			Object key = it.next();
			List annotations = (List) annotationsMap.get(key);
			for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
				ProgramElementNode node = (ProgramElementNode) it2.next();

				List relations = node.getRelations();

				for (Iterator it3 = relations.iterator(); it3.hasNext();) {
					RelationNode relationNode = (RelationNode) it3.next();

					if (relationNode.getKind().equals("Advice")) {
						List children = relationNode.getChildren();

						List aspects = new Vector();

						for (Iterator it4 = children.iterator();
							it4.hasNext();
							) {
							Object object = it4.next();

							if (object instanceof LinkNode) {
								ProgramElementNode pNode =
									((LinkNode) object).getProgramElementNode();

								if (pNode.getProgramElementKind()
									== ProgramElementNode.Kind.ADVICE) {

									StructureNode theAspect = pNode.getParent();

									aspects.add(theAspect);

								}
							}
						}
						if (!aspects.isEmpty()) {
							aspectMap.put(key, aspects);
						}
					}

				}
			}
		}
		return aspectMap;
	}

	/**
	 * This method is copied from StructureModelUtil inoder for it to use the working
	 * version of getLineToAspectMap()
	 * 
	 * @return		the set of aspects with advice that affects the specified package
	 */
	public static Set getAspectsAffectingPackage(ProgramElementNode packageNode) {
		List files = StructureModelUtil.getFilesInPackage(packageNode);
		Set aspects = new HashSet();
		for (Iterator it = files.iterator(); it.hasNext();) {
			ProgramElementNode fileNode = (ProgramElementNode) it.next();
			Map adviceMap =
				getLinesToAspectMap(
					fileNode.getSourceLocation().getSourceFile().getAbsolutePath());
			Collection values = adviceMap.values();
			for (Iterator it2 = values.iterator(); it2.hasNext();) {
				aspects.add(it2.next());
			}
		}
		return aspects;
	}

	public static List getPackagesInModel() {
		List packages = new ArrayList();
		StructureModel model =
			Ajde.getDefault().getStructureModelManager().getStructureModel();
		if (model.equals(StructureModel.NO_STRUCTURE)) {
			return null;
		} else {
			return getPackagesHelper(
				(ProgramElementNode) model.getRoot(),
				ProgramElementNode.Kind.PACKAGE,
				null,
				packages);
		}
	}

	private static List getPackagesHelper(
		ProgramElementNode node,
		ProgramElementNode.Kind kind,
		String prename,
		List matches) {

		if (kind == null || node.getProgramElementKind().equals(kind)) {
			if (prename == null) {
				prename = new String(node.toString());
			} else {
				prename = new String(prename + "." + node);
			}
			Object[] o = new Object[2];
			o[0] = node;
			o[1] = prename;

			matches.add(o);
		}

		for (Iterator it = node.getChildren().iterator(); it.hasNext();) {
			StructureNode nextNode = (StructureNode) it.next();
			if (nextNode instanceof ProgramElementNode) {
				getPackagesHelper(
					(ProgramElementNode) nextNode,
					kind,
					prename,
					matches);
			}
		}

		return matches;
	}

	/**
	 * Helper function sorts a list of resources into alphabetical order
	 */
	private List sortElements(List oldElements) {
		Object[] temp = oldElements.toArray();
		SortingComparator comparator = new SortingComparator();

		Arrays.sort(temp, comparator);

		List newResources = Arrays.asList(temp);

		return newResources;
	}

	private static List sortArray(List oldElements) {
		Object[] temp = oldElements.toArray();
		SortArrayComparator comparator = new SortArrayComparator();

		Arrays.sort(temp, comparator);
		
		List newElements = Arrays.asList(temp);

		return newElements;
	}

	private class SortingComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			ProgramElementNode p1 = (ProgramElementNode) o1;
			ProgramElementNode p2 = (ProgramElementNode) o2;

			String name1 = p1.getName();
			String name2 = p2.getName();

			return name1.compareTo(name2);
		}
	}

	private static class SortArrayComparator implements Comparator {
		public int compare(Object o1, Object o2) {
			Object[] array1 = (Object[]) o1;
			Object[] array2 = (Object[]) o2;

			ProgramElementNode p1 = (ProgramElementNode) array1[1];
			ProgramElementNode p2 = (ProgramElementNode) array2[1];

			String name1 = p1.getName();
			String name2 = p2.getName();

			return name1.compareTo(name2);
		}
	}

	/**
	 * @return		all of the AspectJ and Java source files in a package
	 */ 
	public static List getFilesInPackage(ProgramElementNode packageNode) {
		List packageContents;
		if (packageNode == null) {
			return null;
		} else {
			packageContents = packageNode.getChildren();	
		}
		List files = new ArrayList();
		for (Iterator it = packageContents.iterator(); it.hasNext(); ) {
			ProgramElementNode packageItem = (ProgramElementNode)it.next();
			if (packageItem.getProgramElementKind() == ProgramElementNode.Kind.FILE_JAVA 
				|| packageItem.getProgramElementKind() == ProgramElementNode.Kind.FILE_ASPECTJ) {
				files.add(packageItem);
			}
		} 
		return files;
	}	
}


