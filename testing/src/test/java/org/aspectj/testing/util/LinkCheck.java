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

package org.aspectj.testing.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTML.Tag;
import javax.swing.text.html.HTMLEditorKit;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHandler;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.util.LangUtil;
//import org.aspectj.util.FileUtil;

/**
 * Quick and dirty link checker.
 * This checks that links into file: and http: targets work,
 * and that links out of file: targets work.
 */
public class LinkCheck {
 /*
  * Known issues:
  * - main interface only, though API's easily upgraded
  * - https MalformedUrlExceptions on redirect
  * - Swing won't quit without System.exit
  * - single-threaded
  */   
    static final URL COMMAND_LINE;
    static {
        URL commandLine = null;
        try {
            commandLine = new URL("file://commandLine");
        } catch (Throwable t) {
        }
        COMMAND_LINE = commandLine;
    }

    /** @param args file {-logFile {file} | -printInfo } */
    public static void main(String[] args) {
        final String syntax = "java " 
        + LinkCheck.class.getName() 
        + " file {-log <file> | -printInfo}..";
        if ((null == args) || (0 >= args.length)) {
            System.err.println(syntax);
            System.exit(1);
        }
        final String startingURL = "file:///" + args[0].replace('\\', '/');
        String logFile = null;
        boolean printInfo = false;
        for (int i = 1; i < args.length; i++) {
            if ("-log".equals(args[i]) && ((i+1) < args.length)) {
                logFile = args[++i];
            } else if ("-printInfo".equals(args[i])) {
                printInfo = true;
            } else {
                System.err.println(syntax);
                System.exit(1);                
            }
        }
        final boolean useSystemOut = (null == logFile);
        final MessageHandler mh;
        final OutputStream out;
        if (useSystemOut) {
            mh = new MessageHandler();
            out = null;
        } else {
        
            try {
                out = new FileOutputStream(logFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return;
            }
            final PrintStream ps = new PrintStream(out, true);
            final boolean printAll = printInfo;
            mh = new MessageHandler() {
                public boolean handleMessage(IMessage message) {
                    if (printAll || !message.isInfo()) {
                        ps.println(message.toString());
                    }
                    return super.handleMessage(message);
                }

            };
        }
        Link.Check exists 
            = Link.getProtocolChecker(new String[] {"file", "http"});
        Link.Check contents 
            = Link.getProtocolChecker(new String[] {"file"});
        LinkCheck me = new LinkCheck(mh, exists, contents);
        me.addLinkToCheck(COMMAND_LINE, startingURL); // pwd as base?
        try {
            String label = "checking URLs from " + startingURL;
            if (useSystemOut) {
                System.out.println(label);
            }
            MessageUtil.info("START " + label);
            long start = System.currentTimeMillis();
            me.run();
            long duration = (System.currentTimeMillis() - start)/1000;
            long numChecked = me.checkedUrls.size();
            if (numChecked > 0) {
                float cps = (float) duration  / (float) numChecked;
                StringBuffer sb = new StringBuffer();
                sb.append("DONE. Checked " + numChecked);
                sb.append(" URL's in " + duration);
                sb.append(" seconds (" + cps);
                sb.append(" seconds per URL).");
                MessageUtil.info("END " + label + ": " + sb);
                if (useSystemOut) {
                    System.out.println(sb.toString());
                }
            }
            MessageUtil.info(MessageUtil.renderCounts(mh));
            try {
                if (null != out) {
                    out.flush();
                } 
            } catch (IOException e) {
                // ignore
            }
            if (useSystemOut && (null != logFile)) {
                System.out.println("Find log in " + logFile);
            }
        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e1) {
                }
            }
            System.exit(mh.numMessages(IMessage.ERROR, true)); // XXX dumb swing
        }
    }

