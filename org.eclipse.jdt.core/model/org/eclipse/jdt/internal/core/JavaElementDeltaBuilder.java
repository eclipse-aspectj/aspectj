/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

/**
 * A java element delta biulder creates a java element delta on
 * a java element between the version of the java element
 * at the time the comparator was created and the current version 
 * of the java element.
 *
 * It performs this operation by locally caching the contents of 
 * the java element when it is created. When the method
 * createDeltas() is called, it creates a delta over the cached 
 * contents and the new contents.
 */
public class JavaElementDeltaBuilder {
	/**
	 * The java element handle
	 */
	IJavaElement javaElement;

	/**
	 * The maximum depth in the java element children we should look into
	 */
	int maxDepth = Integer.MAX_VALUE;

	/**
	 * The old handle to info relationships
	 */
	Map infos;

	/**
	 * The old position info
	 */
	Map oldPositions;

	/**
	 * The new position info
	 */
	Map newPositions;

	/**
	 * Change delta
	 */
	JavaElementDelta delta;

	/**
	 * List of added elements
	 */
	ArrayList added;

	/**
	 * List of removed elements
	 */
	ArrayList removed;
	
	/**
	 * Doubly linked list item
	 */
	class ListItem {
		public IJavaElement previous;
		public IJavaElement next;

