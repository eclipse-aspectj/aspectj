/*******************************************************************************
 * Copyright (c) 2011 Contributors.
 * All rights reserved.
 * This program and the accompanying materials are made available
 * under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abraham Nevado - Lucierna	initial implementation
 *   	Just a slight variation of current DocumentParser.java from Alexandre Vasseur. 
 *******************************************************************************/
package org.aspectj.weaver.loadtime.definition;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import org.aspectj.util.LangUtil;

/**
 * This class has been created to avoid deadlocks when instrumenting SAXParser.
 * So it is used as a wrapper for the ligthweigh XML parser LightXMLParser.
 * 
 * @author A. Nevado
 */
public class SimpleAOPParser {

	private final static String ASPECTJ_ELEMENT = "aspectj";
	private final static String WEAVER_ELEMENT = "weaver";
	private final static String DUMP_ELEMENT = "dump";
	private final static String DUMP_BEFOREANDAFTER_ATTRIBUTE = "beforeandafter";
	private final static String DUMP_PERCLASSLOADERDIR_ATTRIBUTE = "perclassloaderdumpdir";
	private final static String INCLUDE_ELEMENT = "include";
	private final static String EXCLUDE_ELEMENT = "exclude";
	private final static String OPTIONS_ATTRIBUTE = "options";
	private final static String ASPECTS_ELEMENT = "aspects";
	private final static String ASPECT_ELEMENT = "aspect";
	private final static String CONCRETE_ASPECT_ELEMENT = "concrete-aspect";
	private final static String NAME_ATTRIBUTE = "name";
	private final static String SCOPE_ATTRIBUTE = "scope";
	private final static String REQUIRES_ATTRIBUTE = "requires";
	private final static String EXTEND_ATTRIBUTE = "extends";
	private final static String PRECEDENCE_ATTRIBUTE = "precedence";
	private final static String PERCLAUSE_ATTRIBUTE = "perclause";
	private final static String POINTCUT_ELEMENT = "pointcut";
	private final static String WITHIN_ATTRIBUTE = "within";
	private final static String EXPRESSION_ATTRIBUTE = "expression";
	private final Definition m_definition;
	private boolean m_inAspectJ;
	private boolean m_inWeaver;
	private boolean m_inAspects;

	private Definition.ConcreteAspect m_lastConcreteAspect;

	private SimpleAOPParser() {
		m_definition = new Definition();
	}

	public static Definition parse(final URL url) throws Exception {
		// FileReader freader = new FileReader("/tmp/aop.xml");
		InputStream in = url.openStream();
		LightXMLParser xml = new LightXMLParser();
		xml.parseFromReader(new InputStreamReader(in));
		SimpleAOPParser sap = new SimpleAOPParser();
		traverse(sap, xml);
		return sap.m_definition;
	}