//    private static boolean isCheckedFileType(URL url) {
//        if (null == url) {
//            return false;
//        } 
//        String file = url.getFile();
//        return !FileUtil.hasZipSuffix(file)
//            && !file.endsWith(".pdf");
//    }
    
    private final Messages messages;
    private final HTMLEditorKit.Parser parser; // XXX untested - stateful
    private final List<Link> linksToCheck;
    private final List<String> checkedUrls;  // String (URL.toString)
    private final List<String> validRefs;  // String (URL.toString)
    private final List<String> refsToCheck;  // String (URL.toString)
    
    private final Link.Check checkExists;
    private final Link.Check checkContents;

    public LinkCheck(IMessageHandler handler, 
        Link.Check checkExists,
        Link.Check checkContents) {
        LangUtil.throwIaxIfNull(handler, "handler");
        LangUtil.throwIaxIfNull(checkExists, "checkExists");
        LangUtil.throwIaxIfNull(checkContents, "checkContents");
        this.messages = new Messages(handler);
        linksToCheck = new ArrayList<>();
        checkedUrls = new ArrayList<>();
        refsToCheck = new ArrayList<>();
        validRefs = new ArrayList<>();
        parser = new HTMLEditorKit() {
            public HTMLEditorKit.Parser getParser() {
                return super.getParser();
            }
        }
        .getParser();
        this.checkExists = checkExists;
        this.checkContents = checkContents;
    }

    public synchronized void addLinkToCheck(URL doc, String link) {
        URL linkURL = makeURL(doc, link);
        if (null == linkURL) {
//            messages.addingNullLinkFrom(doc);
            return;
        }
        String linkString = linkURL.toString();
        if ((null != link) && !checkedUrls.contains(linkString) ) {
            if (!checkExists.check(linkURL)) {
                checkedUrls.add(linkString);
                messages.acceptingUncheckedLink(doc, linkURL);
            } else {
                Link toAdd = new Link(doc, linkURL);
                if (!linksToCheck.contains(toAdd)) { // equals overridden
                    linksToCheck.add(toAdd);
                }
            }
        }
    }

    public synchronized void run() {
        List<Link> list = new ArrayList<>();
        while (0 < linksToCheck.size()) {
            messages.checkingLinks(linksToCheck.size());
            list.clear();
            list.addAll(linksToCheck);
			for (final Link link : list) {
				String urlString = link.url.toString();
				if (!checkedUrls.contains(urlString)) {
					checkedUrls.add(urlString);
					messages.checkingLink(link);
					checkLink(link);
				}
			}
            linksToCheck.removeAll(list);
        }
        // now check that all named references are accounted for
		for (String ref : refsToCheck) {
			if (!validRefs.contains(ref)) {
				messages.namedReferenceNotFound(ref);
			}
		}
    }

    /** @return null if link known or if unable to create */
    private URL makeURL(URL doc, String link) {
        if (checkedUrls.contains(link)) {
            return null;
        }
        URL result = null;
        try {
            result = new URL(link);
        } catch (MalformedURLException e) {
            if (null == doc) {
                messages.malformedUrl(null, link, e);
            } else {
                try {
                    URL res = new URL(doc, link);
                    String resultString = res.toString();
                    if (checkedUrls.contains(resultString)) {
                        return null;
                    }
                    result = res;
                } catch (MalformedURLException me) {
                    messages.malformedUrl(doc, link, me);
                }
            }
        }
        return result;
    }

    /** @param link a Link with a url we can handle */
    private void checkLink(final Link link) {
        if (handleAsRef(link)) {
            return;
        }
        URL url = link.url;
        InputStream input = null;
        try {                        
            URLConnection connection = url.openConnection();
            if (null == connection) {
                messages.cantOpenConnection(url);
                return;
            }
            // get bad urls to fail on read before skipping by type
            input = connection.getInputStream();
            String type = connection.getContentType();
            if (null == type) {
                messages.noContentType(link);
            } else if (!type.toLowerCase().startsWith("text/")) {
                messages.notTextContentType(link);
            } else {
                boolean addingLinks = checkContents.check(url);
                parser.parse(
                    new InputStreamReader(input),
                    new LinkListener(url, addingLinks), true);
            }
        } catch (IOException e) {
            messages.exceptionReading(link, e);
        } finally {
            if (null != input) {
                try {
                    input.close();
                } catch (IOException e1) {
                    // ignore
                }
            }
        }
    }

    /** @return true if link is to an internal ...#name */
    private boolean handleAsRef(Link link) {
        String ref = link.url.getRef();
        if (!LangUtil.isEmpty(ref)) {
            String refString = link.url.toString(); // XXX canonicalize?
            if (!refsToCheck.contains(refString)) {
                refsToCheck.add(refString);
                // add pseudo-link to force reading of ref'd doc XXX hmm
                int refLoc = refString.indexOf("#");
                if (-1 == refLoc) {
                    messages.uncommentedReference(link);
                } else {
                    refString = refString.substring(0, refLoc);
                    addLinkToCheck(link.doc, refString);
                }
                return true;
            }
        }
        return false;
    }

    /** LinkListener callback */
    private boolean addKnownNamedAnchor(URL doc, String name) {
        String namedRef = "#" + name;
        try {
            String ref = new URL(doc + namedRef).toString();
            if (!validRefs.contains(ref)) {
                validRefs.add(ref);
            }
            return true;
        } catch (MalformedURLException e) {
            messages.malformedUrl(doc, namedRef, e);
            return false;
        }    
    }
       
    private class Messages {
        private final IMessageHandler handler;
        private Messages(IMessageHandler handler) {
            LangUtil.throwIaxIfNull(handler, "handler");
            this.handler = handler;
        }
        
        private void info(String label, Object more) {
            MessageUtil.info(handler, label + " " + more);
        }

        private void fail(String label, Object more, Throwable thrown) {
            MessageUtil.fail(handler, label + " " + more, thrown);
        }

        private void uncommentedReference(Link link) {
            info("uncommentedReference", link); // XXX bug?
        }

//        private void addingNullLinkFrom(URL doc) {
//            info("addingNullLinkFrom", doc);
//        }
//
//        private void noContentCheck(Link link) {
//            info("noContentCheck", link);
//        }

        private void notTextContentType(Link link) {
            info("notTextContentType", link);
        }

        private void noContentType(Link link) {
            info("noContentType", link);
        }

        private void checkingLinks(int i) {
            info("checkingLinks", i);
        }

        private void checkingLink(Link link) {
            info("checkingLink", link);
        }

        private void acceptingUncheckedLink(URL doc, URL link) {
            info("acceptingUncheckedLink", "doc=" + doc + " link=" + link);
        }

//        private void cantHandleRefsYet(Link link) {
//            info("cantHandleRefsYet", link.url);
//        }

        private void namedReferenceNotFound(String ref) {
            // XXX find all references to this unfound named reference
            fail("namedReferenceNotFound", ref, null);
        }

        private void malformedUrl(URL doc, String link, MalformedURLException e) {
            fail("malformedUrl", "doc=" + doc + " link=" + link, e);
        }
        
        private void cantOpenConnection(URL url) {
            fail("cantOpenConnection", url, null);
        }
        
        private void exceptionReading(Link link, IOException e) {
            // only info if redirect from http to https
            String m = e.getMessage();
            if ((m != null) 
                && (m.contains("protocol"))
                && (m.contains("https"))
                && "http".equals(link.url.getProtocol())) {
                info("httpsRedirect", link);
                return;
            }
            fail("exceptionReading", link, e);
        }
        
        private void nullLink(URL doc, Tag tag) {
            // ignore - many tags do not have links
        }
        
        private void emptyLink(URL doc, Tag tag) {
            fail("emptyLink", "doc=" + doc + " tag=" + tag, null);
        }
    }

    /**
     * Register named anchors and add any hrefs to the links to check.
     */
    private class LinkListener extends HTMLEditorKit.ParserCallback {
        private final URL doc;
        private final boolean addingLinks;

        private LinkListener(URL doc, boolean addingLinks) {
            this.doc = doc;
            this.addingLinks = addingLinks;
        }

        public void handleStartTag(
            HTML.Tag tag,
            MutableAttributeSet attributes,
            int position) {
            handleSimpleTag(tag, attributes, position);
        }

        public void handleSimpleTag(
            HTML.Tag tag,
            MutableAttributeSet attributes,
            int position) { // XXX use position to emit context?
            boolean isNameAnchor = registerIfNamedAnchor(tag, attributes);
            if (!addingLinks) {
                return;
            }
            Object key = HTML.Tag.FRAME == tag 
                ? HTML.Attribute.SRC
                : HTML.Attribute.HREF;
            String link = (String) attributes.getAttribute(key);

            if (null == link) {
                if (!isNameAnchor) {
                    messages.nullLink(doc, tag);
                }
            } else if (0 == link.length()) {
                if (!isNameAnchor) {
                    messages.emptyLink(doc, tag);
                }
            } else {
                addLinkToCheck(doc, link);
            }
        }
        
        private boolean registerIfNamedAnchor(
            HTML.Tag tag,
            MutableAttributeSet attributes) {
            if (HTML.Tag.A.equals(tag)) {
                String name  
                    = (String) attributes.getAttribute(HTML.Attribute.NAME);
                if (null != name) {
                    addKnownNamedAnchor(doc, name);
                    return true;
                }                
            }
            return false;
        }
        
    }

    private static class Link {
        private static final Check FALSE_CHECKER = new Check() {
            public boolean check(Link link) { return false; }
            public boolean check(URL url) { return false; }
        };
        private static Check getProtocolChecker(String[] protocols) {
            final String[] input 
                = (String[]) LangUtil.safeCopy(protocols, protocols);
            if (0 == input.length) {
                return FALSE_CHECKER;
            }
            return new Check() {
                final List list = Arrays.asList(input);
                public boolean check(URL url) {
                    return (null != url) && list.contains(url.getProtocol());
                }
            };
        }
        private final URL doc;
        private final URL url;
        private String toString;
        private Link(URL doc, URL url) {
            LangUtil.throwIaxIfNull(doc, "doc");
            LangUtil.throwIaxIfNull(url, "url");
            this.doc = doc;
            this.url = url;
        }
        public boolean equals(Object o) {
            if (null == o) {
                return false;
            } 
            if (this == o) {
                return true;
            }
            if (Link.class != o.getClass()) {
                return false; // exact class
            }
            Link other = (Link) o;
            return doc.equals(other) && url.equals(other);
            //return toString().equals(o.toString());
        }
        
        public int hashCode() { // XXX
            return doc.hashCode() + (url.hashCode() >> 4);
//            return toString.hashCode();
        }
        
        public String toString() {
            if (null == toString) {
                toString = url + " linked from " + doc;
            }
            return toString;
        }
        private static class Check {
            public boolean check(Link link) {
                return (null != link) && check(link.url);
            }
            public boolean check(URL url) {
                return (null != url);
            }
        }
    }
}
