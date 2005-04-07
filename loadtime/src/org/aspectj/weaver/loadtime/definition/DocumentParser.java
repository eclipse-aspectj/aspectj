/*******************************************************************************
 * Copyright (c) Jonas Bonér, Alexandre Vasseur
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *******************************************************************************/
package org.aspectj.weaver.loadtime.definition;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;
import org.aspectj.weaver.loadtime.definition.Definition;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.net.URL;
import java.io.IOException;
import java.io.InputStream;
import java.io.File;

/**
 * @author <a href="mailto:alex AT gnilux DOT com">Alexandre Vasseur</a>
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

    /**
     * A handler to the DTD stream so that we are only using one file descriptor
     */
    private final static InputStream DTD_STREAM = DocumentParser.class.getResourceAsStream("/aspectj_1_5_0.dtd");

    private final static String ASPECTJ_ELEMENT = "aspectj";
    private final static String WEAVER_ELEMENT = "weaver";
    private final static String OPTIONS_ATTRIBUTE = "options";
    private final static String ASPECTS_ELEMENT = "aspects";
    private final static String ASPECT_ELEMENT = "aspect";
    private final static String CONCRETE_ASPECT_ELEMENT = "concrete-aspect";
    private final static String NAME_ATTRIBUTE = "name";
    private final static String EXTEND_ATTRIBUTE = "extends";
    private final static String POINTCUT_ELEMENT = "pointcut";
    private final static String EXPRESSION_ATTRIBUTE = "expression";


    private final Definition m_definition;

    private boolean m_inAspectJ;

    private Definition.ConcreteAspect m_lastConcreteAspect;

    private DocumentParser() {
        m_definition = new Definition();
    }

    public static Definition parse(final URL url) throws Exception {
        InputStream in = null;
        try {
            DocumentParser parser = new DocumentParser();

            XMLReader xmlReader = XMLReaderFactory.createXMLReader();
            xmlReader.setEntityResolver(parser);
            xmlReader.setContentHandler(parser);
            //TODO use a locator for error location reporting ?

            try {
              if (xmlReader.getFeature("http://xml.org/sax/features/validation")) {
                xmlReader.setFeature("http://xml.org/sax/features/validation", false);
              }
//              xmlReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
//              xmlReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            }
            catch (SAXNotRecognizedException e) {
              ;//fine, the parser don't do validation
            }

            in = url.openStream();
            xmlReader.parse(new InputSource(in));
            return parser.m_definition;
        } finally {
            try {in.close();} catch (Throwable t) {;}
        }
    }

    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
        if (publicId.equals(DTD_PUBLIC_ID) || publicId.equals(DTD_PUBLIC_ID_ALIAS)) {
            InputStream in = DTD_STREAM;
            if (in == null) {
//                System.err.println("AspectJ - WARN - could not open DTD");
                return null;
            } else {
                return new InputSource(in);
            }
        } else {
//            System.err.println(
//                    "AspectJ - WARN - deprecated DTD "
//                    + publicId
//                    + " - consider upgrading to "
//                    + DTD_PUBLIC_ID
//            );
            return null;//new InputSource();
        }
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (ASPECT_ELEMENT.equals(qName)) {
            String name = attributes.getValue(NAME_ATTRIBUTE);
            if (!isNull(name)) {
                m_definition.getAspectClassNames().add(name);
            }
        } else if (WEAVER_ELEMENT.equals(qName)) {
            String options = attributes.getValue(OPTIONS_ATTRIBUTE);
            if (!isNull(options)) {
                m_definition.appendWeaverOptions(options);
            }
        } else if (CONCRETE_ASPECT_ELEMENT.equals(qName)) {
            String name = attributes.getValue(NAME_ATTRIBUTE);
            String extend = attributes.getValue(EXTEND_ATTRIBUTE);
            if (!isNull(name) && !isNull(extend)) {
                m_lastConcreteAspect = new Definition.ConcreteAspect(name, extend);
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
            ;//nothing to do
        } else {
            throw new SAXException("Unknown element while parsing <aspectj> element: " + qName);
        }
        //TODO include / exclude
        super.startElement(uri, localName, qName, attributes);
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (CONCRETE_ASPECT_ELEMENT.equals(qName)) {
            m_lastConcreteAspect = null;
        } else if (ASPECTJ_ELEMENT.equals(qName)) {
            m_inAspectJ = false;
        }
        super.endElement(uri, localName, qName);
    }

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
        //TODO av do we need to handle "..)AND" or "AND(.." ?
        //FIXME av Java 1.4 code - if KO, use some Strings util
        return expression.replaceAll(" AND ", " && ");
    }

    public static void main(String args[]) throws Throwable {
        Definition def = parse(new File(args[0]).toURL());
        System.out.println(def);
    }

    private boolean isNull(String s) {
        return (s == null || s.length() <= 0);
    }

}
