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
 *   Abraham Nevado - Lucierna simple caching strategy
 *******************************************************************************/
package org.aspectj.weaver.loadtime.definition;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Hashtable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.aspectj.util.LangUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * FIXME AV - doc, concrete aspect
 * 
 * @author Alexandre Vasseur
 * @author A. Nevado
 * @author Andy Clement
 */
public class DocumentParser extends DefaultHandler {
	/**
	 * The current DTD public id. The matching dtd will be searched as a resource.
	 */
	private final static String DTD_PUBLIC_ID = "-//AspectJ//DTD 1.5.0//EN";

	/**
	 * The DTD alias, for better user experience.
	 */
	private final static String DTD_PUBLIC_ID_ALIAS = "-//AspectJ//DTD//EN";

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

	private static Hashtable<String, Definition> parsedFiles = new Hashtable<String, Definition>();
	private static final boolean CACHE;
	private static final boolean LIGHTPARSER;


	static {
		boolean value = false;
		try {
			value = System.getProperty("org.aspectj.weaver.loadtime.configuration.cache", "true").equalsIgnoreCase("true");
		} catch (Throwable t) {
			t.printStackTrace();
		}
		CACHE = value;
		
		value = false;
		try {
			value = System.getProperty("org.aspectj.weaver.loadtime.configuration.lightxmlparser", "false").equalsIgnoreCase("true");
		} catch (Throwable t) {
			t.printStackTrace();
		}
		LIGHTPARSER = value;
	}

	private DocumentParser() {
		m_definition = new Definition();
	}

	public static Definition parse(final URL url) throws Exception {
		InputStream in = null;
		try {
			if (CACHE && parsedFiles.containsKey(url.toString())) {
				return parsedFiles.get(url.toString());
			}
			Definition def=null;
			
			if(LIGHTPARSER){	
				def = SimpleAOPParser.parse(url);
			}else{
				def = saxParsing(url);
			}
		
			if (CACHE && def.getAspectClassNames().size() > 0) {
				parsedFiles.put(url.toString(), def);
			}

			return def;
		} finally {
			try {
				in.close();
			} catch (Throwable t) {

			}
		}
	}

	private static Definition saxParsing(URL url) throws SAXException, ParserConfigurationException, IOException {
		DocumentParser parser = new DocumentParser();

		XMLReader xmlReader = getXMLReader();
		xmlReader.setContentHandler(parser);
		xmlReader.setErrorHandler(parser);

		try {
			xmlReader.setFeature("http://xml.org/sax/features/validation", false);
		} catch (SAXException e) {
			// fine, the parser don't do validation
		}
		try {
			xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
		} catch (SAXException e) {
			// fine, the parser don't do validation
		}
		try {
			xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		} catch (SAXException e) {
			// fine, the parser don't do validation
		}

		xmlReader.setEntityResolver(parser);
		InputStream in = url.openStream();
		xmlReader.parse(new InputSource(in));
		return parser.m_definition;
	}

	private static XMLReader getXMLReader() throws SAXException, ParserConfigurationException {
		XMLReader xmlReader = null;
		/* Try this first for Java 5 */
		try {
			xmlReader = XMLReaderFactory.createXMLReader();
		}

		/* .. and ignore "System property ... not set" and then try this instead */
		catch (SAXException ex) {
			xmlReader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
		}
		return xmlReader;
	}

