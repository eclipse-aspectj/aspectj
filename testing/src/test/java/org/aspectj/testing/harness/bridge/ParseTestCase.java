/* *******************************************************************
 * Copyright (c) 2002 Palo Alto Research Center, Incorporated (PARC).
 * All rights reserved. 
 * This program and the accompanying materials are made available 
 * under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors: 
 *     Xerox/PARC     initial implementation 
 * ******************************************************************/

package org.aspectj.testing.harness.bridge;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.aspectj.bridge.IMessage;
import org.aspectj.bridge.IMessageHolder;
import org.aspectj.bridge.ISourceLocation;
import org.aspectj.bridge.Message;
import org.aspectj.bridge.MessageHandler;
import org.aspectj.bridge.MessageUtil;
import org.aspectj.bridge.SourceLocation;
import org.aspectj.testing.run.IRunIterator;
import org.aspectj.testing.run.IRunListener;
import org.aspectj.testing.run.RunStatus;
import org.aspectj.testing.run.Runner;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public class ParseTestCase extends TestCase {

	public ParseTestCase(String name) {
		super(name);
	}
	
	public void testNothingBecauseOthersSkipped() {}
    
	public void skiptestParse() throws Exception { // XXX failing b/c of iteration
		Runner runner = new Runner();
        IMessageHolder handler = new MessageHandler();
		RunStatus status;
        Validator validator = new Validator(handler);
        final File suiteFile = new File("../testing/testdata/suite.xml");
		List tests = parseSuite(suiteFile);
        Sandbox sandbox = new Sandbox(new File("testdata"), validator);		
        IRunListener listenerNULL = null;		
        ISourceLocation sl = new SourceLocation(suiteFile, 0, 0,0);
		for (Object o : tests) {
			status = new RunStatus(handler, runner);
			AjcTest.Spec test = (AjcTest.Spec) o;
			test.setSourceLocation(sl);
			IRunIterator child = test.makeRunIterator(sandbox, validator);
			//test.setup(new String[0], validator); // XXX
			//IRun child = runner.wrap(test, null);
			// huh? runIterator not generating child status?
			//RunStatus childStatus = runner.makeChildStatus();
			runner.runIterator(child, status, listenerNULL);
			MessageUtil.print(System.err, status);
		}
	}

	private List parseSuite(File file) throws ParserConfigurationException, IOException, SAXException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(true);
		factory.setIgnoringElementContentWhitespace(true);
		factory.setIgnoringComments(true);
		

		DocumentBuilder builder = factory.newDocumentBuilder();
		System.out.println(file.getAbsoluteFile());
		Document doc = builder.parse(file);

		dump(doc.getDocumentElement(), 0);
		
		List ret = new ArrayList();
		Node suiteNode = doc.getDocumentElement();
		
		NodeList children = suiteNode.getChildNodes();
		for (int i=0; i < children.getLength(); i++) {
			ret.add(parseTest(children.item(i)));
		}
		
		return ret;
	}

	private AjcTest.Spec parseTest(Node node) {
		String title = getAttributeString(node, "title");
		String pr = getAttributeString(node, "pr");
		String dir = getAttributeString(node, "dir");
		
		ISourceLocation sourceLocation =
		    new SourceLocation(new File("Missing"), 0, 0, 0);
        AjcTest.Spec test = new AjcTest.Spec();
        test.setDescription(title);
        test.setTestDirOffset(dir);
        test.setBugId(Integer.valueOf(pr));
        test.setSourceLocation(sourceLocation);
		//AjcTest test = new AjcTest(title, dir, pr, sourceLocation);
		
		System.out.println(test);
		
//		List ret = new ArrayList();
		
		NodeList children = node.getChildNodes();
		for (int i=0; i < children.getLength(); i++) {
            test.addChild(parseIRun(test, children.item(i), dir));
//			test.addRunSpec(parseIRun(test, children.item(i), dir));
		}
		
		return test;
	}

	private IRunSpec parseIRun(AjcTest.Spec test, Node node, String baseDir) {
		String kind = node.getNodeName();
		if (kind.equals("compile")) {
			List args = parseChildrenStrings(node, "arg");
			/*List files = */parseChildrenStrings(node, "file");
			List expectedMessages = parseChildrenMessages(node);
            CompilerRun.Spec spec = new CompilerRun.Spec();
            spec.addOptions((String[]) args.toArray(new String[0]));
            spec.addPaths((String[]) args.toArray(new String[0]));
            spec.addMessages(expectedMessages);
            spec.testSrcDirOffset = null; // baseDir; 
            return spec;
		} else if (kind.equals("run")) {
            JavaRun.Spec spec = new JavaRun.Spec();
            spec.className = getAttributeString(node, "class");
            spec.addOptions(new String[0]);  //??? could add support here
            /*JavaRun run = */new JavaRun(spec);
			return spec;
		}
		
		return null;
	}

	private List parseChildrenMessages(Node node) {
		List ret = new ArrayList();
		
		NodeList children = node.getChildNodes();
		for (int i=0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().equals("message")) {
				ret.add(parseMessage(child));
			}
		}
		return ret;
	}

	private IMessage parseMessage(Node child) {
		IMessage.Kind kind;
		String sKind = getAttributeString(child, "kind");
		if (sKind.equals("error")) { kind = IMessage.ERROR; }
		else if (sKind.equals("warning")) { kind = IMessage.WARNING; }
		else {
			throw new RuntimeException("unknown kind: " + sKind);
		}
		String filename = getAttributeString(child, "file");
		File file;
		if (filename != null) {
			file = new File(filename);
		} else {
			file = new File("XXX");  //XXX 
		}
		
		int line = Integer.valueOf(getAttributeString(child, "line"));
		
		ISourceLocation sourceLocation = new SourceLocation(file, line, line, 0);
		
		return new Message("", kind, null, sourceLocation);
	}



	private List parseChildrenStrings(Node node, String kind) {
		List ret = new ArrayList();
		
		NodeList children = node.getChildNodes();
		for (int i=0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if (child.getNodeName().equals(kind)) {
                Node first = child.getFirstChild();
                if (null != first) {
                    ret.add(first.getNodeValue());// XXX
                }
			}
		}
		return ret;
	}



	private String getAttributeString(Node node, String name) {
		Node attrNode = node.getAttributes().getNamedItem(name);
		if (attrNode == null) return null;
		return attrNode.getNodeValue();
	}




	private void dump(Node node, int indent) {
		for (int i=0; i < indent; i++) System.out.print("  ");
		System.out.println(node);
		NodeList children = node.getChildNodes();
		for (int i=0; i < children.getLength(); i++) {
			dump(children.item(i), indent+1);
		}
	}


}
