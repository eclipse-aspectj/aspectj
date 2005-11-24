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
package ataspectj.ltwlog;

import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.AbortException;
import org.aspectj.weaver.loadtime.DefaultMessageHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class MessageHolder extends DefaultMessageHandler {

    static List s_messages = new ArrayList();

    public MessageHolder() {
    	System.out.println("MessageHolder.MessageHolder()");
    }

    public boolean handleMessage(IMessage message) throws AbortException {
        if (isIgnoring(message.getKind())) {
            return false;
        } else {
            s_messages.add(message.toString());
            return true;
        }
    }

    public static boolean startsAs(List messages) {
        if (s_messages.size() < messages.size())
            return false;

        int i = 0;
        for (Iterator iterator = messages.iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            if (!((String)s_messages.get(i)).startsWith(s)) {
                return false;
            }
            i++;
        }
        return true;
    }

    public static int size() {
        return s_messages.size();
    }

    public static void dump() {
        for (Iterator iterator = s_messages.iterator(); iterator.hasNext();) {
            String s = (String) iterator.next();
            System.err.println("Holds:" + s);
        }
    }
}
