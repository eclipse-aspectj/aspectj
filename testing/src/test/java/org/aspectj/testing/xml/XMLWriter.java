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
 * ******************************************************************/

package org.aspectj.testing.xml;

import java.io.PrintWriter;
import java.util.List;
import java.util.Stack;

import org.aspectj.util.LangUtil;

/** 
 * Manage print stream to an XML document.
 * This tracks start/end elements and signals on error,
 * and optionally lineates buffer by accumulating
 * up to a maximum width (including indent).
 * This also has utilities for un/flattening lists and
 * rendering buffer.
 */
public class XMLWriter {
    static final String SP = "  ";
    static final String TAB = SP + SP;
    
    /** maximum value for maxWidth, when flowing buffer */
    public static final int MAX_WIDTH = 8000;

    /** default value for maxWidth, when flowing buffer */
    public static final int DEFAULT_WIDTH = 80;

    /** extremely inefficient! */
    public static String attributeValue(String input) {
//        if (-1 != input.indexOf("&amp;")) {
//            String saved = input;
//            input = LangUtil.replace(input, "&amp;", "ampamp;");
//            if (-1 == input.indexOf("&")) {
//                input = saved;
//            } else {
//                input = LangUtil.replace(input, "&", "&amp;");
//                input = LangUtil.replace(input, "ampamp;", "&amp;");
//            }
//        } else if (-1 != input.indexOf("&")) {
//            input = LangUtil.replace(input, "&", "&amp;");
//        }
        input = input.replace('"', '~');
        input = input.replace('&', '=');
        return input;
    }
    
    /** @return name="{attributeValue({value})" */
    public static String makeAttribute(String name, String value) {
        return (name + "=\"" + attributeValue(value) + "\"");
    }
    
    /** same as flattenList, except also normalize \ -> / */
    public static String flattenFiles(String[] strings) {
        return flattenList(strings).replace('\\', '/');
    }
    
    /** same as flattenList, except also normalize \ -> / */
    public static String flattenFiles(List paths) {
        return flattenList(paths).replace('\\', '/');
    }

    /**
     * Expand comma-delimited String into list of values, without trimming
     * @param list List of items to print - null is silently ignored,
     *         so for empty items use ""
     * @return String[]{} for null input, String[] {input} for input without comma,
     *          or new String[] {items..} otherwise
     * @throws IllegalArgumentException if {any item}.toString() contains a comma
     */
    public static String[] unflattenList(String input) {
        return (String[]) LangUtil.commaSplit(input).toArray(new String[0]);
    }
    
    /** flatten input and add to list */
    public static void addFlattenedItems(List list, String input) {
        LangUtil.throwIaxIfNull(list, "list");
        if (null != input) {
            String[] items = XMLWriter.unflattenList(input);
            if (!LangUtil.isEmpty(items)) {
				for (String item : items) {
					if (!LangUtil.isEmpty(item)) {
						list.add(item);
					}
				}
            }
        }    
    }
    
	/** 
     * Collapse list into a single comma-delimited value (e.g., for list buffer)
     * @param list List of items to print - null is silently ignored,
     *         so for empty items use ""
     * @return item{,item}...
     * @throws IllegalArgumentException if {any item}.toString() contains a comma
     */
    public static String flattenList(List list) {
        if ((null == list) || (0 == list.size())) {
            return "";
        }
        return flattenList(list.toArray());
    }


	/** 
     * Collapse list into a single comma-delimited value (e.g., for list buffer)
     * @param list the String[] items to print - null is silently ignored,
     *         so for empty items use ""
     * @return item{,item}...
     * @throws IllegalArgumentException if list[i].toString() contains a comma
     */
    public static String flattenList(Object[] list) {
        StringBuffer sb = new StringBuffer();
        if (null != list) {
            boolean printed = false;
			for (Object o : list) {
				if (null != o) {
					if (printed) {
						sb.append(",");
					} else {
						printed = true;
					}
					String s = o.toString();
					if (s.contains(",")) {
						throw new IllegalArgumentException("comma in " + s);
					}
					sb.append(s);
				}
			}
        }
        return sb.toString();
    }
    
    /** output sink */
    PrintWriter out;
    
    /** stack of String element names */
    Stack stack = new Stack(); 
    
    /** false if doing attributes */
    boolean attributesDone = true;
    
    /** current element prefix */
    String indent = "";
    
    /** maximum width (in char) of indent and buffer when flowing */
    int maxWidth;
    
    /** 
     * Current text being flowed.
     * length() is always less than maxWidth.
     */
    StringBuffer buffer;

    /** @param out PrintWriter to print to - not null */
    public XMLWriter(PrintWriter out) {
        LangUtil.throwIaxIfNull(out, "out");
        this.out = out;
        buffer = new StringBuffer();
        maxWidth = DEFAULT_WIDTH;
    }
    
    /** 
     * Set maximum width (in chars) of buffer to accumulate.
     * @param maxWidth int 0..MAX_WIDTH for maximum number of char to accumulate 
     */
    public void setMaxWidth(int maxWidth) {
        if (0 > maxWidth) {
            this.maxWidth = 0;
        } else if (MAX_WIDTH < maxWidth) {
            this.maxWidth = MAX_WIDTH;
        } else {
            this.maxWidth = maxWidth;
        }
    }
    