	private void startElement(String qName, Map attrMap) throws Exception {
		if (ASPECT_ELEMENT.equals(qName)) {
			String name = (String) attrMap.get(NAME_ATTRIBUTE);
			String scopePattern = replaceXmlAnd((String) attrMap
					.get(SCOPE_ATTRIBUTE));
			String requiredType = (String) attrMap.get(REQUIRES_ATTRIBUTE);
			if (!isNull(name)) {
				m_definition.getAspectClassNames().add(name);
				if (scopePattern != null) {
					m_definition.addScopedAspect(name, scopePattern);
				}
				if (requiredType != null) {
					m_definition.setAspectRequires(name, requiredType);
				}
			}
		} else if (WEAVER_ELEMENT.equals(qName)) {
			String options = (String) attrMap.get(OPTIONS_ATTRIBUTE);
			if (!isNull(options)) {
				m_definition.appendWeaverOptions(options);
			}
			m_inWeaver = true;
		} else if (CONCRETE_ASPECT_ELEMENT.equals(qName)) {
			String name = (String) attrMap.get(NAME_ATTRIBUTE);
			String extend = (String) attrMap.get(EXTEND_ATTRIBUTE);
			String precedence = (String) attrMap.get(PRECEDENCE_ATTRIBUTE);
			String perclause = (String) attrMap.get(PERCLAUSE_ATTRIBUTE);
			if (!isNull(name)) {
				m_lastConcreteAspect = new Definition.ConcreteAspect(name,
						extend, precedence, perclause);
				m_definition.getConcreteAspects().add(m_lastConcreteAspect);
			}
		} else if (POINTCUT_ELEMENT.equals(qName)
				&& m_lastConcreteAspect != null) {
			String name = (String) attrMap.get(NAME_ATTRIBUTE);
			String expression = (String) attrMap.get(EXPRESSION_ATTRIBUTE);
			if (!isNull(name) && !isNull(expression)) {
				m_lastConcreteAspect.pointcuts.add(new Definition.Pointcut(
						name, replaceXmlAnd(expression)));
			}
		} else if (ASPECTJ_ELEMENT.equals(qName)) {
			if (m_inAspectJ) {
				throw new Exception("Found nested <aspectj> element");
			}
			m_inAspectJ = true;
		} else if (ASPECTS_ELEMENT.equals(qName)) {
			m_inAspects = true;
		} else if (INCLUDE_ELEMENT.equals(qName) && m_inWeaver) {
			String typePattern = getWithinAttribute(attrMap);
			if (!isNull(typePattern)) {
				m_definition.getIncludePatterns().add(typePattern);
			}
		} else if (EXCLUDE_ELEMENT.equals(qName) && m_inWeaver) {
			String typePattern = getWithinAttribute(attrMap);
			if (!isNull(typePattern)) {
				m_definition.getExcludePatterns().add(typePattern);
			}
		} else if (DUMP_ELEMENT.equals(qName) && m_inWeaver) {
			String typePattern = getWithinAttribute(attrMap);
			if (!isNull(typePattern)) {
				m_definition.getDumpPatterns().add(typePattern);
			}
			String beforeAndAfter = (String) attrMap
					.get(DUMP_BEFOREANDAFTER_ATTRIBUTE);
			if (isTrue(beforeAndAfter)) {
				m_definition.setDumpBefore(true);
			}
			String perWeaverDumpDir = (String) attrMap
					.get(DUMP_PERCLASSLOADERDIR_ATTRIBUTE);
			if (isTrue(perWeaverDumpDir)) {
				m_definition.setCreateDumpDirPerClassloader(true);
			}
		} else if (EXCLUDE_ELEMENT.equals(qName) && m_inAspects) {
			String typePattern = getWithinAttribute(attrMap);
			if (!isNull(typePattern)) {
				m_definition.getAspectExcludePatterns().add(typePattern);
			}
		} else if (INCLUDE_ELEMENT.equals(qName) && m_inAspects) {
			String typePattern = getWithinAttribute(attrMap);
			if (!isNull(typePattern)) {
				m_definition.getAspectIncludePatterns().add(typePattern);
			}
		} else {
			throw new Exception(
					"Unknown element while parsing <aspectj> element: " + qName);
		}
	}

	private void endElement(String qName) throws Exception {
		if (CONCRETE_ASPECT_ELEMENT.equals(qName)) {
			m_lastConcreteAspect = null;
		} else if (ASPECTJ_ELEMENT.equals(qName)) {
			m_inAspectJ = false;
		} else if (WEAVER_ELEMENT.equals(qName)) {
			m_inWeaver = false;
		} else if (ASPECTS_ELEMENT.equals(qName)) {
			m_inAspects = false;
		}
	}

	private String getWithinAttribute(Map attributes) {
		return replaceXmlAnd((String) attributes.get(WITHIN_ATTRIBUTE));
	}

	private static String replaceXmlAnd(String expression) {
		// TODO AV do we need to handle "..)AND" or "AND(.." ?
		return LangUtil.replace(expression, " AND ", " && ");
	}

	private boolean isNull(String s) {
		return (s == null || s.length() <= 0);
	}

	private boolean isTrue(String s) {
		return (s != null && s.equals("true"));
	}

	private static void traverse(SimpleAOPParser sap, LightXMLParser xml)
			throws Exception {
		sap.startElement(xml.getName(), xml.getAttributes());
		ArrayList childrens = xml.getChildrens();
		for (int i = 0; i < childrens.size(); i++) {
			LightXMLParser child = (LightXMLParser) childrens.get(i);
			traverse(sap, child);
		}
		sap.endElement(xml.getName());

	}
}
