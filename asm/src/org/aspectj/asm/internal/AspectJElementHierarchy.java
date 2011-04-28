/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Mik Kersten     initial implementation 
 *     Andy Clement    Extensions for better IDE representation
 * ******************************************************************/

package org.aspectj.asm.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IHierarchy;
import org.aspectj.asm.IProgramElement;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.SourceLocation;

/**
 * @author Mik Kersten
 * @author Andy Clement
 */
public class AspectJElementHierarchy implements IHierarchy {

	private static final long serialVersionUID = 6462734311117048620L;

	private transient AsmManager asm;
	protected IProgramElement root = null;
	protected String configFile = null;

	// Access to the handleMap and typeMap are now synchronized - at least the find methods and the updateHandleMap function
	// see pr305788
	private Map<String, IProgramElement> fileMap = null;
	private Map<String, IProgramElement> handleMap = new HashMap<String, IProgramElement>();
	private Map<String, IProgramElement> typeMap = null;

	public AspectJElementHierarchy(AsmManager asm) {
		this.asm = asm;
	}

	public IProgramElement getElement(String handle) {
		return findElementForHandleOrCreate(handle, false);
	}

	public void setAsmManager(AsmManager asm) { // used when deserializing
		this.asm = asm;
	}

	public IProgramElement getRoot() {
		return root;
	}

	public String toSummaryString() {
		StringBuilder s = new StringBuilder();
		s.append("FileMap has " + fileMap.size() + " entries\n");
		s.append("HandleMap has " + handleMap.size() + " entries\n");
		s.append("TypeMap has " + handleMap.size() + " entries\n");
		s.append("FileMap:\n");
		for (Map.Entry<String, IProgramElement> fileMapEntry : fileMap.entrySet()) {
			s.append(fileMapEntry).append("\n");
		}
		s.append("TypeMap:\n");
		for (Map.Entry<String, IProgramElement> typeMapEntry : typeMap.entrySet()) {
			s.append(typeMapEntry).append("\n");
		}
		s.append("HandleMap:\n");
		for (Map.Entry<String, IProgramElement> handleMapEntry : handleMap.entrySet()) {
			s.append(handleMapEntry).append("\n");
		}
		return s.toString();
	}

	public void setRoot(IProgramElement root) {
		this.root = root;
		handleMap = new HashMap<String, IProgramElement>();
		typeMap = new HashMap<String, IProgramElement>();
	}

	public void addToFileMap(String key, IProgramElement value) {
		fileMap.put(key, value);
	}

	public boolean removeFromFileMap(String canonicalFilePath) {
		return fileMap.remove(canonicalFilePath) != null;
	}

	public void setFileMap(HashMap<String, IProgramElement> fileMap) {
		this.fileMap = fileMap;
	}

	public Object findInFileMap(Object key) {
		return fileMap.get(key);
	}

	public Set<Map.Entry<String, IProgramElement>> getFileMapEntrySet() {
		return fileMap.entrySet();
	}

	public boolean isValid() {
		return root != null && fileMap != null;
	}

	/**
	 * Returns the first match
	 * 
	 * @param parent
	 * @param kind not null
	 * @return null if not found
	 */
	public IProgramElement findElementForSignature(IProgramElement parent, IProgramElement.Kind kind, String signature) {
		for (IProgramElement node : parent.getChildren()) {
			if (node.getKind() == kind && signature.equals(node.toSignatureString())) {
				return node;
			} else {
				IProgramElement childSearch = findElementForSignature(node, kind, signature);
				if (childSearch != null) {
					return childSearch;
				}
			}
		}
		return null;
	}

	public IProgramElement findElementForLabel(IProgramElement parent, IProgramElement.Kind kind, String label) {
		for (IProgramElement node : parent.getChildren()) {
			if (node.getKind() == kind && label.equals(node.toLabelString())) {
				return node;
			} else {
				IProgramElement childSearch = findElementForLabel(node, kind, label);
				if (childSearch != null) {
					return childSearch;
				}
			}
		}
		return null;
	}

