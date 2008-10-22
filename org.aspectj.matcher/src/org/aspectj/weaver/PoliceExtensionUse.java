/*******************************************************************************
 * Copyright (c) 2006 IBM 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andy Clement - initial API and implementation
 *******************************************************************************/
package org.aspectj.weaver;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.weaver.patterns.AbstractPatternNodeVisitor;
import org.aspectj.weaver.patterns.AndPointcut;
import org.aspectj.weaver.patterns.KindedPointcut;
import org.aspectj.weaver.patterns.NotPointcut;
import org.aspectj.weaver.patterns.OrPointcut;
import org.aspectj.weaver.patterns.Pointcut;

/**
 * Walks a pointcut and determines if the synchronization related designators have been used: lock() or unlock()
 */
public class PoliceExtensionUse extends AbstractPatternNodeVisitor {

	private boolean synchronizationDesignatorEncountered;
	private World world;
	private Pointcut p;

	public PoliceExtensionUse(World w, Pointcut p) {
		this.world = w;
		this.p = p;
		this.synchronizationDesignatorEncountered = false;
	}

	public boolean synchronizationDesignatorEncountered() {
		return synchronizationDesignatorEncountered;
	}

	public Object visit(KindedPointcut node, Object data) {
		if (world == null)
			return super.visit(node, data); // error scenario can sometimes lead to this LazyClassGen.toLongString()
		if (node.getKind() == Shadow.SynchronizationLock || node.getKind() == Shadow.SynchronizationUnlock)
			synchronizationDesignatorEncountered = true;
		// Check it!
		if (!world.isJoinpointSynchronizationEnabled()) {
			if (node.getKind() == Shadow.SynchronizationLock) {
				IMessage m = MessageUtil.warn(
						"lock() pointcut designator cannot be used without the option -Xjoinpoints:synchronization", p
								.getSourceLocation());
				world.getMessageHandler().handleMessage(m);
			} else if (node.getKind() == Shadow.SynchronizationUnlock) {
				IMessage m = MessageUtil.warn(
						"unlock() pointcut designator cannot be used without the option -Xjoinpoints:synchronization", p
								.getSourceLocation());
				world.getMessageHandler().handleMessage(m);
			}
		}
		return super.visit(node, data);
	}

	public Object visit(AndPointcut node, Object data) {
		node.getLeft().accept(this, data);
		node.getRight().accept(this, data);
		return node;
	}

	public Object visit(NotPointcut node, Object data) {
		node.getNegatedPointcut().accept(this, data);
		return node;
	}

	public Object visit(OrPointcut node, Object data) {
		node.getLeft().accept(this, data);
		node.getRight().accept(this, data);
		return node;
	}

}