    /** shortcut for entire element */
    public void printElement(String name, String attributes) {
        if (!attributesDone) throw new IllegalStateException("finish attributes");
        if (0 != buffer.length()) {  // output on subelement
            outPrintln(buffer + ">");
            buffer.setLength(0);
        }
        String oldIndent = indent;
        if (0 < stack.size()) {
            indent += TAB;
            ((StackElement) stack.peek()).numChildren++;
        }
        outPrintln(indent + "<" + name + " " + attributes + "/>");
        indent = oldIndent;
    }

    /**
     * Start element only
     * @param name the String label of the element
     * @param closeTag if true, delimit the end of the starting tag
     */
    public void startElement(String name, boolean closeTag) {
        startElement(name, "", closeTag);
    }
    
    /** 
     * Start element with buffer on the same line.
     * This does not flow buffer.
     * @param name String tag for the element
     * @param attr {name="value"}.. where value 
     *         is a product of attributeValue(String)
     */
    public void startElement(String name, String attr, boolean closeTag) {
        if (!attributesDone) throw new IllegalStateException("finish attributes");
        LangUtil.throwIaxIfFalse(!LangUtil.isEmpty(name), "empty name");

        if (0 != buffer.length()) { // output on subelement
            outPrintln(buffer + ">");
            buffer.setLength(0);
        }
        if (0 < stack.size()) {
            indent += TAB;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(indent);
        sb.append("<");
        sb.append(name);
        
        if (!LangUtil.isEmpty(attr)) {
            sb.append(" ");
            sb.append(attr.trim());
        }
        attributesDone = closeTag;
        if (closeTag) {
            sb.append(">");
            outPrintln(sb.toString());
        } else if (maxWidth <= sb.length()) {
            outPrintln(sb.toString());
        } else {
            if (0 != this.buffer.length()) {
                throw new IllegalStateException("expected empty attributes starting " + name);
            }
            this.buffer.append(sb.toString());
        }
        if (0 < stack.size()) {
            ((StackElement) stack.peek()).numChildren++;
        }
        stack.push(new StackElement(name));
    }
    
    /** 
     * @param name should be the same as that given to start the element
     * @throws IllegalStateException if start element does not match
     */
    public void endElement(String name) {
//        int level = stack.size();
        String err = null;
        StackElement element = null;
        if (0 == stack.size()) {
            err = "empty stack";
        } else {
            element = (StackElement) stack.pop();
            if (!element.name.equals(name)) {
                err = "expecting element " + element.name;
            }
        }
        if (null != err) {
            err = "endElement(" + name + ") " + stack + ": " + err;
            throw new IllegalStateException(err);
        }
        if (0 < element.numChildren) {
            outPrintln(indent + "</" + name + ">");
        } else if (0 < buffer.length()) {
            outPrintln(buffer + "/>");
            buffer.setLength(0);
        } else {
            outPrintln(indent + "/>");
        }
        if (!attributesDone) {
            attributesDone = true;
        }
        if (0 < stack.size()) {
            indent = indent.substring(0,  indent.length() - TAB.length());
        }
    }
    

    /**
     * Print name=value if neither is null and name is not empty after trimming,
     * accumulating these until they are greater than maxWidth or buffer are
     * terminated with endAttributes(..) or endElement(..).
     * @param value the String to convert as attribute value - ignored if null
     * @param name the String to use as the attribute name
     * @throws IllegalArgumentException if name is null or empty after trimming
     */
    public void printAttribute(String name, String value) {
        if (attributesDone) throw new IllegalStateException("not in attributes");
        if (null == value) {
            return;
        }
        if ((null == name) || (0 == name.trim().length())) {
            throw new IllegalArgumentException("no name=" + name + "=" + value);
        }
        
        String newAttr = name + "=\"" + attributeValue(value) + "\"";
        int indentLen = indent.length();
        int bufferLen = buffer.length();
        int newAttrLen = (0 == bufferLen ? indentLen : 0) + newAttr.length();
        
        if (maxWidth > (bufferLen + newAttrLen)) {
            buffer.append(" ");
            buffer.append(newAttr);
        } else {  // at least print old attributes; maybe also new
            if (0 < bufferLen) {
                outPrintln(buffer.toString());
                buffer.setLength(0);
            }
            buffer.append(indent + SP + newAttr);
        } 
    }

    public void endAttributes() {
        if (attributesDone) throw new IllegalStateException("not in attributes");
        attributesDone = true;
    }
    
    public void printComment(String comment) {
        if (!attributesDone) throw new IllegalStateException("in attributes");
        outPrintln(indent + "<!-- " + comment + "-->");
    }
    
    public void close() {
        if (null != out) {
            out.close();
        }
            
    }

	public void println(String string) {
        outPrintln(string);
	}

    private void outPrintln(String s) {
        if (null == out) {
            throw new IllegalStateException("used after close");
        }
        out.println(s);
    }
    static class StackElement {
        String name;
        int numChildren;
        public StackElement(String name) {
            this.name = name;
        }
    }
    
}