	/**
	 * Find the entry in the model that represents a particular type.
	 * 
	 * @param packageName the package in which the type is declared or null for the default package
	 * @param typeName the name of the type
	 * @return the IProgramElement representing the type, or null if not found
	 */
	public IProgramElement findElementForType(String packageName, String typeName) {

		synchronized (this) {
			// Build a cache key and check the cache
			StringBuilder keyb = (packageName == null) ? new StringBuilder() : new StringBuilder(packageName);
			keyb.append(".").append(typeName);
			String key = keyb.toString();
			IProgramElement cachedValue = typeMap.get(key);
			if (cachedValue != null) {
				return cachedValue;
			}

			List<IProgramElement> packageNodes = findMatchingPackages(packageName);

			for (IProgramElement pkg : packageNodes) {
				// this searches each file for a class
				for (IProgramElement fileNode : pkg.getChildren()) {
					IProgramElement cNode = findClassInNodes(fileNode.getChildren(), typeName, typeName);
					if (cNode != null) {
						typeMap.put(key, cNode);
						return cNode;
					}
				}
			}
		}
		return null;

		// IProgramElement packageNode = null;
		// if (packageName == null) {
		// packageNode = root;
		// } else {
		// if (root == null)
		// return null;
		// List kids = root.getChildren();
		// if (kids == null) {
		// return null;
		// }
		// for (Iterator it = kids.iterator(); it.hasNext() && packageNode == null;) {
		// IProgramElement node = (IProgramElement) it.next();
		// if (packageName.equals(node.getName())) {
		// packageNode = node;
		// }
		// }
		// if (packageNode == null) {
		// return null;
		// }
		// }

		// // this searches each file for a class
		// for (Iterator it = packageNode.getChildren().iterator(); it.hasNext();) {
		// IProgramElement fileNode = (IProgramElement) it.next();
		// IProgramElement cNode = findClassInNodes(fileNode.getChildren(), typeName, typeName);
		// if (cNode != null) {
		// typeMap.put(key, cNode);
		// return cNode;
		// }
		// }
		// return null;
	}

	/**
	 * Look for any package nodes matching the specified package name. There may be multiple in the case where the types within a
	 * package are split across source folders.
	 * 
	 * @param packagename the packagename being searched for
	 * @return a list of package nodes that match that name
	 */
	public List<IProgramElement> findMatchingPackages(String packagename) {
		List<IProgramElement> children = root.getChildren();
		// The children might be source folders or packages
		if (children.size() == 0) {
			return Collections.emptyList();
		}
		if ((children.get(0)).getKind() == IProgramElement.Kind.SOURCE_FOLDER) {
			String searchPackageName = (packagename == null ? "" : packagename); // default package means match on ""
			// dealing with source folders
			List<IProgramElement> matchingPackageNodes = new ArrayList<IProgramElement>();
			for (IProgramElement sourceFolder : children) {
				List<IProgramElement> possiblePackageNodes = sourceFolder.getChildren();
				for (IProgramElement possiblePackageNode : possiblePackageNodes) {
					if (possiblePackageNode.getKind() == IProgramElement.Kind.PACKAGE) {
						if (possiblePackageNode.getName().equals(searchPackageName)) {
							matchingPackageNodes.add(possiblePackageNode);
						}
					}
				}
			}
			// 'binaries' will be checked automatically by the code above as it is represented as a SOURCE_FOLDER
			return matchingPackageNodes;
		} else {
			// dealing directly with packages below the root, no source folders. Therefore at most one
			// thing to return in the list
			if (packagename == null) {
				// default package
				List<IProgramElement> result = new ArrayList<IProgramElement>();
				result.add(root);
				return result;
			}
			List<IProgramElement> result = new ArrayList<IProgramElement>();
			for (IProgramElement possiblePackage : children) {
				if (possiblePackage.getKind() == IProgramElement.Kind.PACKAGE && possiblePackage.getName().equals(packagename)) {
					result.add(possiblePackage);
				}
				if (possiblePackage.getKind() == IProgramElement.Kind.SOURCE_FOLDER) { // might be 'binaries'
					if (possiblePackage.getName().equals("binaries")) {
						for (IProgramElement possiblePackage2 : possiblePackage.getChildren()) {
							if (possiblePackage2.getKind() == IProgramElement.Kind.PACKAGE
									&& possiblePackage2.getName().equals(packagename)) {
								result.add(possiblePackage2);
								break; // ok to break here, can't be another entry under binaries
							}
						}
					}
				}
			}
			if (result.isEmpty()) {
				return Collections.emptyList();
			} else {
				return result;
			}
		}
	}

