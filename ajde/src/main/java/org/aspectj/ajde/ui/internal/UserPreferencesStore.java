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
 *     Helen Hawkins  Converted to new interface (bug 148190)
 * ******************************************************************/



package org.aspectj.ajde.ui.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.aspectj.ajde.Ajde;
import org.aspectj.ajde.ui.UserPreferencesAdapter;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.Message;
import org.aspectj.util.LangUtil;

public class UserPreferencesStore implements UserPreferencesAdapter {
	public static final String FILE_NAME = "/.ajbrowser";
	private static final String VALUE_SEP = ";";
	private Properties properties = new Properties();
	private boolean persist = true;

	public UserPreferencesStore() {
		this(true);
	}

	public UserPreferencesStore(boolean loadDefault) {
		persist = loadDefault;
		if (persist) {
			loadProperties(getPropertiesFilePath());
		}
	}

	@Override
	public String getProjectPreference(String name) {
		return properties.getProperty(name);
	}

	@Override
	public List<String> getProjectMultivalPreference(String name) {
		List<String> values = new ArrayList<>();
		String valuesString = properties.getProperty(name);
		if (valuesString != null && !valuesString.trim().equals("")) {
			StringTokenizer st = new StringTokenizer(valuesString, VALUE_SEP);
			while (st.hasMoreTokens()) {
				values.add(st.nextToken());
			}
		}
		return values;
	}

	@Override
	public void setProjectPreference(String name, String value) {
		properties.setProperty(name, value);
		saveProperties();
	}

	@Override
	public void setProjectMultivalPreference(String name, List values) {
		String valuesString = "";
		for (Object value : values) {
			valuesString += (String) value + ';';
		}
		properties.setProperty(name, valuesString);
		saveProperties();
	}

	public static String getPropertiesFilePath() {
		String path = System.getProperty("user.home");
		if (path == null) {
			path = ".";
		}
		return path + FILE_NAME;
	}

	@Override
	public String getGlobalPreference(String name) {
		return getProjectPreference(name);
	}

	@Override
	public List getGlobalMultivalPreference(String name) {
		return getProjectMultivalPreference(name);
	}

	@Override
	public void setGlobalPreference(String name, String value) {
		setProjectPreference(name, value);
	}

	@Override
	public void setGlobalMultivalPreference(String name, List values) {
		setProjectMultivalPreference(name, values);
	}
	private void loadProperties(String path) {
		if (LangUtil.isEmpty(path)) {
			return;
		}
		File file = new File(path);
		if (!file.canRead()) {
			return;
		}
		FileInputStream in = null;
		try {
			path = getPropertiesFilePath();
			in = new FileInputStream(file);
			properties.load(in);
		} catch (IOException ioe) {
			Message msg = new Message("Error reading properties from " + path,IMessage.ERROR,ioe,null);
			Ajde.getDefault().getMessageHandler().handleMessage(msg);
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
	public void saveProperties() {
		if (!persist) return;

		FileOutputStream out = null;
		String path = null;
		try {
			path = getPropertiesFilePath();
			out = new FileOutputStream(path);
			properties.store(out, "AJDE Settings");
		} catch (IOException ioe) {
			Message msg = new Message("Error writing properties to " + path,IMessage.ERROR,ioe,null);
			Ajde.getDefault().getMessageHandler().handleMessage(msg);
		} finally {
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
	}
}
