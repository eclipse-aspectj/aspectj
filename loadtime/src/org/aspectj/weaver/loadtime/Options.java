/*******************************************************************************
 * Copyright (c) 2005 Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 *   Alexandre Vasseur         initial implementation
 *******************************************************************************/
package org.aspectj.weaver.loadtime;

import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.Message;
import org.aspectj.util.LangUtil;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * A class that hanldes LTW options.
 * Note: AV - I choosed to not reuse AjCompilerOptions and alike since those implies too many dependancies on
 * jdt and ajdt modules.
 *
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
 */
public class Options {

    private static class DefaultMessageHandler implements IMessageHandler {

        boolean isVerbose = false;
        boolean showWeaveInfo = false;
        boolean showWarn = true;

        public boolean handleMessage(IMessage message) throws AbortException {
            return SYSTEM_OUT.handleMessage(message);
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

    private final static String OPTION_15 = "-1.5";
    private final static String OPTION_lazyTjp = "-XlazyTjp";
    private final static String OPTION_noWarn = "-nowarn";
    private final static String OPTION_noWarnNone = "-warn:none";
    private final static String OPTION_proceedOnError = "-proceedOnError";
    private final static String OPTION_verbose = "-verbose";
    private final static String OPTION_reweavable = "-Xreweavable";
    private final static String OPTION_noinline = "-Xnoinline";
    private final static String OPTION_showWeaveInfo = "-showWeaveInfo";
    private final static String OPTIONVALUED_messageHolder = "-XmessageHolderClass:";//TODO rename to Handler

    //FIXME dump option - dump what - dump before/after ?

    public static WeaverOption parse(String options, ClassLoader laoder) {
        // the first option wins
        List flags = LangUtil.anySplit(options, " ");
        Collections.reverse(flags);

        WeaverOption weaverOption = new WeaverOption();
        weaverOption.messageHandler = new DefaultMessageHandler();//default


        // do a first round on the message handler since it will report the options themselves
        for (Iterator iterator = flags.iterator(); iterator.hasNext();) {
            String arg = (String) iterator.next();
            if (arg.startsWith(OPTIONVALUED_messageHolder)) {
                if (arg.length() > OPTIONVALUED_messageHolder.length()) {
                    String handlerClass = arg.substring(OPTIONVALUED_messageHolder.length()).trim();
                    try {
                        Class handler = Class.forName(handlerClass, false, laoder);
                        weaverOption.messageHandler = ((IMessageHandler) handler.newInstance());
                    } catch (Throwable t) {
                        weaverOption.messageHandler.handleMessage(
                                new Message(
                                        "Cannot instantiate message handler " + handlerClass,
                                        IMessage.ERROR,
                                        t,
                                        null
                                )
                        );
                    }
                }
            }
        }

        // configure the other options
        for (Iterator iterator = flags.iterator(); iterator.hasNext();) {
            String arg = (String) iterator.next();
            if (arg.equals(OPTION_15)) {
                weaverOption.java5 = true;
            } else if (arg.equalsIgnoreCase(OPTION_lazyTjp)) {
                weaverOption.lazyTjp = true;
            } else if (arg.equalsIgnoreCase(OPTION_noinline)) {
                weaverOption.noInline = true;
            } else if (arg.equalsIgnoreCase(OPTION_noWarn) || arg.equalsIgnoreCase(OPTION_noWarnNone)) {
                weaverOption.noWarn = true;
            } else if (arg.equalsIgnoreCase(OPTION_proceedOnError)) {
                weaverOption.proceedOnError = true;
            } else if (arg.equalsIgnoreCase(OPTION_reweavable)) {
                weaverOption.reWeavable = true;
            } else if (arg.equalsIgnoreCase(OPTION_showWeaveInfo)) {
                weaverOption.showWeaveInfo = true;
            } else if (arg.equalsIgnoreCase(OPTION_verbose)) {
                weaverOption.verbose = true;
            } else {
                weaverOption.messageHandler.handleMessage(
                        new Message(
                                "Cannot configure weaver with option " + arg + ": unknown option",
                                IMessage.WARNING,
                                null,
                                null
                        )
                );
            }
        }

        // refine message handler configuration
        if (weaverOption.noWarn) {
            weaverOption.messageHandler.dontIgnore(IMessage.WARNING);
        }
        if (weaverOption.verbose) {
            weaverOption.messageHandler.dontIgnore(IMessage.DEBUG);
        }
        if (weaverOption.showWeaveInfo) {
            weaverOption.messageHandler.dontIgnore(IMessage.WEAVEINFO);
        }

        return weaverOption;
    }

    public static class WeaverOption {
        boolean java5;
        boolean lazyTjp;
        boolean noWarn;
        boolean proceedOnError;
        boolean verbose;
        boolean reWeavable;
        boolean noInline;
        boolean showWeaveInfo;
        IMessageHandler messageHandler;
    }
}
