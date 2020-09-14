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

import java.util.Collections;
import java.util.List;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.Message;
import org.aspectj.util.LangUtil;

/**
 * A class that hanldes LTW options. Note: AV - I choosed to not reuse AjCompilerOptions and alike since those implies too many
 * dependancies on jdt and ajdt modules.
 *
 * @author Alexandre Vasseur (alex AT gnilux DOT com)
 */
public class Options {

	private final static String OPTION_15 = "-1.5";
	private final static String OPTION_lazyTjp = "-XlazyTjp";
	private final static String OPTION_noWarn = "-nowarn";
	private final static String OPTION_noWarnNone = "-warn:none";
	private final static String OPTION_proceedOnError = "-proceedOnError";
	private final static String OPTION_verbose = "-verbose";
	private final static String OPTION_debug = "-debug";
	private final static String OPTION_reweavable = "-Xreweavable";// notReweavable is default for LTW
	private final static String OPTION_noinline = "-Xnoinline";
	private final static String OPTION_addSerialVersionUID = "-XaddSerialVersionUID";
	private final static String OPTION_hasMember = "-XhasMember";
	private final static String OPTION_pinpoint = "-Xdev:pinpoint";
	private final static String OPTION_showWeaveInfo = "-showWeaveInfo";
	private final static String OPTIONVALUED_messageHandler = "-XmessageHandlerClass:";
	private static final String OPTIONVALUED_Xlintfile = "-Xlintfile:";
	private static final String OPTIONVALUED_Xlint = "-Xlint:";
	private static final String OPTIONVALUED_joinpoints = "-Xjoinpoints:";
	private static final String OPTIONVALUED_Xset = "-Xset:";
	private static final String OPTION_timers = "-timers";
	private static final String OPTIONVALUED_loadersToSkip = "-loadersToSkip:";

	public static WeaverOption parse(String options, ClassLoader laoder, IMessageHandler imh) {
		WeaverOption weaverOption = new WeaverOption(imh);

		if (LangUtil.isEmpty(options)) {
			return weaverOption;
		}
		// the first option wins
		List<String> flags = LangUtil.anySplit(options, " ");
		Collections.reverse(flags);

		// do a first round on the message handler since it will report the options themselves
		for (String arg : flags) {
			if (arg.startsWith(OPTIONVALUED_messageHandler)) {
				if (arg.length() > OPTIONVALUED_messageHandler.length()) {
					String handlerClass = arg.substring(OPTIONVALUED_messageHandler.length()).trim();
					try {
						Class<?> handler = Class.forName(handlerClass, false, laoder);
						weaverOption.messageHandler = ((IMessageHandler) handler.newInstance());
					} catch (Throwable t) {
						weaverOption.messageHandler.handleMessage(new Message("Cannot instantiate message handler " + handlerClass,
								IMessage.ERROR, t, null));
					}
				}
			}
		}

		// configure the other options
		for (String arg : flags) {
			if (arg.equals(OPTION_15)) {
				weaverOption.java5 = true;
			} else if (arg.equalsIgnoreCase(OPTION_lazyTjp)) {
				weaverOption.lazyTjp = true;
			} else if (arg.equalsIgnoreCase(OPTION_noinline)) {
				weaverOption.noInline = true;
			} else if (arg.equalsIgnoreCase(OPTION_addSerialVersionUID)) {
				weaverOption.addSerialVersionUID = true;
			} else if (arg.equalsIgnoreCase(OPTION_noWarn) || arg.equalsIgnoreCase(OPTION_noWarnNone)) {
				weaverOption.noWarn = true;
			} else if (arg.equalsIgnoreCase(OPTION_proceedOnError)) {
				weaverOption.proceedOnError = true;
			} else if (arg.equalsIgnoreCase(OPTION_reweavable)) {
				weaverOption.notReWeavable = false;
			} else if (arg.equalsIgnoreCase(OPTION_showWeaveInfo)) {
				weaverOption.showWeaveInfo = true;
			} else if (arg.equalsIgnoreCase(OPTION_hasMember)) {
				weaverOption.hasMember = true;
			} else if (arg.startsWith(OPTIONVALUED_joinpoints)) {
				if (arg.length() > OPTIONVALUED_joinpoints.length()) {
					weaverOption.optionalJoinpoints = arg.substring(OPTIONVALUED_joinpoints.length()).trim();
				}
			} else if (arg.equalsIgnoreCase(OPTION_verbose)) {
				weaverOption.verbose = true;
			} else if (arg.equalsIgnoreCase(OPTION_debug)) {
				weaverOption.debug = true;
			} else if (arg.equalsIgnoreCase(OPTION_pinpoint)) {
				weaverOption.pinpoint = true;
			} else if (arg.startsWith(OPTIONVALUED_messageHandler)) {
				// handled in first round
			} else if (arg.startsWith(OPTIONVALUED_Xlintfile)) {
				if (arg.length() > OPTIONVALUED_Xlintfile.length()) {
					weaverOption.lintFile = arg.substring(OPTIONVALUED_Xlintfile.length()).trim();
				}
			} else if (arg.startsWith(OPTIONVALUED_Xlint)) {
				if (arg.length() > OPTIONVALUED_Xlint.length()) {
					weaverOption.lint = arg.substring(OPTIONVALUED_Xlint.length()).trim();
				}
			} else if (arg.startsWith(OPTIONVALUED_Xset)) {
				if (arg.length() > OPTIONVALUED_Xlint.length()) {
					weaverOption.xSet = arg.substring(OPTIONVALUED_Xset.length()).trim();
				}
			} else if (arg.equalsIgnoreCase(OPTION_timers)) {
				weaverOption.timers = true;
			} else if (arg.startsWith(OPTIONVALUED_loadersToSkip)) {
				if (arg.length() > OPTIONVALUED_loadersToSkip.length()) {
					String value = arg.substring(OPTIONVALUED_loadersToSkip.length()).trim();
					weaverOption.loadersToSkip = value;
				}
			} else {
				weaverOption.messageHandler.handleMessage(new Message("Cannot configure weaver with option '" + arg
						+ "': unknown option", IMessage.WARNING, null, null));
			}
		}

		// refine message handler configuration
		if (weaverOption.noWarn) {
			weaverOption.messageHandler.ignore(IMessage.WARNING);
		}
		if (weaverOption.verbose) {
			weaverOption.messageHandler.dontIgnore(IMessage.INFO);
		}
		if (weaverOption.debug) {
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
		boolean hasMember;
		boolean timers = false;
		String optionalJoinpoints;
		boolean noWarn;
		boolean proceedOnError;
		boolean verbose;
		boolean debug;
		boolean notReWeavable = true;// default to notReweavable for LTW (faster)
		boolean noInline;
		boolean addSerialVersionUID;
		boolean showWeaveInfo;
		boolean pinpoint;
		IMessageHandler messageHandler;
		String lint;
		String lintFile;
		String xSet;
		String loadersToSkip;

		public WeaverOption(IMessageHandler imh) {
			// messageHandler = new DefaultMessageHandler();//default
			this.messageHandler = imh;
		}
	}
}