		public ListItem(IJavaElement previous, IJavaElement next) {
			this.previous = previous;
			this.next = next;
		}
	}
/**
 * Creates a java element comparator on a java element
 * looking as deep as necessary.
 */
public JavaElementDeltaBuilder(IJavaElement javaElement) {
	this.javaElement = javaElement;
	this.initialize();
	this.recordElementInfo(
		javaElement, 
		(JavaModel)this.javaElement.getJavaModel(),
		0);
}
/**
 * Creates a java element comparator on a java element
 * looking only 'maxDepth' levels deep.
 */
public JavaElementDeltaBuilder(IJavaElement javaElement, int maxDepth) {
	this.javaElement = javaElement;
	this.maxDepth = maxDepth;
	this.initialize();
	this.recordElementInfo(
		javaElement, 
		(JavaModel)this.javaElement.getJavaModel(),
		0);
}
/**
 * Repairs the positioning information
 * after an element has been added
 */
private void added(IJavaElement element) {
	this.added.add(element);
	ListItem current = this.getNewPosition(element);
	ListItem previous = null, next = null;
	if (current.previous != null)
		previous = this.getNewPosition(current.previous);
	if (current.next != null)
		next = this.getNewPosition(current.next);
	if (previous != null)
		previous.next = current.next;
	if (next != null)
		next.previous = current.previous;
}
/**
 * Builds the java element deltas between the old content of the compilation
 * unit and its new content.
 */
public void buildDeltas() {
	this.recordNewPositions(this.javaElement, 0);
	this.findAdditions(this.javaElement, 0);
	this.findDeletions();
	this.findChangesInPositioning(this.javaElement, 0);
	this.trimDelta(this.delta);
}
/**
 * Finds elements which have been added or changed.
 */
private void findAdditions(IJavaElement newElement, int depth) {
	JavaElementInfo oldInfo = this.getElementInfo(newElement);
	if (oldInfo == null && depth < this.maxDepth) {
		this.delta.added(newElement);
		added(newElement);
	} else {
		this.removeElementInfo(newElement);
	}
	
	if (depth >= this.maxDepth) {
		// mark element as changed
		this.delta.changed(newElement, IJavaElementDelta.F_CONTENT);
		return;
	}

	JavaElementInfo newInfo = null;
	try { 
		newInfo = ((JavaElement)newElement).getElementInfo();
	} catch (JavaModelException npe) {
		return;
	}
	
	this.findContentChange(oldInfo, newInfo, newElement);
		
	if (oldInfo != null && newElement instanceof IParent) {

		IJavaElement[] children = newInfo.getChildren();
		if (children != null) {
			int length = children.length;
			for(int i = 0; i < length; i++) {
				this.findAdditions(children[i], depth + 1);
			}
		}		
	}
}
/**
 * Looks for changed positioning of elements.
 */
private void findChangesInPositioning(IJavaElement element, int depth) {
	if (depth >= this.maxDepth || this.added.contains(element) || this.removed.contains(element))
		return;
		
	if (!isPositionedCorrectly(element)) {
		this.delta.removed(element);
		this.delta.added(element);
	} 
	
	if (element instanceof IParent) {
		JavaElementInfo info = null;
		try { 
			info = ((JavaElement)element).getElementInfo();
		} catch (JavaModelException npe) {
			return;
		}

		IJavaElement[] children = info.getChildren();
		if (children != null) {
			int length = children.length;
			for(int i = 0; i < length; i++) {
				this.findChangesInPositioning(children[i], depth + 1);
			}
		}		
	}
}
/**
 * The elements are equivalent, but might have content changes.
 */
private void findContentChange(JavaElementInfo oldInfo, JavaElementInfo newInfo, IJavaElement newElement) {
	if (oldInfo instanceof MemberElementInfo && newInfo instanceof MemberElementInfo) {
		if (((MemberElementInfo)oldInfo).getModifiers() != ((MemberElementInfo)newInfo).getModifiers()) {
			this.delta.changed(newElement, IJavaElementDelta.F_MODIFIERS);
		} else if (oldInfo instanceof SourceMethodElementInfo && newInfo instanceof SourceMethodElementInfo) {
			if (!CharOperation.equals(
					((SourceMethodElementInfo)oldInfo).getReturnTypeName(), 
					((SourceMethodElementInfo)newInfo).getReturnTypeName())) {
				this.delta.changed(newElement, IJavaElementDelta.F_CONTENT);
			}
		} else if (oldInfo instanceof SourceFieldElementInfo && newInfo instanceof SourceFieldElementInfo) {
			if (!CharOperation.equals(
					((SourceFieldElementInfo)oldInfo).getTypeName(), 
					((SourceFieldElementInfo)newInfo).getTypeName())) {
				this.delta.changed(newElement, IJavaElementDelta.F_CONTENT);
			}
		}
	}
	if (oldInfo instanceof SourceTypeElementInfo && newInfo instanceof SourceTypeElementInfo) {
		SourceTypeElementInfo oldSourceTypeInfo = (SourceTypeElementInfo)oldInfo;
		SourceTypeElementInfo newSourceTypeInfo = (SourceTypeElementInfo)newInfo;
		if (!CharOperation.equals(oldSourceTypeInfo.getSuperclassName(), newSourceTypeInfo.getSuperclassName()) 
			|| !CharOperation.equals(oldSourceTypeInfo.getInterfaceNames(), newSourceTypeInfo.getInterfaceNames())) {
			this.delta.changed(newElement, IJavaElementDelta.F_SUPER_TYPES);
		}
	}
}
/**
 * Adds removed deltas for any handles left in the table
 */
private void findDeletions() {
	Iterator iter = this.infos.keySet().iterator();
	while(iter.hasNext()) {
		IJavaElement element = (IJavaElement)iter.next();
		this.delta.removed(element);
		this.removed(element);
	}
}
private JavaElementInfo getElementInfo(IJavaElement element) {
	return (JavaElementInfo)this.infos.get(element);
}
private ListItem getNewPosition(IJavaElement element) {
	return (ListItem)this.newPositions.get(element);
}
private ListItem getOldPosition(IJavaElement element) {
	return (ListItem)this.oldPositions.get(element);
}
private void initialize() {
	this.infos = new HashMap(20);
	this.oldPositions = new HashMap(20);
	this.newPositions = new HashMap(20);
	this.putOldPosition(this.javaElement, new ListItem(null, null));
	this.putNewPosition(this.javaElement, new ListItem(null, null));
	this.delta = new JavaElementDelta(javaElement);
	
	// if building a delta on a compilation unit or below, 
	// it's a fine grained delta
	if (javaElement.getElementType() >= IJavaElement.COMPILATION_UNIT) {
		this.delta.fineGrained();
	}
	
	this.added = new ArrayList(5);
	this.removed = new ArrayList(5);
}
/**
 * Inserts position information for the elements into the new or old positions table
 */
private void insertPositions(IJavaElement[] elements, boolean isNew) {
	int length = elements.length;
	IJavaElement previous = null, current = null, next = (length > 0) ? elements[0] : null;
	for(int i = 0; i < length; i++) {
		previous = current;
		current = next;
		next = (i + 1 < length) ? elements[i + 1] : null;
		if (isNew) {
			this.putNewPosition(current, new ListItem(previous, next));
		} else {
			this.putOldPosition(current, new ListItem(previous, next));
		}
	}
}
/**
 * Returns true if the given elements represent the an equivalent declaration.
 *
 * <p>NOTE: Since this comparison can be done with handle info only,
 * none of the internal calls need to use the locally cached contents
 * of the old compilation unit.
 */
private boolean isIdentical(JavaElement e1, JavaElement e2) {
	if (e1 == null ^ e2 == null)
		return false;
	if (e1 == null)
		return true;
		
	if (e1.fLEType == e2.fLEType) {
		if (e1.getOccurrenceCount() != e2.getOccurrenceCount())
			return false;
		switch (e1.fLEType) {
			case IJavaElement.FIELD:
			case IJavaElement.IMPORT_DECLARATION:
			case IJavaElement.PACKAGE_DECLARATION:
			case IJavaElement.COMPILATION_UNIT:
				return e1.getElementName().equals(e2.getElementName());
			case IJavaElement.TYPE:
				IType t1= (IType)e1;
				IType t2= (IType)e2;
				try {
					return (!(t1.isClass() ^ t2.isClass()) && t1.getElementName().equals(t2.getElementName()));
				} catch (JavaModelException e) {
					return false;
				}
			case IJavaElement.METHOD:
				IMethod m1= (IMethod)e1;
				IMethod m2= (IMethod)e2;
				try {
					return m1.getElementName().equals(m2.getElementName()) && m1.getSignature().equals(m2.getSignature());
				} catch (JavaModelException e) {
					return false;
				}
			case IJavaElement.INITIALIZER:
			case IJavaElement.IMPORT_CONTAINER:
				return true;
			default:
				return false;
		}
	} else {
		return false;
	}
}
/**
 * Answers true if the elements position has not changed.
 * Takes into account additions so that elements following
 * new elements will not appear out of place.
 */
private boolean isPositionedCorrectly(IJavaElement element) {
	ListItem oldListItem = this.getOldPosition(element);
	if (oldListItem == null)
		return false;
	IJavaElement oldPrevious = oldListItem.previous;
	ListItem newListItem = this.getNewPosition(element);
	if (newListItem == null)
		return false;
	IJavaElement newPrevious = newListItem.previous; 
	if (oldPrevious == newPrevious)
		return true;
	IJavaElement lastNewPrevious = null;
	while(lastNewPrevious != newPrevious) {
		if (isIdentical((JavaElement)oldPrevious, (JavaElement)newPrevious))
			return true;
		lastNewPrevious = newPrevious;
		// if newPrevious is null at this time we should exit the loop.
		if (newPrevious == null) break;
		newPrevious = (this.getNewPosition(newPrevious)).previous;
	}
	return false;
}
private void putElementInfo(IJavaElement element, JavaElementInfo info) {
	this.infos.put(element, info);
}
private void putNewPosition(IJavaElement element, ListItem position) {
	this.newPositions.put(element, position);
}
private void putOldPosition(IJavaElement element, ListItem position) {
	this.oldPositions.put(element, position);
}
/**
 * Records this elements info, and attempts
 * to record the info for the children.
 */
private void recordElementInfo(IJavaElement element, JavaModel model, int depth) {
	if (depth >= this.maxDepth) {
		return;
	}
	JavaElementInfo info = (JavaElementInfo)JavaModelManager.getJavaModelManager().getInfo(element);
	if (info == null) // no longer in the java model.
		return;
	this.putElementInfo(element, info);
		
	if (element instanceof IParent) {
		IJavaElement[] children = info.getChildren();
		if (children != null) {
			insertPositions(children, false);
			for(int i = 0, length = children.length; i < length; i++)
				recordElementInfo(children[i], model, depth + 1);
		}
	}
}
/**
 * Fills the newPositions hashtable with the new position information
 */
private void recordNewPositions(IJavaElement newElement, int depth) {
	if (depth < this.maxDepth && newElement instanceof IParent) {
		JavaElementInfo info = null;
		try { 
			info = ((JavaElement)newElement).getElementInfo();
		} catch (JavaModelException npe) {
			return;
		}

		IJavaElement[] children = info.getChildren();
		if (children != null) {
			insertPositions(children, true);
			for(int i = 0, length = children.length; i < length; i++) {
				recordNewPositions(children[i], depth + 1);
			}
		}
	}
}
/**
 * Repairs the positioning information
 * after an element has been removed
 */
private void removed(IJavaElement element) {
	this.removed.add(element);
	ListItem current = this.getOldPosition(element);
	ListItem previous = null, next = null;
	if (current.previous != null)
		previous = this.getOldPosition(current.previous);
	if (current.next != null)
		next = this.getOldPosition(current.next);
	if (previous != null)
		previous.next = current.next;
	if (next != null)
		next.previous = current.previous;
	
}
private void removeElementInfo(IJavaElement element) {
	this.infos.remove(element);
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("Built delta:\n"); //$NON-NLS-1$
	buffer.append(this.delta.toString());
	return buffer.toString();
}
/**
 * Trims deletion deltas to only report the highest level of deletion
 */
private void trimDelta(JavaElementDelta delta) {
	if (delta.getKind() == IJavaElementDelta.REMOVED) {
		IJavaElementDelta[] children = delta.getAffectedChildren();
		for(int i = 0, length = children.length; i < length; i++) {
			delta.removeAffectedChild((JavaElementDelta)children[i]);
		}
	} else {
		IJavaElementDelta[] children = delta.getAffectedChildren();
		for(int i = 0, length = children.length; i < length; i++) {
			trimDelta((JavaElementDelta)children[i]);
		}
	}
}
}
