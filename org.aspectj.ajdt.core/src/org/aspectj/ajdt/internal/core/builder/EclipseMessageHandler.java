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


package org.aspectj.ajdt.internal.core.builder;

import org.aspectj.bridge.IMessageHandler;

/** @deprecated EclipseMessageAdapter */
public class EclipseMessageHandler { // implements IMessageHandler {
	IMessageHandler handler;
	private int errorCount = 0;

	private EclipseMessageHandler(IMessageHandler handler) {
		super();
		this.handler = handler;
	}
}
	
//	private IMessage makeMessage(ICompilationUnit unit, IProblem problem) {
//		//??? would like to know the column as well as line
//		//??? and also should generate highlighting info
//		int line = problem.getSourceLineNumber();
//        SourceLocation loc =
//		    new SourceLocation(new File(new String(problem.getOriginatingFileName())),
//		                       line, line, 0);
//		                       
//		String locationContext = makeLocationContext(unit, problem);
////		System.err.println("(" + locationContext + ")");
//		                       
//		return new CommandLineMessage(problem.getMessage(),
//					problem.isError() ? IMessage.ERROR : IMessage.WARNING,
//					null, loc, locationContext);
//	}
//
//
//	//XXX some cut-and-paste from eclipse sources
//	public String makeLocationContext(ICompilationUnit compilationUnit, IProblem problem) {
//		//extra from the source the innacurate     token
//		//and "highlight" it using some underneath ^^^^^
//		//put some context around too.
//
//		//this code assumes that the font used in the console is fixed size
//
//		//sanity .....
//		int startPosition = problem.getSourceStart();
//		int endPosition = problem.getSourceEnd();
//		
//		if ((startPosition > endPosition)
//			|| ((startPosition <= 0) && (endPosition <= 0)))
//			return Util.bind("problem.noSourceInformation"); //$NON-NLS-1$
//
//		final char SPACE = '\u0020';
//		final char MARK = '^';
//		final char TAB = '\t';
//		char[] source = compilationUnit.getContents();
//		//the next code tries to underline the token.....
//		//it assumes (for a good display) that token source does not
//		//contain any \r \n. This is false on statements ! 
//		//(the code still works but the display is not optimal !)
//
//		//compute the how-much-char we are displaying around the inaccurate token
//		int begin = startPosition >= source.length ? source.length - 1 : startPosition;
//		int relativeStart = 0;
//		int end = endPosition >= source.length ? source.length - 1 : endPosition;
//		int relativeEnd = 0;
//		label : for (relativeStart = 0;; relativeStart++) {
//			if (begin == 0)
//				break label;
//			if ((source[begin - 1] == '\n') || (source[begin - 1] == '\r'))
//				break label;
//			begin--;
//		}
//		label : for (relativeEnd = 0;; relativeEnd++) {
//			if ((end + 1) >= source.length)
//				break label;
//			if ((source[end + 1] == '\r') || (source[end + 1] == '\n')) {
//				break label;
//			}
//			end++;
//		}
//		//extract the message form the source
//		char[] extract = new char[end - begin + 1];
//		System.arraycopy(source, begin, extract, 0, extract.length);
//		char c;
//		//remove all SPACE and TAB that begin the error message...
//		int trimLeftIndex = 0;
//		while (((c = extract[trimLeftIndex++]) == TAB) || (c == SPACE)) {
//		};
//		System.arraycopy(
//			extract,
//			trimLeftIndex - 1,
//			extract = new char[extract.length - trimLeftIndex + 1],
//			0,
//			extract.length);
//		relativeStart -= trimLeftIndex;
//		//buffer spaces and tabs in order to reach the error position
//		int pos = 0;
//		char[] underneath = new char[extract.length]; // can't be bigger
//		for (int i = 0; i <= relativeStart; i++) {
//			if (extract[i] == TAB) {
//				underneath[pos++] = TAB;
//			} else {
//				underneath[pos++] = SPACE;
//			}
//		}
//		//mark the error position
//		for (int i = startPosition;
//			i <= (endPosition >= source.length ? source.length - 1 : endPosition);
//			i++)
//			underneath[pos++] = MARK;
//		//resize underneathto remove 'null' chars
//		System.arraycopy(underneath, 0, underneath = new char[pos], 0, pos);
//
//		return new String(extract) + "\n" + new String(underneath); //$NON-NLS-2$ //$NON-NLS-1$
//	}
//
//	
//	public boolean handleProblem(ICompilationUnit unit, IProblem problem) {
//		return handleMessage(makeMessage(unit, problem));
//	}
//	
//	public boolean handleThrowable(Throwable t) {
//		return handleThrowable(t.toString(), t);
//	}
//
//	public boolean handleThrowable(String message, Throwable t) {
//		return handleMessage(new Message(message, IMessage.ERROR, t, null));
//	}
//	
//	public boolean handleMessage(IMessage message) throws AbortException {
//		if (message.getKind() == Message.ERROR) errorCount++;
//		
//		//XXX Mik and Jim will figure out what this is doing
//		if (message.getKind() == Message.ERROR || message.getKind() == Message.WARNING) {
//		    ISourceLocation loc = message.getISourceLocation();
//		    String msg = message.getMessage();
//		    if (loc == null) {
//		    	System.err.println(msg);
//		    } else {
//		    	StringBuffer buf = new StringBuffer();
//		    	buf.append(loc.getSourceFile().getPath() + ":" + loc.getLine());
//		    	if (loc.getColumn() != -1) {
//		    		buf.append(":" + loc.getColumn());
//		    	}
//		    	buf.append(": ");
//		    	buf.append(msg);
//		    	buf.append("\n");
//		    	if (message instanceof CommandLineMessage) {  
//		    		buf.append(((CommandLineMessage)message).getLocationContext());	
//		    	}	    	  
//		    	System.err.println(buf.toString());
//		    }
//		    		
//		}
//		return handler.handleMessage(message);
//	}
//
//	public boolean isIgnoring(IMessage.Kind kind) {
//		return handler.isIgnoring(kind);
//	}
//
//	public boolean handleError(String string) {
//		return handleMessage(new Message(string, IMessage.ERROR, null, null));
//	}
//
//	public int getErrorCount() {
//		return errorCount;
//	}
//
//	public void resetErrorCount() {
//		this.errorCount = 0;
//	}