	private IProgramElement findClassInNodes(Collection<IProgramElement> nodes, String name, String typeName) {
		String baseName;
		String innerName;
		int dollar = name.indexOf('$');
		if (dollar == -1) {
			baseName = name;
			innerName = null;
		} else {
			baseName = name.substring(0, dollar);
			innerName = name.substring(dollar + 1);
		}

		for (IProgramElement classNode : nodes) {
			if (!classNode.getKind().isType()) {
				List<IProgramElement> kids = classNode.getChildren();
				if (kids != null && !kids.isEmpty()) {
					IProgramElement node = findClassInNodes(kids, name, typeName);
					if (node != null) {
						return node;
					}
				}
			} else {
			if (baseName.equals(classNode.getName())) {
				if (innerName == null) {
					return classNode;
				} else {
					return findClassInNodes(classNode.getChildren(), innerName, typeName);
				}
			} else if (name.equals(classNode.getName())) {
				return classNode;
			} else if (typeName.equals(classNode.getBytecodeSignature())) {
				return classNode;
			} else if (classNode.getChildren() != null && !classNode.getChildren().isEmpty()) {
				IProgramElement node = findClassInNodes(classNode.getChildren(), name, typeName);
				if (node != null) {
					return node;
				}
			}
			}
		}
		return null;
	}

	/**
	 * @param sourceFilePath modified to '/' delimited path for consistency
	 * @return a new structure node for the file if it was not found in the model
	 */
	public IProgramElement findElementForSourceFile(String sourceFile) {
		try {
			if (!isValid() || sourceFile == null) {
				return IHierarchy.NO_STRUCTURE;
			} else {
				String correctedPath = asm.getCanonicalFilePath(new File(sourceFile));
				// StructureNode node = (StructureNode)getFileMap().get(correctedPath);//findFileNode(filePath, model);
				IProgramElement node = (IProgramElement) findInFileMap(correctedPath);// findFileNode(filePath, model);
				if (node != null) {
					return node;
				} else {
					return createFileStructureNode(correctedPath);
				}
			}
		} catch (Exception e) {
			return IHierarchy.NO_STRUCTURE;
		}
	}

