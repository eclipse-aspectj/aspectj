/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Common Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 *  
 * Contributors: 
 *     PARC     initial implementation 
 * ******************************************************************/


package org.aspectj.weaver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageUtil;

public class Lint {
	private Map kinds = new HashMap();
	private World world;

	public final Kind invalidAbsoluteTypeName =
		new Kind("invalidAbsoluteTypeName", "no match for this type name: {0}");

	public final Kind invalidWildcardTypeName = 
		new Kind("invalidWildcardTypeName", "no match for this type pattern: {0}");
	
	public final Kind unresolvableMember = 
		new Kind("unresolvableMember", "can not resolve this member: {0}");
	
	public final Kind typeNotExposedToWeaver = 
		new Kind("typeNotExposedToWeaver", "this affected type is not exposed to the weaver: {0}");
		
	public final Kind shadowNotInStructure = 
		new Kind("shadowNotInStructure", "the shadow for this join point is not exposed in the structure model: {0}");
		
	public Lint(World world) {
		this.world = world;
	}
	
	
	public void setAll(String messageKind) {
		setAll(getMessageKind(messageKind));
	}
	
	private void setAll(IMessage.Kind messageKind) {
		for (Iterator i = kinds.values().iterator(); i.hasNext(); ) {
			Kind kind = (Kind)i.next();
			kind.setKind(messageKind);
		}
	}
	
	public void setFromProperties(File file) {
		try {
			InputStream s = new FileInputStream(file);
			setFromProperties(s);
		} catch (IOException ioe) {
			MessageUtil.error(world.getMessageHandler(), "problem loading Xlint properties file: " + 
					file.getPath() + ", " + ioe.getMessage());
		}
	}

	public void loadDefaultProperties() {
		InputStream s = getClass().getResourceAsStream("XlintDefault.properties");
		if (s == null) {
			MessageUtil.warn(world.getMessageHandler(), "couldn't load XlintDefault.properties");
			return;
		}
		try {
			setFromProperties(s);
		} catch (IOException ioe) {
			MessageUtil.error(world.getMessageHandler(), "problem loading XlintDefault.properties, " +
					ioe.getMessage());
		}

	}


	private void setFromProperties(InputStream s) throws IOException {
		Properties p = new Properties();
		p.load(s);
		setFromProperties(p);
	}
	
	
	public void setFromProperties(Properties properties) {
		for (Iterator i = properties.entrySet().iterator(); i.hasNext(); ) {
			Map.Entry entry = (Map.Entry)i.next();
			Kind kind = (Kind)kinds.get(entry.getKey());
			if (kind == null) {
				MessageUtil.error(world.getMessageHandler(), "invalid Xlint key: " + entry.getKey());
			} else {
				kind.setKind(getMessageKind((String)entry.getValue()));
			}
		}
	}

	private IMessage.Kind getMessageKind(String v) {
		if (v.equals("ignore")) return null;
		else if (v.equals("warning")) return IMessage.WARNING;
		else if (v.equals("error")) return IMessage.ERROR;
		
		MessageUtil.error(world.getMessageHandler(), 
			"invalid Xlint message kind (must be one of ignore, warning, error): " + v);
		return null;
	}
	
	
	
	public class Kind {
		private String name;
		private String message;
		private IMessage.Kind kind = IMessage.WARNING;
		public Kind(String name, String message) {
			this.name = name;
			this.message = message;
			kinds.put(this.name, this);
		}
		
		public boolean isEnabled() {
			return kind != null;
		}
		
		public IMessage.Kind getKind() {
			return kind;
		}

		public void setKind(IMessage.Kind kind) {
			this.kind = kind;
		}
		
		public void signal(String info, ISourceLocation location) {
			if (kind == null) return;
			
			String text = MessageFormat.format(message, new Object[] {info} );
			text += " [Xlint:" + name + "]";
			world.getMessageHandler().handleMessage(new Message(text, kind, null, location));
		}
	}
}
