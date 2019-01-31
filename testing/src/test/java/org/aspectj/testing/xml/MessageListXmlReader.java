/* *******************************************************************
 * Copyright (c) 2003 Contributors.
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Wes Isberg     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.aspectj.bridge.AbortException;
import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.LangUtil;
import org.xml.sax.SAXException;

/** 
 * Read a list of messages in xml form. 
 * Input files should comply with DOCTYPE
 */
public class MessageListXmlReader {
    private static final String INLINE_DOCTYPE;
    static {
        final String EOL = LangUtil.EOL;
        final StringBuffer r = new StringBuffer();
        
        r.append("<!DOCTYPE ");
        r.append(MessageList.XMLNAME);
        r.append(" [");
        r.append(EOL + "   <!ELEMENT " + MessageList.XMLNAME 
                    + " (" + SoftMessage.XMLNAME + "+) >");
        String name = SoftMessage.XMLNAME;
        r.append(EOL + "   <!ELEMENT " + name + " (" + SoftSourceLocation.XMLNAME + ")*>");
        r.append(EOL + "   <!ATTLIST " + name + " kind CDATA #REQUIRED >");
        r.append(EOL + "   <!ATTLIST " + name + " message CDATA #IMPLIED >");
        name = SoftSourceLocation.XMLNAME;
        r.append(EOL + "   <!ELEMENT " + name + " (#PCDATA) >");
        r.append(EOL + "   <!ATTLIST " + name + " line CDATA #REQUIRED >");
        r.append(EOL + "   <!ATTLIST " + name + " endLine CDATA #IMPLIED >");
        r.append(EOL + "   <!ATTLIST " + name + " column CDATA #IMPLIED >");
        r.append(EOL + "   <!ATTLIST " + name + " sourceFile CDATA #IMPLIED >");

        r.append(EOL + "] >");
        INLINE_DOCTYPE = r.toString();       
    } 

    private static final String[] LOG = new String[] {"info", "debug", "trace" };
    
    private int logLevel;
    
    /**
     * Print IMessage[] to the output file as XML.
     * @param output the File to write to - overwritten
     * @param messages the IMessage[] to write
     * @return null if no warnings detected, warnings otherwise
     */
    public String writeMessages(File output, IMessage[] messages) throws IOException {
        LangUtil.throwIaxIfNull(output, "output");
        LangUtil.throwIaxIfFalse(!LangUtil.isEmpty(messages), "no messages");
        PrintWriter writer = new PrintWriter(new FileOutputStream(output));
        XMLWriter printSink = new XMLWriter(writer);
        writer.println("");
        writer.println(INLINE_DOCTYPE);
        writer.println("");
        writer.println("<" + MessageList.XMLNAME + ">");
        SoftMessage.writeXml(printSink, messages);
        writer.println("</" + MessageList.XMLNAME + ">");
        writer.close();
        return null;
    }
    
    /** @param level 0..2, info..trace */
    public void setLogLevel(int level) {
        if (level < 0) {
            level = 0;
        }
        if (level > 2) {
            level = 2;
        }
        logLevel = level;        
    }
    
    /** 
     * Read the specifications for a list of IMessage from an XML file.
     * @param file the File must be readable, comply with DOCTYPE.
     * @return IMessage[] read from file
     * @see setLogLevel(int)
     */
    public IMessage[] readMessages(File file) throws IOException, AbortException {
        // setup loggers for digester and beanutils...
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog"); // XXX
        System.setProperty("org.apache.commons.logging.simplelog.defaultlog", LOG[logLevel]); // trace debug XXX

        final Digester digester = new Digester();
        setupDigester(digester);
        
        MessageListHolder holder = new MessageListHolder();
        digester.push(holder);
        FileInputStream input = new FileInputStream(file);
        try {
            digester.parse(input);
        } catch (SAXException e) {
            MessageUtil.fail("parsing " + file, e);
        } finally {
            if (null != input) {
                input.close();
                input = null;
            }            
        }
        return (null == holder.list
            ? new IMessage[0]
            : holder.list.getMessages());
    }
   
    /** set up the mapping between the xml and Java. */
    private void setupDigester(Digester digester) {
        // XXX supply sax parser to ignore white space?
        digester.setValidating(true);

        // element names come from the element components
        final String messageListX = MessageList.XMLNAME;
        final String messageX = messageListX + "/" + SoftMessage.XMLNAME;
        final String messageSrcLocX = messageX + "/" + SoftSourceLocation.XMLNAME;

        // ---- each sub-element needs to be created
        // handle messages the same at any level
        digester.addObjectCreate(messageListX,         MessageList.class.getName());
        digester.addObjectCreate(messageX,             SoftMessage.class.getName());
        digester.addObjectCreate(messageSrcLocX,       SoftSourceLocation.class.getName());
        
        // ---- set bean properties for sub-elements created automatically
        // -- some remapped - warnings
        //   - if property exists, map will not be used
        digester.addSetProperties(messageListX); 
        digester.addSetProperties(messageX);
        digester.addSetProperties(messageSrcLocX);
        digester.addSetProperties(messageX, "kind", "kindAsString");
        digester.addSetProperties(messageX, "line", "lineAsString");

        // ---- when subelements are created, add to parent 
        digester.addSetNext(messageListX,         "addMessage", IMessage.class.getName());
        digester.addSetNext(messageX,             "setSourceLocation", ISourceLocation.class.getName());
        
        // can set parent, but prefer to have "knows-about" flow down only...
    }

    // ------------------------------------------------------------ testing code

    /** 
     * This is only to do compile-time checking for the APIs impliedly
     * used in setupDigester(..).
     * The property setter checks are redundant with tests based on
     * expectedProperties().
     */
//    private static void setupDigesterCompileTimeCheck() { 
//        if (true) { throw new Error("never invoked"); }
//
//        MessageListHolder holder = new MessageListHolder();
//        MessageList ml = new MessageList();
//        SoftMessage m = new SoftMessage();
//        SoftSourceLocation sl = new SoftSourceLocation();
//
//        holder.setMessageList(ml);
//        ml.addMessage((IMessage) null);
//        m.setSourceLocation(sl);
//        m.setText((String) null);
//        m.setKindAsString((String) null);
//        
//        sl.setFile((String) null); 
//        sl.setLine((String) null); 
//        sl.setColumn((String) null); 
//        sl.setEndLine((String) null); 
//        
//        // add attribute setters to validate?
//    }

    // inner classes, to make public for bean utilities    
    /** a list of messages */
    public static class MessageList {
        public static final String XMLNAME = "message-list";
        private List messages = new ArrayList();
        public void addMessage(IMessage message) {
            messages.add(message);
        }
        IMessage[] getMessages() {
            return (IMessage[]) messages.toArray(new IMessage[0]);
        }
    }

    /** push on digester stack to hold message list */
    public static class MessageListHolder {
        public MessageList list;
        public void setMessageList(MessageList list) {
            this.list = list;
        }
    }
    
}
 


