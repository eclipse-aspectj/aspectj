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

import java.io.*;
import java.util.*;

import org.aspectj.asm.*;
import org.aspectj.bridge.*;

/**
 * @author Mik Kersten
 */
public class AspectJElementHierarchy implements IHierarchy {
	
	private static final long serialVersionUID = 6462734311117048620L;
	
	protected  IProgramElement root = null;
    protected String configFile = null;

    private Map fileMap = null;
    private Map handleMap = null;
    private Map typeMap = null;
    
	public IProgramElement getElement(String handle) {
		return findElementForHandleOrCreate(handle, false);
	}

    public IProgramElement getRoot() {
        return root;
    }

    public void setRoot(IProgramElement root) {
        this.root = root;
        handleMap = new HashMap();
        typeMap = new HashMap();
    }

	public void addToFileMap( Object key, Object value ){
		fileMap.put( key, value );
	}
	
	public boolean removeFromFileMap(Object key) {
		if (fileMap.containsKey(key)) {
			return (fileMap.remove(key)!=null);
		}
		return true;	
	}

	public void setFileMap(HashMap fileMap) {
		  this.fileMap = fileMap;
	  }

	public Object findInFileMap( Object key ) {
		return fileMap.get(key);
	}


	public Set getFileMapEntrySet() {
		return fileMap.entrySet();
	}
	
	public boolean isValid() {
        return root != null && fileMap != null;
    }
 
	/** 
	 * Returns the first match
	 * 
	 * @param parent
	 * @param kind		not null
	 * @return null if not found
	 */
	public IProgramElement findElementForSignature(IProgramElement parent, IProgramElement.Kind kind, String signature) {
		for (Iterator it = parent.getChildren().iterator(); it.hasNext(); ) {
			IProgramElement node = (IProgramElement)it.next();
			if (node.getKind() == kind && signature.equals(node.toSignatureString())) {
				return node;
			} else {
				IProgramElement childSearch = findElementForSignature(node, kind, signature);
				if (childSearch != null) return childSearch;
			}
		}
		return null;
	}
	
	public IProgramElement findElementForLabel(
		IProgramElement parent,
		IProgramElement.Kind kind,
		String label) {
			
		for (Iterator it = parent.getChildren().iterator(); it.hasNext(); ) {
			IProgramElement node = (IProgramElement)it.next();
			if (node.getKind() == kind && label.equals(node.toLabelString())) {
				return node;
			} else {
				IProgramElement childSearch = findElementForLabel(node, kind, label);
				if (childSearch != null) return childSearch;
			}
		}
		return null;			
	}
	
	/**
	 * @param packageName	if null default package is searched
	 * @param className 	can't be null
	 */ 
	public IProgramElement findElementForType(String packageName, String typeName) {
		StringBuffer keyb = (packageName == null) ? new StringBuffer() : 
		                                            new StringBuffer(packageName);
		keyb.append(".");
		keyb.append(typeName);
		String key = keyb.toString();
		IProgramElement ret = (IProgramElement) typeMap.get(key);
		if (ret == null) {
			IProgramElement packageNode = null;
			if (packageName == null) {
				packageNode = root;
			} else {
				if (root == null) return null;
				List kids = root.getChildren();
				if (kids == null) return null;
				for (Iterator it = kids.iterator(); it.hasNext(); ) {
					IProgramElement node = (IProgramElement)it.next();
					if (packageName.equals(node.getName())) {
						packageNode = node;
					} 
				}
				if (packageNode == null) return null;
			}
			
			// this searches each file for a class
			for (Iterator it = packageNode.getChildren().iterator(); it.hasNext(); ) {
				IProgramElement fileNode = (IProgramElement)it.next();
				IProgramElement cNode = findClassInNodes(fileNode.getChildren(), typeName, typeName);
				if (cNode != null) {
					ret = cNode;
					typeMap.put(key,ret);
				} 
			}			
		}
		return ret;
	}
	
	private IProgramElement findClassInNodes(Collection nodes, String name, String typeName) {
		String baseName;
		String innerName;
		int dollar = name.indexOf('$');
		if (dollar == -1) {
			baseName = name;
			innerName = null;
		} else {
			baseName = name.substring(0, dollar);
			innerName = name.substring(dollar+1);
		}
		
		for (Iterator j = nodes.iterator(); j.hasNext(); ) {
			IProgramElement classNode = (IProgramElement)j.next();
			if (baseName.equals(classNode.getName())) {
				if (innerName == null) return classNode;
				else return findClassInNodes(classNode.getChildren(), innerName, typeName);
			} else if (name.equals(classNode.getName())) {
				return classNode;
			} else if (typeName.equals(classNode.getBytecodeSignature())) { 
				return classNode;
			} else if (classNode.getChildren() != null && !classNode.getChildren().isEmpty()){
				IProgramElement node = findClassInNodes(classNode.getChildren(),name, typeName);
				if (node != null) {
					return node;
				}
			}
		}
		return null;
	}


