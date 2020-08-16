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
 * ******************************************************************/

package org.aspectj.testing.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;

/**
 * Prototype functionality for package view clients.
 */
public class StructureModelUtil {

	public static class ModelIncorrectException extends Exception {

		private static final long serialVersionUID = 8920868549577870993L;

		public ModelIncorrectException(String s) {
			super(s);
		}
	}

	/**
	 * Check the properties of the current model. The parameter string lists properties of the model that should be correct. If any
	 * of the properties are incorrect, a ModelIncorrectException is thrown.
	 * 
	 * @param toCheck comma separated list of name=value pairs that should be found in the ModelInfo object
	 * @throws ModelIncorrectException thrown if any of the name=value pairs in toCheck are not found
	 */
	public static void checkModel(String toCheck) throws ModelIncorrectException {
		Properties modelProperties = AsmManager.lastActiveStructureModel.summarizeModel().getProperties();

		// Break toCheck into pieces and check each exists
		StringTokenizer st = new StringTokenizer(toCheck, ",=");
		while (st.hasMoreTokens()) {
			String key = st.nextToken();
			String expValue = st.nextToken();
			boolean expectingZero = false;
			try {
				expectingZero = (Integer.parseInt(expValue) == 0);
			} catch (NumberFormatException nfe) {
				// this is ok as expectingZero will be false
			}
			String value = modelProperties.getProperty(key);
			if (value == null) {
				if (!expectingZero)
					throw new ModelIncorrectException("Couldn't find '" + key + "' property for the model");
			} else if (!value.equals(expValue)) {
				throw new ModelIncorrectException("Model property '" + key + "' incorrect:  Expected " + expValue + " but found "
						+ value);
			}
		}
	}

	/**
	 * This method returns a map from affected source lines in a class to a List of aspects affecting that line. Based on method of
	 * same name by mik kirsten. To be replaced when StructureModelUtil corrects its implementation
	 * 
	 * @param the full path of the source file to get a map for
	 * 
	 * @return a Map from line numbers to a List of ProgramElementNodes.
	 */
	public static Map getLinesToAspectMap(String sourceFilePath) {

		// Map annotationsMap =
		// AsmManager.getDefault().getInlineAnnotations(
		// sourceFilePath,
		// true,
		// true);

		Map aspectMap = new HashMap();
		// Set keys = annotationsMap.keySet();
		// for (Iterator it = keys.iterator(); it.hasNext();) {
		// Object key = it.next();
		// List annotations = (List) annotationsMap.get(key);
		// for (Iterator it2 = annotations.iterator(); it2.hasNext();) {
		// IProgramElement node = (IProgramElement) it2.next();

		// List relations = node.getRelations();
		//
		// for (Iterator it3 = relations.iterator(); it3.hasNext();) {
		// IRelationship relationNode = (IRelationship) it3.next();

		// if (relationNode.getKind().equals("Advice")) {
		// List children = relationNode.getTargets();
		//
		// List aspects = new Vector();
		//
		// for (Iterator it4 = children.iterator();
		// it4.hasNext();
		// ) {
		// Object object = it4.next();
		//
		// // if (object instanceof LinkNode) {
		// // IProgramElement pNode =
		// // ((LinkNode) object).getProgramElementNode();
		// //
		// // if (pNode.getProgramElementKind()
		// // == IProgramElement.Kind.ADVICE) {
		// //
		// // IProgramElement theAspect = pNode.getParent();
		// //
		// // aspects.add(theAspect);
		// //
		// // }
		// // }
		// }
		// if (!aspects.isEmpty()) {
		// aspectMap.put(key, aspects);
		// }
		// }
		//
		// }
		// }
		// }
		return aspectMap;
	}

	/**
	 * This method is copied from StructureModelUtil inoder for it to use the working version of getLineToAspectMap()
	 * 
	 * @return the set of aspects with advice that affects the specified package
	 */
	public static Set getAspectsAffectingPackage(IProgramElement packageNode) {
		List<IProgramElement> files = StructureModelUtil.getFilesInPackage(packageNode);
		Set aspects = new HashSet();
		for (IProgramElement fileNode : files) {
			Map adviceMap = getLinesToAspectMap(fileNode.getSourceLocation().getSourceFile().getAbsolutePath());
			Collection values = adviceMap.values();
			for (Object value : values) {
				aspects.add(value);
			}
		}
		return aspects;
	}

	public static List getPackagesInModel(AsmManager modl) {
		List packages = new ArrayList();
		IHierarchy model = modl.getHierarchy();
		if (model.getRoot().equals(IHierarchy.NO_STRUCTURE)) {
			return null;
		} else {
			return getPackagesHelper(model.getRoot(), IProgramElement.Kind.PACKAGE, null, packages);
		}
	}

	private static List getPackagesHelper(IProgramElement node, IProgramElement.Kind kind, String prename, List matches) {

		if (kind == null || node.getKind().equals(kind)) {
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

		for (IProgramElement nextNode : node.getChildren()) {
			getPackagesHelper(nextNode, kind, prename, matches);
		}

		return matches;
	}

	/**
	 * Helper function sorts a list of resources into alphabetical order
	 */
	// private List sortElements(List oldElements) {
	// Object[] temp = oldElements.toArray();
	// SortingComparator comparator = new SortingComparator();
	//
	// Arrays.sort(temp, comparator);
	//
	// List newResources = Arrays.asList(temp);
	//
	// return newResources;
	// }
	//
	// private static List sortArray(List oldElements) {
	// Object[] temp = oldElements.toArray();
	// SortArrayComparator comparator = new SortArrayComparator();
	//
	// Arrays.sort(temp, comparator);
	//		
	// List newElements = Arrays.asList(temp);
	//
	// return newElements;
	// }
	// private class SortingComparator implements Comparator {
	// public int compare(Object o1, Object o2) {
	// IProgramElement p1 = (IProgramElement) o1;
	// IProgramElement p2 = (IProgramElement) o2;
	//
	// String name1 = p1.getName();
	// String name2 = p2.getName();
	//
	// return name1.compareTo(name2);
	// }
	// }
	//
	// private static class SortArrayComparator implements Comparator {
	// public int compare(Object o1, Object o2) {
	// Object[] array1 = (Object[]) o1;
	// Object[] array2 = (Object[]) o2;
	//
	// IProgramElement p1 = (IProgramElement) array1[1];
	// IProgramElement p2 = (IProgramElement) array2[1];
	//
	// String name1 = p1.getName();
	// String name2 = p2.getName();
	//
	// return name1.compareTo(name2);
	// }
	// }
	/**
	 * @return all of the AspectJ and Java source files in a package
	 */
	public static List<IProgramElement> getFilesInPackage(IProgramElement packageNode) {
		List<IProgramElement> packageContents;
		if (packageNode == null) {
			return null;
		} else {
			packageContents = packageNode.getChildren();
		}
		List<IProgramElement> files = new ArrayList<>();
		for (IProgramElement packageItem : packageContents) {
			if (packageItem.getKind() == IProgramElement.Kind.FILE_JAVA
					|| packageItem.getKind() == IProgramElement.Kind.FILE_ASPECTJ) {
				files.add(packageItem);
			}
		}
		return files;
	}
}