	/**
	 * TODO: discriminate columns
	 */
	public IProgramElement findElementForSourceLine(ISourceLocation location) {
		try {
			return findElementForSourceLine(asm.getCanonicalFilePath(location.getSourceFile()), location.getLine());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Never returns null
	 * 
	 * @param sourceFilePath canonicalized path for consistency
	 * @param lineNumber if 0 or 1 the corresponding file node will be returned
	 * @return a new structure node for the file if it was not found in the model
	 */
	public IProgramElement findElementForSourceLine(String sourceFilePath, int lineNumber) {
		String canonicalSFP = asm.getCanonicalFilePath(new File(sourceFilePath));
		// Used to do this:
		// IProgramElement node2 = findNodeForSourceLineHelper(root, canonicalSFP, lineNumber, -1);

		// Find the relevant source file node first
		IProgramElement node = findNodeForSourceFile(root, canonicalSFP);
		if (node == null) {
			return createFileStructureNode(sourceFilePath);
		}

		// Check if there is a more accurate child node of that source file node:
		IProgramElement closernode = findCloserMatchForLineNumber(node, lineNumber);
		if (closernode == null) {
			return node;
		} else {
			return closernode;
		}
	}

	/**
	 * Discover the node representing a particular source file.
	 * 
	 * @param node where in the model to start looking (usually the root on the initial call)
	 * @param sourcefilePath the source file being searched for
	 * @return the node representing that source file or null if it cannot be found
	 */
	public IProgramElement findNodeForSourceFile(IProgramElement node, String sourcefilePath) {
		// 1. why is <root> a sourcefile node?
		// 2. should isSourceFile() return true for a FILE that is a .class file...?
		if ((node.getKind().isSourceFile() && !node.getName().equals("<root>")) || node.getKind().isFile()) {
			ISourceLocation nodeLoc = node.getSourceLocation();
			if (nodeLoc != null && asm.getCanonicalFilePath(nodeLoc.getSourceFile()).equals(sourcefilePath)) {
				return node;
			}
			return null; // no need to search children of a source file node
		} else {
			// check the children
			for (IProgramElement child : node.getChildren()) {
				IProgramElement foundit = findNodeForSourceFile(child, sourcefilePath);
				if (foundit != null) {
					return foundit;
				}
			}
			return null;
		}
	}

	public IProgramElement findElementForOffSet(String sourceFilePath, int lineNumber, int offSet) {
		String canonicalSFP = asm.getCanonicalFilePath(new File(sourceFilePath));
		IProgramElement node = findNodeForSourceLineHelper(root, canonicalSFP, lineNumber, offSet);
		if (node != null) {
			return node;
		} else {
			return createFileStructureNode(sourceFilePath);
		}
	}

	private IProgramElement createFileStructureNode(String sourceFilePath) {
		// SourceFilePath might have originated on windows on linux...
		int lastSlash = sourceFilePath.lastIndexOf('\\');
		if (lastSlash == -1) {
			lastSlash = sourceFilePath.lastIndexOf('/');
		}
		// '!' is used like in URLs "c:/blahblah/X.jar!a/b.class"
		int i = sourceFilePath.lastIndexOf('!');
		int j = sourceFilePath.indexOf(".class");
		if (i > lastSlash && i != -1 && j != -1) {
			// we are a binary aspect in the default package
			lastSlash = i;
		}
		String fileName = sourceFilePath.substring(lastSlash + 1);
		IProgramElement fileNode = new ProgramElement(asm, fileName, IProgramElement.Kind.FILE_JAVA, new SourceLocation(new File(
				sourceFilePath), 1, 1), 0, null, null);
		// fileNode.setSourceLocation();
		fileNode.addChild(NO_STRUCTURE);
		return fileNode;
	}

	/**
	 * For a specified node, check if any of the children more accurately represent the specified line.
	 * 
	 * @param node where to start looking
	 * @param lineno the line number
	 * @return any closer match below 'node' or null if nothing is a more accurate match
	 */
	public IProgramElement findCloserMatchForLineNumber(IProgramElement node, int lineno) {
		if (node == null || node.getChildren() == null) {
			return null;
		}
		for (IProgramElement child : node.getChildren()) {
			ISourceLocation childLoc = child.getSourceLocation();
			if (childLoc != null) {
				if (childLoc.getLine() <= lineno && childLoc.getEndLine() >= lineno) {
					// This child is a better match for that line number
					IProgramElement evenCloserMatch = findCloserMatchForLineNumber(child, lineno);
					if (evenCloserMatch == null) {
						return child;
					} else {
						return evenCloserMatch;
					}
				} else if (child.getKind().isType()) { // types are a bit clueless about where they are... do other nodes have
					// similar problems??
					IProgramElement evenCloserMatch = findCloserMatchForLineNumber(child, lineno);
					if (evenCloserMatch != null) {
						return evenCloserMatch;
					}
				}
			}
		}
		return null;
	}

	private IProgramElement findNodeForSourceLineHelper(IProgramElement node, String sourceFilePath, int lineno, int offset) {
		if (matches(node, sourceFilePath, lineno, offset) && !hasMoreSpecificChild(node, sourceFilePath, lineno, offset)) {
			return node;
		}

		if (node != null) {
			for (IProgramElement child : node.getChildren()) {
				IProgramElement foundNode = findNodeForSourceLineHelper(child, sourceFilePath, lineno, offset);
				if (foundNode != null) {
					return foundNode;
				}
			}
		}

		return null;
	}

	private boolean matches(IProgramElement node, String sourceFilePath, int lineNumber, int offSet) {
		// try {
		// if (node != null && node.getSourceLocation() != null)
		// System.err.println("====\n1: " +
		// sourceFilePath + "\n2: " +
		// node.getSourceLocation().getSourceFile().getCanonicalPath().equals(sourceFilePath)
		// );
		ISourceLocation nodeSourceLocation = (node != null ? node.getSourceLocation() : null);
		return node != null
				&& nodeSourceLocation != null
				&& nodeSourceLocation.getSourceFile().getAbsolutePath().equals(sourceFilePath)
				&& ((offSet != -1 && nodeSourceLocation.getOffset() == offSet) || offSet == -1)
				&& ((nodeSourceLocation.getLine() <= lineNumber && nodeSourceLocation.getEndLine() >= lineNumber) || (lineNumber <= 1 && node
						.getKind().isSourceFile()));
		// } catch (IOException ioe) {
		// return false;
		// }
	}

	private boolean hasMoreSpecificChild(IProgramElement node, String sourceFilePath, int lineNumber, int offSet) {
		for (IProgramElement child : node.getChildren()) {
			if (matches(child, sourceFilePath, lineNumber, offSet)) {
				return true;
			}
		}
		return false;
	}

	public String getConfigFile() {
		return configFile;
	}

	public void setConfigFile(String configFile) {
		this.configFile = configFile;
	}

	public IProgramElement findElementForHandle(String handle) {
		return findElementForHandleOrCreate(handle, true);
	}

	// TODO: optimize this lookup
	// only want to create a file node if can't find the IPE if called through
	// findElementForHandle() to mirror behaviour before pr141730
	public IProgramElement findElementForHandleOrCreate(String handle, boolean create) {
		// try the cache first...
		IProgramElement ipe = null;
		synchronized (this) {
			ipe = handleMap.get(handle);
			if (ipe != null) {
				return ipe;
			}
			ipe = findElementForHandle(root, handle);
			if (ipe == null && create) {
				ipe = createFileStructureNode(getFilename(handle));
			}
			if (ipe != null) {
				cache(handle, ipe);
			}
		}
		return ipe;
	}

	private IProgramElement findElementForHandle(IProgramElement parent, String handle) {
		for (IProgramElement node : parent.getChildren()) {
			String nodeHid = node.getHandleIdentifier();
			if (handle.equals(nodeHid)) {
				return node;
			} else {
				if (handle.startsWith(nodeHid)) {
					// it must be down here if it is anywhere
					IProgramElement childSearch = findElementForHandle(node, handle);
					if (childSearch != null) {
						return childSearch;
					}
				}
			}
		}
		return null;
	}

	//
	// private IProgramElement findElementForBytecodeInfo(
	// IProgramElement node,
	// String parentName,
	// String name,
	// String signature) {
	// for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
	// IProgramElement curr = (IProgramElement)it.next();
	// if (parentName.equals(curr.getParent().getBytecodeName())
	// && name.equals(curr.getBytecodeName())
	// && signature.equals(curr.getBytecodeSignature())) {
	// return node;
	// } else {
	// IProgramElement childSearch = findElementForBytecodeInfo(curr, parentName, name, signature);
	// if (childSearch != null) return childSearch;
	// }
	// }
	// return null;
	// }

	protected void cache(String handle, IProgramElement pe) {
		if (!AsmManager.isCompletingTypeBindings()) {
			handleMap.put(handle, pe);
		}
	}

	public void flushTypeMap() {
		typeMap.clear();

	}

	public void flushHandleMap() {
		handleMap.clear();
	}

	public void flushFileMap() {
		fileMap.clear();
	}

	public void forget(IProgramElement compilationUnitNode, IProgramElement typeNode) {
		String k = null;
		synchronized (this) {
			// handle map
			// type map
			for (Map.Entry<String, IProgramElement> typeMapEntry : typeMap.entrySet()) {
				if (typeMapEntry.getValue() == typeNode) {
					k = typeMapEntry.getKey();
					break;
				}
			}
			if (k != null) {
				typeMap.remove(k);
			}
		}

		if (compilationUnitNode != null) {
			k = null;
			for (Map.Entry<String, IProgramElement> entry : fileMap.entrySet()) {
				if (entry.getValue() == compilationUnitNode) {
					k = entry.getKey();
					break;
				}
			}
			if (k != null) {
				fileMap.remove(k);
			}
		}
	}

	// TODO rename this method ... it does more than just the handle map
	public void updateHandleMap(Set<String> deletedFiles) {
		// Only delete the entries we need to from the handle map - for performance reasons
		List<String> forRemoval = new ArrayList<String>();
		Set<String> k = null;
		synchronized (this) {
			k = handleMap.keySet();
			for (String handle : k) {
				IProgramElement ipe = handleMap.get(handle);
				if (ipe == null) {
					System.err.println("handleMap expectation not met, where is the IPE for " + handle);
				}
				if (ipe == null || deletedFiles.contains(getCanonicalFilePath(ipe))) {
					forRemoval.add(handle);
				}
			}
			for (String handle : forRemoval) {
				handleMap.remove(handle);
			}
			forRemoval.clear();
			k = typeMap.keySet();
			for (String typeName : k) {
				IProgramElement ipe = typeMap.get(typeName);
				if (deletedFiles.contains(getCanonicalFilePath(ipe))) {
					forRemoval.add(typeName);
				}
			}
			for (String typeName : forRemoval) {
				typeMap.remove(typeName);
			}
			forRemoval.clear();
		}
		for (Map.Entry<String, IProgramElement> entry : fileMap.entrySet()) {
			String filePath = entry.getKey();
			if (deletedFiles.contains(getCanonicalFilePath(entry.getValue()))) {
				forRemoval.add(filePath);
			}
		}
		for (String filePath : forRemoval) {
			fileMap.remove(filePath);
		}
	}

	private String getFilename(String hid) {
		return asm.getHandleProvider().getFileForHandle(hid);
	}

	private String getCanonicalFilePath(IProgramElement ipe) {
		if (ipe.getSourceLocation() != null) {
			return asm.getCanonicalFilePath(ipe.getSourceLocation().getSourceFile());
		}
		return "";
	}

}
