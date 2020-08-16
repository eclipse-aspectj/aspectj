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

package org.aspectj.ajde.ui;

import java.util.List;

import org.aspectj.ajde.Ajde;
import org.aspectj.asm.AsmManager;
import org.aspectj.asm.IProgramElement;
import org.aspectj.asm.IRelationship;
import org.aspectj.asm.IRelationshipMap;

/**
 * @author Mik Kersten
 */
public abstract class StructureViewNodeFactory {

	private final AbstractIconRegistry iconRegistry;

	public StructureViewNodeFactory(AbstractIconRegistry iconRegistry) {
		this.iconRegistry = iconRegistry;
	}

	public IStructureViewNode createNode(IProgramElement node) {
		return createNode(node, null);
	}

	public IStructureViewNode createNode(IProgramElement node, List children) {
		AbstractIcon icon = iconRegistry.getStructureIcon(node.getKind(), node.getAccessibility());

		IStructureViewNode svNode = createDeclaration(node, icon, children);
		String nodeHandle = node.getHandleIdentifier();
		// Don't put relationships on fields as they can then appear twice when building the outline -
		// once under clinit field-set nodes and once under the field declaration.
		if (nodeHandle != null && !node.getKind().equals(IProgramElement.Kind.FIELD)) {
			AsmManager manager = Ajde.getDefault().getModel();
			IRelationshipMap relMap = (manager == null ? null : manager.getRelationshipMap());
			List relationships = (relMap == null ? null : relMap.get(nodeHandle));
			if (relationships != null) {
				for (Object relationship : relationships) {
					IRelationship rel = (IRelationship) relationship;
					if (rel != null && rel.getTargets().size() > 0) {
						IStructureViewNode relNode = createRelationship(rel, iconRegistry.getIcon(rel.getKind()));
						if (relNode != null) {
							svNode.add(relNode, 0);
							for (String handle : rel.getTargets()) {
								IProgramElement link = Ajde.getDefault().getModel().getHierarchy().findElementForHandle(handle);
								if (link != null) {
									IStructureViewNode linkNode = createLink(link, iconRegistry.getStructureIcon(link.getKind(),
											link.getAccessibility()));
									relNode.add(linkNode);
								}
							}
						}
					}
				}
			}
		}
		return svNode;
	}

	/**
	 * Implementors must override this method in order to create link new nodes.
	 */
	protected abstract IStructureViewNode createLink(IProgramElement node, AbstractIcon icon);

	/**
	 * Implementors must override this method in order to create new relationship nodes.
	 * 
	 * If returned node is null it will not be added to the tree.
	 */
	protected abstract IStructureViewNode createRelationship(IRelationship relationship, AbstractIcon icon);

	/**
	 * Implementors must override this method in order to create new nodes.
	 */
	protected abstract IStructureViewNode createDeclaration(IProgramElement node, AbstractIcon icon, List children);

	/**
	 * Don't show code elements under types since they show under the corresponding initializers.
	 */
	public static boolean acceptNode(IProgramElement parent, IProgramElement child) {
		if (parent.getKind() == IProgramElement.Kind.CLASS && child.getKind() == IProgramElement.Kind.CODE) {
			return false;
		} else {
			return true;
		}
	}
}
