/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://eclipse.org/legal/epl-v10.html 
 * 
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.AbortException;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class DefaultMessageHandler implements IMessageHandler {

    boolean isVerbose = false;
    boolean showWeaveInfo = false;
    boolean showWarn = true;

    public boolean handleMessage(IMessage message) throws AbortException {
        if (isIgnoring(message.getKind())) {
    		return false;
    	} else {
            if (message.getKind().isSameOrLessThan(IMessage.INFO)) {
                return SYSTEM_OUT.handleMessage(message);
            } else {
                return SYSTEM_ERR.handleMessage(message);
            }
        }
    }

    public boolean isIgnoring(IMessage.Kind kind) {
        if (kind.equals(IMessage.WEAVEINFO)) {
            return !showWeaveInfo;
        }
        if (kind.isSameOrLessThan(IMessage.INFO)) {
            return !isVerbose;
        }
        return !showWarn;
    }

    public void dontIgnore(IMessage.Kind kind) {
        if (kind.equals(IMessage.WEAVEINFO)) {
            showWeaveInfo = true;
        } else if (kind.equals(IMessage.DEBUG)) {
            isVerbose = true;
        } else if (kind.equals(IMessage.WARNING)) {
            showWarn = false;
        }
    }

}