	/**
	 * @param		sourceFilePath	modified to '/' delimited path for consistency
	 * @return		a new structure node for the file if it was not found in the model
	 */
	public IProgramElement findElementForSourceFile(String sourceFile) {
       	try {
	       	if (!isValid() || sourceFile == null) {   
	            return IHierarchy.NO_STRUCTURE;
	        } else {
	            String correctedPath = 
	            	AsmManager.getDefault().getCanonicalFilePath(new File(sourceFile));
	            //StructureNode node = (StructureNode)getFileMap().get(correctedPath);//findFileNode(filePath, model);
				IProgramElement node = (IProgramElement)findInFileMap(correctedPath);//findFileNode(filePath, model);
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
			return findElementForSourceLine(
				AsmManager.getDefault().getCanonicalFilePath(
					                     location.getSourceFile()), 
				location.getLine());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Never returns null 
	 * 
	 * @param		sourceFilePath	canonicalized path for consistency
	 * @param 		lineNumber		if 0 or 1 the corresponding file node will be returned
	 * @return		a new structure node for the file if it was not found in the model
	 */
	public IProgramElement findElementForSourceLine(String sourceFilePath, int lineNumber) {
		String canonicalSFP = AsmManager.getDefault().getCanonicalFilePath(
									new File(sourceFilePath));
		IProgramElement node = findNodeForSourceLineHelper(root, canonicalSFP, lineNumber, -1);
		if (node != null) {
			return node;	
		} else {			
			return createFileStructureNode(sourceFilePath);
		}
	}

	public IProgramElement findElementForOffSet(String sourceFilePath, int lineNumber, int offSet) {
		String canonicalSFP = AsmManager.getDefault().getCanonicalFilePath(
									new File(sourceFilePath));
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
		// '!' is used like in URLs  "c:/blahblah/X.jar!a/b.class"
		int i = sourceFilePath.lastIndexOf('!');
		int j = sourceFilePath.indexOf(".class");
		if (i > lastSlash && i != -1 && j != -1) {
			// we are a binary aspect in the default package
			lastSlash = i;
		}
		String fileName = sourceFilePath.substring(lastSlash+1);
		IProgramElement fileNode = new ProgramElement(fileName, IProgramElement.Kind.FILE_JAVA, new SourceLocation(new File(sourceFilePath), 1, 1),0,null,null);
		//fileNode.setSourceLocation();
		fileNode.addChild(NO_STRUCTURE);
		return fileNode;
	}


	private IProgramElement findNodeForSourceLineHelper(IProgramElement node, String sourceFilePath, int lineNumber, int offSet) {
		if (matches(node, sourceFilePath, lineNumber, offSet) 
			&& !hasMoreSpecificChild(node, sourceFilePath, lineNumber, offSet)) {
			return node;	
		} 
		
		if (node != null && node.getChildren() != null) {
			for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
				IProgramElement foundNode = findNodeForSourceLineHelper(
					(IProgramElement)it.next(), 
					sourceFilePath, 
					lineNumber,
					offSet); 		
				if (foundNode != null) return foundNode;
			}
		}
		
		return null;		
	}
	
	private boolean matches(IProgramElement node, String sourceFilePath, int lineNumber, int offSet) {
//		try {			
//			if (node != null && node.getSourceLocation() != null)
//				System.err.println("====\n1: " + 
//					sourceFilePath + "\n2: " +
//					node.getSourceLocation().getSourceFile().getCanonicalPath().equals(sourceFilePath)
//				);				
			return node != null 
				&& node.getSourceLocation() != null
				&& node.getSourceLocation().getSourceFile().getAbsolutePath().equals(sourceFilePath)
				&& ((offSet != -1 && node.getSourceLocation().getOffset() == offSet) 
						|| offSet == -1 )
				&& ((node.getSourceLocation().getLine() <= lineNumber
					&& node.getSourceLocation().getEndLine() >= lineNumber)
					||
					(lineNumber <= 1
					 && node.getKind().isSourceFile())
				);
//		} catch (IOException ioe) { 
//			return false;
//		} 
	}
	
	private boolean hasMoreSpecificChild(IProgramElement node, String sourceFilePath, int lineNumber, int offSet) {
		for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
			IProgramElement child = (IProgramElement)it.next();
			if (matches(child, sourceFilePath, lineNumber, offSet)) return true;
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
		return findElementForHandleOrCreate(handle,true);
	}
	
	// TODO: optimize this lookup
	// only want to create a file node if can't find the IPE if called through
	// findElementForHandle() to mirror behaviour before pr141730
	private IProgramElement findElementForHandleOrCreate(String handle, boolean create) {
		// try the cache first...
		IProgramElement ret = (IProgramElement) handleMap.get(handle);
		if (ret != null) return ret;
		
		ret = findElementForHandle(root,handle);
		if (ret == null && create) {
			ret = createFileStructureNode(getFilename(handle));
		}
		if (ret != null) {
			cache(handle,(ProgramElement)ret);
		}
		return ret;
	}
	
	private IProgramElement findElementForHandle(IProgramElement parent, String handle) {
		for (Iterator it = parent.getChildren().iterator(); it.hasNext(); ) {
			IProgramElement node = (IProgramElement)it.next();
			if (handle.equals(node.getHandleIdentifier())) {
				return node;
			} else {
				IProgramElement childSearch = findElementForHandle(node,handle);
				if (childSearch != null) return childSearch;
			}
		}
		return null;
	}
	
//	
//	private IProgramElement findElementForBytecodeInfo(
//		IProgramElement node, 
//		String parentName,
//		String name, 
//		String signature) {
//		for (Iterator it = node.getChildren().iterator(); it.hasNext(); ) {
//			IProgramElement curr = (IProgramElement)it.next();
//			if (parentName.equals(curr.getParent().getBytecodeName())
//				&& name.equals(curr.getBytecodeName()) 
//				&& signature.equals(curr.getBytecodeSignature())) {
//				return node;
//			} else {
//				IProgramElement childSearch = findElementForBytecodeInfo(curr, parentName, name, signature);
//				if (childSearch != null) return childSearch;
//			}
//		}
//		return null;
//	}
	
	protected void cache(String handle, IProgramElement pe) {
		if (!AsmManager.isCompletingTypeBindings()) {
			handleMap.put(handle,pe);
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

    // TODO rename this method ... it does more than just the handle map
	public void updateHandleMap(Set deletedFiles) {
		// Only delete the entries we need to from the handle map - for performance reasons
		List forRemoval = new ArrayList();
		Set k = handleMap.keySet();
		for (Iterator iter = k.iterator(); iter.hasNext();) {
			String handle = (String) iter.next();
			IProgramElement ipe = (IProgramElement)handleMap.get(handle);
			if (deletedFiles.contains(getCanonicalFilePath(ipe)))forRemoval.add(handle); 
		}
		for (Iterator iter = forRemoval.iterator(); iter.hasNext();) {
			String handle = (String) iter.next();
			handleMap.remove(handle);
		}
		forRemoval.clear();
		k = typeMap.keySet();
		for (Iterator iter = k.iterator(); iter.hasNext();) {
			String typeName = (String) iter.next();
			IProgramElement ipe = (IProgramElement)typeMap.get(typeName);
			if (deletedFiles.contains(getCanonicalFilePath(ipe))) forRemoval.add(typeName);
		}
		for (Iterator iter = forRemoval.iterator(); iter.hasNext();) {
			String typeName = (String) iter.next();
			typeMap.remove(typeName);
		}
		forRemoval.clear();
		k = fileMap.keySet();
		for (Iterator iter = k.iterator(); iter.hasNext();) {
			String filePath = (String) iter.next();
			IProgramElement ipe = (IProgramElement)fileMap.get(filePath);
			if (deletedFiles.contains(getCanonicalFilePath(ipe))) forRemoval.add(filePath);
		}
		for (Iterator iter = forRemoval.iterator(); iter.hasNext();) {
			String filePath = (String) iter.next();
			fileMap.remove(filePath);
		}
	}
	
	private String getFilename(String hid) {
		return AsmManager.getDefault().getHandleProvider().getFileForHandle(hid);
	}
	
	private String getCanonicalFilePath(IProgramElement ipe) {
		if (ipe.getSourceLocation() != null) {
			return AsmManager.getDefault().getCanonicalFilePath(ipe.getSourceLocation().getSourceFile());			
		}
		return "";
	}

}