	public InputSource resolveEntity(String publicId, String systemId) throws SAXException {
		if (publicId.equals(DTD_PUBLIC_ID) || publicId.equals(DTD_PUBLIC_ID_ALIAS)) {
			InputStream in = DocumentParser.class.getResourceAsStream("/aspectj_1_5_0.dtd");
			if (in == null) {
				System.err.println("AspectJ - WARN - could not read DTD " + publicId);
				return null;
			} else {
				return new InputSource(in);
			}
		} else {
			System.err.println("AspectJ - WARN - unknown DTD " + publicId + " - consider using " + DTD_PUBLIC_ID);
			return null;
		}
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (ASPECT_ELEMENT.equals(qName)) {
			String name = attributes.getValue(NAME_ATTRIBUTE);
			String scopePattern = replaceXmlAnd(attributes.getValue(SCOPE_ATTRIBUTE));
			String requiredType = attributes.getValue(REQUIRES_ATTRIBUTE);
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
			String options = attributes.getValue(OPTIONS_ATTRIBUTE);
			if (!isNull(options)) {
				m_definition.appendWeaverOptions(options);
			}
			m_inWeaver = true;
		} else if (CONCRETE_ASPECT_ELEMENT.equals(qName)) {
			String name = attributes.getValue(NAME_ATTRIBUTE);
			String extend = attributes.getValue(EXTEND_ATTRIBUTE);
			String precedence = attributes.getValue(PRECEDENCE_ATTRIBUTE);
			String perclause = attributes.getValue(PERCLAUSE_ATTRIBUTE);
			if (!isNull(name)) {
				m_lastConcreteAspect = new Definition.ConcreteAspect(name, extend, precedence, perclause);
				// if (isNull(precedence) && !isNull(extend)) {// if no precedence, then extends must be there
				// m_lastConcreteAspect = new Definition.ConcreteAspect(name, extend);
				// } else if (!isNull(precedence)) {
				// // wether a pure precedence def, or an extendsANDprecedence def.
				// m_lastConcreteAspect = new Definition.ConcreteAspect(name, extend, precedence, perclause);
				// }
				m_definition.getConcreteAspects().add(m_lastConcreteAspect);
			}
		} else if (POINTCUT_ELEMENT.equals(qName) && m_lastConcreteAspect != null) {
			String name = attributes.getValue(NAME_ATTRIBUTE);
			String expression = attributes.getValue(EXPRESSION_ATTRIBUTE);
			if (!isNull(name) && !isNull(expression)) {
				m_lastConcreteAspect.pointcuts.add(new Definition.Pointcut(name, replaceXmlAnd(expression)));
			}
		} else if (ASPECTJ_ELEMENT.equals(qName)) {
			if (m_inAspectJ) {
				throw new SAXException("Found nested <aspectj> element");
			}
			m_inAspectJ = true;
		} else if (ASPECTS_ELEMENT.equals(qName)) {
			m_inAspects = true;
		} else if (INCLUDE_ELEMENT.equals(qName) && m_inWeaver) {
			String typePattern = getWithinAttribute(attributes);
			if (!isNull(typePattern)) {
				m_definition.getIncludePatterns().add(typePattern);
			}
		} else if (EXCLUDE_ELEMENT.equals(qName) && m_inWeaver) {
			String typePattern = getWithinAttribute(attributes);
			if (!isNull(typePattern)) {
				m_definition.getExcludePatterns().add(typePattern);
			}
		} else if (DUMP_ELEMENT.equals(qName) && m_inWeaver) {
			String typePattern = getWithinAttribute(attributes);
			if (!isNull(typePattern)) {
				m_definition.getDumpPatterns().add(typePattern);
			}
			String beforeAndAfter = attributes.getValue(DUMP_BEFOREANDAFTER_ATTRIBUTE);
			if (isTrue(beforeAndAfter)) {
				m_definition.setDumpBefore(true);
			}
			String perWeaverDumpDir = attributes.getValue(DUMP_PERCLASSLOADERDIR_ATTRIBUTE);
			if (isTrue(perWeaverDumpDir)) {
				m_definition.setCreateDumpDirPerClassloader(true);
			}
		} else if (EXCLUDE_ELEMENT.equals(qName) && m_inAspects) {
			String typePattern = getWithinAttribute(attributes);
			if (!isNull(typePattern)) {
				m_definition.getAspectExcludePatterns().add(typePattern);
			}
		} else if (INCLUDE_ELEMENT.equals(qName) && m_inAspects) {
			String typePattern = getWithinAttribute(attributes);
			if (!isNull(typePattern)) {
				m_definition.getAspectIncludePatterns().add(typePattern);
			}
		} else {
			throw new SAXException("Unknown element while parsing <aspectj> element: " + qName);
		}
		super.startElement(uri, localName, qName, attributes);
	}

	private String getWithinAttribute(Attributes attributes) {
		return replaceXmlAnd(attributes.getValue(WITHIN_ATTRIBUTE));
	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (CONCRETE_ASPECT_ELEMENT.equals(qName)) {
			m_lastConcreteAspect = null;
		} else if (ASPECTJ_ELEMENT.equals(qName)) {
			m_inAspectJ = false;
		} else if (WEAVER_ELEMENT.equals(qName)) {
			m_inWeaver = false;
		} else if (ASPECTS_ELEMENT.equals(qName)) {
			m_inAspects = false;
		}
		super.endElement(uri, localName, qName);
	}

	// TODO AV - define what we want for XML parser error - for now stderr
	public void warning(SAXParseException e) throws SAXException {
		super.warning(e);
	}

	public void error(SAXParseException e) throws SAXException {
		super.error(e);
	}

	public void fatalError(SAXParseException e) throws SAXException {
		super.fatalError(e);
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

}
